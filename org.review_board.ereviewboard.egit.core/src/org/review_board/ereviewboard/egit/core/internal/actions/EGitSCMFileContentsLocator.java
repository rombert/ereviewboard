package org.review_board.ereviewboard.egit.core.internal.actions;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.GitProvider;
import org.eclipse.egit.core.project.GitProjectData;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.errors.InvalidObjectIdException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.RepositoryProvider;
import org.review_board.ereviewboard.core.internal.scm.SCMFileContentsLocator;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.review_board.ereviewboard.egit.core.internal.Activator;
import org.review_board.ereviewboard.egit.core.internal.TraceLocation;

/**
 * @author Robert Munteanu
 * 
 */
public class EGitSCMFileContentsLocator implements SCMFileContentsLocator {

    private Repository codeRepository;
    private String revision;

    public void init(Repository codeRepository, String filePath, String revision) {

        this.codeRepository = codeRepository;
        this.revision = revision;
    }

    public boolean isEnabled() {

        return codeRepository != null && codeRepository.getTool() == RepositoryType.Git;
    }

    public byte[] getContents(IProgressMonitor monitor) throws CoreException {
        
        if ( FileDiff.PRE_CREATION.equals(revision) )
            return new byte[0];
        
        ObjectId objectId;
		try {
			objectId = ObjectId.fromString(revision);
		} catch (InvalidObjectIdException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The revision " + revision + " is not a valid git objectId", e));
		}

        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {

            RepositoryProvider provider = RepositoryProvider.getProvider(project);

            if (!(provider instanceof GitProvider))
                continue;

            GitProvider gitProvider = (GitProvider) provider;
            
            GitProjectData data = gitProvider.getData();
            
            RepositoryMapping repositoryMapping = data.getRepositoryMapping(project);
            
            org.eclipse.jgit.lib.Repository repository = repositoryMapping.getRepository();
            
            Activator.getDefault().trace(TraceLocation.DIFF, NLS.bind("Trying to find {0} in {1}.", objectId, repository));
            
            try {
                ObjectLoader objectLoader = repository.open(objectId);
                
                Activator.getDefault().trace(TraceLocation.DIFF, NLS.bind("{0} found in {1}", objectId, repository));
                
                return objectLoader.getCachedBytes();
            } catch (LargeObjectException e) {
                Activator.getDefault().log(IStatus.WARNING, NLS.bind("Failed loading {0} from {1}", objectId, repository), e);
            } catch (MissingObjectException e) {
                Activator.getDefault().trace(TraceLocation.DIFF, NLS.bind("{0} not found in {1}", objectId, repository));
            } catch (IOException e) {
                Activator.getDefault().log(IStatus.WARNING, NLS.bind("Failed loading {0} from {1}", objectId, repository), e);
            }
        }
        
        throw new CoreException(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "No repository found containing " + objectId));
    }

}
