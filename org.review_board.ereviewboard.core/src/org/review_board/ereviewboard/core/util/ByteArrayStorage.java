package org.review_board.ereviewboard.core.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;

/**
 * @author Robert Munteanu
 *
 */
public class ByteArrayStorage implements IStorage {
    
    private final byte[] diff;

    public ByteArrayStorage(byte[] diff) {
        this.diff = diff;
    }

    public Object getAdapter(Class adapter) {
        return null;
    }

    public boolean isReadOnly() {
        return true;
    }

    public String getName() {
        return null;
    }

    public IPath getFullPath() {
        return null;
    }

    public InputStream getContents()  {
        return new ByteArrayInputStream(diff);
    }
}