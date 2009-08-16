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

import org.json.JSONException;
import org.json.JSONObject;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class Comment implements Marshallable {

    private static final long serialVersionUID = 2864269615892045077L;

    private int id;
    private Comment replyTo;
    private Date timestamp;
    private String text;
    private User user;
    private int firstLine;
    private int numLines;
    private FileDiff fileDiff;
    private FileDiff interFileDiff;
    private boolean publicComment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Comment getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Comment replyTo) {
        this.replyTo = replyTo;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(int firstLine) {
        this.firstLine = firstLine;
    }

    public int getNumLines() {
        return numLines;
    }

    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }

    public int getLastLine() {
        return firstLine + numLines;
    }

    public FileDiff getFileDiff() {
        return fileDiff;
    }

    public void setFileDiff(FileDiff fileDiff) {
        this.fileDiff = fileDiff;
    }

    public FileDiff getInterFileDiff() {
        return interFileDiff;
    }

    public void setInterFileDiff(FileDiff interFileDiff) {
        this.interFileDiff = interFileDiff;
    }

    public boolean isPublicComment() {
        return publicComment;
    }

    public void setPublicComment(boolean publicComment) {
        this.publicComment = publicComment;
    }

    public void marshall(JSONObject jsonObject) {
        try {
            id = jsonObject.getInt("id");
            if (jsonObject.has("reply_to")) {
                replyTo = ReviewboardUtil.parseEntity(Comment.class, jsonObject
                        .getJSONObject("reply_to"));
            } else {
                replyTo = null;
            }
            timestamp = ReviewboardUtil.marshallDate(jsonObject.getString("timestamp"));
            text = jsonObject.getString("text");
            user = ReviewboardUtil.parseEntity(User.class, jsonObject
                    .getJSONObject("user"));
            fileDiff = ReviewboardUtil.parseEntity(FileDiff.class, jsonObject
                    .getJSONObject("filediff"));
            if (jsonObject.getString("interfilediff").equals("null")) {
                interFileDiff = null;
            } else {
                interFileDiff = ReviewboardUtil.parseEntity(FileDiff.class, jsonObject
                        .getJSONObject("interfilediff"));
            }
            firstLine = jsonObject.getInt("first_line");
            numLines = jsonObject.getInt("num_lines");
            publicComment = ReviewboardUtil.marshallBoolean(jsonObject.getInt("public"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject unmarshall() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id", id);
            if (replyTo != null) {
                jsonObject.put("reply_to", replyTo.unmarshall());
            }
            jsonObject.put("timestamp", ReviewboardUtil.unmarshallDate(timestamp));
            jsonObject.put("text", text);
            jsonObject.put("user", user.unmarshall());
            jsonObject.put("filediff", fileDiff.unmarshall());
            if (interFileDiff == null) {
                jsonObject.put("interfilediff", "null");
            } else {
                jsonObject.put("interfilediff", interFileDiff);
            }
            jsonObject.put("first_line", firstLine);
            jsonObject.put("num_lines", numLines);
            jsonObject.put("public", ReviewboardUtil.unmarshallBoolean(publicComment));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonObject;
    }

}
