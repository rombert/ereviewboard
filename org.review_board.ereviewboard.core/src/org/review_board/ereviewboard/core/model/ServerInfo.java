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
package org.review_board.ereviewboard.core.model;

import java.io.Serializable;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Robert Munteanu
 *
 */
public class ServerInfo implements Serializable {
    
    private static final Pattern DIGITS = Pattern.compile("(\\d)+");

    private final String productName;
    private final String productVersion;
    private final String productPackageVersion;
    private final boolean isRelease;
    private final TimeZone timeZone;
    
    public ServerInfo(String productName, String productVersion, String productPackageVersion,
            boolean isRelease, TimeZone timeZone) {
        this.productName = productName;
        this.productVersion = productVersion;
        this.productPackageVersion = productPackageVersion;
        this.isRelease = isRelease;
        this.timeZone = timeZone;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getProductPackageVersion() {
        return productPackageVersion;
    }

    public boolean isRelease() {
        return isRelease;
    }
    
    /**
     * @return the server's TimeZone or <code>null</code> if not supported by the current version
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public boolean isAtLeast(int majorVersion, int minorVersion) {
        
        String[] components = productVersion.split("\\.");
        int ourMajorVersion = Integer.parseInt(components[0]);
        Matcher matcher = DIGITS.matcher(components[1]);
        if ( !matcher.find() )
            throw new IllegalArgumentException("Unparseable version " + productVersion);
        
        int ourMinorVersion = Integer.parseInt(matcher.group(1));
        
        if ( ourMajorVersion > majorVersion )
            return true;
        else if ( ourMajorVersion < majorVersion )
            return false;
        
        if ( ourMinorVersion >= minorVersion )
            return true;
        
        return false;
    }
    
    
    
}
