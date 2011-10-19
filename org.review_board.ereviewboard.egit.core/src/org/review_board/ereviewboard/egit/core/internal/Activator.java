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
package org.review_board.ereviewboard.egit.core.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * @author Robert Munteanu
 * 
 */
public class Activator extends Plugin {

    public static final String PLUGIN_ID = "org.review_board.ereviewboard.egit.core";

    private static volatile Activator DEFAULT;

    public void start(BundleContext context) throws Exception {

        super.start(context);

        DEFAULT = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        DEFAULT = null;
    }

    public static Activator getDefault() {

        return DEFAULT;
    }

    public void trace(TraceLocation location, String message) {

        if (!Platform.inDebugMode())
            return;

        String debugOption = Platform.getDebugOption(PLUGIN_ID + "/debug" + location.getPrefix());
        
        if ( !Boolean.parseBoolean(debugOption) ) 
            return;

        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    /**
     * 
     * @param severity one of the {@link IStatus} severity constants
     * @param message
     * @param cause the cause, can be <code>null</code>
     * 
     * @see #log(int, String)
     */
    public void log(int severity, String message, Throwable cause) {
        
        getLog().log(new Status(severity, PLUGIN_ID, message, cause));
    }
    
    /**
     * 
     * @param severity one of the {@link IStatus} severity constants
     * @param message 
     */
    public void log(int severity, String message) {
        
        log(severity, message, null);
    }
}
