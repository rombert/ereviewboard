package org.review_board.ereviewboard.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.review_board.ereviewboard.core.ReviewboardConstants;

import junit.framework.TestCase;

public class ReviewboardUtilTest extends TestCase {

    public void testGetReviewRequestUrl() {
        String expected = "foobar" + ReviewboardConstants.REVIEW_REQUEST_URL + "1";
        assertEquals(expected, ReviewboardUtil.getReviewRequestUrl("foobar", "1"));
        assertEquals(expected, ReviewboardUtil.getReviewRequestUrl("foobar/", "1"));
    }

    public void testCloneEntitySimple() {
        CustomClass testObject = buildCustomClassObject();
        assertEquals(testObject, ReviewboardUtil.cloneEntity(testObject));
    }

    public void testCloneEntityDeep() {
        CustomClass deepTestObject = buildCustomClassObject();
        CustomClass testObject = new CustomClass();
        testObject.setCustomObject(deepTestObject);
        assertEquals(testObject.getCustomObject(),
                ReviewboardUtil.cloneEntity(testObject).getCustomObject());
    }

    private CustomClass buildCustomClassObject() {
        CustomClass testObject = new CustomClass();
        testObject.setInteger(123);
        testObject.setString("abc");
        return testObject;
    }

    public void testJoinListWithInteger() {
        List<Integer> list = Arrays.asList(new Integer[] { 1, 2, 3 });
        assertEquals("1, 2, 3", ReviewboardUtil.joinList(list));
    }

    public void testJoinListWithCustomClass() {
        List<CustomClass> list = new ArrayList<CustomClass>();
        list.add(new CustomClass());
        list.add(new CustomClass());
        assertEquals("bla, bla", ReviewboardUtil.joinList(list));
    }

    public void testSplitString() {
        List<String> expected = Arrays.asList("foo", "bar");
        assertEquals(expected, ReviewboardUtil.splitString("foo, bar"));
    }

    public void testSplitStringWithWhitespace() {
        List<String> expected = Arrays.asList("foo", "bar");
        assertEquals(expected, ReviewboardUtil.splitString("foo , bar"));
    }
    
    public void unmaskNullWithNull() {
        
        assertEquals("", ReviewboardUtil.unmaskNull(null));
    }
    
    public void unmaskNullWithTextNull() {

        assertEquals("", ReviewboardUtil.unmaskNull("null"));
    }
    
    public void unmaskNullWithNotNull() {

        assertEquals("thisText", ReviewboardUtil.unmaskNull("thisText"));
    }

    private static class CustomClass implements Serializable {
        private String string;
        private int integer;
        private CustomClass customObject;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public int getInteger() {
            return integer;
        }

        public void setInteger(int integer) {
            this.integer = integer;
        }

        public CustomClass getCustomObject() {
            return customObject;
        }

        public void setCustomObject(CustomClass customObject) {
            this.customObject = customObject;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(integer)
                    .append(string)
                    .toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CustomClass) {
                CustomClass toCompare = (CustomClass) obj;
                return new EqualsBuilder()
                        .append(integer, toCompare.getInteger())
                        .append(string, toCompare.getString())
                        .isEquals();
            }
            return false;
        }

        @Override
        public String toString() {
            return "bla";
        }
    }

}
