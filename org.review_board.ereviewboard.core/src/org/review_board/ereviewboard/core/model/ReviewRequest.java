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

import org.json.JSONObject;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * Domain class for review requests.
 *
 * @author Markus Knittig
 */
public class ReviewRequest {

    private int id;
    private String submitter;
    private Date timeAdded;
    private Date lastUpdated;
    private ReviewRequestStatus status = ReviewRequestStatus.PENDING;
    private boolean publicReviewRequest;
    private Integer changeNumber;
    private String summary = "";
    private String description = "";
    private String testingDone = "";
    private List<Integer> bugsClosed = new ArrayList<Integer>();
    private String branch = "";
    private List<String> targetGroups = new ArrayList<String>();
    private List<String> targetPeople = new ArrayList<String>();
    private String repository;;

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

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<String> getTargetGroups() {
        return targetGroups;
    }

    public String getTargetGroupsText() {
        return ReviewboardUtil.joinList(targetGroups);
    }

    public void setTargetGroups(List<String> targetGroups) {
        this.targetGroups = targetGroups;
    }

    public List<String> getTargetPeople() {
        return targetPeople;
    }

    public String getTargetPeopleText() {
        return ReviewboardUtil.joinList(targetPeople);
    }

    public void setTargetPeople(List<String> targetUsers) {
        this.targetPeople = targetUsers;
    }
    
    public String getRepository() {
        return repository;
    }
    
    public void setRepository(String repository) {
        this.repository = repository;
    }
    
}
