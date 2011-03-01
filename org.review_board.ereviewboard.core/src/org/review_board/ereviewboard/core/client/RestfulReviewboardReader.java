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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Comment;
import org.review_board.ereviewboard.core.model.Diff;
import org.review_board.ereviewboard.core.model.DiffComment;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.Review;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.ReviewRequestStatus;
import org.review_board.ereviewboard.core.model.Screenshot;
import org.review_board.ereviewboard.core.model.ServerInfo;
import org.review_board.ereviewboard.core.model.User;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * Class for converting Review Board API call responses (JSON format) to Java objects.
 *
 * @author Markus Knittig
 */
public class RestfulReviewboardReader {

    public ServerInfo readServerInfo(String source) throws ReviewboardException {
        
        try {
            JSONObject jsonServerInfo = checkedGetJSonRootObject(source).getJSONObject("info");
            JSONObject jsonProduct = jsonServerInfo.getJSONObject("product");
            
            String name = jsonProduct.getString("name");
            String version = jsonProduct.getString("version");
            String packageVersion = jsonProduct.getString("package_version");
            boolean isRelease = jsonProduct.getBoolean("is_release");
            
            return new ServerInfo(name, version, packageVersion, isRelease);
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    private JSONObject checkedGetJSonRootObject(String source) throws ReviewboardException {
        
        try {
            JSONObject object = new JSONObject(source);
            
            if (object.getString("stat").equals("fail")) {
                JSONObject jsonError = object.getJSONObject("err");
                throw new ReviewboardException(jsonError.getString("msg"));
            }
            
            return object;

        } catch (JSONException e) {
            throw new ReviewboardException("The server has responded with an invalid JSon object : " + e.getMessage(), e);
        }
        
    }
    
    public List<User> readUsers(String source) throws ReviewboardException {
        try {
            JSONObject jsonUsers = checkedGetJSonRootObject(source);
            return ReviewboardUtil.parseEntities(User.class, jsonUsers.getJSONArray("users"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<ReviewGroup> readGroups(String source) throws ReviewboardException {
        try {
            JSONObject jsonGroups = checkedGetJSonRootObject(source);
            return ReviewboardUtil.parseEntities(ReviewGroup.class,
                    jsonGroups.getJSONArray("groups"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<ReviewRequest> readReviewRequests(String source) throws ReviewboardException {
        try {

            JSONObject object = checkedGetJSonRootObject(source);
            
            JSONArray jsonReviewRequests = object.getJSONArray("review_requests");
            List<ReviewRequest> reviewRequests = new ArrayList<ReviewRequest>();
            
            for ( int i = 0 ; i < jsonReviewRequests.length() ; i++ )
                reviewRequests.add(readReviewRequest(jsonReviewRequests.getJSONObject(i)));
            
            return reviewRequests;
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    private ReviewRequest readReviewRequest(JSONObject jsonReviewRequest) throws JSONException {
        
        ReviewRequest reviewRequest = new ReviewRequest();
        
        JSONObject links = jsonReviewRequest.getJSONObject("links");
        
        reviewRequest.setId(jsonReviewRequest.getInt("id"));
        reviewRequest.setSubmitter(links.getJSONObject("submitter").getString("title"));
        reviewRequest.setStatus(ReviewRequestStatus.parseStatus(jsonReviewRequest.getString("status")));
        reviewRequest.setSummary(jsonReviewRequest.getString("summary"));
        reviewRequest.setTestingDone(jsonReviewRequest.getString("testing_done"));
        reviewRequest.setDescription(jsonReviewRequest.getString("description"));
        reviewRequest.setPublic(jsonReviewRequest.getBoolean("public"));
        reviewRequest.setLastUpdated(ReviewboardUtil.marshallDate(jsonReviewRequest.getString("last_updated")));
        reviewRequest.setTimeAdded(ReviewboardUtil.marshallDate(jsonReviewRequest.getString("time_added")));
        reviewRequest.setBranch(jsonReviewRequest.getString("branch"));
        if ( links.has("repository") )
            reviewRequest.setRepository(links.getJSONObject("repository").getString("title"));
        
        // change number
        String changeNumString = jsonReviewRequest.getString("changenum");
        Integer changeNum = changeNumString.equals("null") ? null : Integer.valueOf(changeNumString);
        reviewRequest.setChangeNumber(changeNum);
        
        // bugs
        JSONArray jsonBugs = jsonReviewRequest.getJSONArray("bugs_closed");
        List<String> bugs = new ArrayList<String>();
        for (int j = 0; j < jsonBugs.length(); j++)
            bugs.add(jsonBugs.getString(j));
        reviewRequest.setBugsClosed(bugs);
        
        // target people
        JSONArray jsonTargetPeople = jsonReviewRequest.getJSONArray("target_people");
        List<String> targetPeople = new ArrayList<String>();
        for ( int j = 0 ; j < jsonTargetPeople.length(); j++ )
            targetPeople.add(jsonTargetPeople.getJSONObject(j).getString("title"));
        reviewRequest.setTargetPeople(targetPeople);

        // target groups
        JSONArray jsonTargetGroups = jsonReviewRequest.getJSONArray("target_groups");
        List<String> targetGroups = new ArrayList<String>();
        for ( int j = 0 ; j < jsonTargetGroups.length(); j++ )
            targetGroups.add(jsonTargetGroups.getJSONObject(j).getString("title"));
        reviewRequest.setTargetGroups(targetGroups);
        return reviewRequest;
    }
    
    public List<Integer> readReviewRequestIds(String source) throws ReviewboardException {
        
        try {
            JSONObject jsonReviewRequests = checkedGetJSonRootObject(source);
            JSONArray reviewRequests = jsonReviewRequests.getJSONArray("review_requests");
            
            List<Integer> ids = new ArrayList<Integer>(reviewRequests.length());
            
            for ( int i = 0 ; i < reviewRequests.length(); i++ )
                ids.add(reviewRequests.getJSONObject(i).getInt("id"));
            
            return ids;
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public List<Repository> readRepositories(String source) throws ReviewboardException {
        try {
            JSONObject jsonRepositories = checkedGetJSonRootObject(source);
            return ReviewboardUtil.parseEntities(Repository.class,
                    jsonRepositories.getJSONArray("repositories"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public ReviewRequest readReviewRequest(String source) throws ReviewboardException {
        
        try {
            
            JSONObject jsonObject = checkedGetJSonRootObject(source);
            
            return readReviewRequest(jsonObject.getJSONObject("review_request"));
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<Review> readReviews(String source) throws ReviewboardException {
        
        try {
            JSONObject rootObject = checkedGetJSonRootObject(source);
            JSONArray jsonReviews = rootObject.getJSONArray("reviews");
            
            List<Review> reviews = new ArrayList<Review>();
            for ( int i = 0 ; i < jsonReviews.length(); i++ ) {
                
                JSONObject jsonReview = jsonReviews.getJSONObject(i);
                
                Review review = new Review();
                review.setId(jsonReview.getInt("id"));
                review.setBodyTop(jsonReview.getString("body_top"));
                review.setBodyBottom(jsonReview.getString("body_bottom"));
                review.setUser(jsonReview.getJSONObject("links").getJSONObject("user").getString("title"));
                review.setPublicReview(jsonReview.getBoolean("public"));
                review.setShipIt(jsonReview.getBoolean("ship_it"));
                review.setTimestamp(ReviewboardUtil.marshallDate(jsonReview.getString("timestamp")));
                
                reviews.add(review);
            }
            
            return reviews;
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<Comment> readComments(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = checkedGetJSonRootObject(source);
            return ReviewboardUtil.parseEntities(Comment.class,
                    jsonReviewRequest.getJSONArray("comments"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<Diff> readDiffs(String source) throws ReviewboardException {

        try {
            JSONObject json = checkedGetJSonRootObject(source);
            JSONArray jsonDiffs = json.getJSONArray("diffs");
            
            List<Diff> diffList = new ArrayList<Diff>();
            for (int i = 0; i < jsonDiffs.length(); i++) {

                JSONObject jsonDiff = jsonDiffs.getJSONObject(i);
                int revision = jsonDiff.getInt("revision");
                int id = jsonDiff.getInt("id");
                Date timestamp = ReviewboardUtil.marshallDate(jsonDiff.getString("timestamp"));
                
                diffList.add(new Diff(id, timestamp, revision));
            }
            
            return diffList;
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
   
    public List<Screenshot> readScreenshots(String source) throws ReviewboardException {
        
        try {
            JSONObject json = checkedGetJSonRootObject(source);
            JSONArray jsonScreenshots = json.getJSONArray("screenshots");
            
            List<Screenshot> screenshotList = new ArrayList<Screenshot>();
            for (int i = 0; i < jsonScreenshots.length(); i++) {

                JSONObject jsonScreenshot = jsonScreenshots.getJSONObject(i);
                int id = jsonScreenshot.getInt("id");
                String caption = jsonScreenshot.getString("caption");
                String url = jsonScreenshot.getString("url");
                
                screenshotList.add(new Screenshot(id, caption, url));
            }
            
            return screenshotList;
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<DiffComment> readDiffComments(String source) throws ReviewboardException {
        
        try {
            JSONObject object = checkedGetJSonRootObject(source);
            JSONArray jsonDiffComments = object.getJSONArray("diff_comments");
            
            List<DiffComment> diffComments = new ArrayList<DiffComment>();
            for ( int i = 0; i < jsonDiffComments.length(); i++ ) {
                JSONObject jsonDiffComment = jsonDiffComments.getJSONObject(i);
                int id = jsonDiffComment.getInt("id");
                String username = jsonDiffComment.getJSONObject("links").getJSONObject("user").getString("title");
                String text = jsonDiffComment.getString("text");
                Date timestamp = ReviewboardUtil.marshallDate(jsonDiffComment.getString("timestamp"));
                diffComments.add(new DiffComment(id, username, text, timestamp));
            }
            
            return diffComments;
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

}
