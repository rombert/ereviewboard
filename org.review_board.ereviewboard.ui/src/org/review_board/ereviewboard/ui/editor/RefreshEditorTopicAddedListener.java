package org.review_board.ereviewboard.ui.editor;

import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.reviews.core.model.ITopic;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.review_board.ereviewboard.core.model.reviews.TopicAddedListener;

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
        
        AbstractRepositoryConnector connector = TasksUi.getRepositoryConnector(_taskEditorPage.getConnectorKind());
        
        TasksUiInternal.synchronizeTask(connector, _taskEditorPage.getTask(), true, new JobChangeAdapter());
    }
}
