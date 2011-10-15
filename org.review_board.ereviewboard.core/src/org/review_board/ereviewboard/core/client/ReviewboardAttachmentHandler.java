/*******************************************************************************
 * Copyright (c) 2004, 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.core.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardRepositoryConnector;
import org.review_board.ereviewboard.core.exception.ReviewboardException;

/**
 * 
 * @author Robert Munteanu
 *
 */
public class ReviewboardAttachmentHandler extends AbstractTaskAttachmentHandler {

    public static final String ATTACHMENT_ATTRIBUTE_REVISION = "REVISION";

    public static final Long ATTACHMENT_SIZE_UNKNOWN = Long.valueOf(-1);
    
    private final ReviewboardRepositoryConnector reviewboardRepositoryConnector;
    

    public ReviewboardAttachmentHandler(ReviewboardRepositoryConnector reviewboardRepositoryConnector) {
        this.reviewboardRepositoryConnector = reviewboardRepositoryConnector;
        
    }

    @Override
    public boolean canGetContent(TaskRepository repository, ITask task) {
        return true;
    }

    @Override
    public boolean canPostContent(TaskRepository repository, ITask task) {
        return false;
    }

    @Override
    public InputStream getContent(TaskRepository repository, ITask task,
            TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
        
        try {
            ReviewboardClient client = reviewboardRepositoryConnector.getClientManager().getClient(repository);

            TaskAttribute revision = attachmentAttribute.getAttribute(ATTACHMENT_ATTRIBUTE_REVISION);
            int reviewRequestId = Integer.parseInt(task.getTaskId());
            
            if ( revision != null ) {
            
                
                int revisionId = Integer.parseInt(revision.getValue());
    
                byte[] rawDiff = client.getRawDiff(reviewRequestId, revisionId, monitor);
                
                return new ByteArrayInputStream(rawDiff);
            } else {
                
                int screenshotId = Integer.parseInt(attachmentAttribute.getValue());
                
                byte[] screenshot = client.getScreenshot(reviewRequestId, screenshotId, monitor);
                
                return new ByteArrayInputStream(screenshot);
            }
        } catch (ReviewboardException e) {
            throw new CoreException(new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, "Failed retrieving diff", e));
        }
    }

    @Override
    public void postContent(TaskRepository repository, ITask task,
            AbstractTaskAttachmentSource source, String comment, TaskAttribute attachmentAttribute,
            IProgressMonitor monitor) throws CoreException {
        
    }

}
