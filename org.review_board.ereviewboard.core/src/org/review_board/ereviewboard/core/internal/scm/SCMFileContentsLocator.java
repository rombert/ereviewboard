/*******************************************************************************
 * Copyright (c) 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.core.internal.scm;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;

/**
 * The <tt>SCMFileContentsLocator</tt> is able to lookup the contents of a remote file at
 * a specified revision from a <tt>SCM</tt> system.
 * 
 * <p>The methods are invoked as follows:</p>
 * 
 * <ol>
 *   <li>the {@linkplain #init(Repository, String, String)}} method is invoked</li>
 *   <li>the {@linkplain #isEnabled()} method is invoked</li>
 *   <li>if the {@linkplain #isEnabled()} method returns true, the {@linkplain #getContents(IProgressMonitor)}
 *   method is invoked</li>
 * </ol>
 * 
 * @author Robert Munteanu
 */
public interface SCMFileContentsLocator {

    /**
     * @param codeRepository the detected code repository, possibly <code>null</code>
     * @param filePath the file path in the SCM repository
     * @param revision the file revision, possibly {@link FileDiff#PRE_CREATION}
     */
    void init(Repository codeRepository, String filePath, String revision);
    
    /**
     * @return true if this instance can retrieve the file contents from the parameters
     * given to {@link #init(Repository, String, String)}
     */
    boolean isEnabled();
    
    /**
     * Gets the file contents from SCM 
     * 
     * @param monitor
     * @return the contents of the file retrieved from SCM, never <code>null</code>
     * @throws CoreException if the contents can not be retrieved
     */
    byte[] getContents(IProgressMonitor monitor) throws CoreException;
}
