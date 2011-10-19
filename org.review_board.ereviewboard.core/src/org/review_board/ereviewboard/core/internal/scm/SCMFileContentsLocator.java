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
import org.review_board.ereviewboard.core.model.Repository;

/**
 * The <tt>SCMFileContentsLocator</tt> is able to lookup the contents of a remote file at
 * a specified revision from a <tt>SCM</tt> system.
 * 
 * @author Robert Munteanu
 *
 */
public interface SCMFileContentsLocator {

    /**
     * @param codeRepository the detected code repository, possibly <code>null</code>
     * @param filePath the file path
     * @param revision the file revision, <code>null</code> if the file does not exist in the <tt>SCM</tt> system
     */
    void init(Repository codeRepository, String filePath, String revision);
    
    boolean isEnabled();
    
    byte[] getContents(IProgressMonitor monitor) throws CoreException;
}
