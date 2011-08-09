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
package org.review_board.ereviewboard.core.util;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Robert Munteanu
 */
public class ResourceUtilTest {

    @Test
    public void simplePathIsUnchanged() {
        
        String path = "/project/directory/file.txt";
        
        assertThat(ResourceUtil.getResourcePathPermutations(path), is(singletonList(path)));
    }
    
    @Test
    public void pathWithTrunkIsCleaned() {
        
        String path = "/project/trunk/directory/file.txt";
        String cleanPath = "/project/directory/file.txt";
        
        assertThat(ResourceUtil.getResourcePathPermutations(path), is(Arrays.asList(path, cleanPath)));
    }

    @Test
    public void pathWithBranchesIsCleaned() {
        
        String path = "/project/branches/release/directory/file.txt";
        String cleanPath = "/project/directory/file.txt";
        
        assertThat(ResourceUtil.getResourcePathPermutations(path), is(Arrays.asList(path, cleanPath)));
    }

    @Test
    public void pathWithTagsIsCleaned() {
        
        String path = "/project/tags/1.0/directory/file.txt";
        String cleanPath = "/project/directory/file.txt";
        
        assertThat(ResourceUtil.getResourcePathPermutations(path), is(Arrays.asList(path, cleanPath)));
    }
}
