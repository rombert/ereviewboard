package org.review_board.ereviewboard.ui.editor;

import org.eclipse.mylyn.reviews.core.model.ITopic;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.review_board.ereviewboard.core.model.reviews.TopicAddedListener;
import org.review_board.ereviewboard.ui.util.EditorUtil;

/**
 * @author Robert Munteanu
 *
 */
public class RefreshEditorTopicAddedListener implements TopicAddedListener {

    private AbstractTaskEditorPage _taskEditorPage;

    public RefreshEditorTopicAddedListener(AbstractTaskEditorPage taskEditorPage) {
        
        _taskEditorPage = taskEditorPage;
    }
    
    
    public void topicAdded(ITopic topic) {
        
        EditorUtil.refreshEditorPage(_taskEditorPage);
    }
}
