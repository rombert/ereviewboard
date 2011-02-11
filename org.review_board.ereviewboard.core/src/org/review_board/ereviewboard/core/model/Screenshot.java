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

/**
 * @author Robert Munteanu
 */
public class Screenshot {

    private final int id;
    private final String caption;
    private final String url;

    public Screenshot(int id, String caption, String url) {

        this.id = id;
        this.caption = caption;
        this.url = url;
    }

    public int getId() {

        return id;
    }

    public String getCaption() {

        return caption;
    }

    public String getUrl() {

        return url;
    }
    
    public String getContentType() {
        
        return "image/" + url.substring(url.lastIndexOf('.') + 1);
    }
    
    public String getFileName() {
        
        return url.substring(url.lastIndexOf('/') + 1);
    }

}
