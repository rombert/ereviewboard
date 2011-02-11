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
import java.util.List;
import java.util.Set;
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
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.review_board.ereviewboard.core.client.ReviewboardAttachmentHandler;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class ReviewboardRepositoryConnector extends AbstractRepositoryConnector {
    
    private static final String CLIENT_LABEL = "Reviewboard (supports 1.5 and later)";

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
        return repository.getConnectorKind().equals(getConnectorKind());
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
    public AbstractTaskAttachmentHandler getTaskAttachmentHandler() {
     
        return new ReviewboardAttachmentHandler(this);
    }
    
    @Override
    public TaskData getTaskData(TaskRepository taskRepository, String taskId,
            IProgressMonitor monitor) throws CoreException {
        try {
            ReviewboardClient client = getClientManager().getClient(taskRepository);
            return client.getTaskData(taskRepository, taskId, monitor);
        } catch (ReviewboardException e) {
            Status status = new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, "Failed getting task data for task with id " + taskId , e);
            ReviewboardCorePlugin.getDefault().getLog().log(status);
            throw new CoreException(status);
        }
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
        
        Date repositoryDate = getTaskMapping(taskData).getModificationDate();
        Date localDate = task.getModificationDate();
        
        if ( repositoryDate == null )
            return false;

        return !repositoryDate.equals(localDate);
    }
    
    @Override
    public void preSynchronization(ISynchronizationSession event, IProgressMonitor monitor)
            throws CoreException {
        try {
            // No Tasks, don't contact the repository
            if (event.getTasks().isEmpty())
                return;

            TaskRepository repository = event.getTaskRepository();

            // no previous sync, all are stale
            if (repository.getSynchronizationTimeStamp() == null || repository.getSynchronizationTimeStamp().length() == 0) {
                for (ITask task : event.getTasks())
                    event.markStale(task);
                return;
            }
            
            Date lastSyncTimestamp = new Date(Long.parseLong(repository.getSynchronizationTimeStamp()));
            
            ReviewboardClient client = getClientManager().getClient(repository);
            
            List<Integer> changedReviewIds = client.getReviewsIdsChangedSince(lastSyncTimestamp, monitor);
            
            if ( changedReviewIds.isEmpty() )
                return;
            
            for ( ITask task : event.getTasks() )
                if ( changedReviewIds.contains(Integer.valueOf(task.getTaskId())) )
                    event.markStale(task);
            
            
        } catch (ReviewboardException e) {
            Status status = new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, "Failed retrieving changed review ids", e);
            ReviewboardCorePlugin.getDefault().getLog().log(status);
            throw new CoreException(status);
        }
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
    public void postSynchronization(ISynchronizationSession event, IProgressMonitor monitor) throws CoreException {

        try {
            monitor.beginTask("", 1);
            event.getTaskRepository().setSynchronizationTimeStamp(String.valueOf(getSynchronizationTimestamp(event).getTime()));
        } finally {
            monitor.done();
        }
    }

    private Date getSynchronizationTimestamp(ISynchronizationSession event) {

        Date mostRecent = new Date(0);
        Date mostRecentTimeStamp = null;
        if (event.getTaskRepository().getSynchronizationTimeStamp() == null) {
            mostRecentTimeStamp = mostRecent;
        } else {
            mostRecentTimeStamp = new Date(Long.parseLong(event.getTaskRepository() .getSynchronizationTimeStamp()));
        }
        for (ITask task : event.getChangedTasks()) {
            Date taskModifiedDate = task.getModificationDate();
            if (taskModifiedDate != null && taskModifiedDate.after(mostRecent)) {
                mostRecent = taskModifiedDate;
                mostRecentTimeStamp = task.getModificationDate();
            }
        }
        return mostRecentTimeStamp;
    }

    @Override
    public void updateRepositoryConfiguration(TaskRepository taskRepository,
            IProgressMonitor monitor) throws CoreException {
        // ignore
    }

    @Override
    public void updateTaskFromTaskData(TaskRepository taskRepository, ITask task, TaskData taskData) {
        
        TaskMapper scheme = new ReviewboardTaskMapper(taskData);
        scheme.applyTo(task);
        
        task.setUrl(getTaskUrl(taskRepository.getUrl(), task.getTaskId()));
        task.setCompletionDate(scheme.getCompletionDate());
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

    @Override
    public AbstractTaskDataHandler getTaskDataHandler() {
        return new AbstractTaskDataHandler() {
            @Override
            public TaskAttributeMapper getAttributeMapper(TaskRepository taskRepository) {
                return new ReviewboardAttributeMapper(taskRepository);
            }

            @Override
            public boolean initializeTaskData(TaskRepository repository, TaskData data,
                    ITaskMapping initializationData, IProgressMonitor monitor) throws CoreException {
                // ignore
                return false;
            }

              @Override
            public RepositoryResponse postTaskData(TaskRepository repository, TaskData taskData,
                    Set<TaskAttribute> oldAttributes, IProgressMonitor monitor)
                    throws CoreException {
                // ignore
                return null;
            }
        };

    }
    
    @Override
    public ITaskMapping getTaskMapping(TaskData taskData) {
        
        return new ReviewboardTaskMapper(taskData);
    }
}
