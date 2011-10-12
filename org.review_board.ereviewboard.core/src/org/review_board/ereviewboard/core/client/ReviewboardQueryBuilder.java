package org.review_board.ereviewboard.core.client;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardQueryBuilder {
    
    public static final String PATH_INFO = "info";

    public static final String PATH_REVIEW_REQUESTS = "review-requests";

    public static final String PATH_SCREENSHOTS = "screenshots";

    public static final String PATH_REVIEWS = "reviews";

    public static final String PATH_REPLIES = "replies";

    public static final String PATH_DIFF_COMMENTS = "diff-comments";

    public static final String PATH_SCREENSHOT_COMMENTS = "screenshot-comments";

    public static final String PATH_DIFFS = "diffs";

    public static final String PATH_FILES = "files";
    
    public static final String PATH_REPOSITORIES = "repositories";

    public static final String PATH_USERS = "users";

    public static final String PATH_GROUPS = "groups";

    public static final String PATH_DRAFT = "draft";

    private final QueryBuilder _queryBuilder;
    
    public ReviewboardQueryBuilder() {
        
        this("/api");
    }
    
    public ReviewboardQueryBuilder(String existingQuery) {
        
        _queryBuilder = QueryBuilder.fromString(existingQuery);
    }

    public ReviewboardQueryBuilder descend(String path, String value) {
        
        _queryBuilder.append('/').append(path).append('/').append(value);
        
        return this;
    }

    public ReviewboardQueryBuilder descend(String path, int value) {
        
        _queryBuilder.append('/').append(path).append('/').append(value);
        
        return this;
    }
    
    public ReviewboardQueryBuilder descend(String path) {
        
        _queryBuilder.append('/').append(path).append('/');
        
        return this;
    }
    
    public ReviewboardQueryBuilder paginate(int start, int maxResults) {
        
        _queryBuilder.setParameter("start", start).setParameter("max-results", maxResults);
        
        return this;
    }
    
    public ReviewboardQueryBuilder countsOnly() {
        
        _queryBuilder.setParameter("counts-only", 1);
        
        return this;
    }
    
    public String createQuery() {

        _queryBuilder.ensureTrailingSlash();
        
        return _queryBuilder.createQuery();
    }
}
