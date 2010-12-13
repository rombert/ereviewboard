/*******************************************************************************
 * Copyright (c) 2004 - 2009 Robert Munteanu (robert.munteanu@gmail.com)
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.review_board.ereviewboard.core.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Robert Munteanu
 * 
 */
public final class IOUtil {

    /**
     * Closes the <tt>closeable</tt>, ignoring any {@link IOException}s which
     * occur
     * 
     * @param closeable the object to close, possibly <code>null</code>
     */
    public static void closeSilently(Closeable closeable) {

        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            // ignored
        }
    }

    private IOUtil() {
    }

}
