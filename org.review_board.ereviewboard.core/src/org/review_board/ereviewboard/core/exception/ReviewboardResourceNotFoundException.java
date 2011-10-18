package org.review_board.ereviewboard.core.exception;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardResourceNotFoundException extends ReviewboardHttpException {

    public ReviewboardResourceNotFoundException() {
        super();
    }

    public ReviewboardResourceNotFoundException(String location) {
        super("No resource found at " + location);
    }
}
