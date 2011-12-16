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

import com.google.common.base.Objects;

/**
 * The <tt>ObjectLink</tt> represents a link to another object in the <em>ReviewBoard</em> model.
 * 
 * @author Robert Munteanu
 *
 */
public class ObjectLink {

    private int _linkId;
    private Class<?> _linkClass;
    
    public ObjectLink(int linkId, Class<?> linkClass) {
        _linkId = linkId;
        _linkClass = linkClass;
    }
    
    public Class<?> getLinkClass() {
        return _linkClass;
    }
    
    public int getLinkId() {
        return _linkId;
    }
    
    @Override
    public int hashCode() {
        
        return Objects.hashCode(_linkId, _linkClass);
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if ( !(obj instanceof ObjectLink) )
            return false;
        
        ObjectLink other = (ObjectLink) obj;
        
        return Objects.equal(_linkId, other._linkId) && Objects.equal(_linkClass, other._linkClass);
    }
    
    @Override
    public String toString() {
        
        return "ObjectLink [linkId: "+_linkId+", linkClass: " + _linkClass+"]";
    }
}
