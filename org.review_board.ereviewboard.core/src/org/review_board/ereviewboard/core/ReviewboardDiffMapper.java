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

import static org.review_board.ereviewboard.core.ReviewboardAttributeMapper.Attribute.*;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.review_board.ereviewboard.core.model.Diff;
import org.review_board.ereviewboard.core.model.FileDiff;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardDiffMapper {
    
    private static final String PREFIX_FILE = "file-";
    
    private final TaskData taskData;

    public ReviewboardDiffMapper(TaskData taskData, Diff latestDiff) {
        
        Assert.isNotNull(taskData, "taskData may not be null");
        Assert.isNotNull(latestDiff, "latestDiff may not be null");
        
        this.taskData = taskData;
        
        TaskAttribute diffAttribute = taskData.getRoot().createAttribute(LATEST_DIFF.toString());
        diffAttribute.setValue(String.valueOf(latestDiff.getRevision()));
        
        taskData.getAttributeMapper().setDateValue(diffAttribute.createAttribute(LAST_UPDATED.toString()), latestDiff.getTimestamp());
    }
    
    public ReviewboardDiffMapper(TaskData taskData) {
        
        this.taskData = taskData;
    }

    public void addFileDiff(FileDiff fileDiff) {
        
        TaskAttribute diff = latestDiff();
        
        TaskAttribute fileDiffAttribute = diff.createAttribute(PREFIX_FILE + fileDiff.getId());
        fileDiffAttribute.setValue(String.valueOf(fileDiff.getId()));
        fileDiffAttribute.createAttribute(SOURCE_FILE.toString()).setValue(fileDiff.getSourceFile());
        fileDiffAttribute.createAttribute(DEST_FILE.toString()).setValue(fileDiff.getDestinationFile());
        fileDiffAttribute.createAttribute(SOURCE_REVISION.toString()).setValue(fileDiff.getSourceRevision());
        fileDiffAttribute.createAttribute(DEST_DETAIL.toString()).setValue(fileDiff.getDestinationDetail());
    }
    
    private TaskAttribute latestDiff() {
        
        TaskAttribute attribute = taskData.getRoot().getAttribute(LATEST_DIFF.toString());
        
        Assert.isNotNull(attribute,LATEST_DIFF.toString() + " attribute not found");
        
        return attribute;
    }

    public List<FileDiff> getFileDiffs() {

        List<FileDiff> fileDiffs = new ArrayList<FileDiff>();
        
        for (TaskAttribute fileDiffAttribute : latestDiff().getAttributes().values()) {
            
            if ( !fileDiffAttribute.getId().startsWith(PREFIX_FILE) )
                continue;

            int fileDiffId = Integer.parseInt(fileDiffAttribute.getValue());
            final String sourcePath = fileDiffAttribute.getAttribute(SOURCE_FILE.toString()).getValue();
            final String sourceRevision = fileDiffAttribute.getAttribute(SOURCE_REVISION.toString()).getValue();
            final String destinationFile = fileDiffAttribute.getAttribute(DEST_FILE.toString()).getValue();
            final String destinationDetail = fileDiffAttribute.getAttribute(DEST_DETAIL.toString()).getValue();

            fileDiffs.add(new FileDiff(fileDiffId, sourcePath, sourceRevision,
                    destinationFile, destinationDetail));
        }

        return fileDiffs;
    }
    
    public int getDiffRevision() {
        
        return Integer.parseInt(latestDiff().getValue());
    }

  
    public String getTimestamp() {

        Date timestamp = taskData.getAttributeMapper().getDateValue(latestDiff().getAttribute(LAST_UPDATED.toString()));
        
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(timestamp);
    }
}
