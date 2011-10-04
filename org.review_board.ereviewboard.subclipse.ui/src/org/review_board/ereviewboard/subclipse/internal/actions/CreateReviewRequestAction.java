package org.review_board.ereviewboard.subclipse.internal.actions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IActionDelegate;
import org.review_board.ereviewboard.core.ReviewboardClientManager;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.client.StatusAndInfoCommand;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * @author Robert Munteanu
 */
public class CreateReviewRequestAction implements IActionDelegate {

    private IProject currentProject;
    
    public void run(IAction action) {
        
        if ( currentProject == null )
            return;
        
        SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(currentProject, SVNProviderPlugin.getTypeId());
        
        Assert.isNotNull(svnProvider, "No " + SVNTeamProvider.class.getSimpleName() + " for " + currentProject);
        
        ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(currentProject);
        
        ReviewboardClientManager clientManager = ReviewboardCorePlugin.getDefault().getConnector().getClientManager();
        Repository reviewBoardRepository = null;
        
        System.out.println("Local repository is " + localResource.getRepository().getRepositoryRoot().toString());
        
        for ( ReviewboardClient client : clientManager.getAllClients() ) {
            for ( Repository repository : client.getClientData().getRepositories() ) {
                
                System.out.println("Considering repository of type " + repository.getTool()  + " and path " + repository.getPath());
                
                if ( repository.getTool() != RepositoryType.Subversion )
                    continue;
                
                if ( localResource.getRepository().getRepositoryRoot().toString().equals(repository.getPath()) )
                    reviewBoardRepository = repository;
            }
        }

        
        File tmpFile = null;
        Reader reader = null;
        
        try {
            LocalResourceStatus status = localResource.getStatus();
            
            Assert.isNotNull(status, "No status for resource " + localResource);
            
            StatusAndInfoCommand command = new StatusAndInfoCommand(localResource, true, false, false);
            
            command.run(new NullProgressMonitor());
            
            ISVNStatus[] statuses = command.getStatuses();
            List<File> modified = new ArrayList<File>();
            List<File> added = new ArrayList<File>();
            List<File> unversioned = new ArrayList<File>();
            
            List<File> all = new ArrayList<File>();
            
            for ( ISVNStatus svnStatus : statuses ) {
                if ( SVNStatusKind.ADDED.equals(svnStatus.getTextStatus()))
                    added.add(svnStatus.getFile());
                else if ( SVNStatusKind.MODIFIED.equals(svnStatus.getTextStatus()))
                    modified.add(svnStatus.getFile());
                else if ( SVNStatusKind.UNVERSIONED.equals(svnStatus.getTextStatus()) ) {
                    unversioned.add(svnStatus.getFile());
                    continue;
                } else
                    Assert.isTrue(false, "Unhandled " + SVNStatusKind.class.getSimpleName()+ " " + svnStatus.getTextStatus() + " for file " + svnStatus.getFile());
                
                all.add(svnStatus.getFile());
            }

            ISVNClientAdapter svnClient = localResource.getRepository().getSVNClient();
            
            tmpFile = File.createTempFile("ereviewboard", "diff");
            svnClient.diff(all.toArray(new File[all.size()]), tmpFile, true);
            
            File projectFile = currentProject.getLocation().toFile();
            StringBuilder text = new StringBuilder();   
            
            if ( reviewBoardRepository == null )
                text.append("No matching repository found.\n");
            else
                text.append("Repository with name " + reviewBoardRepository.getName() +" and location " + reviewBoardRepository.getPath() + " found.\n");
            
            append(added, projectFile, "Added", text);
            append(modified, projectFile, "Modified", text);
            append(unversioned, projectFile, "Unversioned - will not be added to the diff", text);
            
            reader = new FileReader(tmpFile);
            List<?> diffLines = IOUtils.readLines(reader);
            
            for ( Object line : diffLines )
                text.append(line).append('\n');
            
            MessageDialog.openInformation(null, "Review request summary for " + currentProject.getName(), text.toString());
        } catch (SVNException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SVNClientException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(tmpFile);
            IOUtils.closeQuietly(reader);
        }
    }

    private void append(List<File> files, File base, String heading, StringBuilder text) {

        if ( files.isEmpty() )
            return;
        
        text.append(heading).append(":\n\n");
        for ( File file : files ) {
            
            String unqualified = file.getAbsolutePath().substring(base.getAbsolutePath().length());
            
            text.append(" - ").append(unqualified).append("\n");
        }
        text.append("\n");
    }

    public void selectionChanged(IAction action, ISelection selection) {

        if ( selection instanceof IStructuredSelection ) {
            
            IStructuredSelection sel = (IStructuredSelection) selection;
            
            if ( sel.getFirstElement() instanceof IProject ) {
                currentProject = (IProject) sel.getFirstElement();
                
                return;
            }
        }
        
        currentProject = null;
    }
}
