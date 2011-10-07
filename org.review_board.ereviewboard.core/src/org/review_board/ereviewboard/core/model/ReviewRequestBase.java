package org.review_board.ereviewboard.core.model;

import java.util.ArrayList;
import java.util.List;

import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewRequestBase {

    private int id;
    private boolean isPublic;
    private String summary = "";
    private String description = "";
    private String testingDone = "";
    private List<String> bugsClosed = new ArrayList<String>();
    private String branch = "";
    private List<String> targetGroups = new ArrayList<String>();
    private List<String> targetPeople = new ArrayList<String>();

    public ReviewRequestBase() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
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

    public List<String> getBugsClosed() {
        return bugsClosed;
    }

    public String getBugsClosedText() {
        return ReviewboardUtil.joinList(bugsClosed);
    }

    public void setBugsClosed(List<String> bugsClosed) {
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

}