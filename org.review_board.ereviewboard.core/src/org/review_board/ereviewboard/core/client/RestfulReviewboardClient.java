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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.*;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper.Attribute;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardTaskMapper;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.*;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * RESTful implementation of {@link ReviewboardClient}.
 * 
 * @author Markus Knittig
 */
public class RestfulReviewboardClient implements ReviewboardClient {
    
    private final AbstractWebLocation location;

    private final RestfulReviewboardReader reviewboardReader;

    private ReviewboardClientData clientData;

    private ReviewboardHttpClient httpClient;

    public RestfulReviewboardClient(AbstractWebLocation location, ReviewboardClientData clientData,
            TaskRepository repository) {
        this.location = location;
        this.clientData = clientData;

        reviewboardReader = new RestfulReviewboardReader();

        httpClient = new ReviewboardHttpClient(location, repository.getCharacterEncoding(),
                Boolean.valueOf(repository.getProperty("selfSignedSSL")));

        refreshRepositorySettings(repository);
    }

    public ReviewboardClientData getClientData() {
        return clientData;
    }

    public void refreshRepositorySettings(TaskRepository repository) {
        // Nothing to do yet
    }

    public TaskData getTaskData(TaskRepository taskRepository, final String taskId,
            IProgressMonitor monitor) throws ReviewboardException {

        long start = System.currentTimeMillis();
        
        monitor.beginTask("", 4);

        try {
            
            ReviewRequest reviewRequest = reviewboardReader.readReviewRequest(httpClient.executeGet(
                    "/api/review-requests/" + Integer.parseInt(taskId) + "/", monitor));
            
            Policy.advance(monitor, 1);
            
            TaskData taskData = getTaskDataForReviewRequest(taskRepository, reviewRequest, false);
            
            List<Diff> diffs = loadDiffs(Integer.parseInt(taskData.getTaskId()), monitor);
            
            Policy.advance(monitor, 1);
            
            loadReviewsAndDiffsAsComment(taskData, diffs, monitor);
            
            loadDiffsAndScreenshotsAsAttachments(taskData, taskRepository, diffs, monitor);

            return taskData;
        } finally {
            
            double elapsed = ( System.currentTimeMillis() - start) / 1000.0;
            
            System.out.println("Review request with id  " + taskId + " synchronized in " + NumberFormat.getNumberInstance().format(elapsed) + " seconds.");
            
            monitor.done();
        }
    }

    /**
     * Advances monitor by one
     * 
     */
    private void loadReviewsAndDiffsAsComment(TaskData taskData, List<Diff> diffs, IProgressMonitor monitor)
            throws  ReviewboardException {

        SortedMap<Date, Comment2> sortedComments = new TreeMap<Date, Comment2>();

        for (Diff diff : diffs ) {

            Comment2 comment = new Comment2();
            comment.setAuthor(taskData.getAttributeMapper().getTaskRepository().createPerson(
                    taskData.getRoot().getAttribute(Attribute.SUBMITTER.toString()).getValue()));
            comment.setText(Diff.DIFF_REVISION_PREFIX + diff.getRevision());

            sortedComments.put(diff.getTimestamp(), comment);
        }
        
        List<Review> reviews = reviewboardReader.readReviews(httpClient.executeGet("/api/review-requests/" + taskData.getTaskId() + "/reviews", monitor));

        Policy.advance(monitor, 1);

        int shipItCount = 0;
        
        for ( Review review : reviews ) {

            int reviewId = review.getId();
            int totalResults = readDiffComments(Integer.parseInt(taskData.getTaskId()), reviewId, monitor).size();

            StringBuilder text = new StringBuilder();
            boolean shipit = review.getShipIt();
            boolean appendWhiteSpace = false;
            if ( shipit ) {
                text.append("Ship it!");
                shipItCount++;
                appendWhiteSpace = true;
            }
            if ( review.getBodyTop().length() != 0  ) {
                if ( appendWhiteSpace )
                    text.append("\n\n");
                
                text.append(review.getBodyTop());
                appendWhiteSpace = true;
            }
            if ( totalResults != 0 ) {
                if ( appendWhiteSpace )
                    text.append("\n\n");
                
                text.append(totalResults).append(" inline comments.");
                
                appendWhiteSpace = true;
            }
            if ( review.getBodyBottom().length() != 0  ) {
                if ( appendWhiteSpace )
                    text.append("\n\n");

                text.append(review.getBodyBottom());
            }
            
            Comment2 comment = new Comment2();
            comment.setAuthor(taskData.getAttributeMapper().getTaskRepository().createPerson(review.getUser()));
            comment.setText(text.toString());

            sortedComments.put(review.getTimestamp(), comment);

        }
        
        TaskAttribute shipItAttribute = taskData.getRoot().createAttribute(Attribute.SHIP_IT.toString());
        shipItAttribute.setValue(String.valueOf(shipItCount));
        shipItAttribute.getMetaData().setLabel(Attribute.SHIP_IT.getDisplayName()).setType(Attribute.SHIP_IT.getAttributeType());
        shipItAttribute.getMetaData().setReadOnly(true).setKind(Attribute.SHIP_IT.getAttributeKind());

        int commentIndex = 1;
        
        for ( Map.Entry<Date, Comment2>  entry : sortedComments.entrySet() )
            entry.getValue().applyTo(taskData, commentIndex++, entry.getKey());
    }

