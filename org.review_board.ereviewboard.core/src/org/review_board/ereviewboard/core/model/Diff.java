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
 * Domain class for a diff
 * 
 * <p>A diff may contains multiple file-based diffs.</p>
 * 
 * @author Robert Munteanu
 *
 */
public class Diff implements Serializable {
    
    public static final String DIFF_REVISION_PREFIX = "Diff revision ";
    
    private final int id;
    private final String name;
    private final Date timestamp;
    private int revision;
    
    public Diff(int id, String name, Date timestamp, int revision) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.revision = revision;
    }
    
    public int getId() {
        return id;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public int getRevision() {
        return revision;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return DIFF_REVISION_PREFIX + revision;
    }

}
