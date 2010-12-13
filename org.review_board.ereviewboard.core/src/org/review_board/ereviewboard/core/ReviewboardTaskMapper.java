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

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper.Attribute;
import org.review_board.ereviewboard.core.model.ReviewRequestStatus;

/**
 * Utility class for mapping between TaskData and Task
 *
 * @author Markus Knittig
 */
public class ReviewboardTaskMapper extends TaskMapper {

	public ReviewboardTaskMapper(TaskData taskData) {
		super(taskData);
	}

	public ReviewboardTaskMapper(TaskData taskData,
			boolean createNonExistingAttributes) {
		super(taskData, createNonExistingAttributes);
	}

	public void setTaskKey(String key) {
        setValue(TaskAttribute.TASK_KEY, key);
    }
	
	@Override
	public Date getCompletionDate() {
	    
	    TaskAttribute statusAttribute = getTaskData().getRoot().getAttribute(Attribute.STATUS.toString());
	    
	    // task is mapped using the 'task.common' attributes, therefore we delegate to the super implementation
	    if ( statusAttribute == null )
            return super.getCompletionDate();
	       
	    if ( ReviewRequestStatus.PENDING.name().equalsIgnoreCase(statusAttribute.getValue()))
	        return null;
	    
	    TaskAttribute completionValue = getTaskData().getRoot().getAttribute(Attribute.LAST_UPDATED.toString());
	    
	    // getTaskData().getAttributeMapper() is just an AbstractTaskMapper
	    return ReviewboardAttributeMapper.parseDateValue(completionValue.getValue());
	}

}
