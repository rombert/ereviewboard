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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import junit.framework.TestCase;

/**
 * @author Markus Knittig
 *
 */
public class ReviewRequestQueryTest extends TestCase {

    private ReviewRequestQuery query;

    public void testAllReviewRequestQueryToQueryString() {
        query = new AllReviewRequestQuery(ReviewRequestStatus.NONE);
        assertEquals("?status=none", query.getQuery());
    }

    public void testGroupReviewRequestQueryToQueryString() {
        query = new GroupReviewRequestQuery(ReviewRequestStatus.PENDING, "test");
        assertEquals("?status=pending&to-groups=test", query.getQuery());
    }

    public void testFromUserReviewRequestQueryToQueryString() {
        query = new FromUserReviewRequestQuery(ReviewRequestStatus.DISCARDED, "test");
        assertEquals("?status=discarded&from-users=test", query.getQuery());
    }

    public void testToUserReviewRequestQueryToQueryString() {
        query = new ToUserReviewRequestQuery(ReviewRequestStatus.DISCARDED, "test");
        assertEquals("?status=discarded&to-users=test", query.getQuery());
    }

    public void testToRepositoryReviewRequestQueryToQueryString() {
        query = new RepositoryReviewRequestQuery(ReviewRequestStatus.SUBMITTED, 1, 2);
        assertEquals("?status=submitted&repository=1&changenum=2", query.getQuery());
    }

    public void testQueryStringToAllReviewRequestQuery() {
        query = StatusReviewRequestQuery.fromQueryString("?status=pending");
        assertTrue(query instanceof AllReviewRequestQuery);
    }

    public void testQueryStringToGroupReviewRequestQuery() {
        
        ReviewRequestQuery groupQuery = StatusReviewRequestQuery.fromQueryString("?status=discarded&to-groups=test");
        
        assertThat(groupQuery, instanceOf(GroupReviewRequestQuery.class));
        assertThat(((GroupReviewRequestQuery) groupQuery).getStatus(), is(ReviewRequestStatus.DISCARDED));
        assertThat(((GroupReviewRequestQuery) groupQuery).getGroupname(), is("test"));
        
    }

    public void testQueryStringToFromUserReviewRequestQuery() {
        ReviewRequestQuery fromUsersQuery = StatusReviewRequestQuery.fromQueryString("?status=submitted&from-users=test");
        
        assertThat(fromUsersQuery, instanceOf(FromUserReviewRequestQuery.class));
        assertThat(((FromUserReviewRequestQuery) fromUsersQuery).getStatus(), is(ReviewRequestStatus.SUBMITTED));
        assertThat(((FromUserReviewRequestQuery) fromUsersQuery).getUsername(), is("test"));
    }

    public void testQueryStringToToUserReviewRequestQuery() {
        ReviewRequestQuery toUserQuery = StatusReviewRequestQuery.fromQueryString("?status=pending&to-users=test");
        
        assertThat(toUserQuery, instanceOf(ToUserReviewRequestQuery.class));
        assertThat(((ToUserReviewRequestQuery) toUserQuery).getStatus(), is(ReviewRequestStatus.PENDING));
        assertThat(((ToUserReviewRequestQuery) toUserQuery).getUsername(), is("test"));
    }

    public void testQueryStringToRepositoryReviewRequestQuery() {
        ReviewRequestQuery repositoryQuery = StatusReviewRequestQuery.fromQueryString("?status=all&repository=1&changenum=2");
        
        assertThat(repositoryQuery, instanceOf(RepositoryReviewRequestQuery.class));
        assertThat(((RepositoryReviewRequestQuery) repositoryQuery).getStatus(), is(ReviewRequestStatus.ALL));
        assertThat(((RepositoryReviewRequestQuery) repositoryQuery).getRepositoryId(), is(1));
        assertThat(((RepositoryReviewRequestQuery) repositoryQuery).getChangeNum(), is(2));
    }
    
    public void testGroupReviewRequestRoundTrip() {
        
        GroupReviewRequestQuery query = new GroupReviewRequestQuery(ReviewRequestStatus.PENDING, "group");
        ReviewRequestQuery restoredQuery = StatusReviewRequestQuery.fromQueryString(query.getQuery());
        
        assertTrue(restoredQuery instanceof GroupReviewRequestQuery);
        
        GroupReviewRequestQuery groupQuery = (GroupReviewRequestQuery) restoredQuery;
        assertEquals("group", groupQuery.getGroupname());
    }
    
    public void testFromUserReviewRequestRoundTrip() {
        
        FromUserReviewRequestQuery query = new FromUserReviewRequestQuery(ReviewRequestStatus.PENDING, "username");
        ReviewRequestQuery restoredQuery = StatusReviewRequestQuery.fromQueryString(query.getQuery());
        
        assertTrue(restoredQuery instanceof FromUserReviewRequestQuery);
        
        FromUserReviewRequestQuery fromUserQuery = ( FromUserReviewRequestQuery ) query;
        assertEquals("username", fromUserQuery.getUsername());
    }
    
    public void testAllReviewRequestRoundTrip() {
        
        AllReviewRequestQuery query = new AllReviewRequestQuery(ReviewRequestStatus.PENDING);
        ReviewRequestQuery restoredQuery = StatusReviewRequestQuery.fromQueryString(query.getQuery());
        
        assertTrue(restoredQuery instanceof AllReviewRequestQuery);
    }
    
    public void testRepositoryReviewRequestRoundTrip() {
        
        RepositoryReviewRequestQuery query = new RepositoryReviewRequestQuery(ReviewRequestStatus.PENDING, 5, 3);
        ReviewRequestQuery restoredQuery = StatusReviewRequestQuery.fromQueryString(query.getQuery());
        
        assertTrue(restoredQuery instanceof RepositoryReviewRequestQuery);
        
        RepositoryReviewRequestQuery repositoryQuery = (RepositoryReviewRequestQuery) restoredQuery;
        assertEquals(5, repositoryQuery.getRepositoryId());
        assertEquals(3, repositoryQuery.getChangeNum());
    }
    
    public void testToUserReviewRequestRoundTrip() {
        
        ToUserReviewRequestQuery query = new ToUserReviewRequestQuery(ReviewRequestStatus.PENDING, "username");
        ReviewRequestQuery restoredQuery = StatusReviewRequestQuery.fromQueryString(query.getQuery());
        
        assertTrue(restoredQuery instanceof ToUserReviewRequestQuery);
        
        ToUserReviewRequestQuery toUserQuery = (ToUserReviewRequestQuery) restoredQuery;
        assertEquals("username", toUserQuery.getUsername());
    }    
}
