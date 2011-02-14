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

/**
 * Abstract class for queries getting review requests by status.
 *
 * @author Markus Knittig
 */
public abstract class StatusReviewRequestQuery implements ReviewRequestQuery {

    private static final String QUERY_FRAGMENT_TO_USER = "to/user/";
    
    private static final String QUERY_FRAGMENT_FROM_USER = "from/user/";

    private static final String QUERY_FRAGMENT_GROUP = "to/group/";

    private static final String QUERY_FRAGMENT_REPOSITORY = "repository/";
    
    private ReviewRequestStatus status;

    public StatusReviewRequestQuery(ReviewRequestStatus status) {
        super();
        this.status = status;
    }

    public String getQuery() {
        if (status == null) {
            return "";
        } else {
            return String.format("/?status=%s", status.getDisplayname().toLowerCase());
        }
    }

    public void setStatus(ReviewRequestStatus status) {
        this.status = status;
    }
    
    public ReviewRequestStatus getStatus() {
        return status;
    }

    public static ReviewRequestQuery fromQueryString(String queryString) {
        ReviewRequestQuery result = null;
        ReviewRequestStatus status = ReviewRequestStatus.PENDING;

        int statusIndex = queryString.indexOf("?");
        if (statusIndex > 0) {
            status = ReviewRequestStatus.parseStatus(queryString.substring(statusIndex + 8));
        } else {
            statusIndex = queryString.length();
        }

        if (queryString.startsWith(QUERY_FRAGMENT_GROUP)) {
            result = new GroupReviewRequestQuery(status, removeTrailingSlashIfPresent(queryString.substring(QUERY_FRAGMENT_GROUP.length(), statusIndex)));
        } else if (queryString.startsWith(QUERY_FRAGMENT_TO_USER)) {
            result = new ToUserReviewRequestQuery(status, removeTrailingSlashIfPresent(queryString.substring(QUERY_FRAGMENT_TO_USER.length(), statusIndex)));
        } else if (queryString.startsWith(QUERY_FRAGMENT_FROM_USER)) {
            result = new FromUserReviewRequestQuery(status, removeTrailingSlashIfPresent(queryString.substring(QUERY_FRAGMENT_FROM_USER.length(), statusIndex)));
        } else if (queryString.startsWith(QUERY_FRAGMENT_REPOSITORY)) {
            int changeNumIndex = queryString.indexOf("/changenum/");
            int repositoryEndIndex = queryString.indexOf(QUERY_FRAGMENT_REPOSITORY) + + QUERY_FRAGMENT_REPOSITORY.length();
            int repositoryId = Integer.parseInt(queryString .substring(repositoryEndIndex , changeNumIndex));
            String changeIdString = queryString.substring( changeNumIndex + 11, statusIndex);
            changeIdString = removeTrailingSlashIfPresent(changeIdString);
            int changeId = Integer.parseInt(changeIdString);
            result = new RepositoryReviewRequestQuery(status, repositoryId, changeId);
        } else {
            result = new AllReviewRequestQuery(status);
        }

        return result;
    }

    private static String removeTrailingSlashIfPresent(String value) {
        if ( value.endsWith("/"))
            value = value.substring(0, value.length() - 1);
        return value;
    }

}
