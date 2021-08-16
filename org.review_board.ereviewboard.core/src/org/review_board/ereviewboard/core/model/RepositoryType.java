/*******************************************************************************
 * Copyright (c) 2004, 2012 Robert Munteanu and others.
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
 * 
 * @author Robert Munteanu
 *
 */
public enum RepositoryType {

    Bazaar("Bazaar"), VersionVault("VersionVault"), ClearCase("ClearCase") {
        @Override
        protected boolean matchesDisplayName(String displayName) {
            return super.matchesDisplayName(displayName) || "Clear Case".equals(displayName);
        }
    }, CVS("CVS"), Git("Git"), Mercurial("Mercurial"), Perforce("Perforce"), PerforceNetApp("Perforce NetApp"), PerforceVMWare("Perforce (VMware)"), PlasticSCM("Plastic SCM"), Subversion("Subversion");

    private final String displayName;

    public static RepositoryType fromDisplayName(String displayName) {
        
        for (RepositoryType repositoryType : RepositoryType.values() )
            if ( repositoryType.matchesDisplayName(displayName) )
                return repositoryType;
        
        throw new IllegalArgumentException("Unknown " + RepositoryType.class.getSimpleName() + 
                " display name '" + displayName + "'");
    }
    
    private RepositoryType(String displayName) {
        
        this.displayName = displayName;
    }
    
    protected boolean matchesDisplayName(String displayName) {
        
        return Objects.equal(displayName, this.displayName);
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

