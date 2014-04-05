package org.review_board.ereviewboard.core.client;


/**
 * @author Robert Munteanu
 *
 */
public enum ErrorCode {

    OBJECT_DOES_NOT_EXIST(100), INVALID_FORM_DATA(105), FILE_NOT_FOUND(207);

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
