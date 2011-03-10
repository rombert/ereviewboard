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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Diff;
import org.review_board.ereviewboard.core.model.DiffComment;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.Review;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewReply;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.ReviewRequestStatus;
import org.review_board.ereviewboard.core.model.ScreenshotComment;
import org.review_board.ereviewboard.core.model.User;

/**
 * @author Markus Knittig
 * 
 */
public class RestfulReviewboardReaderTest {

    private RestfulReviewboardReader reader;

    @Before
    public void setUp() {

        reader = new RestfulReviewboardReader();
    }

    @Test
    public void readUsers() throws Exception {

        // http://www.reviewboard.org/docs/manual/dev/webapi/2.0/resources/user-list/
        PagedResult<User> pagedResult = reader.readUsers(readJsonTestResource("users.json"));
        
        assertThat("total_result", pagedResult.getTotalResults(), is(4));
        
        List<User> users = pagedResult.getResults();

        assertThat("users.size", users.size(), is(4));

        User user = users.get(0);

        assertThat("users[0].email", user.getEmail(), is("admin@example.com"));
        assertThat("users[0].firstName", user.getFirstName(), is("Admin"));
        assertThat("users[0].fullName", user.getFullName(), is("Admin User"));
        assertThat("users[0].id", user.getId(), is(1));
        assertThat("users[0].lastName", user.getLastName(), is("User"));
        assertThat("users[0].userName", user.getUsername(), is("admin"));
        assertThat("users[0].url", user.getUrl(), is("/users/admin/"));
    }

