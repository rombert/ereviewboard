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

    public static ReviewRequestQuery fromQueryString(String queryString) {
        ReviewRequestQuery result = null;
        ReviewRequestStatus status = ReviewRequestStatus.PENDING;

        int statusIndex = queryString.indexOf("?");
        if (statusIndex > 0) {
            status = ReviewRequestStatus.parseStatus(queryString.substring(statusIndex + 8));
        } else {
            statusIndex = queryString.length();
        }

        if (queryString.startsWith("/to/group/")) {
            result = new GroupReviewRequestQuery(status, queryString.substring(11, statusIndex));
        } else if (queryString.startsWith("/to/user/")) {
            result = new ToUserReviewRequestQuery(status, queryString.substring(9, statusIndex));
        } else if (queryString.startsWith("/from/user/")) {
            result = new FromUserReviewRequestQuery(status, queryString.substring(11, statusIndex));
        } else if (queryString.startsWith("/repository/")) {
            int changeNumIndex = queryString.indexOf("/changenum/");
            result = new RepositoryReviewRequestQuery(status, Integer.parseInt(queryString
                    .substring(12, changeNumIndex)), Integer.parseInt(queryString.substring(
                    changeNumIndex + 11, statusIndex)));
        } else {
            result = new AllReviewRequestQuery(status);
        }

        return result;
    }

}
