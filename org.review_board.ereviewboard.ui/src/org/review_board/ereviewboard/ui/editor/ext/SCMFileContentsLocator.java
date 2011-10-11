package org.review_board.ereviewboard.ui.editor.ext;

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
