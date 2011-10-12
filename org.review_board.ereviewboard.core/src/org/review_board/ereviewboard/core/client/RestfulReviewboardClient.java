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

import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_DIFFS;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_DIFF_COMMENTS;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_DRAFT;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_FILES;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_GROUPS;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_INFO;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_REPLIES;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_REPOSITORIES;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_REVIEWS;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_REVIEW_REQUESTS;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_SCREENSHOTS;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_SCREENSHOT_COMMENTS;
import static org.review_board.ereviewboard.core.client.ReviewboardQueryBuilder.PATH_USERS;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Diff;
import org.review_board.ereviewboard.core.model.DiffComment;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.Review;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewReply;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.ReviewRequestDraft;
import org.review_board.ereviewboard.core.model.ReviewRequestStatus;
import org.review_board.ereviewboard.core.model.Screenshot;
import org.review_board.ereviewboard.core.model.ScreenshotComment;
import org.review_board.ereviewboard.core.model.ServerInfo;
import org.review_board.ereviewboard.core.model.User;

/**
 * RESTful implementation of {@link ReviewboardClient}.
 * 
 * @author Markus Knittig
 */
public class RestfulReviewboardClient implements ReviewboardClient {
    
    private static final int PAGED_RESULT_INCREMENT = 50;

    private static String asBooleanParameter(boolean parameter) {
        
        return parameter ? "1" : "0";
    }
    
    private final RestfulReviewboardReader reviewboardReader;

    private ReviewboardClientData clientData;

    private ReviewboardHttpClient httpClient;

    public RestfulReviewboardClient(AbstractWebLocation location, ReviewboardClientData clientData,
            TaskRepository repository) {
        this.clientData = clientData;

        reviewboardReader = new RestfulReviewboardReader();

        httpClient = new ReviewboardHttpClient(location, repository.getCharacterEncoding());

        refreshRepositorySettings(repository);
    }

    public ReviewboardClientData getClientData() {
        return clientData;
    }

    public void refreshRepositorySettings(TaskRepository repository) {
        // Nothing to do yet
    }

    public List<Review> getReviews(final int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
        PagedLoader<Review> loader = new PagedLoader<Review>(PAGED_RESULT_INCREMENT, monitor, "Retrieving reviews") {
            @Override
            protected PagedResult<Review> doLoadInternal(int start, int maxResults, IProgressMonitor monitor)
                    throws ReviewboardException {
            	
            	ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId)
            	        .descend(PATH_REVIEWS).paginate(start, maxResults);
            	
                return reviewboardReader.readReviews(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
        };
        
        return loader.doLoad();
    }
    
    public List<ReviewReply> getReviewReplies(final int reviewRequestId, final int reviewId, IProgressMonitor monitor) throws ReviewboardException {
 
        PagedLoader<ReviewReply> loader = new PagedLoader<ReviewReply>(PAGED_RESULT_INCREMENT, monitor, "Retrieving review replies") {
            
            @Override
            protected PagedResult<ReviewReply> doLoadInternal(int start, int maxResults,
                    IProgressMonitor monitor) throws ReviewboardException {
                
                ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId)
                        .descend(PATH_REVIEWS, reviewId).descend(PATH_REPLIES).paginate(start, maxResults);

                return reviewboardReader.readReviewReplies(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
        };
        
        return loader.doLoad();
    }
    
    public int countDiffCommentsForReply(int reviewRequestId, int reviewId, int reviewReplyId, IProgressMonitor reviewDiffMonitor) throws ReviewboardException {

    	ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).descend(PATH_REVIEWS, reviewId).
    			descend(PATH_REPLIES, reviewReplyId).descend(PATH_DIFF_COMMENTS).countsOnly();
    	
        String result = httpClient.executeGet(queryBuilder.createQuery(), reviewDiffMonitor);
        
        return reviewboardReader.readCount(result);
    }
    
    public int countScreenshotCommentsForReply(int reviewRequestId, int reviewId, int reviewReplyId, IProgressMonitor reviewDiffMonitor) throws ReviewboardException {
        
    	ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).descend(PATH_REVIEWS, reviewId).
                descend(PATH_REPLIES, reviewReplyId).descend(PATH_SCREENSHOT_COMMENTS).countsOnly();
        
        String result = httpClient.executeGet(queryBuilder.createQuery(), reviewDiffMonitor);
        
        return reviewboardReader.readCount(result);
    }

