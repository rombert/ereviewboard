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
package org.review_board.ereviewboard.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class ReviewRequest implements Marshallable {

    private int id;
    private User submitter;
    private Date timeAdded;
    private Date lastUpdated;
    private ReviewRequestStatus status = ReviewRequestStatus.PENDING;
    private boolean publicReviewRequest;
    private Integer changeNumber;
    private Repository repository;
    private String summary = "";
    private String description = "";
    private String testingDone = "";
    private List<Integer> bugsClosed = new ArrayList<Integer>();
    private String branch = "";
    private List<ReviewGroup> targetGroups = new ArrayList<ReviewGroup>();
    private List<User> targetPeople = new ArrayList<User>();

    // TODO Add Diffs
    // TODO Add Screenshots
    // TODO Add Inactive screenshots
    // TODO Add Change descriptions

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getSubmitter() {
        return submitter;
    }

    public void setSubmitter(User submitter) {
        this.submitter = submitter;
    }

    public Date getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Date timeAdded) {
        this.timeAdded = timeAdded;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public ReviewRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewRequestStatus status) {
        this.status = status;
    }

    public boolean isPublic() {
        return publicReviewRequest;
    }

    public void setPublic(boolean isPublic) {
        this.publicReviewRequest = isPublic;
    }

    public Integer getChangeNumber() {
        return changeNumber;
    }

    public String getChangeNumberText() {
        if (changeNumber == null) {
            return "None";
        }
        return String.valueOf(changeNumber);
    }

    public void setChangeNumber(Integer changeNumber) {
        this.changeNumber = changeNumber;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTestingDone() {
        return testingDone;
    }

    public void setTestingDone(String testingDone) {
        this.testingDone = testingDone;
    }

    public List<Integer> getBugsClosed() {
        return bugsClosed;
    }

    public String getBugsClosedText() {
        return ReviewboardUtil.joinList(bugsClosed);
    }

    public void setBugsClosed(List<Integer> bugsClosed) {
        this.bugsClosed = bugsClosed;
    }

    public void setBugsClosedText(String bugsClosedText) {
        bugsClosed.clear();
        for (String bugClosed : ReviewboardUtil.splitString(bugsClosedText)) {
            try {
                bugsClosed.add(Integer.parseInt(bugClosed));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<ReviewGroup> getTargetGroups() {
        return targetGroups;
    }

    public String getTargetGroupsText() {
        return ReviewboardUtil.joinList(targetGroups);
    }

    public void setTargetGroups(List<ReviewGroup> targetGroups) {
        this.targetGroups = targetGroups;
    }

    public List<User> getTargetPeople() {
        return targetPeople;
    }

    public String getTargetPeopleText() {
        return ReviewboardUtil.joinList(targetPeople);
    }

    public void setTargetPeople(List<User> targetUsers) {
        this.targetPeople = targetUsers;
    }

    public void marshall(JSONObject jsonObject) {
        try {
            id = jsonObject.getInt("id");
            submitter = ReviewboardUtil.parseEntity(User.class, jsonObject
                    .getJSONObject("submitter"));
            timeAdded = ReviewboardUtil.marshallDate(jsonObject.getString("time_added"));
            lastUpdated = ReviewboardUtil.marshallDate(jsonObject.getString("last_updated"));
            status = ReviewRequestStatus.parseStatus(jsonObject.getString("status"));
            publicReviewRequest = ReviewboardUtil.marshallBoolean(jsonObject, "public");
            marshallChangeNumber(jsonObject);
            repository = ReviewboardUtil.parseEntity(Repository.class, jsonObject
                    .getJSONObject("repository"));
            summary = jsonObject.getString("summary");
            description = jsonObject.getString("description");
            testingDone = jsonObject.getString("testing_done");
            marshallClosedBugs(jsonObject);
            branch = jsonObject.getString("branch");
            targetGroups = ReviewboardUtil.parseEntities(ReviewGroup.class, jsonObject
                    .getJSONArray("target_groups"));
            targetPeople = ReviewboardUtil.parseEntities(User.class, jsonObject
                    .getJSONArray("target_people"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void marshallChangeNumber(JSONObject jsonObject) {
        try {
            changeNumber = jsonObject.getInt("changenum");
        } catch (JSONException e) {
            // ignore
        }
    }

    private void marshallClosedBugs(JSONObject jsonObject) {
        try {
            JSONArray jsonBugsClosed = jsonObject.getJSONArray("bugs_closed");
            bugsClosed.clear();
            for (int iter = 0; iter < jsonBugsClosed.length(); iter++) {
                // FIXME Should string like "1 2" be parsed or is this a Review
                // Board bug?
                String bugsClosedString = jsonBugsClosed.getString(iter);
                bugsClosed.add(Integer.parseInt(bugsClosedString));
            }
        } catch (Exception e) {
            // ignore
        }

    }

    public JSONObject unmarshall() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id", id);
            jsonObject.put("submitter", submitter.unmarshall());
            jsonObject.put("time_added", ReviewboardUtil.unmarshallDate(timeAdded));
            jsonObject.put("last_updated", ReviewboardUtil.unmarshallDate(lastUpdated));
            jsonObject.put("text", status);
            jsonObject.put("public", ReviewboardUtil.unmarshallBoolean(publicReviewRequest));
            jsonObject.put("changenum", changeNumber);
            jsonObject.put("repository", repository.unmarshall());
            jsonObject.put("summary", summary);
            jsonObject.put("description", description);
            jsonObject.put("testing_done", testingDone);
            jsonObject.put("bugs_closed", bugsClosed);
            jsonObject.put("branch", branch);
            jsonObject.put("target_groups", targetGroups);
            jsonObject.put("target_people", targetPeople);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonObject;
    }

}
