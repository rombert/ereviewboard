/*******************************************************************************
 * Copyright (c) 2004, 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - adapted from MantisBT implementation
 *******************************************************************************/
package org.review_board.ereviewboard.ui.editor;

import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;

/**
 * @author Robert Munteanu
 * 
 */
@SuppressWarnings("restriction")
// implementation based on com.itsolut.mantis.ui.AbstractRichTextPart
public class RichTextPart extends TaskEditorRichTextPart {

    private String _key;

    public RichTextPart(ReviewboardAttributeMapper.Attribute attribute, boolean expandedByDefault) {

        setPartName(attribute.getDisplayName());
        setExpandVertically(false);

        if (!expandedByDefault)
            collapse();

        _key = attribute.toString();
    }

    @Override
    public void initialize(AbstractTaskEditorPage taskEditorPage) {

        super.initialize(taskEditorPage);

        TaskAttribute attribute = getTaskData().getRoot().getAttribute(_key);
        setAttribute(attribute);

        if ((attribute != null && attribute.getValue() != null && attribute.getValue().length() > 0))
            expand();

    }

    private void collapse() {

        setSectionStyle(getSectionStyle() & ~ExpandableComposite.EXPANDED);
    }
    
    private void expand() {
    	
    	setSectionStyle(getSectionStyle() | ExpandableComposite.EXPANDED);
    }

}
