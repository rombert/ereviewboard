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

import org.json.JSONException;
import org.json.JSONObject;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class FileDiff implements Marshallable {

    private int id;
    private DiffSet diffSet;
    private String destDetail;
    private String sourceRevision;
    private String sourceFile;
    private String destFile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DiffSet getDiffSet() {
        return diffSet;
    }

    public void setDiffSet(DiffSet diffSet) {
        this.diffSet = diffSet;
    }

    public String getDestDetail() {
        return destDetail;
    }

    public void setDestDetail(String destDetail) {
        this.destDetail = destDetail;
    }

    public String getSourceRevision() {
        return sourceRevision;
    }

    public void setSourceRevision(String sourceRevision) {
        this.sourceRevision = sourceRevision;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getDestFile() {
        return destFile;
    }

    public void setDestFile(String destFile) {
        this.destFile = destFile;
    }

    public void marshall(JSONObject jsonObject) {
        try {
            id = jsonObject.getInt("id");
            diffSet = ReviewboardUtil.parseEntity(DiffSet.class, jsonObject
                    .getJSONObject("diffset"));
            destDetail = jsonObject.getString("dest_detail");
            sourceRevision = jsonObject.getString("source_revision");
            sourceFile = jsonObject.getString("source_file");
            destFile = jsonObject.getString("dest_file");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject unmarshall() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id", id);
            jsonObject.put("diffset", diffSet.unmarshall());
            jsonObject.put("dest_detail", destDetail);
            jsonObject.put("source_revision", sourceRevision);
            jsonObject.put("source_file", sourceFile);
            jsonObject.put("dest_file", destFile);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonObject;
    }

}
