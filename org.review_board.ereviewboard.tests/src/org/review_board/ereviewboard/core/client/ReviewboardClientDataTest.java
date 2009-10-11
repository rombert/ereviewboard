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

import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.User;

import junit.framework.TestCase;

/**
 * @author Markus Knittig
 *
 */
public class ReviewboardClientDataTest extends TestCase {

    private ReviewboardClientData reviewboardClientData;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        reviewboardClientData = new ReviewboardClientData();
    }

    public void testGetUser() {
        User joe = new User("Joe");
        reviewboardClientData.getUsers().add(joe);
        assertEquals(joe, reviewboardClientData.getUser("Joe"));
    }

    public void testGetNotExistingUser() {
        assertEquals(null, reviewboardClientData.getUser("Joe"));
    }

    public void testGetGroup() {
        ReviewGroup test = new ReviewGroup("Test");
        reviewboardClientData.getGroups().add(test);
        assertEquals(test, reviewboardClientData.getGroup("Test"));
    }

    public void testGetNotExistingGroup() {
        assertEquals(null, reviewboardClientData.getGroup("Test"));
    }

}
