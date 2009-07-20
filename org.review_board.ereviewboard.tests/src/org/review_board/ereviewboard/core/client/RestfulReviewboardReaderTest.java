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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.TestCase;

import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.User;

/**
 * @author Markus Knittig
 *
 */
public class RestfulReviewboardReaderTest extends TestCase {

    private RestfulReviewboardReader testReader;

    protected void setUp() throws Exception {
        super.setUp();
        testReader = new RestfulReviewboardReader();
    }

    private String inputStreamToString(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        bufferedReader.close();

        return stringBuilder.toString();
    }

    public void testReadUsers() throws Exception {
        InputStream in = getClass().getResourceAsStream("/jsondata/users.json");

        List<User> users = testReader.readUsers(inputStreamToString(in));

        assertEquals(2, users.size());
        assertEquals("joe.doe@example.com", users.get(0).getEmail());
    }

    public void testReadGroups() throws Exception {
        InputStream in = getClass().getResourceAsStream("/jsondata/groups.json");

        List<ReviewGroup> groups = testReader.readGroups(inputStreamToString(in));

        assertEquals(1, groups.size());
    }

    public void testReadRepositories() throws Exception {
        InputStream in = getClass().getResourceAsStream("/jsondata/repositories.json");

        List<Repository> reviewRequests = testReader.readRepositories(inputStreamToString(in));

        assertEquals(1, reviewRequests.size());
    }

    public void testReadReviewRequests() throws Exception {
        InputStream in = getClass().getResourceAsStream("/jsondata/review_requests.json");

        List<ReviewRequest> reviewRequests = testReader.readReviewRequests(inputStreamToString(in));

        assertEquals(1, reviewRequests.size());
    }

    public void testReadReviewRequest() throws Exception {
        InputStream in = getClass().getResourceAsStream("/jsondata/review_request.json");

        ReviewRequest reviewRequest = testReader.readReviewRequest(inputStreamToString(in));

        assertNotNull(reviewRequest);
        assertEquals(2, reviewRequest.getTargetUsers().size());
    }

}