    private String readJsonTestResource(String resourceName) throws IOException {

        String fullResourceName = "/jsondata/" + resourceName;
        
        InputStream in = getClass().getResourceAsStream(fullResourceName);
        
        if ( in == null )
            throw new IOException("No resource : " + fullResourceName);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;

            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');
            bufferedReader.close();

            return stringBuilder.toString();
        } finally {
            bufferedReader.close();
        }
    }

    @Test
    public void readGroups() throws Exception {

        // http://www.reviewboard.org/docs/manual/dev/webapi/2.0/resources/review-group-list/
        List<ReviewGroup> groups = reader.readGroups(readJsonTestResource("groups.json"));

        assertThat("groups.size", groups.size(), is(4));

        ReviewGroup firstGroup = groups.get(0);
        assertThat("groups[0].displayName", firstGroup.getDisplayName(), is("Dev Group"));
        assertThat("groups[0].id", firstGroup.getId(), is(1));
        assertThat("groups[0].mailingList", firstGroup.getMailingList(), is("devgroup@example.com"));
        assertThat("groups[0].name", firstGroup.getName(), is("devgroup"));
        assertThat("groups[0].url", firstGroup.getUrl(), is("/groups/devgroup/"));
    }

    @Test
    public void readRepositories() throws Exception {

        // http://www.reviewboard.org/docs/manual/dev/webapi/2.0/resources/repository-list/
        PagedResult<Repository> pagedRepositories = reader.readRepositories(readJsonTestResource("repositories.json"));
        
        assertThat("totalResults", pagedRepositories.getTotalResults(), is(2));
        
        List<Repository> repositories = pagedRepositories.getResults();

        assertThat("repositories.size", repositories.size(), is(2));

        Repository repository = repositories.get(0);
        assertThat("repositories[0].id", repository.getId(), is(1));
        assertThat("repositories[0].name", repository.getName(), is("Review Board SVN"));
        assertThat("repositories[0].path", repository.getPath(),
                is("http://reviewboard.googlecode.com/svn"));
        assertThat("repositories[0].tool", repository.getTool(), is("Subversion"));
    }

    @Test
    public void readReviewRequests() throws Exception {

        // http://www.reviewboard.org/docs/manual/1.5/webapi/2.0/resources/review-request-list/
        List<ReviewRequest> reviewRequests = reader.readReviewRequests(readJsonTestResource("review_requests.json"));

        assertThat("reviewRequests.size", reviewRequests.size(), is(5));
        
        ReviewRequest firstRequest = reviewRequests.get(0);

        // first review request, most fields are set
        assertThat("firstReviewRequest.id", firstRequest.getId(), is(8));
        assertThat("firstReviewRequest.submitter", firstRequest.getSubmitter(), is("admin"));
        assertThat("firstReviewRequest.summary", firstRequest.getSummary(), is("Interdiff Revision Test"));
        assertThat("firstReviewRequest.description", firstRequest.getDescription(), is("This is a test designed for interdiffs."));
        assertThat("firstReviewRequest.public", firstRequest.isPublic(), is(true));
        assertThat("firstReviewRequest.status", firstRequest.getStatus(), is(ReviewRequestStatus.PENDING));
        assertThat("firstReviewRequest.changeNum", firstRequest.getChangeNumber(), nullValue());
        assertThat("firstReviewRequest.lastUpdated", firstRequest.getLastUpdated(), is(ReviewboardAttributeMapper.parseDateValue("2010-08-28 02:26:18")));
        assertThat("firstReviewRequest.timeAdded", firstRequest.getTimeAdded(), is(ReviewboardAttributeMapper.parseDateValue("2009-02-25 02:01:21")));
        assertThat("firstReviewRequest.branch", firstRequest.getBranch(), is("trunk"));
        assertThat("firstReviewRequest.bugsClosed", firstRequest.getBugsClosed().size(), is(0));
        assertThat("firstReviewRequest.testingDone", firstRequest.getTestingDone(), is(""));
        
        List<String> targetPeople = firstRequest.getTargetPeople();
        assertThat("firstReviewRequest.targetPeople.size", targetPeople.size(), is(1));
        assertThat("firstReviewRequest.targetPeople[0]", targetPeople.get(0), is("grumpy"));
        
        assertThat("firstReviewRequest.targetGroups", firstRequest.getTargetGroups().size(), is(0));
        assertThat("firstReviewRequest.repository", firstRequest.getRepository(), is("Review Board SVN"));
        
        // second review request, just test the fields which are not set in the first one
        ReviewRequest secondRequest = reviewRequests.get(1);
        
        assertThat("reviewRequests[1].bugsClosed", secondRequest.getBugsClosed(), is(Collections.singletonList("12345")));
        assertThat("reviewRequests[1].changeNumber", secondRequest.getChangeNumber(), is(1234));
        
        // third review request, just test the fields which are not set in the first one
        ReviewRequest thirdRequest = reviewRequests.get(2);
        
        assertThat("reviewRequests[2].targetGroups", thirdRequest.getTargetGroups(), is(Collections.singletonList("emptygroup")));
        assertThat("reviewRequests[2].testingDone", thirdRequest.getTestingDone(), is("Bar"));
        
    }

    @Test
    public void readReviewRequest() throws Exception {

        // http://www.reviewboard.org/docs/manual/1.5/webapi/2.0/resources/review-request/
        ReviewRequest reviewRequest = reader.readReviewRequest(readJsonTestResource("review_request.json"));

        assertNotNull(reviewRequest);
        assertEquals(1, reviewRequest.getTargetPeople().size());
    }

    @Test
    public void readReviews() throws Exception {

        // http://www.reviewboard.org/docs/manual/dev/webapi/2.0/resources/review-list/
        List<Review> reviews = reader.readReviews(readJsonTestResource("reviews.json"));
        assertThat("reviews.size", reviews.size(), is(1));
        
        Review firstReview = reviews.get(0);
        assertThat("reviews[0].id", firstReview.getId(), is(8));
        assertThat("reviews[0].bodyBottom", firstReview.getBodyBottom(), is(""));
        assertThat("reviews[0].bodyTop", firstReview.getBodyTop(), is(""));
        assertThat("reviews[0].user", firstReview.getUser(), is("admin"));
        assertThat("reviews[0].public", firstReview.isPublicReview(), is(true));
        assertThat("reviews[0].shipIt", firstReview.getShipIt(), is(false));
        assertThat("reviews[0].timestamp", firstReview.getTimestamp(), is(ReviewboardAttributeMapper.parseDateValue("2010-08-28 02:25:31")));
    }

    @Test
    public void readReviewReplies() throws ReviewboardException, IOException {
        
        // http://www.reviewboard.org/docs/manual/1.5/webapi/2.0/resources/review-reply-list/
        PagedResult<ReviewReply> result = reader.readReviewReplies(readJsonTestResource("review_replies.json"));
        
        assertThat("totalResults", result.getTotalResults(), is(1));
        
        List<ReviewReply> replies = result.getResults();
        assertThat("replies.size", replies.size(), is(1));
        
        ReviewReply reply = replies.get(0);
        assertThat("replies[0].id", reply.getId(), is(10));
        assertThat("replies[0].bodyTop", reply.getBodyTop(), is("Excellent point."));
        assertThat("replies[0].bodyBottom", reply.getBodyBottom(), is(""));
        assertThat("replies[0].publicReply", reply.isPublicReply(), is(true));
        assertThat("replies[0].timestamp", reply.getTimestamp(), is(ReviewboardAttributeMapper.parseDateValue("2010-08-28 02:26:47")));
        assertThat("replies[0].user", reply.getUser(), is("admin"));
    }
    
    @Test
    public void readDiffComments() throws ReviewboardException, IOException {
        
        List<DiffComment> diffs = reader.readDiffComments(readJsonTestResource("diff_comments.json"));

        assertThat("diffComments.size", diffs.size(), is(2));
        
        DiffComment firstComment = diffs.get(0);
        
        assertThat("diffComments[0].id", firstComment.getId(), is(5));
        assertThat("diffComments[0].username", firstComment.getUsername(), is("admin"));
        assertThat("diffComments[0].text", firstComment.getText(), is("This is just a sample comment."));
        assertThat("diffComments[0].timestamp", firstComment.getTimestamp(), is(ReviewboardAttributeMapper.parseDateValue("2010-08-22 17:25:41")));
    }
    
    @Test
    public void readDiffs() throws ReviewboardException, IOException {
        
        // http://www.reviewboard.org/docs/manual/1.5/webapi/2.0/resources/diff-list/
        List<Diff> diffs = reader.readDiffs(readJsonTestResource("diffs.json"));
        
        assertThat("diffs.size", diffs.size(), is(3));
        
        Diff firstDiff = diffs.get(0);
        
        assertThat("diffs[0].id", firstDiff.getId(), is(8));
        assertThat("diffs[0].revision", firstDiff.getRevision(), is(1));
        assertThat("diffs[0].timestamp", firstDiff.getTimestamp(), is (ReviewboardAttributeMapper.parseDateValue("2009-02-25 02:01:21")));
    }

    @Test
    public void readScreenshotComments() throws ReviewboardException, IOException {
        
        List<ScreenshotComment> comments = reader.readScreenshotComments(readJsonTestResource("screenshot_comments.json"));

        assertThat("screenshotComments.size", comments.size(), is(3));
        
        ScreenshotComment firstComment = comments.get(0);
        
        assertThat("screenshotComments[0].id", firstComment.getId(), is(1));
        assertThat("screenshotComments[0].username", firstComment.getUsername(), is("admin"));
        assertThat("screenshotComments[0].text", firstComment.getText(), is("This comment makes an astute observation."));
        assertThat("screenshotComments[0].timestamp", firstComment.getTimestamp(), is(ReviewboardAttributeMapper.parseDateValue("2010-08-28 02:24:31")));
    }
    
    @Test
    public void readCount() throws ReviewboardException, IOException {

        assertThat("count", reader.readCount(readJsonTestResource("count.json")), is(6));
    }
}
