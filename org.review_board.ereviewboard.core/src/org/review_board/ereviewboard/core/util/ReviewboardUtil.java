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
package org.review_board.ereviewboard.core.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.review_board.ereviewboard.core.ReviewboardConstants;
import org.review_board.ereviewboard.core.model.Marshallable;

import com.rits.cloning.Cloner;

/**
 * @author Markus Knittig
 *
 */
public final class ReviewboardUtil {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Cloner cloner = new Cloner();

    private ReviewboardUtil() {
        super();
    }

    public static Date marshallDate(String time) {
        Date date = null;

        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return date;
    }

    public static String unmarshallDate(Date date) {
        return dateFormat.format(date);
    }

    public static boolean marshallBoolean(JSONObject jsonObject, String key) {
        try {
            Object bool = jsonObject.get(key);
            if (bool instanceof Boolean) {
                return (Boolean) bool;
            } else {
                if (((Integer) bool) > 0) {
                    return true;
                }

                return false;
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    public static int unmarshallBoolean(boolean bool) {
        if (bool) {
            return 1;
        }

        return 0;
    }

    public static <T extends Marshallable> T parseEntity(Class<T> entityClass, JSONObject jsonObject) {
        T entity = null;

        try {
            entity = entityClass.newInstance();
            entity.marshall(jsonObject);
        } catch (Exception e) {
            entity = null;
        }

        return entity;
    }

    public static <T extends Marshallable> List<T> parseEntities(Class<T> entityClass,
            JSONArray jsonArray) {
        List<T> entities = new ArrayList<T>();

        try {
            for (int iter = 0; iter < jsonArray.length(); iter++) {
                T entity = entityClass.newInstance();
                entity.marshall(jsonArray.getJSONObject(iter));
                entities.add(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return entities;
    }

    public static String getReviewRequestUrl(String repositoryUrl, String taskId) {
        return stripEndingSlash(repositoryUrl) + ReviewboardConstants.REVIEW_REQUEST_URL + taskId;
    }

    private static String stripEndingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static <T> T cloneEntity(T entity) {
        return cloner.deepClone(entity);
    }

    public static <T> String joinList(List<T> list) {
        StringBuilder result = new StringBuilder();

        String delimiter = "";
        for (T item : list) {
            result.append(delimiter);
            result.append(item.toString());
            delimiter = ", ";
        }

        return result.toString();
    }

    public static List<String> splitString(String string) {
        List<String> result = new ArrayList<String>();
        for (String item : string.split(",\\s?")) {
            result.add(item.trim());
        }
        return result;
    }

    public static List<String> toStringList(List<?> list) {
        List<String> result = new ArrayList<String>();
        for (Object string : list) {
            result.add(string.toString());
        }
        return result;
    }

}