    private List<DiffComment> readDiffComments(int reviewRequestId, int reviewId, IProgressMonitor monitor) throws ReviewboardException {
        
        return reviewboardReader.readDiffComments(httpClient.executeGet("/api/review-requests/" + reviewRequestId+"/reviews/" + reviewId +"/diff-comments", monitor));
    }
    
    /**
     * Advances monitor by one
     */
    private void loadDiffsAndScreenshotsAsAttachments(TaskData taskData, TaskRepository taskRepository, List<Diff> diffs, IProgressMonitor monitor) throws ReviewboardException {
        
        List<Screenshot> screenshots = loadScreenshots(Integer.parseInt(taskData.getTaskId()), monitor);
        
        Policy.advance(monitor, 1);
        
        if ( diffs.isEmpty() && screenshots.isEmpty() )
            return;
        
        int mostRecentRevision = diffs.size();

        for (Diff diff : diffs) {
            TaskAttribute attribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_ATTACHMENT + diff.getRevision());
            TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attribute);
            mapper.setFileName(diff.getName());
            mapper.setDescription(diff.getName());
            mapper.setAuthor(taskRepository.createPerson(taskData.getRoot().getAttribute(ReviewboardAttributeMapper.Attribute.SUBMITTER.toString()).getValue()));
            mapper.setCreationDate(diff.getTimestamp());
            mapper.setAttachmentId(Integer.toString(diff.getId()));
            mapper.setPatch(Boolean.TRUE);
            mapper.setDeprecated(diff.getRevision() != mostRecentRevision);
            mapper.setLength(ReviewboardAttachmentHandler.ATTACHMENT_SIZE_UNKNOWN);
            mapper.applyTo(attribute);
            
