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

import java.util.List;

import org.json.JSONObject;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Comment;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.Review;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.User;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class RestfulReviewboardReader {

    public List<User> readUsers(String source) throws ReviewboardException {
        try {
            JSONObject jsonUsers = new JSONObject(source);
            return ReviewboardUtil.parseEntities(User.class, jsonUsers.getJSONArray("users"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<ReviewGroup> readGroups(String source) throws ReviewboardException {
        try {
            JSONObject jsonGroups = new JSONObject(source);
            return ReviewboardUtil.parseEntities(ReviewGroup.class, jsonGroups
                    .getJSONArray("groups"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<ReviewRequest> readReviewRequests(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequests = new JSONObject(source);
            List<ReviewRequest> reviewRequests = ReviewboardUtil.parseEntities(ReviewRequest.class,
                    jsonReviewRequests.getJSONArray("review_requests"));
            return reviewRequests;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<Repository> readRepositories(String source) throws ReviewboardException {
        try {
            JSONObject jsonRepositories = new JSONObject(source);
            return ReviewboardUtil.parseEntities(Repository.class, jsonRepositories
                    .getJSONArray("repositories"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public ReviewRequest readReviewRequest(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = new JSONObject(source);
            return ReviewboardUtil.parseEntity(ReviewRequest.class, jsonReviewRequest
                    .getJSONObject("review_request"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<Review> readReviews(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = new JSONObject(source);
            return ReviewboardUtil.parseEntities(Review.class, jsonReviewRequest
                    .getJSONArray("reviews"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<Comment> readComments(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = new JSONObject(source);
            return ReviewboardUtil.parseEntities(Comment.class, jsonReviewRequest
                    .getJSONArray("comments"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<Review> readReplies(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = new JSONObject(source);
            return ReviewboardUtil.parseEntities(Review.class, jsonReviewRequest
                    .getJSONArray("replies"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

}
