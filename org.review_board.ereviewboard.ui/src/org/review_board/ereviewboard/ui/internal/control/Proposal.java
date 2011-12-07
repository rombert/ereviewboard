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
package org.review_board.ereviewboard.ui.internal.control;

/**
 * A simple proposal which can be displayed
 * 
 * @author Robert Munteanu
 *
 */
public class Proposal {
    
    private final String _value;
    private final String _label;
    
    public Proposal(String value, String label) {
        
        _value = value;
        _label = label;
    }
    
    public String getValue() {
        
        return _value;
    }
    
    public String getLabel() {
        
        return _label;
    }
}