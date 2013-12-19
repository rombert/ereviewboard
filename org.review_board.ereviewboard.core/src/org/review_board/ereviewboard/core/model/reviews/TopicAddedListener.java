package org.review_board.ereviewboard.core.model.reviews;

import org.eclipse.mylyn.reviews.core.model.IComment;

/**
 * @author Robert Munteanu
 *
 */
public interface TopicAddedListener {

    void topicAdded(IComment topic);
}
