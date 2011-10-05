package org.review_board.ereviewboard.subclipse.internal.wizards;

import org.eclipse.core.runtime.Assert;
import org.review_board.ereviewboard.core.client.ReviewboardClient;

/**
 * Holds information needed by all pages in the wizard
 * 
 * @author Robert Munteanu
 * 
 */
class CreateReviewRequestWizardContext {

    private ReviewboardClient _reviewboardClient;

    public ReviewboardClient getReviewboardClient() {

        Assert.isLegal(_reviewboardClient != null);
        
        return _reviewboardClient;
    }

    public void setReviewboardClient(ReviewboardClient reviewboardClient) {

        _reviewboardClient = reviewboardClient;
    }

}