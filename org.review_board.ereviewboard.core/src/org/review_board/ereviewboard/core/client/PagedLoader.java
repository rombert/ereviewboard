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
        
        while ( true ) {
            // we perform the monitor work ourselves, so pass a NPM downstream
            PagedResult<T> pagedUsers = doLoadInternal(currentPage * increment, increment, new NullProgressMonitor());
            if ( results == null ) {
                results = new ArrayList<T>(pagedUsers.getTotalResults());
                monitor.beginTask(progressMessage, pagedUsers.getTotalResults());
            }
            
            Policy.advance(monitor, pagedUsers.getResults().size());
            
            results.addAll(pagedUsers.getResults());
            currentPage++;
            
            if ( results.size() == pagedUsers.getTotalResults())
                break;
        }
        
        monitor.done();
        
        return results;
    }
}
