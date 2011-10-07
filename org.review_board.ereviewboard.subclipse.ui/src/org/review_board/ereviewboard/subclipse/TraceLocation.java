package org.review_board.ereviewboard.subclipse;


/**
 * @author Robert Munteanu
 *
 */
public enum TraceLocation {

    MAIN(""), DIFF("/diff");
    
    private final String _prefix;
    
    private TraceLocation(String prefix) {
        
        _prefix = prefix;
    }
    
    String getPrefix() {
        
        return _prefix;
    }
}
