package org.review_board.ereviewboard.core.client;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

/**
 * @author Robert Munteanu
 */
public class QueryBuilderTest {
    
    private static final String TEST_QUERY_STRING = "/base/info?p1=true&p2=false";

    @Test
    public void buildQuery() {
        
        String query = new QueryBuilder("/base/").append("info").
                setParameter("p1", "true").setParameter("p2", "false").createQuery();
        
        assertThat(query, CoreMatchers.is(TEST_QUERY_STRING));
        
    }
    
    @Test
    public void parseString() {
        
        assertThat(QueryBuilder.fromString(TEST_QUERY_STRING).createQuery(), CoreMatchers.is(TEST_QUERY_STRING));
    }

    @Test
    public void parseStringWithReplace() {
        
        assertThat(QueryBuilder.fromString(TEST_QUERY_STRING).setParameter("p1", "false").createQuery(), 
                CoreMatchers.is("/base/info?p1=false&p2=false"));
    }
    
    @Test
    public void parseStringWithNoQuery() {
        
        assertThat(QueryBuilder.fromString("/base/info").createQuery(), CoreMatchers.is("/base/info"));
    }
    
    @Test
    public void clearParameters() {
        
        assertThat(QueryBuilder.fromString(TEST_QUERY_STRING).clearParameters().createQuery(), CoreMatchers.is("/base/info"));
    }

}
