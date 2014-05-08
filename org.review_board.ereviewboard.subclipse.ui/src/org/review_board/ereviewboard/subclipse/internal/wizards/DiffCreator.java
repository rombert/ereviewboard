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
package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * The <tt>DiffCreator</tt> creates ReviewBoard-compatible diffs
 * 
 * <p>Once specific problem with svn diff is that moved files have an incorrect header.</p>
 * 
 * @see <a href="https://github.com/reviewboard/rbtools/blob/release-0.3.4/rbtools/postreview.py#L1731">post-review handling of svn renames</a>
 * @author Robert Munteanu
 */
public class DiffCreator {

    private static final Pattern FILE_LINE = Pattern.compile("^(Index:|\\+{3}|-{3}) (.*?)(\\t.*)?$");

    public byte[] createDiff(Set<ChangedFile> selectedFiles, File rootLocation, ISVNClientAdapter svnClient) throws IOException, SVNClientException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        List<File> changes = new ArrayList<File>(selectedFiles.size());
        Map<String, String> copies = new HashMap<String, String>();
        for (ChangedFile changedFile : selectedFiles) {
            if (changedFile.getCopiedFromPathRelativeToProject() != null)
                copies.put(changedFile.getPathRelativeToProject(), changedFile.getCopiedFromPathRelativeToProject());
            changes.add(changedFile.getFile().getAbsoluteFile());
        }

        // TODO - new line between multiple files?
        // TODO - handle renames
        for (File file : changes) {

            File tmpFile = File.createTempFile("ereviewboard", ".diff");
            
            try {
                svnClient.diff(file, SVNRevision.BASE, file, SVNRevision.WORKING, tmpFile, false);
    
                List<String> patchLines = FileUtils.readLines(tmpFile);
    
                for (String line : patchLines) {
                    Matcher m = FILE_LINE.matcher(line);
    
                    if (m.find()) {
    
                        StringBuilder newLine = new StringBuilder();
    
                        newLine.append(m.group(1)).append(' ');
    
                        if (file.getCanonicalPath().startsWith(rootLocation.getCanonicalPath())) {
                            newLine.append(file.getCanonicalPath().substring(
                                    rootLocation.getCanonicalPath().length() + 1));
                        } else {
                            newLine.append(m.group(2));
                        }
                        if (m.group(3) != null) {
                            newLine.append(m.group(3));
                        }
    
                        line = newLine.toString();
                    }
    
    
                    outputStream.write(line.getBytes());
                    outputStream.write('\n');
                }
            } finally {

                FileUtils.deleteQuietly(tmpFile);
            }
        }

        return outputStream.toByteArray();
    }
}
