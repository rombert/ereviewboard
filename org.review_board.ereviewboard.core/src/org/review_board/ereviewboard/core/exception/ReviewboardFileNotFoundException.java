package org.review_board.ereviewboard.core.exception;

import org.review_board.ereviewboard.core.client.ErrorCode;

/**
 * The <tt>ReviewboardFileNotFoundException</tt> signals that a file that was
 * referenced in an API call was not found
 *
 */
public class ReviewboardFileNotFoundException extends ReviewboardApiException {

    public ReviewboardFileNotFoundException() {

    }

    public ReviewboardFileNotFoundException(String fileName, String revision) {
        super("No file named '" + fileName + "' found in the repository at revision '" + revision
                + "'", ErrorCode.FILE_NOT_FOUND.getErrorCode());
    }

}
