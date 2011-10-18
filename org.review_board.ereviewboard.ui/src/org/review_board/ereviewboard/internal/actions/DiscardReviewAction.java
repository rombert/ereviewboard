package org.review_board.ereviewboard.internal.actions;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;
import org.review_board.ereviewboard.ui.editor.ext.TaskDiffAction;

/**
 * @author Robert Munteanu
 */
public class DiscardReviewAction implements TaskDiffAction {

    private TaskRepository _repository;
    private Integer _diffRevisionId;
    private int _reviewRequestId;
    private ReviewboardDiffMapper _diffMapper;

    public void init(TaskRepository repository, int reviewRequestId, Repository codeRepository,
            ReviewboardDiffMapper diffMapper, Integer diffRevisionId) {
        _repository = repository;
        _diffMapper = diffMapper;
        _diffRevisionId = diffRevisionId;
        _reviewRequestId = reviewRequestId;
        
    }

    public boolean isEnabled() {
        return _diffRevisionId != null;
    }

    public IStatus execute(IProgressMonitor monitor) {
        
        ReviewboardClient client = ReviewboardCorePlugin.getDefault().getConnector().getClientManager().getClient(_repository);
        
        DiscardReviewDialog dialog = new DiscardReviewDialog(null, client,_reviewRequestId, _diffMapper.getNumberOfDraftComments(_diffRevisionId));
        dialog.open();
        
        return new Status(IStatus.OK, ReviewboardUiPlugin.PLUGIN_ID, STATUS_CODE_REFRESH_REVIEW_REQUEST, Status.OK_STATUS.getMessage(), null);
    }
}