    public List<DiffComment> readDiffComments(final int reviewRequestId, final int diffId, final int fileDiffId, IProgressMonitor monitor) throws ReviewboardException {
        
        PagedLoader<DiffComment> loader = new PagedLoader<DiffComment>(PAGED_RESULT_INCREMENT, monitor, "Retrieving diff comments") {
            @Override
            protected PagedResult<DiffComment> doLoadInternal(int start, int maxResults, IProgressMonitor monitor)
                    throws ReviewboardException {
            	
                ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).
                		descend(PATH_DIFFS, diffId).descend(PATH_FILES, fileDiffId).
                		descend(PATH_DIFF_COMMENTS).paginate(start, maxResults);
            	
                return reviewboardReader.readDiffComments(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
        };
        
        return loader.doLoad();
    }
    
    public int countDiffComments(int reviewRequestId, int reviewId, IProgressMonitor monitor) throws ReviewboardException {
    	
        ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).
                descend(PATH_REVIEWS, reviewId).descend(PATH_DIFF_COMMENTS).countsOnly();
        
        return reviewboardReader.readCount(httpClient.executeGet(queryBuilder.createQuery(), monitor));
    }

    private List<Repository> getRepositories(IProgressMonitor monitor) throws ReviewboardException {
        
        PagedLoader<Repository> loader = new PagedLoader<Repository>(PAGED_RESULT_INCREMENT, monitor, "Retrieving repositories") {
            
            @Override
            protected PagedResult<Repository> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
                
            	ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REPOSITORIES).paginate(start, maxResults);

                return reviewboardReader.readRepositories(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
        };
        
        return loader.doLoad();
    }

    private List<User> getUsers(IProgressMonitor monitor) throws ReviewboardException {
    
        PagedLoader<User> loader = new PagedLoader<User>(PAGED_RESULT_INCREMENT, monitor, "Retrieving users") {

            @Override
            protected PagedResult<User> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
                
            	ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_USERS).paginate(start, maxResults);
                
                return reviewboardReader.readUsers(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
            
        };
        
        return loader.doLoad();
    }
    
    private List<ReviewGroup> getReviewGroups(IProgressMonitor monitor) throws ReviewboardException {
        
        PagedLoader<ReviewGroup> loader = new PagedLoader<ReviewGroup>(PAGED_RESULT_INCREMENT, monitor, "Retrieving review groups") {
            
            @Override
            protected PagedResult<ReviewGroup> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
                

            	ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_GROUPS).paginate(start, maxResults);
            	
                return reviewboardReader.readGroups(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
        };
        
        return loader.doLoad();
    }
    
    private TimeZone getTimeZone(IProgressMonitor monitor) throws ReviewboardException {
        
        monitor.beginTask("Retrieving server information", 1);
        
        try {
            return reviewboardReader.readServerInfo(httpClient.executeGet(new ReviewboardQueryBuilder().descend(PATH_INFO).createQuery(), monitor)).getTimeZone();
        } finally {
            monitor.done();
        }
    }

    public List<ReviewRequest> getReviewRequests(final String query, int queryMaxResults, IProgressMonitor monitor) throws ReviewboardException {

        
        PagedLoader<ReviewRequest> loader = new PagedLoader<ReviewRequest>(PAGED_RESULT_INCREMENT, monitor, "Loading review requests") {
            
            @Override
            protected PagedResult<ReviewRequest> doLoadInternal(int start, int maxResults,
                    IProgressMonitor monitor) throws ReviewboardException {
                
                QueryBuilder queryBuilder = QueryBuilder.fromString(query).setParameter("start", start).setParameter("max-results", maxResults);
                ReviewboardQueryBuilder rQueryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, queryBuilder.createQuery());

                return reviewboardReader.readReviewRequests(httpClient.executeGet(rQueryBuilder.createQuery(), monitor));
            }
        };
        loader.setLimit(queryMaxResults);
        
