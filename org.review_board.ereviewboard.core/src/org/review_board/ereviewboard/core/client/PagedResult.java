package org.review_board.ereviewboard.core.client;

import java.util.List;

/**
 * Holds information about a returned list of entities and the total count
 * 
 * <p>
 * Many methods in the ReviewBoard Web API return a limited amount of results,
 * while holding the total results information. This class encapsulates this
 * concept.
 * </p>
 * 
 * @author Robert Munteanu
 * 
 * @param <T>
 */
public class PagedResult<T> {

    public static <T> PagedResult<T> create(List<T> result, int totalResults) {

        return new PagedResult<T>(result, totalResults);
    }

    private final List<T> result;
    private final int totalResults;

    private PagedResult(List<T> result, int totalResults) {
        this.result = result;
        this.totalResults = totalResults;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<T> getResults() {
        return result;
    }
}
