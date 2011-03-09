/*******************************************************************************
 * Copyright (c) 2004, 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author Robert Munteanu
 */
public class ReviewboardCommentMapperTest {

    @Test
    public void buildTextWithAllAttributes() {
        
        ReviewboardCommentMapper mapper = new ReviewboardCommentMapper();
        mapper.setHeading("heading");
        mapper.setTop("top");
        mapper.setBody("body");
        mapper.setBottom("bottom");
        
        assertThat(mapper.buildText().toString(), is("heading\n\ntop\n\nbody\n\nbottom"));
    }
    
    @Test
    public void buildTextMissingAttributes() {
        
        ReviewboardCommentMapper mapper = new ReviewboardCommentMapper();
        mapper.setHeading("heading");
        mapper.setBottom("bottom");
        
        assertThat(mapper.buildText().toString(), is("heading\n\nbottom"));
    }
    
    @Test
    public void buildTextWithOneAttribute() {
        
        ReviewboardCommentMapper mapper = new ReviewboardCommentMapper();
        mapper.setBody("body");
        
        assertThat(mapper.buildText().toString(), is("body"));
    }    
}
