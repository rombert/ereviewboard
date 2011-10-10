package org.review_board.ereviewboard.core;

/**
 * @author Robert Munteanu
 *
 */
public enum TraceLocation {

    MAIN(""), SYNC("/sync");
    
    private final String _prefix;
    
    private TraceLocation(String prefix) {
        
        _prefix = prefix;
    }
    
    String getPrefix() {
        
        return _prefix;
    }
}
