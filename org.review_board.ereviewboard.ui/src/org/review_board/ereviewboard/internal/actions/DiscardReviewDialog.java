package org.review_board.ereviewboard.internal.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;

class DiscardReviewDialog extends ReviewboardDialog {

    private final ReviewboardClient _client;
    private final int _reviewRequestId;
    private int _numberOfDraftComments;

    public DiscardReviewDialog(Shell shell, ReviewboardClient client, int reviewRequestId, int numberOfDraftComments) {
        super(shell);
        
        _client = client;
        _reviewRequestId = reviewRequestId;
        _numberOfDraftComments = numberOfDraftComments;
    }

    @Override
    protected void executeAction(IProgressMonitor monitor) throws ReviewboardException {
        
        monitor.beginTask("Deleting Review draft", 1);
        
        try {
            _client.deleteReviewDraft(_reviewRequestId, monitor);
        } finally {
            monitor.done();
        }
    }

    @Override
    protected void createPageControlsWithComposite(Composite composite) {
        
        setTitle("Discard review draft");
        setMessage("Complete this action to discard the current review draft. All comments will be deleted.");
        
        Label label = new Label(composite, SWT.NONE);
        label.setText(NLS.bind("{0} draft comment(s) will be discarded.", _numberOfDraftComments));
    }

}
