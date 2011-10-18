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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper.Attribute;
import org.review_board.ereviewboard.core.model.Diff;
import org.review_board.ereviewboard.core.model.DiffComment;
import org.review_board.ereviewboard.core.model.FileDiff;

import com.google.common.collect.Multimap;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardDiffMapper {
    
    private static final String PREFIX_DIFF = "diff-";

    private static final String PREFIX_FILE = "file-";
    
    private final TaskData taskData;
    
    public ReviewboardDiffMapper(TaskData taskData) {
        
        Assert.isNotNull(taskData, "taskData may not be null");
        
        this.taskData = taskData;
        if ( taskData.getRoot().getAttribute(DIFFS.toString()) == null )
            taskData.getRoot().createAttribute(DIFFS.toString());
    }
    
    private TaskAttribute diff(int diffRevisionId) {
        
        TaskAttribute attribute = taskData.getRoot().getAttribute(DIFFS.toString()).getAttribute(PREFIX_DIFF + diffRevisionId);
        
        Assert.isNotNull(attribute,DIFFS.toString() + " attribute not found");
        
        return attribute;
    }

    public List<FileDiff> getFileDiffs(int diffRevisionId) {

        List<FileDiff> fileDiffs = new ArrayList<FileDiff>();
        
        for (TaskAttribute fileDiffAttribute : diff(diffRevisionId).getAttributes().values()) {
            
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
  
    public String getTimestamp(int diffRevisionId) {

        Date timestamp = taskData.getAttributeMapper().getDateValue(diff(diffRevisionId).getAttribute(LAST_UPDATED.toString()));
        
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(timestamp);
    }

    public void addDiff(Diff diff, List<FileDiff> fileDiffs, Multimap<Integer, DiffComment> fileIdToDiffComments) {
        
        Assert.isNotNull(diff, "diff may not be null");
        TaskAttribute diffsAttribute = taskData.getRoot().getAttribute(DIFFS.toString());
        TaskAttribute diffAttribute = diffsAttribute.createAttribute(PREFIX_DIFF + diff.getRevision());
        diffAttribute.setValue(String.valueOf(diff.getRevision()));
        taskData.getAttributeMapper().setDateValue(diffAttribute.createAttribute(LAST_UPDATED.toString()), diff.getTimestamp());
        int totalPublicComments = 0;
        int totalDraftComments = 0;
        
        for ( FileDiff fileDiff : fileDiffs ) {
        
            Collection<DiffComment> diffComments = fileIdToDiffComments.get(fileDiff.getId());
            int draftComments = 0;
            int publicComments = 0;
            for ( DiffComment diffComment : diffComments ) {
                if ( Boolean.FALSE.equals(diffComment.getPublic()) )
                    draftComments++;
                else
                    publicComments++;
                    
                    
            }
            totalPublicComments += publicComments;
            totalDraftComments += draftComments;
            
            TaskAttribute fileDiffAttribute = diffAttribute.createAttribute(PREFIX_FILE + fileDiff.getId());
            fileDiffAttribute.setValue(String.valueOf(fileDiff.getId()));
            fileDiffAttribute.createAttribute(SOURCE_FILE.toString()).setValue(fileDiff.getSourceFile());
            fileDiffAttribute.createAttribute(DEST_FILE.toString()).setValue(fileDiff.getDestinationFile());
            fileDiffAttribute.createAttribute(SOURCE_REVISION.toString()).setValue(fileDiff.getSourceRevision());
            fileDiffAttribute.createAttribute(DEST_DETAIL.toString()).setValue(fileDiff.getDestinationDetail());
            fileDiffAttribute.createAttribute(NUM_PUBLIC_COMMENTS.toString()).setValue(String.valueOf(publicComments));
            fileDiffAttribute.createAttribute(NUM_DRAFT_COMMENTS.toString()).setValue(String.valueOf(draftComments));
        }
        
        diffAttribute.createAttribute(NUM_PUBLIC_COMMENTS.toString()).setValue(String.valueOf(totalPublicComments));
        diffAttribute.createAttribute(NUM_DRAFT_COMMENTS.toString()).setValue(String.valueOf(totalDraftComments));
    }
    
    public int getPublicCommentCountForFileDiff(int fileDiffId) {
        
        return getIntFileDiffAttribute(fileDiffId, NUM_PUBLIC_COMMENTS);
    }

    private int getIntFileDiffAttribute(int fileDiffId, Attribute attributeKey) {
        
        for ( Integer diffRevisionId : getDiffRevisions() ) {
            for ( TaskAttribute attribute : diff(diffRevisionId).getAttributes().values() ) {
                
                if ( ! attribute.getId().startsWith(PREFIX_FILE) )
                    continue;
                
                if ( Integer.parseInt(attribute.getValue()) == fileDiffId )
                    return Integer.parseInt(attribute.getAttribute(attributeKey.toString()).getValue());
            }
        }
        
        throw new IllegalArgumentException("No data for file diff with id " + fileDiffId);
    }
    
    public int getDraftCommentCountForFileDiff(int fileDiffId) {
        
        return getIntFileDiffAttribute(fileDiffId, NUM_DRAFT_COMMENTS);
    }
    
    public Integer getLatestDiffRevisionId() {
        
        List<Integer> revisions = getDiffRevisions();
        
        if ( revisions.isEmpty() )
            return null;
        
        return revisions.get(revisions.size() - 1);
    }
    
    public List<Integer> getDiffRevisions() {
        
        List<Integer> revisions = new ArrayList<Integer>();
        for ( Map.Entry<String, TaskAttribute> entry : taskData.getRoot().getAttribute(DIFFS.toString()).getAttributes().entrySet() ) {
            
            String attributeId = entry.getKey();
            
            if ( !attributeId.startsWith(PREFIX_DIFF) )
                continue;
            
            int diffRevisionId = Integer.parseInt(entry.getValue().getValue());
            
            revisions.add(diffRevisionId);
            
        }
        
        Collections.sort(revisions);
        
        return revisions;
    }

    public int getNumberOfPublicComments(int diffRevisionId) {
        
        return Integer.parseInt(diff(diffRevisionId).getAttribute(NUM_PUBLIC_COMMENTS.toString()).getValue());
    }

    public int getNumberOfDraftComments(int diffRevisionId) {
        
        return Integer.parseInt(diff(diffRevisionId).getAttribute(NUM_DRAFT_COMMENTS.toString()).getValue());
    }
}
