/*******************************************************************************
 * Copyright (c) 2004 - 2009 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers, Atlassian, Sven Krzyzak
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2009 Markus Knittig
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Markus Knittig - adapted Trac, Redmine & Atlassian implementations for
 *                      Review Board
 *******************************************************************************/
package org.review_board.ereviewboard.core.client;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Review;
import org.review_board.ereviewboard.core.model.ReviewRequest;

/**
 * @author Markus Knittig
 *
 */
public interface ReviewboardClient {

    ReviewboardClientData getClientData();

    void refreshRepositorySettings(TaskRepository repository);

    List<ReviewRequest> getReviewRequests(String query) throws ReviewboardException;

    ReviewRequest newReviewRequest(ReviewRequest reviewRequest) throws ReviewboardException;

    ReviewRequest getReviewRequest(int reviewRequestId) throws ReviewboardException;

    List<String> getRawDiffs(int reviewRequestId) throws ReviewboardException;

    List<Review> getReviews(int reviewRequestId) throws ReviewboardException;

    void updateReviewRequest(ReviewRequest reviewRequest) throws ReviewboardException;

    void updateRepositoryData(boolean force, IProgressMonitor monitor);

    boolean hasRepositoryData();

    TaskData getTaskData(TaskRepository taskRepository, String taskId, IProgressMonitor monitor);

    void performQuery(TaskRepository repository, IRepositoryQuery query,
            TaskDataCollector collector, IProgressMonitor monitor) throws CoreException;

    boolean validCredentials(String username, String password);

}
