package org.review_board.ereviewboard.core.model.reviews;

import org.eclipse.mylyn.reviews.core.model.ITopic;

/**
 * @author Robert Munteanu
 *
 */
public interface TopicAddedListener {

    void topicAdded(ITopic topic);
}
