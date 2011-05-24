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

import java.util.List;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
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
    
    public void setTestingDone(String testingDone) {

        setAttributeValue(Attribute.TESTING_DONE, testingDone);
    }
    
    public String getTestingDone() {
    
        return getAttributeValue(Attribute.TESTING_DONE);
    }
    
    public void setRepository(String repository) {
        
        setAttributeValue(Attribute.REPOSITORY, repository);
    }

    public void setBranch(String branch) {
        
        setAttributeValue(Attribute.BRANCH, branch);
    }

    public void setChangeNum(Integer changeNumber) {

        setAttributeValue(Attribute.CHANGENUM, changeNumber == null ? "" : changeNumber.toString());
    }
    
    public void setBugsClosed(List<String> bugsClosed) {
     
        setAttributeValues(Attribute.BUGS_CLOSED, bugsClosed);
    }
    
    public void setPublic(boolean isPublic) {

        setAttributeValue(Attribute.PUBLIC, Boolean.toString(isPublic));
    }

    public void setTargetPeople(List<String> targetPeople) {
        
        setAttributeValues(Attribute.TARGET_PEOPLE, targetPeople);
    }
    
    public void setTargetGroups(List<String> targetGroups) {
        
        setAttributeValues(Attribute.TARGET_GROUPS, targetGroups);
    }
    
    /**
     * Applies final customisations to mapped task data
     */
    public void complete() {
        
        for ( String attribute : new String[] { TaskAttribute.SUMMARY, TaskAttribute.USER_REPORTER, TaskAttribute.DESCRIPTION } )
            getTaskData().getRoot().getMappedAttribute(attribute).getMetaData().setReadOnly(true);
    }
    
    private String getAttributeValue(Attribute attributeEnum) {
        
        TaskAttribute attribute = getTaskData().getRoot().getAttribute(attributeEnum.toString());
        
        if ( attribute == null )
            return null;
        
        return attribute.getValue();
    }
    
    private void setAttributeValue(Attribute enumAttribute, String value) {
    
        TaskAttribute attribute = getWriteableAttribute(enumAttribute);

        if ( attribute != null )
            attribute.setValue(value);

    }
    
    private void setAttributeValues(Attribute enumAttribute, List<String> values) {
        
        TaskAttribute attribute = getWriteableAttribute(enumAttribute);

        if ( attribute != null )
            attribute.setValues(values);
    }


    private TaskAttribute getWriteableAttribute(Attribute attributeEnum) {
        String attributeKey = attributeEnum.toString();
        TaskAttribute attribute = getTaskData().getRoot().getAttribute(attributeKey);
        if (attribute == null) {
            attribute = createAttribute(attributeEnum);
        } else if (attribute != null && attribute.getMetaData().isReadOnly()) {
            attribute = null;
        }
        return attribute;
    }

    private TaskAttribute createAttribute(Attribute attributeEnum) {

        TaskAttribute attribute = getTaskData().getRoot().createAttribute(attributeEnum.toString());
        attribute.getMetaData().defaults().setType(attributeEnum.getAttributeType())
            .setReadOnly(true).setKind(attributeEnum.getAttributeType()).setLabel(attributeEnum.getDisplayName())
            .setKind(attributeEnum.getAttributeKind());
            
        return attribute;
    }

    
    /**
     * Must be called after the completion date is set, otherwise the operations might be incorrect
     */
    public void addOperations() {
        
        TaskAttribute operation = getTaskData().getRoot().createAttribute(TaskAttribute.OPERATION);
        TaskOperation.applyTo(operation, "Operation", "Operation");
        
        TaskAttribute leave = getTaskData().getRoot().createAttribute(TaskAttribute.PREFIX_OPERATION+"leave");
        TaskOperation.applyTo(leave, "Operation.LeaveUnchanged", "Leave unchanged");

        if ( getCompletionDate() == null ) {
            TaskAttribute operationStatusAttribute = createAttribute(Attribute.OPERATION_STATUS);
            operationStatusAttribute.getMetaData().setReadOnly(false);
            operationStatusAttribute.putOption(ReviewRequestStatus.SUBMITTED.name(), ReviewRequestStatus.SUBMITTED.getDisplayname());
            operationStatusAttribute.putOption(ReviewRequestStatus.DISCARDED.name(), ReviewRequestStatus.DISCARDED.getDisplayname());
            
            TaskAttribute close = getTaskData().getRoot().createAttribute(TaskAttribute.PREFIX_OPERATION+"close");
            TaskOperation.applyTo(close, "close", "Close as");
            close.getMetaData().putValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID, ReviewboardAttributeMapper.Attribute.OPERATION_STATUS.toString());
        } else {
            TaskAttribute close = getTaskData().getRoot().createAttribute(TaskAttribute.PREFIX_OPERATION+"reopen");
            TaskOperation.applyTo(close, "reopen", "Reopen");
        }
    }
}
