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

import junit.framework.TestCase;

/**
 * @author Robert Munteanu
 *
 */
public class ServerInfoTest extends TestCase{
    
    public void testIsVersionAtLeast() {
        
        ServerInfo info = new ServerInfo("ReviewBoard", "1.5.3.1", "1.5.3.1", true, null);
        
        assertTrue(info.isAtLeast(1, 5));
        assertFalse(info.isAtLeast(1, 6));
        assertTrue(info.isAtLeast(0, 8));
        assertFalse(info.isAtLeast(2, 0));
    }
    
    public void test16Version() {
        
        ServerInfo info = new ServerInfo("ReviewBoard", "1.6 alpha 0 (dev)", "1.6alpha0", false, null);
        assertTrue(info.isAtLeast(1, 5));
    }

}
