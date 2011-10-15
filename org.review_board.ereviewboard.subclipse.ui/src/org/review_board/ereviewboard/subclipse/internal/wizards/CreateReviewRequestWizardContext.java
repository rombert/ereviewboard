/*******************************************************************************
 * Copyright (c) 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
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