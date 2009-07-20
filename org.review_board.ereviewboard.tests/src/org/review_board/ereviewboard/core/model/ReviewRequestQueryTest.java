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

import junit.framework.TestCase;

/**
 * @author Markus Knittig
 *
 */
public class ReviewRequestQueryTest extends TestCase {

    private ReviewRequestQuery query;

    public void testAllReviewRequestQueryToQueryString() {
        query = new AllReviewRequestQuery(ReviewRequestStatus.NONE);
        assertEquals("all?status=none", query.getQuery());
    }

    public void testGroupReviewRequestQueryToQueryString() {
        query = new GroupReviewRequestQuery(ReviewRequestStatus.PENDING, "test");
        assertEquals("to/group/test?status=pending", query.getQuery());
    }

    public void testFromUserReviewRequestQueryToQueryString() {
        query = new FromUserReviewRequestQuery(ReviewRequestStatus.DISCARDED, "test");
        assertEquals("from/user/test?status=discarded", query.getQuery());
    }

    public void testToUserReviewRequestQueryToQueryString() {
        query = new ToUserReviewRequestQuery(ReviewRequestStatus.DISCARDED, "test");
        assertEquals("to/user/test?status=discarded", query.getQuery());
    }

    public void testToRepositoryReviewRequestQueryToQueryString() {
        query = new RepositoryReviewRequestQuery(ReviewRequestStatus.SUBMITTED, 1, 2);
        assertEquals("repository/1/changenum/2?status=submitted", query.getQuery());
    }

    public void testQueryStringToAllReviewRequestQuery() {
        query = StatusReviewRequestQuery.fromQueryString("/all?status=pending");
        assertTrue(query instanceof AllReviewRequestQuery);
    }

    public void testQueryStringToGroupReviewRequestQuery() {
        query = StatusReviewRequestQuery.fromQueryString("/to/group/test?status=discarded");
        assertTrue(query instanceof GroupReviewRequestQuery);
    }

    public void testQueryStringToFromUserReviewRequestQuery() {
        query = StatusReviewRequestQuery.fromQueryString("/from/user/test?status=discarded");
        assertTrue(query instanceof FromUserReviewRequestQuery);
    }

    public void testQueryStringToToUserReviewRequestQuery() {
        query = StatusReviewRequestQuery.fromQueryString("/to/user/test?status=discarded");
        assertTrue(query instanceof ToUserReviewRequestQuery);
    }

    public void testQueryStringToRepositoryReviewRequestQuery() {
        query = StatusReviewRequestQuery
                .fromQueryString("/repository/1/changenum/2?status=submitted");
        assertTrue(query instanceof RepositoryReviewRequestQuery);
    }

}
