package org.review_board.ereviewboard.core.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewTest {

    @Test
    public void hasShipItText() {
        
        assertThat(newReview("Ship It!").hasShipItText(), is(true));
        assertThat(newReview("Ship it!").hasShipItText(), is(true));
        assertThat(newReview("Ship it.").hasShipItText(), is(true));
        assertThat(newReview("Ship it").hasShipItText(), is(true));
    }

    private Review newReview(String bodyTop) {
        Review r = new Review();
        r.setBodyTop(bodyTop);
        return r;
    }
    
    @Test
    public void doesNotHaveShipItText() {
        
        assertThat(newReview("").hasShipItText(), is(false));
        assertThat(newReview("Good job").hasShipItText(), is(false));
        assertThat(newReview("Sheep it").hasShipItText(), is(false));
    }
}
