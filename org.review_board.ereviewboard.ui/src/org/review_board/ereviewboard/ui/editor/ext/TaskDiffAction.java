package org.review_board.ereviewboard.ui.editor.ext;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.review_board.ereviewboard.core.model.Repository;

/**
 * @author Robert Munteanu
 * 
 */
public interface TaskDiffAction {

    void init(TaskRepository repository, int reviewRequestId, Repository codeRepository, List<DiffResource> diffResources);

    boolean isEnabled();

    IStatus execute(IProgressMonitor monitor);
}