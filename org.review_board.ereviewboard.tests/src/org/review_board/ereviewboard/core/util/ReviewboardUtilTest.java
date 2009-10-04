package org.review_board.ereviewboard.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class ReviewboardUtilTest extends TestCase {

    public void testJoinListWithInteger() {
        List<Integer> list = Arrays.asList(new Integer[] { 1, 2, 3 });
        assertEquals("1, 2, 3", ReviewboardUtil.joinList(list));
    }

    private static class CustomClass {
        @Override
        public String toString() {
            return "bla";
        }
    }

    public void testJoinListWithCustomClass() {
        List<CustomClass> list = new ArrayList<CustomClass>();
        list.add(new CustomClass());
        list.add(new CustomClass());
        assertEquals("bla, bla", ReviewboardUtil.joinList(list));
    }

}
