package org.review_board.ereviewboard.core.exception;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardHttpException extends ReviewboardException {

    public ReviewboardHttpException() {

    }

    public ReviewboardHttpException(String message) {
        super(message);
    }

    public ReviewboardHttpException(String message, Exception exception) {
        super(message, exception);
    }

}
