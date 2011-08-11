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
package org.review_board.ereviewboard.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import static org.review_board.ereviewboard.core.ReviewboardAttributeMapper.Attribute.*;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardDiffMapper {
    
    /**
     * Signals that the file is newly created in this diff and is not expected to be found in the source
     * repository
     */
    public static final String REVISION_PRE_CREATION = "PRE-CREATION";
    
    private final TaskData taskData;

    public ReviewboardDiffMapper(TaskData taskData, int diffRevision) {
        
        Assert.isNotNull(taskData, "taskData may not be null");
        
        this.taskData = taskData;
        
        TaskAttribute diffAttribute = taskData.getRoot().createAttribute(LATEST_DIFF.toString());
        diffAttribute.setValue(String.valueOf(diffRevision));
    }
    
    public ReviewboardDiffMapper(TaskData taskData) {
        
        this.taskData = taskData;
    }

    public void addFileDiff(int fileDiffId, String sourceFile, String sourceRevision) {
        
        TaskAttribute diff = latestDiff();
        int nextId = diff.getAttributes().size() - 1;
        
        TaskAttribute fileDiffAttribute = diff.createAttribute("file-" + nextId);
        fileDiffAttribute.setValue(String.valueOf(fileDiffId));
        fileDiffAttribute.createAttribute(SOURCE_FILE.toString()).setValue(sourceFile);
        fileDiffAttribute.createAttribute(SOURCE_REVISION.toString()).setValue(sourceRevision);
    }
    
    private TaskAttribute latestDiff() {
        
        TaskAttribute attribute = taskData.getRoot().getAttribute(LATEST_DIFF.toString());
        
        Assert.isNotNull(attribute,LATEST_DIFF.toString() + " attribute not found");
        
        return attribute;
    }

    public List<TaskAttribute> getFileDiffs() {
        
        return new ArrayList<TaskAttribute>(latestDiff().getAttributes().values());
    }
    
    public int getDiffRevision() {
        
        return Integer.parseInt(latestDiff().getValue());
    }
}
