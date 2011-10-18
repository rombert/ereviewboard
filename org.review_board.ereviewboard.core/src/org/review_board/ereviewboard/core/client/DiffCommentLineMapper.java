package org.review_board.ereviewboard.core.client;

import java.util.HashMap;
import java.util.Map;

import org.review_board.ereviewboard.core.model.DiffData;

/**
 * Maps ReviewBoard diff comments lines to lines in the new and old files 
 *
 */
public class DiffCommentLineMapper {
    
    private Map<Integer, int[]> _unifiedDiffLineMappings = new HashMap<Integer, int[]>();
    
    public DiffCommentLineMapper(DiffData diffData) {
        
        for ( DiffData.Chunk chunk : diffData.getChunks() )
            for ( DiffData.Line line : chunk.getLines() )
                _unifiedDiffLineMappings.put(line.getDiffRowNumber(), new int[] { line.getLeftFileRowNumber(), line.getRightFileRowNumber() });
        
    }

    /**
     * Returns a two-element array containing the line numbers of the old file and the new file
     * 
     * <p>If there is no match for the line number in the old or new file, -1 is returned.</p>
     * 
     * @param diffLineNumber the line number in the diff table
     * @return the line mappings, possibly null if the diffLineNumber is out of bounds
     */
    public int[] getLineMappings(int diffLineNumber) {
        
        return _unifiedDiffLineMappings.get(diffLineNumber);
    }

    public int getDiffMappingForOldFile(int oldFileLineNumber) {
        
        return getDiffMappingForIndex(oldFileLineNumber, 0);
    }
    
    public int getDiffMappingForNewFile(int newFileLineNumber) {
        
        return getDiffMappingForIndex(newFileLineNumber, 1);
    }
    

    private int getDiffMappingForIndex(int leftSideLineNumber, int index) {
        for ( Map.Entry<Integer, int[]> lineEntry : _unifiedDiffLineMappings.entrySet() )
            if ( lineEntry.getValue()[index] == leftSideLineNumber )
                return lineEntry.getKey().intValue();
        
        throw new IllegalArgumentException("No diff mapping entry for line " + leftSideLineNumber);
    }

}
