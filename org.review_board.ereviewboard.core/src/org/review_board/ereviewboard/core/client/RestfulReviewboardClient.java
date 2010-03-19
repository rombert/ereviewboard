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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardTaskMapper;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Comment;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.Review;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.User;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class RestfulReviewboardClient implements ReviewboardClient {

    private final HttpClient httpClient;

    private final RestfulReviewboardReader reviewboardReader;

    private AbstractWebLocation location;

    private ReviewboardClientData clientData;

    private String cookie = "";

    private String characterEncoding;

    public RestfulReviewboardClient(AbstractWebLocation location, ReviewboardClientData clientData,
            TaskRepository repository) {
        this.location = location;
        this.clientData = clientData;

        reviewboardReader = new RestfulReviewboardReader();
        characterEncoding = repository.getCharacterEncoding();

        httpClient = createAndInitHttpClient(repository);

        refreshRepositorySettings(repository);
    }

    private HttpClient createAndInitHttpClient(TaskRepository repository) {
        if (Boolean.valueOf(repository.getProperty("selfSignedSSL"))) {
            Protocol.registerProtocol("https",
                    new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
        }
        HttpClient httpClient = new HttpClient();
        WebUtil.configureHttpClient(httpClient, "Mylyn");
        httpClient.getParams().setContentCharset(characterEncoding);
        return httpClient;
    }

    public ReviewboardClientData getClientData() {
        return clientData;
    }


    public void refreshRepositorySettings(TaskRepository repository) {
        // Nothing to do yet
    }

    public TaskData getTaskData(TaskRepository taskRepository, final String taskId,
            IProgressMonitor monitor) {
        // TODO Get review request

        TaskData taskData = new TaskData(new TaskAttributeMapper(taskRepository),
                ReviewboardCorePlugin.REPOSITORY_KIND, location.getUrl(), taskId);

        return taskData;
    }

    public void login(String username, String password, IProgressMonitor monitor) throws ReviewboardException {
        PostMethod loginRequest = new PostMethod(location.getUrl() + "/api/json/accounts/login/");

        loginRequest.setParameter("username", username);
        loginRequest.setParameter("password", password);

        if (StringUtils.isNotBlank(executeMethod(loginRequest, monitor))) {
            if (reviewboardReader.isStatOK(getResponseBodyAsString(loginRequest, monitor))) {
                cookie = loginRequest.getResponseHeader("Set-Cookie").getValue();
            } else {
                //TODO Use a custom exception for error handling
                throw new RuntimeException(reviewboardReader.getErrorMessage(
                        getResponseBodyAsString(loginRequest, monitor)));
            }
        } else {
            //TODO Use a custom exception for error handling
            throw new RuntimeException("Review Board site is not up!");
        }

    }

    private String getResponseBodyAsString(HttpMethodBase request, IProgressMonitor monitor) {

        monitor = Policy.monitorFor(monitor);
        monitor.beginTask("Get request body for" + request.getName() + " request", IProgressMonitor.UNKNOWN);
        try {
            return IOUtils.toString(WebUtil.getResponseBodyAsStream(request, monitor));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            monitor.done();
        }
    }

    private String getCookie(IProgressMonitor monitor) throws ReviewboardException {
        if (cookie.equals("")) {
            AuthenticationCredentials credentials =
                location.getCredentials(AuthenticationType.REPOSITORY);
            login(credentials.getUserName(), credentials.getPassword(), monitor);
        }
        return cookie;
    }

    private String stripSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.lastIndexOf("/"));
        }
        return url;
    }

    private String executeGet(String url, IProgressMonitor monitor) throws ReviewboardException {
        GetMethod getRequest = new GetMethod(stripSlash(location.getUrl()) + url);
        getRequest.getParams().setParameter("Set-Cookie", getCookie(monitor));

        return executeMethod(getRequest, monitor);
    }

    private String executePost(String url, IProgressMonitor monitor)  throws ReviewboardException {
        return executePost(url, new HashMap<String, String>(), monitor);
    }

    private String executePost(String url, Map<String, String> parameters,
            IProgressMonitor monitor)  throws ReviewboardException {
        PostMethod postRequest = new PostMethod(stripSlash(location.getUrl()) + url);
        postRequest.getParams().setParameter("Set-Cookie", getCookie(monitor));

        for (String key : parameters.keySet()) {
            postRequest.setParameter(key, parameters.get(key));
        }

        return executeMethod(postRequest, monitor);
    }

    private String executeMethod(HttpMethodBase request, IProgressMonitor monitor) {
        HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);

        monitor = Policy.monitorFor(monitor);
        try {
            monitor.beginTask(request.getName() + " request on " + request.getURI(), IProgressMonitor.UNKNOWN);

            WebUtil.execute(httpClient, hostConfiguration, request, monitor);
            return getResponseBodyAsString(request, monitor);
        } catch (Exception e) {
            new RuntimeException(e);
        } finally {
            request.releaseConnection();
            monitor.done();
        }

        return "";
    }

    public List<Repository> getRepositories(IProgressMonitor monitor) throws ReviewboardException {
        return reviewboardReader.readRepositories(executeGet("/api/json/repositories/", monitor));
    }

    public List<User> getUsers(IProgressMonitor monitor) throws ReviewboardException {
        return reviewboardReader.readUsers(executeGet("/api/json/users/", monitor));
    }

    public List<ReviewGroup> getReviewGroups(IProgressMonitor monitor) throws ReviewboardException {
        return reviewboardReader.readGroups(executeGet("/api/json/groups/", monitor));
    }

    public List<ReviewRequest> getReviewRequests(String query, IProgressMonitor monitor) throws ReviewboardException {
        return reviewboardReader.readReviewRequests(
                executeGet("/api/json/reviewrequests/" + query, monitor));
    }

    public ReviewRequest newReviewRequest(ReviewRequest reviewRequest, IProgressMonitor monitor) throws ReviewboardException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("repository_id", String.valueOf(reviewRequest.getRepository().getId()));
        if (reviewRequest.getChangeNumber() != null) {
            parameters.put("changenum", String.valueOf(reviewRequest.getChangeNumber()));
        }

        ReviewRequest newReviewRequest = reviewboardReader.readReviewRequest(executePost(
                "/api/json/reviewrequests/new/", parameters, monitor));
        reviewRequest.setId(newReviewRequest.getId());
        reviewRequest.setTimeAdded(newReviewRequest.getTimeAdded());
        reviewRequest.setLastUpdated(newReviewRequest.getLastUpdated());
        reviewRequest.setSubmitter(newReviewRequest.getSubmitter());

        // TODO
        // reviewRequest.getTargetPeople().add(newReviewRequest.getSubmitter());
        // reviewRequest.setSummary("Test");
        // reviewRequest.setDescription("Test");
        // updateReviewRequest(reviewRequest);

        return reviewRequest;
    }

    public ReviewRequest getReviewRequest(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        return reviewboardReader.readReviewRequest(executeGet("/api/json/reviewrequests/"
                + reviewRequestId + "/", monitor));
    }

    public List<Review> getReviews(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        List<Review> result = reviewboardReader.readReviews(
                executeGet("/api/json/reviewrequests/" + reviewRequestId + "/reviews/", monitor));

        for (Review review : result) {
            sortCommentsByLine(review);
        }

        return result;
    }

    private void sortCommentsByLine(Review review) {
        Collections.sort(review.getComments(), new Comparator<Comment>() {
            public int compare(Comment comment1, Comment comment2) {
                return ((Integer) comment1.getFirstLine()).compareTo(comment2.getFirstLine());
            }
        });
    }

    public void updateReviewRequest(ReviewRequest reviewRequest, IProgressMonitor monitor) throws ReviewboardException {
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("status", reviewRequest.getStatus().toString());
        // parameters.put("public", reviewRequest.getPublicReviewRequest());
        parameters.put("summary", reviewRequest.getSummary());
        parameters.put("description", reviewRequest.getDescription());
        parameters.put("testing_done", reviewRequest.getTestingDone());
        parameters.put("branch", reviewRequest.getBranch());
        parameters.put("bugs_closed", ReviewboardUtil.joinList(reviewRequest.getBugsClosed()));
        parameters.put("target_groups", ReviewboardUtil.joinList(reviewRequest.getTargetGroups()));
        parameters.put("target_people", ReviewboardUtil.joinList(reviewRequest.getTargetPeople()));

        executePost("/api/json/reviewrequests/" + reviewRequest.getId() + "/draft/set/",
                parameters, monitor);
        executePost("/api/json/reviewrequests/" + reviewRequest.getId() + "/draft/save/", monitor);
        executePost("/api/json/reviewrequests/" + reviewRequest.getId() + "/publish/", monitor);
    }

    public boolean hasRepositoryData() {
        return (clientData.lastupdate != 0);
    }

    public void updateRepositoryData(boolean force, IProgressMonitor monitor) {
        if (hasRepositoryData() && !force) {
            return;
        }

        try {
            monitor.subTask("Retrieving Reviewboard groups");
            clientData.setGroups(getReviewGroups(monitor));
            monitorWorked(monitor);

            monitor.subTask("Retrieving Reviewboard users");
            clientData.setUsers(getUsers(monitor));
            monitorWorked(monitor);

            monitor.subTask("Retrieving Reviewboard repositories");
            clientData.setRepositories(getRepositories(monitor));
            monitorWorked(monitor);

            clientData.lastupdate = new Date().getTime();
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException(e);
        }
    }

    private void monitorWorked(IProgressMonitor monitor) {
        monitor.worked(1);
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    public void performQuery(TaskRepository repository, IRepositoryQuery query,
            TaskDataCollector collector, IProgressMonitor monitor) throws CoreException {
        try {
            List<ReviewRequest> reviewRequests = getReviewRequests(query.getUrl(), monitor);
            for (ReviewRequest reviewRequest : reviewRequests) {
                TaskData taskData = getTaskDataForReviewRequest(repository, reviewRequest);
                collector.accept(taskData);
            }
        } catch (ReviewboardException e) {
            throw new CoreException(Status.CANCEL_STATUS);
        }
    }

    private TaskData getTaskDataForReviewRequest(TaskRepository taskRepository,
            ReviewRequest reviewRequest) {
        String summary = reviewRequest.getSummary();
        String id = String.valueOf(reviewRequest.getId());
        String owner = reviewRequest.getSubmitter().getUsername();
        Date creationDate = reviewRequest.getTimeAdded();
        Date dateModified = reviewRequest.getLastUpdated();
        String description = reviewRequest.getDescription();

        TaskData taskData = new TaskData(new TaskAttributeMapper(taskRepository),
                ReviewboardCorePlugin.REPOSITORY_KIND, location.getUrl(), id);
        taskData.setPartial(true);

        ReviewboardTaskMapper mapper = new ReviewboardTaskMapper(taskData, true);
        mapper.setTaskKey(id);
        mapper.setCreationDate(creationDate);
        mapper.setModificationDate(dateModified);
        mapper.setSummary(summary);
        mapper.setOwner(owner);
        mapper.setDescription(description);
        mapper.setTaskUrl(ReviewboardUtil.getReviewRequestUrl(taskRepository.getUrl(), id));

        return taskData;
    }

    public List<String> getRawDiffs(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        List<String> diffs = new ArrayList<String>();
        int iter = 1;

        // XXX Ugly hack, there should ba an API call for this function
        while (true) {
            try {
                diffs.add(executeGet(String.format("/r/%d/diff/%d/raw/", reviewRequestId, iter), monitor));
                iter++;
            } catch (Exception e) {
                break;
            }
        }

        return diffs;
    }

    public boolean validCredentials(String username, String password, IProgressMonitor monitor) {
        try {
            login(username, password, monitor);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
