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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.Policy;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.TraceLocation;
import org.review_board.ereviewboard.core.exception.ReviewboardException;

/**
 * The <tt>PagedLoader</tt> contains the logic for performing requests against a resource list which
 * requires pagination.
 * 
 * @author Robert Munteanu
 *
 * @param <T>
 */
public abstract class PagedLoader<T> {

    private final int increment;
    private final IProgressMonitor monitor;
    private final String progressMessage;
    private List<T> results = null;
    private int limit;
    
    public PagedLoader(int increment, IProgressMonitor monitor, String progressMessage) {
        
        this.increment = increment;
        this.monitor = monitor;
        this.progressMessage = progressMessage;
    }

    protected abstract PagedResult<T> doLoadInternal(int start, int maxResults, IProgressMonitor monitor) throws ReviewboardException;


    public List<T> doLoad() throws ReviewboardException {
        
        if ( results != null )
            throw new IllegalStateException("Cannot reuse a " + PagedLoader.class.getSimpleName());
        
        int currentPage = 0;
        int actualLimit = 0;
        int stillToLoad = 0;
        
        ReviewboardCorePlugin.getDefault().trace(TraceLocation.SYNC, this + " starting run.");
        
        while ( true ) {


            int adjustedIncrement = increment;

            // for the last call we typically don't need all the results
            if ( stillToLoad > 0 && stillToLoad < increment )
                adjustedIncrement = stillToLoad;

            // we perform the monitor work ourselves, so pass a NPM downstream            
            PagedResult<T> pagedUsers = doLoadInternal(currentPage * increment, adjustedIncrement, new NullProgressMonitor());
            
            ReviewboardCorePlugin.getDefault().trace(TraceLocation.SYNC, this + " page : " + currentPage + ": loaded " + pagedUsers.getResults().size() + " elements.");
            
            if ( results == null ) {
                results = new ArrayList<T>(pagedUsers.getTotalResults());
                monitor.beginTask(progressMessage, pagedUsers.getTotalResults());
                if ( limit == 0 )
                    actualLimit = pagedUsers.getTotalResults();
                else
                    actualLimit = Math.min(limit, pagedUsers.getTotalResults());
            }
            
            Policy.advance(monitor, pagedUsers.getResults().size());
            results.addAll(pagedUsers.getResults());
            
            currentPage++;
            
            stillToLoad = actualLimit - results.size();
            
            ReviewboardCorePlugin.getDefault().trace(TraceLocation.SYNC, this + " page : " + currentPage + ": still to load: " + stillToLoad);
            
            if ( stillToLoad == 0 )
                break;
            
            if ( stillToLoad < 0 ) {
                results.subList(results.size() + stillToLoad, results.size()).clear();
                break;
            }
        }
        
        ReviewboardCorePlugin.getDefault().trace(TraceLocation.SYNC, this + " completed run.");
        
        monitor.done();
        
        return results;
    }
    

    public void setLimit(int limit) {
        
        if ( limit <= 0 )
            throw new IllegalArgumentException("limit must be > 0");
        
        this.limit = limit;
        
    }
    
    @Override
    public String toString() {
        
        return getClass().getSimpleName() + " [ " + progressMessage + " ]";
    }
}
