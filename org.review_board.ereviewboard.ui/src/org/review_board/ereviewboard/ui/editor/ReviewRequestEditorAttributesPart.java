/*******************************************************************************
 * Copyright (c) 2004 - 2009 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers, Atlassian, Sven Krzyzak
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2009 Markus Knittig
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Markus Knittig - adapted Trac, Redmine & Atlassian implementations for
 *                      Review Board
 *******************************************************************************/
package org.review_board.ereviewboard.ui.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.User;

/**
 * @author Markus Knittig
 *
 */
public class ReviewRequestEditorAttributesPart extends AbstractFormPagePart {

    private Composite parentComposite;

    private FormToolkit toolkit;

    private ReviewRequest reviewRequest;

    public ReviewRequestEditorAttributesPart(ReviewRequest reviewRequest) {
        this.reviewRequest = reviewRequest;
    }

    @Override
    public Control createControl(Composite parent, FormToolkit toolkit) {
        this.toolkit = toolkit;

        ExpandableComposite expandableComposite = toolkit.createExpandableComposite(parent,
                ExpandableComposite.TWISTIE);
        expandableComposite.setText("Attributes");
        // GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL,
        // SWT.CENTER).applyTo(expandableComposite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(expandableComposite);

        parentComposite = new Composite(expandableComposite, SWT.NONE);
        toolkit.adapt(parentComposite);
        toolkit.paintBordersFor(parentComposite);
        GridLayoutFactory.fillDefaults().numColumns(4).applyTo(parentComposite);

        expandableComposite.setClient(parentComposite);
        expandableComposite.setExpanded(true);

        createAttributeName("Submitter:");
        if (reviewRequest.getSubmitter() == null) {
            createLabelAttribute("");
        } else {
            createLabelAttribute(reviewRequest.getSubmitter().getUsername());
        }

        Label reviewersLabel = createAttributeName("Reviewers");
        reviewersLabel.setForeground(parentComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        createLabelAttribute("");

        createAttributeName("Branch:");
        createTextAttribute(reviewRequest.getBranch());

        createAttributeName("Groups");
        String groups = "";
        for (ReviewGroup group : reviewRequest.getTargetGroups()) {
            groups += ", " + group.getName();
        }
        createTextAttribute(substract(groups));

        createAttributeName("Bugs:");
        String bugs = "";
        for (Integer bug : reviewRequest.getBugsClosed()) {
            bugs += ", " + bug;
        }
        createTextAttribute(substract(bugs));

        createAttributeName("People:");
        String people = "";
        for (User user : reviewRequest.getTargetUsers()) {
            people += ", " + user.getUsername();
        }
        createTextAttribute(substract(people));

        createAttributeName("Change Number:");
        if (reviewRequest.getChangeNumber() == null) {
            createLabelAttribute("None");
        } else {
            createLabelAttribute(String.valueOf(reviewRequest.getChangeNumber()));
        }

        createAttributeName("Repository:");
        if (reviewRequest.getRepository() == null) {
            createLabelAttribute("");
        } else {
            createLabelAttribute(reviewRequest.getRepository().getName());
        }

        createMultiTextAttribute("Description:", reviewRequest.getDescription());
        createMultiTextAttribute("Testing done:", reviewRequest.getTestingDone());

        return expandableComposite;
    }

    private String substract(String string) {
        if (string.length() > 2) {
            return string.substring(2);
        }
        return "";
    }

    private void createMultiTextAttribute(String name, String content) {
        Label descriptionLabel = createAttributeName(name);
        GridDataFactory.fillDefaults().span(4, 1).applyTo(descriptionLabel);

        Text text = toolkit.createText(parentComposite, content, SWT.MULTI | SWT.WRAP
                | SWT.V_SCROLL);
        GridDataFactory.swtDefaults().span(4, 1).hint(700, 100).applyTo(text);
    }

    private Label createAttributeName(String name) {
        Label label = toolkit.createLabel(parentComposite, name);
        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
        label.setForeground(attributeNameColor);
        return label;
    }

    private Label createLabelAttribute(String text) {
        return toolkit.createLabel(parentComposite, text);
    }

    private Text createTextAttribute(String content) {
        Text text = toolkit.createText(parentComposite, content);
        text.setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
        return text;
    }

}
