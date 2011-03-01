package org.review_board.ereviewboard.core.util;

import java.util.Arrays;
import java.util.List;

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

    public void testSplitString() {
        List<String> expected = Arrays.asList("foo", "bar");
        assertEquals(expected, ReviewboardUtil.splitString("foo, bar"));
    }

    public void testSplitStringWithWhitespace() {
        List<String> expected = Arrays.asList("foo", "bar");
        assertEquals(expected, ReviewboardUtil.splitString("foo , bar"));
    }
    
    public void testUnmaskNullWithNull() {
        
        assertEquals("", ReviewboardUtil.unmaskNull(null));
    }
    
    public void testUnmaskNullWithTextNull() {

        assertEquals("", ReviewboardUtil.unmaskNull("null"));
    }
    
    public void testUnmaskNullWithNotNull() {

        assertEquals("thisText", ReviewboardUtil.unmaskNull("thisText"));
    }

}
