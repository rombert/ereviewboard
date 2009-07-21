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
package org.review_board.ereviewboard.core;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class ReviewboardRepositoryConnector extends AbstractRepositoryConnector {

    private static final String CLIENT_LABEL = "Reviewboard (supports 1.0 and later)";

    private final static Pattern REVIEW_REQUEST_ID_FROM_TASK_URL = Pattern
            .compile(ReviewboardConstants.REVIEW_REQUEST_URL + "(\\d+)");

    private ReviewboardClientManager clientManager;

    private TaskRepositoryLocationFactory taskRepositoryLocationFactory;

    public ReviewboardRepositoryConnector() {
        super();

        if (ReviewboardCorePlugin.getDefault() != null) {
            ReviewboardCorePlugin.getDefault().setConnector(this);
        }
    }

    @Override
    public boolean canCreateNewTask(TaskRepository repository) {
        return false;
        // return repository.getConnectorKind().equals(getConnectorKind());
    }

    @Override
    public boolean canCreateTaskFromKey(TaskRepository repository) {
        return true;
    }

    @Override
    public String getConnectorKind() {
        return ReviewboardCorePlugin.REPOSITORY_KIND;
    }

    @Override
    public String getLabel() {
        return CLIENT_LABEL;
    }

    @Override
    public String getRepositoryUrlFromTaskUrl(String taskFullUrl) {
        int index = taskFullUrl.indexOf(ReviewboardConstants.REVIEW_REQUEST_URL);

        if (index > 0) {
            return taskFullUrl.substring(0, index);
        } else {
            return null;
        }
    }

    @Override
    public TaskData getTaskData(TaskRepository taskRepository, String taskId,
            IProgressMonitor monitor) throws CoreException {
        ReviewboardClient client = getClientManager().getClient(taskRepository);
        return client.getTaskData(taskRepository, taskId, monitor);
    }

    @Override
    public String getTaskIdFromTaskUrl(String taskFullUrl) {
        Matcher matcher = REVIEW_REQUEST_ID_FROM_TASK_URL.matcher(taskFullUrl);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    @Override
    public String getTaskUrl(String repositoryUrl, String taskId) {
        return ReviewboardUtil.getReviewRequestUrl(repositoryUrl, taskId);
    }

    @Override
    public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
        TaskMapper scheme = new TaskMapper(taskData);
        Date repositoryDate = scheme.getModificationDate();
        Date localeDate = task.getModificationDate();

        if (localeDate != null) {
            return !localeDate.equals(repositoryDate);
        }

        return true;
    }

    @Override
    public IStatus performQuery(TaskRepository repository, IRepositoryQuery query,
            TaskDataCollector collector, ISynchronizationSession session, IProgressMonitor monitor) {
        ReviewboardClient client = getClientManager().getClient(repository);

        try {
            client.updateRepositoryData(false, monitor);
            client.performQuery(repository, query, collector, monitor);
        } catch (CoreException e) {
            return e.getStatus();
        }

        return Status.OK_STATUS;
    }

    @Override
    public void updateRepositoryConfiguration(TaskRepository taskRepository,
            IProgressMonitor monitor) throws CoreException {
        // ignore
    }

    @Override
    public void updateTaskFromTaskData(TaskRepository taskRepository, ITask task, TaskData taskData) {
        TaskMapper scheme = new TaskMapper(taskData);
        scheme.applyTo(task);
        task.setCompletionDate(scheme.getCompletionDate());
    }

    @Override
    public boolean canSynchronizeTask(TaskRepository taskRepository, ITask task) {
        return false;
    }

    public synchronized ReviewboardClientManager getClientManager() {
        if (clientManager == null) {
            IPath path = ReviewboardCorePlugin.getDefault().getRepostioryAttributeCachePath();
            clientManager = new ReviewboardClientManager(path.toFile());
        }
        clientManager.setTaskRepositoryLocationFactory(taskRepositoryLocationFactory);

        return clientManager;
    }

    public void stop() {
        if (clientManager != null) {
            clientManager.writeCache();
        }
    }

    public void setTaskRepositoryLocationFactory(TaskRepositoryLocationFactory factory) {
        this.taskRepositoryLocationFactory = factory;
        if (clientManager != null) {
            clientManager.setTaskRepositoryLocationFactory(factory);
        }
    }

}
