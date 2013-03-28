package org.review_board.ereviewboard.core.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.review_board.ereviewboard.core.ReviewboardConstants;

public class ReviewboardUtilTest extends TestCase {

    public void testGetReviewRequestUrl() {
        String expected = "foobar" + ReviewboardConstants.REVIEW_REQUEST_URL + "1";
        assertEquals(expected, ReviewboardUtil.getReviewRequestUrl("foobar", "1"));
        assertEquals(expected, ReviewboardUtil.getReviewRequestUrl("foobar/", "1"));
    }

    public void testJoinListWithInteger() {
        List<Integer> list = Arrays.asList(new Integer[] { 1, 2, 3 });
        assertEquals("1, 2, 3", ReviewboardUtil.joinList(list));
    }
    
    public void testMarshallDate_1_6() {
        
        assertEquals(1282962378000l, toGmtMillis(ReviewboardUtil.marshallDate("2010-08-28 02:26:18.474")));
    }

    public void testMarshallDate_1_7() {
        
        assertEquals(1282962378000l, toGmtMillis(ReviewboardUtil.marshallDate("2010-08-28T02:26:18.474Z")));
    }
    
    private static long toGmtMillis(Date date) {
        
       return TimeZone.getDefault().getOffset(date.getTime()) + date.getTime();
    }
}
