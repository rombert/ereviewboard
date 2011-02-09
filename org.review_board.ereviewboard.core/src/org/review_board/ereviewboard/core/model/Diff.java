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

import java.util.Date;

/**
 * Domain class for a diff
 * 
 * <p>A diff may contains multiple file-based diffs.</p>
 * 
 * @author Robert Munteanu
 *
 */
public class Diff {
    
    private final int id;
    private final Date timestamp;
    private int revision;
    
    public Diff(int id, Date timestamp, int revision) {
        this.id = id;
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
        return "Diff revision " + revision;
    }

}
