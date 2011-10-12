package org.review_board.ereviewboard.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.reviews.core.model.ITopic;
import org.eclipse.mylyn.reviews.ui.ReviewBehavior;
import org.eclipse.mylyn.tasks.core.ITask;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;
import org.review_board.ereviewboard.ui.ReviewboardUiUtil;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardReviewBehaviour extends ReviewBehavior {
    public ReviewboardReviewBehaviour(ITask task) {
        super(task);
    }

    @Override
    public IStatus addTopic(ITopic topic, IProgressMonitor monitor) {
        return new Status(IStatus.INFO, ReviewboardUiPlugin.PLUGIN_ID, "Adding comments is not supported.");
    }
}