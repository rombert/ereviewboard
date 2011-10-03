package org.review_board.ereviewboard.subclipse.internal.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IActionDelegate;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

/**
 * @author Robert Munteanu
 */
public class StartReviewAction implements IActionDelegate {

    private IProject currentProject;
    
    public void run(IAction action) {
        
        if ( currentProject == null )
            return;
        
        SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(currentProject, SVNProviderPlugin.getTypeId());
        if ( svnProvider == null ) {
            MessageDialog.openWarning(null, "Not supported", "Only subclipse-mapped projects are supported.");
            return;
        }
        
        ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(currentProject);
        
        try {
            LocalResourceStatus status = localResource.getStatus();
            String projectUrl = String.valueOf(status.getUrl());
            
            MessageDialog.openInformation(null, "Title", "Will create review for " + currentProject.getName() + "\n\nLocation: " + projectUrl);
        } catch (SVNException e) {
            e.printStackTrace();
        }
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
