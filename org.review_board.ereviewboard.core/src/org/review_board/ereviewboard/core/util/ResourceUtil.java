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
package org.review_board.ereviewboard.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert Munteanu
 *
 */
public abstract class ResourceUtil {
    
    /**
     * Returns a list of paths which are likely candidates for workspace locations for a given originalPath
     * 
     * <p>Some VCSs, notably Subversion, map tags and branches and the head of the repository to sub-folders.
     * For instance, the file <tt>/project/directory/file.txt</tt> can be mapped as:
     * 
     * <ul>
     *   <li>/project/trunk/directory/file.txt</li>
     *   <li>/project/branches/release/directory/file.txt</li>
     *   <li>/trunk/project/directory/file.txt</li>
     *   <li>/branches/release/project/directory/file.txt</li>
     * </ul>
     * 
     * and in a similar manner for tags.</p>
     * 
     * <p>It is not usual to check out the project with the complete repository path. Instead, the SVN-specific parts
     * are not present. To find out which is the 'base' resource path, we try to filter these away and present
     * multiple possible permutations.<p>
     * 
     * @param originalPath
     * @return the list of permutations, containing at least the original path
     */
    public static List<String> getResourcePathPermutations(String originalPath) {
        
        List<String> paths = new ArrayList<String>(2);
        paths.add(originalPath);
        if ( originalPath.indexOf("/trunk/") != -1 )
            paths.add(originalPath.replace("/trunk", ""));
        handlePrefixedPath(originalPath, paths, "/branches/");
        handlePrefixedPath(originalPath, paths, "/tags/");
        return paths;
    }

    private static void handlePrefixedPath(String originalPath, List<String> paths, String prefix) {
        if ( originalPath.indexOf(prefix) != -1 ) {
            int start = originalPath.indexOf(prefix);
            int end = originalPath.indexOf('/', start + prefix.length());
            
            paths.add(originalPath.substring(0, start) + originalPath.substring(end));
        }
    }
    
    private ResourceUtil() {
        
    }

}
