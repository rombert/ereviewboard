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

import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.exception.ReviewboardApiException;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.exception.ReviewboardInvalidFormDataException;
import org.review_board.ereviewboard.core.exception.ReviewboardObjectDoesNotExistException;
import org.review_board.ereviewboard.core.model.*;
import org.review_board.ereviewboard.core.model.Change.Field;
import org.review_board.ereviewboard.core.model.Change.FieldChange;
import org.review_board.ereviewboard.core.model.DiffData.Chunk;
import org.review_board.ereviewboard.core.model.DiffData.Line;
import org.review_board.ereviewboard.core.model.DiffData.Type;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Class for converting Review Board API call responses (JSON format) to Java objects.
 *
 * @author Markus Knittig
 */
public class RestfulReviewboardReader {
    
    private static final Joiner CSV_JOINER = Joiner.on(',');

    public ServerInfo readServerInfo(String source) throws ReviewboardException {
        
        try {
            JSONObject jsonServerInfo = checkedGetJSonRootObject(source).getJSONObject("info");
            JSONObject jsonProduct = jsonServerInfo.getJSONObject("product");
            
            String name = jsonProduct.getString("name");
            String version = jsonProduct.getString("version");
            String packageVersion = jsonProduct.getString("package_version");
            boolean isRelease = jsonProduct.getBoolean("is_release");
            
            
            JSONObject jsonSite = jsonServerInfo.getJSONObject("site");
            TimeZone timeZone = jsonSite.has("time_zone") ? TimeZone.getTimeZone(jsonSite.getString("time_zone")) : null;
            
            return new ServerInfo(name, version, packageVersion, isRelease, timeZone);
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    private JSONObject checkedGetJSonRootObject(String source) throws ReviewboardException {
        
        if ( source == null || source.length() == 0 )
            throw new ReviewboardException("The response is empty.");
        
        try {
            JSONObject object = new JSONObject(source);
            
            if (object.getString("stat").equals("fail")) {
                JSONObject jsonError = object.getJSONObject("err");
                String message = jsonError.getString("msg");
                int code = jsonError.getInt("code");
                
                if ( ErrorCode.INVALID_FORM_DATA.is(code) )
                    throw new ReviewboardInvalidFormDataException(gatherFieldErrors(object));
                else if ( ErrorCode.OBJECT_DOES_NOT_EXIST.is(code) )
                    throw new ReviewboardObjectDoesNotExistException(message);
                
                throw new ReviewboardApiException(message, code);
            }
            
            return object;

        } catch (JSONException e) {
            
            String invalidSnippet = '\n' + source.substring(0, Math.min(200, source.length())) + "...";
            
            throw new ReviewboardException("The server has responded with an invalid JSon object : " + e.getMessage() + invalidSnippet, e);
        }
        
    }

    private Map<String, List<String>> gatherFieldErrors(JSONObject object) throws JSONException {
        
        Map<String, List<String>> allErrors = new LinkedHashMap<String, List<String>>();
        
        JSONObject jsonFields = object.getJSONObject("fields");
        
        for ( String fieldName : JSONObject.getNames(jsonFields) ) {
            
            JSONArray jsonErrorsForField = jsonFields.getJSONArray(fieldName);
            List<String> errorsForField = new ArrayList<String>(jsonErrorsForField.length());
            for ( int  i = 0 ; i < jsonErrorsForField.length(); i++ )
                errorsForField.add(jsonErrorsForField.getString(i));
            
            allErrors.put(fieldName, errorsForField);
        }
        
        return allErrors;
    }
    
    public PagedResult<User> readUsers(String source) throws ReviewboardException {
        
        try {
            JSONObject rootObject = checkedGetJSonRootObject(source);
            
            int totalResults = rootObject.getInt("total_results");
            
            JSONArray jsonUsers = rootObject.getJSONArray("users");
            List<User> users = new ArrayList<User>();
            
            for ( int i = 0 ; i < jsonUsers.length(); i++ ) {
                
                JSONObject jsonUser = jsonUsers.getJSONObject(i);
                
                User user = new User();
                user.setId(jsonUser.getInt("id"));
                user.setUrl(jsonUser.getString("url"));
                user.setUsername(jsonUser.getString("username"));
                // some fields are not set for private profiles
                user.setEmail(jsonUser.has("email") ? jsonUser.getString("email") :  "");
                user.setFirstName(jsonUser.has("first_name") ? jsonUser.getString("first_name") : "");
                user.setLastName(jsonUser.has("last_name") ? jsonUser.getString("last_name"): "");
                
                users.add(user);
            }
            
            return PagedResult.create(users, totalResults);
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public PagedResult<ReviewGroup> readGroups(String source) throws ReviewboardException {
        try {
            JSONObject rootObject = checkedGetJSonRootObject(source);
            JSONArray jsonGroups = rootObject.getJSONArray("groups");
            int totalResults = rootObject.getInt("total_results");
            
            List<ReviewGroup> groups = new ArrayList<ReviewGroup>();
            for ( int i = 0; i < jsonGroups.length(); i++ ) {
                
                JSONObject jsonObject = jsonGroups.getJSONObject(i);
                
                ReviewGroup group = new ReviewGroup();
                group.setId(jsonObject.getInt("id"));
                group.setName(jsonObject.getString("name"));
                group.setDisplayName(jsonObject.getString("display_name"));
                group.setUrl(jsonObject.getString("url"));
                group.setMailingList(jsonObject.getString("mailing_list"));
                
                groups.add(group);
            }
            
            return PagedResult.create( groups, totalResults );
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public PagedResult<ReviewRequest> readReviewRequests(String source) throws ReviewboardException {
        try {

            JSONObject object = checkedGetJSonRootObject(source);
            int totalResult= object.getInt("total_results");
            
            JSONArray jsonReviewRequests = object.getJSONArray("review_requests");
            List<ReviewRequest> reviewRequests = new ArrayList<ReviewRequest>();
            
            for ( int i = 0 ; i < jsonReviewRequests.length() ; i++ )
                reviewRequests.add(readReviewRequest(jsonReviewRequests.getJSONObject(i)));
            
            return PagedResult.create(reviewRequests, totalResult);
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    private ReviewRequest readReviewRequest(JSONObject jsonReviewRequest) throws JSONException {
        
        ReviewRequest reviewRequest = fillReviewRequestBase(new ReviewRequest(),jsonReviewRequest);
        
        JSONObject links = jsonReviewRequest.getJSONObject("links");
        
        reviewRequest.setSubmitter(links.getJSONObject("submitter").getString("title"));
        reviewRequest.setStatus(ReviewRequestStatus.parseStatus(jsonReviewRequest.getString("status")));
        reviewRequest.setLastUpdated(ReviewboardUtil.marshallDate(jsonReviewRequest.getString("last_updated")));
        reviewRequest.setTimeAdded(ReviewboardUtil.marshallDate(jsonReviewRequest.getString("time_added")));
        if ( links.has("repository") )
            reviewRequest.setRepository(links.getJSONObject("repository").getString("title"));
        
        // change number
        String changeNumString = jsonReviewRequest.getString("changenum");
        Integer changeNum = changeNumString.equals("null") ? null : Integer.valueOf(changeNumString);
        reviewRequest.setChangeNumber(changeNum);
        
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
    
    public PagedResult<Repository> readRepositories(String source) throws ReviewboardException {
        
        try {
            JSONObject rootObject = checkedGetJSonRootObject(source);
            
            int totalResults = rootObject.getInt("total_results");
            
            JSONArray jsonRepositories = rootObject.getJSONArray("repositories");
            
            List<Repository> repositories = new ArrayList<Repository>();
            
            for ( int i = 0 ; i < jsonRepositories.length(); i++ ) {
                
                JSONObject jsonRepository = jsonRepositories.getJSONObject(i);
                
                Repository repository = new Repository();
                repository.setId(jsonRepository.getInt("id"));
                repository.setName(jsonRepository.getString("name"));
                repository.setTool(RepositoryType.fromDisplayName(jsonRepository.getString("tool")));
                repository.setPath(jsonRepository.getString("path"));
                
                repositories.add(repository);
            }
            
            return PagedResult.create(repositories, totalResults);
        } catch (JSONException e) {
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
    
    private <T extends ReviewRequestBase> T fillReviewRequestBase(T reviewRequest, JSONObject jsonReviewRequest) throws JSONException {
        
        reviewRequest.setId(jsonReviewRequest.getInt("id"));
        reviewRequest.setSummary(jsonReviewRequest.getString("summary"));
        reviewRequest.setTestingDone(jsonReviewRequest.getString("testing_done"));
        reviewRequest.setDescription(jsonReviewRequest.getString("description"));
        reviewRequest.setPublic(jsonReviewRequest.getBoolean("public"));
        reviewRequest.setBranch(jsonReviewRequest.getString("branch"));
        
        // bugs
        reviewRequest.setBugsClosed(readStringArray(jsonReviewRequest, "bugs_closed"));
        
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

    private List<String> readStringArray(JSONObject object, String arrayName) throws JSONException {
        
        JSONArray jsonBugs = object.getJSONArray(arrayName);
        List<String> bugs = new ArrayList<String>();
        for (int j = 0; j < jsonBugs.length(); j++)
            bugs.add(jsonBugs.getString(j));
        return bugs;
    }

    public ReviewRequestDraft readReviewRequestDraft(String source) throws ReviewboardException {

        try {
            
            JSONObject jsonObject = checkedGetJSonRootObject(source);
            
            return fillReviewRequestBase(new ReviewRequestDraft(), jsonObject.getJSONObject("draft"));
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public PagedResult<Review> readReviews(String source) throws ReviewboardException {
        
        try {
            JSONObject rootObject = checkedGetJSonRootObject(source);
            int totalResults = rootObject.getInt("total_results");
            JSONArray jsonReviews = rootObject.getJSONArray("reviews");
            
            List<Review> reviews = new ArrayList<Review>();
            for ( int i = 0 ; i < jsonReviews.length(); i++ ) {
                
                JSONObject jsonReview = jsonReviews.getJSONObject(i);
                
                reviews.add(getReview(jsonReview));
            }
            
            return PagedResult.create(reviews, totalResults);
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public Review readReview(String source) throws ReviewboardException {
        
        try {
            JSONObject rootObject = checkedGetJSonRootObject(source);
            JSONObject jsonReview = rootObject.getJSONObject("review");
            
            return getReview(jsonReview);
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
        
    }

    private Review getReview(JSONObject jsonReview) throws JSONException {
        
        Review review = new Review();
        review.setId(jsonReview.getInt("id"));
        review.setBodyTop(jsonReview.getString("body_top"));
        review.setBodyBottom(jsonReview.getString("body_bottom"));
        review.setUser(jsonReview.getJSONObject("links").getJSONObject("user").getString("title"));
        review.setPublicReview(jsonReview.getBoolean("public"));
        review.setShipIt(jsonReview.getBoolean("ship_it"));
        review.setTimestamp(ReviewboardUtil.marshallDate(jsonReview.getString("timestamp")));
        return review;
    }
    
    public PagedResult<ReviewReply> readReviewReplies(String source) throws ReviewboardException {
        
        
        try {
            JSONObject rootObject = checkedGetJSonRootObject(source);
            
            int totalResults = rootObject.getInt("total_results");
            
            JSONArray jsonReplies = rootObject.getJSONArray("replies");
            List<ReviewReply> replies = new ArrayList<ReviewReply>();
            
            for ( int i = 0; i < jsonReplies.length(); i++ ) {
                
                JSONObject jsonReply = jsonReplies.getJSONObject(i);
                
                ReviewReply reply = new ReviewReply();
                reply.setId(jsonReply.getInt("id"));
                reply.setBodyTop(jsonReply.getString("body_top"));
                reply.setBodyBottom(jsonReply.getString("body_bottom"));
                reply.setPublicReply(jsonReply.getBoolean("public"));
                reply.setTimestamp(ReviewboardUtil.marshallDate(jsonReply.getString("timestamp")));
                reply.setUser(jsonReply.getJSONObject("links").getJSONObject("user").getString("title"));
                
                replies.add(reply);
            }
            
            return PagedResult.create(replies, totalResults);
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public PagedResult<Diff> readDiffs(String source) throws ReviewboardException {

        try {
            JSONObject json = checkedGetJSonRootObject(source);
            
            int totalResults = json.getInt("total_results");
            JSONArray jsonDiffs = json.getJSONArray("diffs");
            
            List<Diff> diffList = new ArrayList<Diff>();
            for (int i = 0; i < jsonDiffs.length(); i++) {

                JSONObject jsonDiff = jsonDiffs.getJSONObject(i);
                
                diffList.add(parseDiff(jsonDiff));
            }
            
            return PagedResult.create(diffList, totalResults);
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
   
    private Diff parseDiff(JSONObject jsonDiff) throws JSONException {

        int revision = jsonDiff.getInt("revision");
        String name = jsonDiff.getString("name");
        int id = jsonDiff.getInt("id");
        Date timestamp = ReviewboardUtil.marshallDate(jsonDiff.getString("timestamp"));
        
        return new Diff(id, name, timestamp, revision);

    }
    
    public Diff readDiff(String source) throws ReviewboardException {
        
        try {
            JSONObject object = checkedGetJSonRootObject(source);
            
            return parseDiff(object.getJSONObject("diff"));
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public PagedResult<FileDiff> readFileDiffs(String source) throws ReviewboardException {
        try {
            JSONObject json = checkedGetJSonRootObject(source);
            
            int totalResults = json.getInt("total_results");
            JSONArray jsonDiffs = json.getJSONArray("files");
            
            List<FileDiff> diffList = new ArrayList<FileDiff>();
            for (int i = 0; i < jsonDiffs.length(); i++) {

                JSONObject jsonDiff = jsonDiffs.getJSONObject(i);
                int id = jsonDiff.getInt("id");
                String sourceFile = jsonDiff.getString("source_file");
                String sourceRevision = jsonDiff.getString("source_revision");
                String destinationFile = jsonDiff.getString("dest_file");
                String destinationDetail = jsonDiff.getString("dest_detail");
                
                diffList.add(new FileDiff(id, sourceFile, sourceRevision, destinationFile, destinationDetail));
            }
            
            return PagedResult.create(diffList, totalResults);
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public Screenshot readScreenshot(String source) throws ReviewboardException {
        try {

            return readScreenshot(checkedGetJSonRootObject(source).getJSONObject("screenshot"));

        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    private Screenshot readScreenshot(JSONObject jsonScreenshot) throws JSONException {

        int id = jsonScreenshot.getInt("id");
        String caption = jsonScreenshot.getString("caption");
        String url = jsonScreenshot.getString("url");

        return new Screenshot(id, caption, url);
    }
    
    public PagedResult<Screenshot> readScreenshots(String source) throws ReviewboardException {
        
        try {
            JSONObject json = checkedGetJSonRootObject(source);
            
            int totalResults = json.getInt("total_results");
            JSONArray jsonScreenshots = json.getJSONArray("screenshots");
            
            List<Screenshot> screenshotList = new ArrayList<Screenshot>();
            for (int i = 0; i < jsonScreenshots.length(); i++)
                screenshotList.add(readScreenshot(jsonScreenshots.getJSONObject(i)));
            
            return PagedResult.create(screenshotList, totalResults);
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public PagedResult<DiffComment> readDiffComments(String source) throws ReviewboardException {
        
        try {
            JSONObject object = checkedGetJSonRootObject(source);
            
            int totalResults = object.getInt("total_results");
            JSONArray jsonDiffComments = object.getJSONArray("diff_comments");
            
            List<DiffComment> diffComments = new ArrayList<DiffComment>();
            for ( int i = 0; i < jsonDiffComments.length(); i++ ) {
                JSONObject jsonDiffComment = jsonDiffComments.getJSONObject(i);
                DiffComment comment = new DiffComment();
                
                mapComment(jsonDiffComment, comment);
                comment.setFirstLine(jsonDiffComment.getInt("first_line"));
                comment.setNumLines(jsonDiffComment.getInt("num_lines"));
                String fileHref = jsonDiffComment.getJSONObject("links").getJSONObject("filediff").getString("href");
                int fileId = Integer.parseInt( fileHref.replaceFirst(".*files/", "").replace("/", "") );
                comment.setFileId(fileId);
                
                diffComments.add(comment);
            }
            
            return PagedResult.create(diffComments, totalResults);
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    private DiffComment mapDiffComment(JSONObject jsonDiffComment) throws JSONException {
        
        DiffComment comment = new DiffComment();
        
        mapComment(jsonDiffComment, comment);
        comment.setFirstLine(jsonDiffComment.getInt("first_line"));
        comment.setNumLines(jsonDiffComment.getInt("num_lines"));
        String fileHref = jsonDiffComment.getJSONObject("links").getJSONObject("filediff").getString("href");
        int fileId = Integer.parseInt( fileHref.replaceFirst(".*files/", "").replace("/", "") );
        comment.setFileId(fileId);
        
        return comment;
    }
    
    public DiffComment readDiffComment(String result) throws ReviewboardException {
        
        try {
            JSONObject jsonDiffComment = checkedGetJSonRootObject(result);
            
            return mapDiffComment(jsonDiffComment.getJSONObject("diff_comment"));
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    private void mapComment(JSONObject jsonComment, Comment comment) throws JSONException {
       
        comment.setId(jsonComment.getInt("id"));
        comment.setUsername(jsonComment.getJSONObject("links").getJSONObject("user").getString("title"));
        comment.setText(jsonComment.getString("text"));
        comment.setTimestamp(ReviewboardUtil.marshallDate(jsonComment.getString("timestamp")));
    }

    public PagedResult<ScreenshotComment> readScreenshotComments(String source) throws ReviewboardException {
        
        try {
            JSONObject object = checkedGetJSonRootObject(source);

            int totalResults = object.getInt("total_results");
            JSONArray jsonDiffComments = object.getJSONArray("screenshot_comments");
            
            List<ScreenshotComment> diffComments = new ArrayList<ScreenshotComment>();
            for ( int i = 0; i < jsonDiffComments.length(); i++ ) {
                JSONObject jsonDiffComment = jsonDiffComments.getJSONObject(i);
                ScreenshotComment comment = new ScreenshotComment();
                
                mapComment(jsonDiffComment, comment);
                
                diffComments.add(comment);
            }
            
            return PagedResult.create( diffComments, totalResults);
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public int readCount(String source) throws ReviewboardException {
        
        try {
            JSONObject root = checkedGetJSonRootObject(source);
            
            return root.getInt("count");
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public void ensureSuccess(String source) throws ReviewboardException {
        
        checkedGetJSonRootObject(source);
    }

    public DiffData readDiffData(String source) throws ReviewboardException{
        
        try {
            JSONObject jsonDiffData = checkedGetJSonRootObject(source).getJSONObject("diff_data");
            
            DiffData diffData = new DiffData();
            diffData.setBinary(jsonDiffData.getBoolean("binary"));
            diffData.setNewFile(jsonDiffData.getBoolean("new_file"));
            diffData.setNumChanges(jsonDiffData.getInt("num_changes"));
            
            JSONArray changedChunkIndexes = jsonDiffData.getJSONArray("changed_chunk_indexes");
            for ( int i = 0 ; i < changedChunkIndexes.length(); i++)
                diffData.getChangedChunkIndexes().add(changedChunkIndexes.getInt(i));
            
            JSONArray chunks = jsonDiffData.getJSONArray("chunks");
            for ( int i = 0 ; i < chunks.length(); i++ ) {
                
                JSONObject jsonChunk = chunks.getJSONObject(i);
                
                Chunk chunk = new Chunk();
                chunk.setChange(Type.fromString(jsonChunk.getString("change")));
                chunk.setCollapsable(jsonChunk.getBoolean("collapsable"));
                chunk.setIndex(jsonChunk.getInt("index"));
                chunk.setNumLines(jsonChunk.getInt("numlines"));
                
                JSONArray lines = jsonChunk.getJSONArray("lines");
                for ( int j =0 ; j < lines.length(); j++ ) {
                    
                    JSONArray lineArray = lines.getJSONArray(j);
                    
                    Line line = new Line();
                    line.setDiffRowNumber(lineArray.getInt(0));
                    line.setLeftFileRowNumber(getPossiblyEmptyInt(lineArray, 1));
                    line.setLeftLineText(lineArray.getString(2));

                    line.setRightFileRowNumber(getPossiblyEmptyInt(lineArray, 4));
                    line.setRightLineText(lineArray.getString(5));
                    
                    line.setWhitespaceOnly(lineArray.getBoolean(7));
                    
                    chunk.getLines().add(line);
                }
                
                diffData.getChunks().add(chunk);
            }
            
            return diffData;
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    private int getPossiblyEmptyInt(JSONArray lineArray, int index) throws JSONException {
        
        String value = lineArray.getString(index);
        if ( value.length() == 0 )
            return -1;
        
        return Integer.parseInt(value);
    }

    public Change readChange(String source) throws ReviewboardException {
     
        try {
            JSONObject jsonChange = checkedGetJSonRootObject(source).getJSONObject("change");

            return readChangeObject(jsonChange);

        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    private Change readChangeObject(JSONObject jsonChange) throws JSONException {
        
        JSONObject jsonFieldsChanged = jsonChange.getJSONObject("fields_changed");
        List<FieldChange> fieldChanges = Lists.<Change.FieldChange>newArrayList();
        
        for ( Field field : Field.values() ) {
            
            if ( !jsonFieldsChanged.has(field.toString()) )
                continue;
            
            JSONObject jsonFieldChange = jsonFieldsChanged.getJSONObject(field.toString());
            
            switch ( field ) {
            
                case bugs_closed:
                    String newBugs = CSV_JOINER.join(readStringArray(jsonFieldChange, "added"));
                    String oldBugs = CSV_JOINER.join(readStringArray(jsonFieldChange, "removed"));
                    
                    fieldChanges.add(new FieldChange(field, null, newBugs, oldBugs, null, null));
                    break;
            
                case diff:
                    
                    JSONObject addedDiff = jsonFieldChange.getJSONObject("added");
                    
                    fieldChanges.add(new FieldChange(field, new ObjectLink(addedDiff.getInt("id"), Diff.class), null, null, null, null));
                    break;
                    
                    // TODO unhandled
                case screenshots:
                case file_attachments:
                case target_groups:
                case target_people:
                    continue;
                    
                case status:
                    
                    fieldChanges.add(new FieldChange(field, null, ReviewRequestStatus.parseStatus(jsonFieldChange.getString("new")).toString(), 
                            ReviewRequestStatus.parseStatus(jsonFieldChange.getString("old")).toString(), null, null));
                    break;
                    
                case summary:
                case description:
                case testing_done:
                case branch:
                    
                    fieldChanges.add(new FieldChange(field, null, jsonFieldChange.getString("new"), jsonFieldChange.getString("old"), null, null));
                    break;

                default:
                    
                    ReviewboardCorePlugin.getDefault().log(IStatus.WARNING, "Could not parse change due to unhandled field with name " + field.toString());
                    continue;
            }
        }
        
        return new Change(jsonChange.getInt("id"), jsonChange.getString("text"), 
                ReviewboardUtil.marshallDate(jsonChange.getString("timestamp")), fieldChanges);
    }
    
    public PagedResult<Change> readChanges(String source) throws ReviewboardException {
        
        try {
            JSONObject json = checkedGetJSonRootObject(source);
            
            int totalResults = json.getInt("total_results");
            JSONArray jsonChanges = json.getJSONArray("changes");
            
            List<Change> changesList = Lists.newArrayList();
            for (int i = 0; i < jsonChanges.length(); i++)
                changesList.add(readChangeObject(jsonChanges.getJSONObject(i)));
            
            return PagedResult.create(changesList, totalResults);
            
        } catch (JSONException e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
}
