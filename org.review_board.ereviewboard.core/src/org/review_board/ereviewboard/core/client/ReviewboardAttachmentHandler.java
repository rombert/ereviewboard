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
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * 
 * @author Robert Munteanu
 *
 */
public class ReviewboardAttachmentHandler extends AbstractTaskAttachmentHandler {

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
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public void postContent(TaskRepository repository, ITask task,
            AbstractTaskAttachmentSource source, String comment, TaskAttribute attachmentAttribute,
            IProgressMonitor monitor) throws CoreException {
        
    }

}
