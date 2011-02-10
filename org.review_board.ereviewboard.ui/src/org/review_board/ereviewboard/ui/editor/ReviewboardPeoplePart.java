/*******************************************************************************
 * Copyright (c) 2004, 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - adapted from Bugzilla implementation
 *******************************************************************************/
package org.review_board.ereviewboard.ui.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;

/**
 * @author Robert Munteanu
 */
// implementation based on org.eclipse.mylyn.internal.bugzilla.ui.editor.BugzillaPeoplePart
class ReviewboardPeoplePart extends AbstractTaskEditorPart {

    public ReviewboardPeoplePart() {
        
        setPartName("People");
    }
    
    @Override
    public void createControl(Composite parent, FormToolkit toolkit) {
        
        Section section = createSection(parent, toolkit, true);
        Composite peopleComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 5;
        peopleComposite.setLayout(layout);

        addAttribute(peopleComposite, toolkit, getTaskData().getRoot().getMappedAttribute(TaskAttribute.USER_REPORTER), false);
        
        createReviewersLabel(toolkit, peopleComposite);
        
        addAttribute(peopleComposite, toolkit, getTaskData().getRoot().getAttribute(ReviewboardAttributeMapper.Attribute.TARGET_PEOPLE.toString()), true);
        addAttribute(peopleComposite, toolkit, getTaskData().getRoot().getAttribute(ReviewboardAttributeMapper.Attribute.TARGET_GROUPS.toString()), true);

        toolkit.paintBordersFor(peopleComposite);
        section.setClient(peopleComposite);
        setSection(toolkit, section);
    }

    public void createReviewersLabel(FormToolkit toolkit, Composite peopleComposite) {
        
        Label labelControl = toolkit.createLabel(peopleComposite, "Reviewers");
        labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        GridDataFactory.defaultsFor(labelControl).indent(5, 0).span(2, 1).applyTo(labelControl);
    }    
    
    private void addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute, boolean extraIndent) {
        AbstractAttributeEditor editor = createAttributeEditor(attribute);
        
        if ( editor == null )
            return;

        editor.createLabelControl(composite, toolkit);
        GridDataFactory.defaultsFor(editor.getLabelControl()).indent(extraIndent ? 10 : 5 , 0).applyTo( editor.getLabelControl());
        editor.createControl(composite, toolkit);
        getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).hint(130, SWT.DEFAULT).applyTo(editor.getControl());
    }
    


}
