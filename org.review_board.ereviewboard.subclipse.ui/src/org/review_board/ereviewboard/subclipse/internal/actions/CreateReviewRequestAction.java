package org.review_board.ereviewboard.subclipse.internal.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.review_board.ereviewboard.subclipse.internal.wizards.CreateReviewRequestWizard;

/**
 * @author Robert Munteanu
 */
public class CreateReviewRequestAction implements IActionDelegate {

    private IProject currentProject;
    
    public void run(IAction action) {
        
        if ( currentProject == null )
            return;
        

        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        
        new WizardDialog(win.getShell(), new CreateReviewRequestWizard(currentProject)).open();
        
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
