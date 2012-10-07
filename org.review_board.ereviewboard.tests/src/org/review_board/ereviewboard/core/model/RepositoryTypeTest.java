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

import junit.framework.TestCase;

public class RepositoryTypeTest extends TestCase {

    public void testClearCase() {
        
        assertEquals(RepositoryType.fromDisplayName("ClearCase"), RepositoryType.ClearCase);
        assertEquals(RepositoryType.fromDisplayName("Clear Case"), RepositoryType.ClearCase);
    }
    
    public void testSubversion() {
        
        assertEquals(RepositoryType.fromDisplayName("Subversion"), RepositoryType.Subversion);
    }
}
