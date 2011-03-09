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
package org.review_board.ereviewboard.core.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.review_board.ereviewboard.core.exception.ReviewboardException;

/**
 * 
 * @author Robert Munteanu
 *
 */
public class PagedLoaderTest {

    @Test(expected = IllegalStateException.class)
    public void reuseIsRejected() throws ReviewboardException {
        
        PagedLoader<String> loader = new PagedLoader<String>(1, new NullProgressMonitor(), "Dummy") {

            @Override
            protected PagedResult<String> doLoadInternal(int start, int maxResults,
                    IProgressMonitor monitor) throws ReviewboardException {
                return PagedResult.create(Arrays.asList("asd"), 1);
            }
        };
        
        loader.doLoad();
        loader.doLoad();
    }
    
    @Test
    public void loadFromSingleGo() throws ReviewboardException {
        
        final int[] callCount = new int[] { 0 };
        
        PagedLoader<String> loader = new PagedLoader<String>(1, new NullProgressMonitor(), "Dummy") {

            @Override
            protected PagedResult<String> doLoadInternal(int start, int maxResults,
                    IProgressMonitor monitor) throws ReviewboardException {
                
                callCount[0]++;
                
                return PagedResult.create(Arrays.asList("asd"), 1);
            }
        };
        
        List<String> results = loader.doLoad();
        
        assertThat(callCount[0], is(1));
        assertThat(results.size(), is(1));
        assertThat(results.get(0), is("asd"));
    }
    
    @Test
    public void loadFromTwoPasses() throws ReviewboardException {
        
        final int[] callCount = new int[] { 0 };
        
        PagedLoader<String> loader = new PagedLoader<String>(1, new NullProgressMonitor(), "Dummy") {

            @Override
            protected PagedResult<String> doLoadInternal(int start, int maxResults,
                    IProgressMonitor monitor) throws ReviewboardException {
             
                String element = callCount[0] == 0 ? "asd" : "def";

                assertThat("incorrect start", start, is(callCount[0]));
                assertThat("incorrect maxResults", maxResults, is(1));
                
                callCount[0]++;
                
                return PagedResult.create(Arrays.asList(element), 2);
            }
        };
        
        List<String> results = loader.doLoad();
        
        assertThat(callCount[0], is(2));
        assertThat(results.size(), is(2));
        assertThat(results.get(0), is("asd"));
        assertThat(results.get(1), is("def"));
    }
}
