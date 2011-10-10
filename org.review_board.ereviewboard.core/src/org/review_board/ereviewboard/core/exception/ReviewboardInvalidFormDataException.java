package org.review_board.ereviewboard.core.exception;

import java.util.List;
import java.util.Map;

import org.review_board.ereviewboard.core.client.ErrorCode;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Robert Munteanu
 * 
 */
public class ReviewboardInvalidFormDataException extends ReviewboardApiException {

    public ReviewboardInvalidFormDataException(Map<String, List<String>> fieldErrors) {
        super(toMessage(fieldErrors), ErrorCode.INVALID_FORM_DATA.getErrorCode());
    }

    private static String toMessage(Map<String, List<String>> fieldErrors) {

        StringBuilder errors = new StringBuilder();
        for (Map.Entry<String, List<String>> fieldError : fieldErrors.entrySet())
            errors.append(fieldError.getKey()).append(" : ").append(
                    ReviewboardUtil.joinList(fieldError.getValue())).append(". ");

        errors.delete(errors.length() - 1, errors.length());

        return errors.toString();
    }
}
