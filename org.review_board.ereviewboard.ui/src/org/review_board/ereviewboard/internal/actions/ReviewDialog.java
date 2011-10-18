package org.review_board.ereviewboard.internal.actions;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Review;

/**
 * @author Robert Munteanu
 *
 */
class ReviewDialog extends ReviewboardDialog {

    private final ReviewboardClient _client;
    private final int _reviewRequestId;
    private final Review _review;
    private int _numberOfDraftComments;

    public ReviewDialog(Shell parentShell, ReviewboardClient client,  int reviewRequestId, int numberOfDraftComments) {
        super(parentShell);
        _review = new Review();
        _review.setPublicReview(true);
        _reviewRequestId = reviewRequestId;
        _numberOfDraftComments = numberOfDraftComments;
        _client = client;
    }

    @Override
    protected void createPageControlsWithComposite(Composite composite) {
        
        setTitle("Review");
        setMessage("Add optional top and bottom comments and then publish the review.");
        
        Label topLabel = new Label(composite, SWT.NONE);
        topLabel.setText("Top");
        
        final Text topEditor = new Text(composite, SWT.MULTI | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).minSize(200, 60).applyTo(topEditor);
        topEditor.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
                _review.setBodyTop(topEditor.getText());
                
            }
        });

        Label bottomLabel = new Label(composite, SWT.NONE);
        bottomLabel.setText("Bottom");
        
        final Text bottomEditor = new Text(composite, SWT.MULTI | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).minSize(200, 60).applyTo(bottomEditor);
        bottomEditor.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
                _review.setBodyBottom(bottomEditor.getText());
                
            }
        });

        final Button shipIt = new Button(composite, SWT.CHECK);
        shipIt.setText("Ship It");
        shipIt.addSelectionListener(new SelectionListener() {
            
            public void widgetSelected(SelectionEvent e) {
            
                _review.setShipIt(shipIt.getSelection());
                
            }
            
            public void widgetDefaultSelected(SelectionEvent e) {
                
            }
        });
        
        Label comments = new Label(composite, SWT.NONE);
        comments.setText(NLS.bind("{0} draft comment(s) will be published.", _numberOfDraftComments));
    }
    
    @Override
    protected void executeAction(IProgressMonitor monitor) throws ReviewboardException {
        
        monitor.beginTask("Publishing review", 1);

        try {
            _client.createReview(_reviewRequestId, _review, monitor);
        } finally {
            monitor.done();
        }
    }
}