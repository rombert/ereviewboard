package org.review_board.ereviewboard.ui.util;


/**
 * @author Robert Munteanu
 *
 */
public abstract class Labels {

    public static String commentsAndDrafts(int commentCount, int draftCount) {
        
        if ( commentCount == 0 && draftCount == 0)
            return "";
        
        StringBuilder builder = new StringBuilder();
        if ( commentCount > 0 ) {
            builder.append(commentCount).append(" comments");
            if ( draftCount > 0 )
                builder.append(", ").append(draftCount).append(" drafts ");
        } else { // draftCount > 0
            builder.append(draftCount).append(" drafts");
        }
            
        return builder.toString();
    }
    
    private Labels() {
        
    }
    
    
}
