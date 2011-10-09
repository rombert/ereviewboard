package org.review_board.ereviewboard.core.exception;

/**
 * Signals that an error has been returned by the Reviewboard instance
 * 
 * @author Robert Munteanu
 */
public class ReviewboardApiException extends ReviewboardException {

    private int _code;

    public ReviewboardApiException() {

    }

    public ReviewboardApiException(String message, int code) {
        super(message);

        _code = code;
    }

    public int getCode() {
     
        return _code;
    }

}