        return loader.doLoad();
    }
    
    public List<Diff> loadDiffs(final int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
        PagedLoader<Diff> loader = new PagedLoader<Diff>(PAGED_RESULT_INCREMENT, monitor, "Loading diffs") {
            
            @Override
            protected PagedResult<Diff> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
                

                ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).descend(PATH_DIFFS).paginate(start, maxResults);
                
                return reviewboardReader.readDiffs(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
        };
        
        return loader.doLoad();
    }
    
    public List<FileDiff> getFileDiffs(final int reviewRequestId, final int latestDiff, IProgressMonitor monitor) throws ReviewboardException {
        
        PagedLoader<FileDiff> loader = new PagedLoader<FileDiff>(PAGED_RESULT_INCREMENT, monitor, "Loading diffs") {
            
            @Override
            protected PagedResult<FileDiff> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
                

                ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).
                        descend(PATH_DIFFS, latestDiff).descend(PATH_FILES).paginate(start, maxResults);
                
                return reviewboardReader.readFileDiffs(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
        };
        
        return loader.doLoad();
    }

    public List<Screenshot> loadScreenshots(final int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
        PagedLoader<Screenshot> loader = new PagedLoader<Screenshot>(PAGED_RESULT_INCREMENT, monitor, "Loading diffs") {
            
            @Override
            protected PagedResult<Screenshot> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
                

                ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).
                        descend(PATH_SCREENSHOTS).paginate(start, maxResults);
                
                return reviewboardReader.readScreenshots(httpClient.executeGet(queryBuilder.createQuery(), monitor));
            }
        };
        
        return loader.doLoad();
    }
    
    public List<ScreenshotComment> getScreenshotComments(final int reviewRequestId, final int screenshotId, final IProgressMonitor screenshotCommentMonitor) throws ReviewboardException {
        
        PagedLoader<ScreenshotComment> loader = new PagedLoader<ScreenshotComment>(PAGED_RESULT_INCREMENT, screenshotCommentMonitor, "Retrieving screenshot comments") {
            
            @Override
            protected PagedResult<ScreenshotComment> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
                
                ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).
                        descend(PATH_SCREENSHOTS, screenshotId).descend(PATH_SCREENSHOT_COMMENTS).paginate(start, maxResults);
                
                return reviewboardReader.readScreenshotComments(httpClient.executeGet(queryBuilder.createQuery(), screenshotCommentMonitor));
            }
        };
        
        return loader.doLoad();
    }
    
    public boolean hasRepositoryData() {
        
        return (clientData.lastupdate != 0);
    }

    public void updateRepositoryData(boolean force, IProgressMonitor monitor) throws ReviewboardException {
        
        if (hasRepositoryData() && !force)
            return;
        
        monitor.beginTask("Refreshing repository data", 100);

        try {
            
            // users usually outnumber groups and repositories
            // try to get good progress reporting by approximating the ratios
            // repositories with small data sets will not need very accurate progress reporting anyway
            clientData.setUsers(getUsers(Policy.subMonitorFor(monitor, 90)));
            
            clientData.setGroups(getReviewGroups(Policy.subMonitorFor(monitor, 5)));

            clientData.setRepositories(getRepositories(Policy.subMonitorFor(monitor, 4)));
            
            clientData.setTimeZone(getTimeZone(Policy.subMonitorFor(monitor, 1)));

            clientData.lastupdate = new Date().getTime();
        } finally  {
            monitor.done();
        }
    }

    public byte[] getRawDiff(int reviewRequestId, int diffRevision, IProgressMonitor monitor) throws ReviewboardException {
        
        ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId)
                .descend(PATH_DIFFS, diffRevision);
        
        return httpClient.executeGetForBytes(queryBuilder.createQuery(),"text/x-patch", monitor);
    }
    
    public byte[] getRawFileDiff(int reviewRequestId, int diffRevision, int fileId, IProgressMonitor monitor) throws ReviewboardException {
        
        ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId)
                .descend(PATH_DIFFS, diffRevision).descend(PATH_FILES, fileId);

        return httpClient.executeGetForBytes(queryBuilder.createQuery(),"text/x-patch", monitor);
    }
    
    public byte[] getScreenshot(int reviewRequestId, int screenshotId, IProgressMonitor monitor) throws ReviewboardException {
        
        monitor.beginTask("Getting screenshot content", 2);
        
        try {
            ReviewboardQueryBuilder builder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).
                    descend(PATH_SCREENSHOTS, screenshotId);
            
            Screenshot screenshot = reviewboardReader.readScreenshot(httpClient.executeGet(builder.createQuery(), monitor));
            
            monitor.worked(1);
            
            return httpClient.executeGetForBytes(screenshot.getUrl(), "image/*", monitor);
        } finally {
            monitor.done();
        }
    }

    public IStatus validate(String username, String password, IProgressMonitor monitor) {

        try {
            
            if ( !httpClient.apiEntryPointExist(monitor) )
                return new Status(IStatus.ERROR, ReviewboardCorePlugin.PLUGIN_ID, "Repository not found. Please make sure that the path to the repository correct and the  server version is at least 1.5");
            
            Policy.advance(monitor, 1);
            
            httpClient.login(username, password, monitor);
            
            Policy.advance(monitor, 1);
            
            ServerInfo serverInfo = reviewboardReader.readServerInfo(httpClient.executeGet("/api/info/", monitor));
            
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
    
    public List<ReviewRequest> getReviewRequestsChangedSince(final Date timestamp, IProgressMonitor monitor) throws ReviewboardException {
        
            if ( timestamp == null )
                throw new IllegalArgumentException("Timestamp may not be null");
            
            PagedLoader<ReviewRequest> loader = new PagedLoader<ReviewRequest>(PAGED_RESULT_INCREMENT, monitor, "Getting review requests changed since " + timestamp) {
                
                @Override
                protected PagedResult<ReviewRequest> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
        
                    ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS).
                            setParameter("status", ReviewRequestStatus.ALL.asSubmittableValue()).
                            setParameter("last-updated-from", timestamp).
                            paginate(start, maxResults);
                    
                    return reviewboardReader.readReviewRequests(httpClient.executeGet(queryBuilder.createQuery(), monitor));
                }
            };
            
            return loader.doLoad();
    }

    public ReviewRequest getReviewRequest(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
        ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId);

        return reviewboardReader.readReviewRequest(httpClient.executeGet(queryBuilder.createQuery(), monitor));
    }

    public void updateStatus(int reviewRequestId, ReviewRequestStatus status, IProgressMonitor monitor) throws ReviewboardException {

        if ( status == ReviewRequestStatus.ALL || status == ReviewRequestStatus.NONE  || status == null)
            throw new ReviewboardException("Invalid status to update to : " + status );
        
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("status", status.asSubmittableValue());
        
        ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId);
        
        String result = httpClient.executePut(queryBuilder.createQuery(), parameters, monitor);
        
        reviewboardReader.ensureSuccess(result);
    }
    
    public ReviewRequest createReviewRequest(Repository repository, IProgressMonitor monitor) throws ReviewboardException {
        
        ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS);
        
        String result = httpClient.executePost(queryBuilder.createQuery(), Collections.singletonMap("repository", String.valueOf(repository.getId())),  monitor);
        
        return reviewboardReader.readReviewRequest(result);
    }
    
    public Diff createDiff(int reviewRequestId, String baseDir, byte[] diffContent, IProgressMonitor monitor) throws ReviewboardException {

            ReviewboardHttpClient.UploadItem uploadItem = new ReviewboardHttpClient.UploadItem(
                    "path", "main.diff", diffContent);
            
            Map<String, String> parameters = new HashMap<String, String>(1);
            if ( baseDir != null )
                parameters.put("basedir", baseDir);
            
            ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).
                    descend(PATH_DIFFS);

            String result = httpClient.executePost(queryBuilder.createQuery(), parameters, Collections.singletonList(uploadItem), monitor);

            return reviewboardReader.readDiff(result);
    }

    public ReviewRequestDraft updateReviewRequest(ReviewRequest reviewRequest, boolean publish, String changeDescription, IProgressMonitor monitor) throws ReviewboardException {
        
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("branch", reviewRequest.getBranch());
        parameters.put("bugs_closed", reviewRequest.getBugsClosedText());
        if ( changeDescription != null )
            parameters.put("changedescription", changeDescription);
        parameters.put("description", reviewRequest.getDescription());
        parameters.put("public", asBooleanParameter(publish));
        parameters.put("summary", reviewRequest.getSummary());
        parameters.put("target_groups", reviewRequest.getTargetGroupsText());
        parameters.put("target_people", reviewRequest.getTargetPeopleText());
        parameters.put("testing_done", reviewRequest.getTestingDone());
        
        ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequest.getId()).
                descend(PATH_DRAFT);
        
        String result = httpClient.executePut(queryBuilder.createQuery(), parameters, monitor);
        
        return reviewboardReader.readReviewRequestDraft(result);

    }

    public Review createReview(int reviewRequestId, Review review, IProgressMonitor monitor) throws ReviewboardException {
        
        Map<String, String> parameters = new HashMap<String, String>();
        if ( review.getBodyTop() != null )
            parameters.put("body_top", review.getBodyTop());
        if ( review.getBodyBottom() != null )
            parameters.put("body_bottom", review.getBodyBottom());
        parameters.put("public", asBooleanParameter(review.isPublicReview()));
        parameters.put("ship_it", asBooleanParameter(review.getShipIt()));
        
        ReviewboardQueryBuilder queryBuilder = new ReviewboardQueryBuilder().descend(PATH_REVIEW_REQUESTS, reviewRequestId).
                descend(PATH_REVIEWS);
        
        String result = httpClient.executePost(queryBuilder.createQuery(), parameters, monitor);
        
        return reviewboardReader.readReview(result);
    }
}
