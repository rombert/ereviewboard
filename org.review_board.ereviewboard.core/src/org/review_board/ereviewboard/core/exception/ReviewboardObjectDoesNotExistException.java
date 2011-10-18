package org.review_board.ereviewboard.core.exception;

import org.review_board.ereviewboard.core.client.ErrorCode;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardObjectDoesNotExistException extends ReviewboardApiException {
    
    public ReviewboardObjectDoesNotExistException(String message) {
        
        super(message, ErrorCode.OBJECT_DOES_NOT_EXIST.getErrorCode());
    }

}
