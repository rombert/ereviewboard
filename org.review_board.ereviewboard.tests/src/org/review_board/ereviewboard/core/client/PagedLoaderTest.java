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

import java.util.ArrayList;
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
    
    private static class StubPagedLoader extends PagedLoader<String> {

        public int callCount;
        private final int stubbedMaxResults;
        
        public StubPagedLoader(int increment, IProgressMonitor monitor, String progressMessage, int stubbedMaxResults) {
            super(increment, monitor, progressMessage);
            this.stubbedMaxResults = stubbedMaxResults;
        }

        @Override
        protected PagedResult<String> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException {
            
            List<String> result = new ArrayList<String>();
            for ( int i = 0 ; i < maxResults; i++ )
                result.add(callCount+"-"+ i);
            
            callCount++;
            
            return PagedResult.create(result, stubbedMaxResults);
        }
        
    }

    @Test(expected = IllegalStateException.class)
    public void reuseIsRejected() throws ReviewboardException {
        
        PagedLoader<String> loader = new StubPagedLoader(1, new NullProgressMonitor(), "Dummy", 10);        
        loader.doLoad();
        loader.doLoad();
    }
    
    @Test
    public void loadFromSingleGo() throws ReviewboardException {
        
        StubPagedLoader loader = new StubPagedLoader(10, new NullProgressMonitor(), "Dummy", 10);
        
        List<String> results = loader.doLoad();
        
        assertThat(loader.callCount, is(1));
        assertThat(results.size(), is(10));
        assertThat(results.get(0), is("0-0"));
    }
    
    @Test
    public void loadFromSingleGoWithResultsUnderIncrement() throws ReviewboardException {
        
        StubPagedLoader loader = new StubPagedLoader(10, new NullProgressMonitor(), "Dummy", 8);
        
        List<String> results = loader.doLoad();
        
        assertThat(loader.callCount, is(1));
        assertThat(results.size(), is(8));
        assertThat(results.get(0), is("0-0"));
    }
    
    @Test
    public void loadFromTwoPasses() throws ReviewboardException {
        
        StubPagedLoader loader = new StubPagedLoader(5, new NullProgressMonitor(), "Dummy", 10);
        
        List<String> results = loader.doLoad();
        
        assertThat(loader.callCount, is(2));
        assertThat(results.size(), is(10));
        assertThat(results.get(0), is("0-0"));
        assertThat(results.get(5), is("1-0"));
    }
    
    @Test
    public void loadWithLimit() throws ReviewboardException {
        
        StubPagedLoader loader = new StubPagedLoader(1, new NullProgressMonitor(), "Dummy", 2);
        
        loader.setLimit(1);
        
        List<String> results = loader.doLoad();
        
        assertThat(loader.callCount, is(1));
        assertThat(results.size(), is(1));
        assertThat(results.get(0), is("0-0"));
    }
    
    @Test
    public void loadWithLimitTwoPasses() throws ReviewboardException {
        
        StubPagedLoader loader = new StubPagedLoader(5, new NullProgressMonitor(), "Dummy", 10);
        
        loader.setLimit(7);
        
        List<String> results = loader.doLoad();
        
        assertThat(loader.callCount, is(2));
        assertThat(results.size(), is(7));
    }
    
    
    @Test
    public void loadWithLimitUnderIncrement() throws ReviewboardException {
        
        StubPagedLoader loader = new StubPagedLoader(50, new NullProgressMonitor(), "Dummy", 100);
        
        loader.setLimit(40);
        
        List<String> results = loader.doLoad();
        
        assertThat(loader.callCount, is(1));
        assertThat(results.size(), is(40));
    }
}
