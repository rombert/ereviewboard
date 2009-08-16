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

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class Review implements Marshallable {

    private static final long serialVersionUID = -7242357272778386934L;

    private int id;
    private String bodyTop;
    private String bodyBottom;
    private int shipIt;
    private Date timestamp;
    private User user;
    private boolean publicReview;
    private List<Comment> comments;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBodyTop() {
        return bodyTop;
    }

    public void setBodyTop(String bodyTop) {
        this.bodyTop = bodyTop;
    }

    public String getBodyBottom() {
        return bodyBottom;
    }

    public void setBodyBottom(String bodyBottom) {
        this.bodyBottom = bodyBottom;
    }

    public int getShipIt() {
        return shipIt;
    }

    public void setShipIt(int shipIt) {
        this.shipIt = shipIt;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isPublicReview() {
        return publicReview;
    }

    public void setPublicReview(boolean publicReview) {
        this.publicReview = publicReview;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void marshall(JSONObject jsonObject) {
        try {
            id = jsonObject.getInt("id");
            bodyTop = jsonObject.getString("body_top");
            bodyBottom = jsonObject.getString("body_bottom");
            shipIt = jsonObject.getInt("ship_it");
            timestamp = ReviewboardUtil.marshallDate(jsonObject.getString("timestamp"));
            user = ReviewboardUtil.parseEntity(User.class, jsonObject
                    .getJSONObject("user"));
            publicReview = ReviewboardUtil.marshallBoolean(jsonObject, "public");
            comments = ReviewboardUtil.parseEntities(Comment.class, jsonObject
                    .getJSONArray("comments"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject unmarshall() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id", id);
            jsonObject.put("body_top", bodyTop);
            jsonObject.put("body_bottom", bodyBottom);
            jsonObject.put("ship_it", shipIt);
            jsonObject.put("timestamp", ReviewboardUtil.unmarshallDate(timestamp));;
            jsonObject.put("user", user.unmarshall());
            jsonObject.put("public", ReviewboardUtil.unmarshallBoolean(publicReview));
            //FIXME unmarshall
            jsonObject.put("comments", comments);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonObject;
    }

}
