package org.review_board.ereviewboard.ui.util;

import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;

/**
 * @author Robert Munteanu
 *
 */
public abstract class EditorUtil {

    public static void refreshEditorPage(AbstractTaskEditorPage page) {
        
        AbstractRepositoryConnector connector = TasksUi.getRepositoryConnector(page.getConnectorKind());
        
        TasksUiInternal.synchronizeTask(connector, page.getTask(), true, new JobChangeAdapter());
    }
    
    private EditorUtil() {
        
    }
}
