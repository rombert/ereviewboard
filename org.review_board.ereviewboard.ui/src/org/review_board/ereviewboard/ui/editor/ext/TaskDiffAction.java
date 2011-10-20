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
package org.review_board.ereviewboard.ui.editor.ext;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.model.Repository;

/**
 * The <tt>TaskDiffAction</tt> is able to perform an action related to a submitted diff
 * 
 * <p>The methods are invoked as follows:</p>
 * 
 * <ol>
 *   <li>the {@linkplain #init(TaskRepository, int, Repository, ReviewboardDiffMapper, Integer)}} method is invoked</li>
 *   <li>the {@linkplain #isEnabled()} method is invoked</li>
 *   <li>if the {@linkplain #isEnabled()} method returns true, the {@linkplain #execute(IProgressMonitor)}
 *   method is invoked</li>
 * </ol>
 * 
 * @author Robert Munteanu
 * 
 */
// TODO rename to ReviewRequestDiffAction
public interface TaskDiffAction {
    
    /**
     * Signals that after the action is succesfully executed the locally cached review request data should be refreshed
     */
    int STATUS_CODE_REFRESH_REVIEW_REQUEST = 1;

    /**
     * @param repository the task repository to which the review request is associated 
     * @param reviewRequestId the id of the review request
     * @param codeRepository the source code repository associated with the review request
     * @param diffMapper the mapper which describes the diffs associated with the review request
     * @param diffRevisionId the diff revision id from this action, or null if applicable for all diff actions
     */
    void init(TaskRepository repository, int reviewRequestId, Repository codeRepository, ReviewboardDiffMapper diffMapper, Integer diffRevisionId);

    /**
     * @return true if this action can be executed for the properties given to {@link #init(TaskRepository, int, Repository, ReviewboardDiffMapper, Integer)}
     */
    boolean isEnabled();

    /**
     * Executes this action
     * 
     * @param monitor
     * @return the status corresponding to the result of the action
     * 
     * @see #STATUS_CODE_REFRESH_REVIEW_REQUEST
     */
    IStatus execute(IProgressMonitor monitor);
}