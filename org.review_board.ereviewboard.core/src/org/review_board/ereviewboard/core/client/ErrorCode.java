package org.review_board.ereviewboard.core.client;


/**
 * @author Robert Munteanu
 *
 */
public enum ErrorCode {

    INVALID_FORM_DATA(105);

    private final int _errorCode;

    private ErrorCode(int errorCode) {

        _errorCode = errorCode;
    }
    
    public boolean is(int errorCode) {
        
        return _errorCode == errorCode;
    }

    public int getErrorCode() {
        return _errorCode;
    }

}
