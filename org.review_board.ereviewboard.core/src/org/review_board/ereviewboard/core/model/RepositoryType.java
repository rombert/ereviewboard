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
 * 
 * @author Robert Munteanu
 *
 */
public enum RepositoryType {

    Bazaar("Bazaar"), ClearCase("Clear Case"), CVS("CVS"), Git("Git"), Mercurial("Mercurial"), Perforce("Perforce"), PerforceVMWare("Perforce (VMware)"), PlasticSCM("Plastic SCM"), Subversion("Subversion");

    private final String displayName;

    public static RepositoryType fromDisplayName(String displayName) {
        
        for (RepositoryType repositoryType : RepositoryType.values() )
            if ( repositoryType.getDisplayName().equals(displayName) )
                return repositoryType;
        
        throw new IllegalArgumentException("Unkonwn " + RepositoryType.class.getSimpleName() + 
                " display name '" + displayName + "'");
    }
    
    private RepositoryType(String displayName) {
        
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

