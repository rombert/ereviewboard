package org.review_board.ereviewboard.core.client;

import  org.eclipse.compare.patch.IHunk;

/**
 * Maps ReviewBoard diff comments lines to lines in the new and old files 
 * 
 * <p>ReviewBoard diff comments are mapped to the virtual diff table which is shown in the web
 * interface, not to a location in the old or new file.</p>
 * 
 * <p>Take for instance the following side-by-side comparison:</p>
 * 
 * <pre>
 * 
 * 1: This is the first line     This is the first line
 * 2: This is the second line
 * 3:                            This is the third line
 * 4: This is the fourth line    
 * 4: This is the fifth line     This is the fifth line ( adjusted )
 * </pre>
 * 
 * <p>The line mappings are the following:</p>
 * 
 * <table border="1">
 * <tr><th>Unified diff</th><th>Old file</th><th>New file</th></tr>
 * <tr><td>1</td><td>1</td><td>1</td></tr>
 * <tr><td>2</td><td>2</td><td>N/A</td></tr>
 * <tr><td>3</td><td>N/A</td><td>2</td></tr>
 * <tr><td>4</td><td>3</td><td>N/A</td></tr>
 * <tr><td>5</td><td>4</td><td>3</td></tr>
 * </table>
 * 
 * @author Robert Munteanu
 *
 */
public class DiffCommentLineMapper {
    
    public DiffCommentLineMapper(IHunk[] hunks) {
        
        
    }

    /**
     * Returns a two-element array containing the line numbers of the old file and the new file
     * 
     * <p>If there is no match for the line number in the old or new file, -1 is returned.</p>
     * 
     * @param diffLineNumber the line number in the diff table
     * @return the line mappings, never null
     */
    public int[] getLineMappings(int diffLineNumber) {
        
        return new int[] { diffLineNumber, diffLineNumber };
    }

}
