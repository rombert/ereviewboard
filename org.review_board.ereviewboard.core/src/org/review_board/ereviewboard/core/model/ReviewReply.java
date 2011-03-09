/*******************************************************************************
 * Copyright (c) 2004, 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.core.model;

import java.io.Serializable;
import java.util.Date;
/**
 * @author Robert Munteanu
 */
public class ReviewReply implements Serializable {

    private int id;
    private String bodyTop;
    private String bodyBottom;
    private Date timestamp;
    private String user;
    private boolean publicReply;
    
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
    
    public Date getTimestamp() {
        
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        
        this.timestamp = timestamp;
    }
    
    public String getUser() {
        
        return user;
    }
    
    public void setUser(String user) {
        
        this.user = user;
    }
    
    public boolean isPublicReply() {
        
        return publicReply;
    }
    
    public void setPublicReply(boolean publicReply) {
        
        this.publicReply = publicReply;
    }
}
