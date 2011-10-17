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
 * @author Robert Munteanu
 * 
 */
public interface TaskDiffAction {

    /**
     * @param repository
     * @param reviewRequestId
     * @param codeRepository
     * @param diffMapper
     * @param diffRevisionId the diff revision id from this action, or null if applicable for all diff actions
     */
    void init(TaskRepository repository, int reviewRequestId, Repository codeRepository, ReviewboardDiffMapper diffMapper, Integer diffRevisionId);

    boolean isEnabled();

    IStatus execute(IProgressMonitor monitor);
}