            attribute.createAttribute(ReviewboardAttachmentHandler.ATTACHMENT_ATTRIBUTE_REVISION).setValue(String.valueOf(diff.getRevision()));
        }
        
        int attachmentIndex = mostRecentRevision;
        
        for ( Screenshot screenshot : screenshots ) {
  
            TaskAttribute attribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_ATTACHMENT + ++ attachmentIndex);
            TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attribute);
            mapper.setFileName(screenshot.getFileName());
            mapper.setDescription(screenshot.getCaption());
            mapper.setAttachmentId(Integer.toString(screenshot.getId()));
            mapper.setLength(ReviewboardAttachmentHandler.ATTACHMENT_SIZE_UNKNOWN);
            mapper.setContentType(screenshot.getContentType());
            mapper.setUrl(stripPathFromLocation() + screenshot.getUrl());
            mapper.applyTo(attribute);
        }
        
    }

    private String stripPathFromLocation() throws ReviewboardException {
        
        try {
            URI uri = new URI(location.getUrl());
            uri.getPath();
            return location.getUrl().substring(0, location.getUrl().length() - uri.getPath().length());
        } catch (URISyntaxException e) {
            throw new ReviewboardException("Unable to retrive host from the location", e);
        }
    }

    public List<Repository> getRepositories(IProgressMonitor monitor) throws ReviewboardException {
        return reviewboardReader.readRepositories(httpClient.executeGet("/api/repositories/", monitor));
    }

    public List<User> getUsers(IProgressMonitor monitor) throws ReviewboardException {
        return reviewboardReader.readUsers(httpClient.executeGet("/api/users/", monitor));
    }

    public List<ReviewGroup> getReviewGroups(IProgressMonitor monitor) throws ReviewboardException {
        return reviewboardReader.readGroups(httpClient.executeGet("/api/groups/", monitor));
    }

    public List<ReviewRequest> getReviewRequests(String query, IProgressMonitor monitor)
            throws ReviewboardException {
        
        return reviewboardReader.readReviewRequests( httpClient.executeGet("/api/review-requests/" + query, monitor));
    }
    
    private List<Diff> loadDiffs(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
        return reviewboardReader.readDiffs(httpClient.executeGet("/api/review-requests/" + reviewRequestId+"/diffs", monitor));
    }

    private List<Screenshot> loadScreenshots(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
        return reviewboardReader.readScreenshots(httpClient.executeGet("/api/review-requests/" + reviewRequestId+"/screenshots", monitor));
    }
    
    private List<Integer> getReviewRequestIds(String query, IProgressMonitor monitor)
            throws ReviewboardException {
        return reviewboardReader.readReviewRequestIds(
                httpClient.executeGet("/api/review-requests/" + query, monitor));
    }

    public boolean hasRepositoryData() {
        return (clientData.lastupdate != 0);
    }

    public void updateRepositoryData(boolean force, IProgressMonitor monitor) throws ReviewboardException {
        if (hasRepositoryData() && !force) {
            return;
        }
        
        monitor.beginTask("Refreshing repository data", 3);

        try {
            clientData.setGroups(getReviewGroups(monitor));
            Policy.advance(monitor, 1);

            clientData.setUsers(getUsers(monitor));
            Policy.advance(monitor, 1);

            clientData.setRepositories(getRepositories(monitor));
            Policy.advance(monitor, 1);
            
            clientData.lastupdate = new Date().getTime();
        } finally  {
            monitor.done();
        }
    }

    public void performQuery(TaskRepository repository, IRepositoryQuery query,
            TaskDataCollector collector, IProgressMonitor monitor) throws CoreException {
        try {
            
            List<ReviewRequest> reviewRequests = getReviewRequests(query.getUrl(), monitor);
            
            for (ReviewRequest reviewRequest : reviewRequests) {
                TaskData taskData = getTaskDataForReviewRequest(repository, reviewRequest, true);
                collector.accept(taskData);
            }
        } catch (ReviewboardException e) {
            
            // Mylyn does not log the error cause, just decorates the query in the task list
            Status status = new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, "Failed performing query : " + e.getMessage(), e);
            ReviewboardCorePlugin.getDefault().getLog().log(status);
            throw new CoreException(status);
        }
    }

    private TaskData getTaskDataForReviewRequest(TaskRepository taskRepository,
            ReviewRequest reviewRequest, boolean partial) {

        String id = String.valueOf(reviewRequest.getId());
        Date dateModified = reviewRequest.getLastUpdated();
        ReviewRequestStatus status = reviewRequest.getStatus();

        TaskData taskData = new TaskData(new ReviewboardAttributeMapper(taskRepository),
                ReviewboardCorePlugin.REPOSITORY_KIND, location.getUrl(), id);
        taskData.setPartial(partial);

        ReviewboardTaskMapper mapper = new ReviewboardTaskMapper(taskData, true);
        // mapped fields
        mapper.setTaskKey(id);
        mapper.setReporter(reviewRequest.getSubmitter());
        mapper.setCreationDate(reviewRequest.getTimeAdded());
        mapper.setSummary(reviewRequest.getSummary());
        mapper.setDescription(reviewRequest.getDescription());
        mapper.setTaskUrl(ReviewboardUtil.getReviewRequestUrl(taskRepository.getUrl(), id));
        mapper.setStatus(status.name().toLowerCase());
        if ( status != ReviewRequestStatus.PENDING )
            mapper.setCompletionDate(dateModified);

        
        if ( reviewRequest.getRepository() != null )
            mapper.setRepository(reviewRequest.getRepository());
        mapper.setBranch(reviewRequest.getBranch());
        mapper.setChangeNum(reviewRequest.getChangeNumber());
        mapper.setPublic(reviewRequest.isPublic());
        mapper.setBugsClosed(reviewRequest.getBugsClosed());
        mapper.setTestingDone(reviewRequest.getTestingDone());
        mapper.setTargetPeople(reviewRequest.getTargetPeople());
        mapper.setTargetGroups(reviewRequest.getTargetGroups());
        
        if ( !partial) {
            // on purpose not set for partial tasks
            mapper.setModificationDate(dateModified);
        }
        
        mapper.complete();
        
        return taskData;
    }

    public byte[] getRawDiff(int reviewRequestId, int diffRevision, IProgressMonitor monitor) throws ReviewboardException {
        
        return httpClient.executeGetForBytes("/api/review-requests/" + reviewRequestId + "/diffs/" + diffRevision +"/","text/x-patch", monitor);
    }
    
    public byte[] getScreenshot(String url, IProgressMonitor monitor) throws ReviewboardException {
        return httpClient.executeGetForBytes("/" + url, "image/*", monitor);
    }

    public IStatus validate(String username, String password, IProgressMonitor monitor) {

        try {
            
            if ( !httpClient.apiEntryPointExist(monitor) )
                return new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, "Repository not found. Please make sure that the path to the repository correct and the  server version is at least 1.5");
            
            Policy.advance(monitor, 1);
            
            httpClient.login(username, password, monitor);
            
            Policy.advance(monitor, 1);
            
            ServerInfo serverInfo = reviewboardReader.readServerInfo(httpClient.executeGet("/api/info", monitor));
            
            Policy.advance(monitor, 1);
            
            if ( !serverInfo.isAtLeast(1, 5) )
                return new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, "The version " + serverInfo.getProductVersion() + " is not supported. Please use a repository version of 1.5 or newer.");
            
            return Status.OK_STATUS;
        } catch ( ReviewboardException e ) {
            return new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, e.getMessage(), e);
        } catch (Exception e) {
            return new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, "Unexpected exception : " + e.getMessage(), e);
        } finally {
            monitor.done();
        }
    }
    
    public List<Integer> getReviewsIdsChangedSince(Date timestamp, IProgressMonitor monitor) throws ReviewboardException {
        
        try {
            // TODO: extract into a ReviewRequestQuery
            Assert.isNotNull(timestamp);
            
            String query = "?last-updated-from=" + URLEncoder.encode( ReviewboardAttributeMapper.newIso86011DateFormat().format(timestamp), "UTF-8");
            return getReviewRequestIds( query, monitor);
            
        } catch (UnsupportedEncodingException e) {
            throw new ReviewboardException("Failed encoding the query url", e);
        }
        
    }

    private static class Comment2 {

        private IRepositoryPerson author;
        private String text;

        public void setAuthor(IRepositoryPerson author) {
            this.author = author;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void applyTo(TaskData taskData, int index, Date creationDate) {

            TaskAttribute attribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + index);

            TaskCommentMapper mapper = new TaskCommentMapper();
            mapper.setCommentId(String.valueOf(index));
            mapper.setCreationDate(creationDate);
            mapper.setAuthor(author);
            mapper.setText(text);
            mapper.setNumber(index);

            mapper.applyTo(attribute);
        }
    }
}
