package org.review_board.ereviewboard.subclipse.internal.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.team.core.RepositoryProvider;
import org.review_board.ereviewboard.core.model.Repository;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;


/**
 * The <tt>ReviewboardToSvnMapper</tt> maps between various Reviewboard items and their SVN correspondents
 * 
 * @author Robert Munteanu
 *
 */
public class ReviewboardToSvnMapper {
    
    public IProject findProjectForRepository(Repository codeRepository, TaskRepository taskRepository) {
        
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        for ( IProject project : workspace.getRoot().getProjects() ) {
            
            SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(project, SVNProviderPlugin.getTypeId());
            
            if ( svnProvider == null )
                continue;
            
            ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(project);
            String svnRepositoryPath = projectSvnResource.getRepository().getRepositoryRoot().toString();
            
            if ( codeRepository.getPath().equals(svnRepositoryPath) )
                return project;
        }
        
        return null;
    }

}
