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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.User;

/**
 * Container for persistent Review Board client data.
 *
 * @author Markus Knittig
 */
public class ReviewboardClientData implements Serializable {
    
    private Map<String, User> usersByUsername = new HashMap<String, User>();
    
    private List<ReviewGroup> groups = new ArrayList<ReviewGroup>();
    private List<Repository> repositories = new ArrayList<Repository>();

    long lastupdate = 0;

    public Collection<User> getUsers() {
        
        return Collections.unmodifiableCollection(usersByUsername.values());
    }
    
    public User getUser(String username) {
     
        return usersByUsername.get(username);
    }

    public void setUsers(List<User> users) {
        
        usersByUsername.clear();
        
        for ( User user : users )
            usersByUsername.put(user.getUsername(), user);
    }

    public List<ReviewGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ReviewGroup> groups) {
        this.groups = groups;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public ReviewGroup getGroup(String groupname) {
        return getItem(groups, new ReviewGroup(groupname));
    }

    private <T> T getItem(List<T> list, T search) {
        int index = list.indexOf(search);
        if (index > -1) {
            return list.get(index);
        }
        return null;
    }
}
