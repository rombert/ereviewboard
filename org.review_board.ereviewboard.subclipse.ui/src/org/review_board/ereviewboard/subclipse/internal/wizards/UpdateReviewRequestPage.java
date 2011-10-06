package org.review_board.ereviewboard.subclipse.internal.wizards;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * @author Robert Munteanu
 *
 */
class UpdateReviewRequestPage extends WizardPage {

    private CreateReviewRequestWizardContext _context;

    public UpdateReviewRequestPage(CreateReviewRequestWizardContext context) {

        super("Update Review Request", "Update Review Request", null);
        setMessage("Provide an optional description for the changes you are about to publish.");
        
        _context = context;
    }
    
    public void createControl(Composite parent) {
        
        Composite control = new Composite(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().applyTo(control);
        
        Label label = new Label(control, SWT.NONE);
        label.setText("Updating review requests is not yet implemented.");
        
        setControl(control);
    }

    @Override
    public boolean isPageComplete() {
    
        return false;
    }
}
