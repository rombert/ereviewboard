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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Diff;
import org.review_board.ereviewboard.core.model.DiffComment;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.Review;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.Screenshot;
import org.review_board.ereviewboard.core.model.ServerInfo;
import org.review_board.ereviewboard.core.model.User;

/**
 * RESTful implementation of {@link ReviewboardClient}.
 * 
 * @author Markus Knittig
 */
public class RestfulReviewboardClient implements ReviewboardClient {
    
    private final RestfulReviewboardReader reviewboardReader;

    private ReviewboardClientData clientData;

    private ReviewboardHttpClient httpClient;

    public RestfulReviewboardClient(AbstractWebLocation location, ReviewboardClientData clientData,
            TaskRepository repository) {
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


    public List<Review> getReviews(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
        return reviewboardReader.readReviews(httpClient.executeGet("/api/review-requests/" + reviewRequestId + "/reviews", monitor));
    }

    public List<DiffComment> readDiffComments(int reviewRequestId, int reviewId, IProgressMonitor monitor) throws ReviewboardException {
        
        return reviewboardReader.readDiffComments(httpClient.executeGet("/api/review-requests/" + reviewRequestId+"/reviews/" + reviewId +"/diff-comments", monitor));
    }
    

    private List<Repository> getRepositories(IProgressMonitor monitor) throws ReviewboardException {
        
        monitor.beginTask("Retrieving repositories", IProgressMonitor.UNKNOWN);

        try {
            return reviewboardReader.readRepositories(httpClient.executeGet("/api/repositories/",
                    monitor));
        } finally {
            monitor.done();
        }
    }

    private List<User> getUsers(IProgressMonitor monitor) throws ReviewboardException {
        
        List<User> allResults = null;
        
        int increment = 50;
        int currentPage = 0;

        while ( true ) {
            // we perform the monitor work ourselves, so pass a NPM downstream
            PagedResult<User> pagedUsers = readUsersPaged(new NullProgressMonitor(), currentPage * increment, increment);
            if ( allResults == null ) {
                allResults = new ArrayList<User>(pagedUsers.getTotalResults());
                monitor.beginTask("Retrieving users", pagedUsers.getTotalResults());
            }
            
            Policy.advance(monitor, pagedUsers.getResults().size());
            
            allResults.addAll(pagedUsers.getResults());
            currentPage++;
            
            if ( allResults.size() == pagedUsers.getTotalResults())
                break;
        }
        
        monitor.done();
        
        return allResults;
    }

    private PagedResult<User> readUsersPaged(IProgressMonitor monitor, int start, int maxResults)
            throws ReviewboardException {
        
        StringBuilder query = new StringBuilder();
        
        query.append("/api/users?start=").append(start).append("&max-results="+maxResults);
        
        return reviewboardReader.readUsers(httpClient.executeGet(query.toString(), monitor));
    }

    private List<ReviewGroup> getReviewGroups(IProgressMonitor monitor) throws ReviewboardException {
        
        monitor.beginTask("Retrieving review groups", 1);
        
        try {
            return reviewboardReader.readGroups(httpClient.executeGet(
                    "/api/groups/", monitor));
        } finally {
            monitor.done();
        }
    }

    public List<ReviewRequest> getReviewRequests(String query, IProgressMonitor monitor)
            throws ReviewboardException {
        
        return reviewboardReader.readReviewRequests( httpClient.executeGet("/api/review-requests/" + query, monitor));
    }
    
    public List<Diff> loadDiffs(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
        return reviewboardReader.readDiffs(httpClient.executeGet("/api/review-requests/" + reviewRequestId+"/diffs", monitor));
    }

    public List<Screenshot> loadScreenshots(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        
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
        
        if (hasRepositoryData() && !force)
            return;
        
        monitor.beginTask("Refreshing repository data", 100);

        try {
            
            // users usually outnumber groups and repositories
            // try to get good progress reporting by approximating the ratios
            // repositories with small data sets will not need very accurate progress reporting anyway
            clientData.setUsers(getUsers(Policy.subMonitorFor(monitor, 90)));
            
            clientData.setGroups(getReviewGroups(Policy.subMonitorFor(monitor, 5)));

            clientData.setRepositories(getRepositories(Policy.subMonitorFor(monitor, 5)));

            clientData.lastupdate = new Date().getTime();
        } finally  {
            monitor.done();
        }
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
            
            if ( timestamp == null )
                throw new IllegalArgumentException("Timestamp may not be null");
            
            String query = "?status=all&max-results=10000&last-updated-from=" + URLEncoder.encode( ReviewboardAttributeMapper.newIso86011DateFormat().format(timestamp), "UTF-8");
            return getReviewRequestIds( query, monitor);
            
        } catch (UnsupportedEncodingException e) {
            throw new ReviewboardException("Failed encoding the query url", e);
        }
        
    }

    public ReviewRequest getReviewRequest(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {

        return reviewboardReader.readReviewRequest(httpClient.executeGet("/api/review-requests/" + reviewRequestId + "/", monitor));
    }
}
