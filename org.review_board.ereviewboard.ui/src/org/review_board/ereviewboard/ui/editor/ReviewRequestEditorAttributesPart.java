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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.client.ReviewboardClientData;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.User;
import org.review_board.ereviewboard.core.util.ReviewboardUtil;

/**
 * @author Markus Knittig
 *
 */
public class ReviewRequestEditorAttributesPart extends AbstractFormPagePart {

    private Composite parentComposite;
    private FormToolkit toolkit;

    private Label sumbitterLabel;
    private Label reviewersLabel;
    private Text branchText;
    private Text groupsText;
    private Text bugsText;
    private Text peopleText;
    private Label changeNumLabel;
    private Label repositoryLabel;
    private Text descriptionText;
    private Text testingDoneText;

    private ReviewRequest reviewRequest;
    private ReviewboardClient client;
    private ReviewRequestEditorHeaderPart headerPart;

    public ReviewRequestEditorAttributesPart(ReviewRequest reviewRequest, ReviewboardClient client,
            ReviewRequestEditorHeaderPart headerPart) {
        this.reviewRequest = reviewRequest;
        this.client = client;
        this.headerPart = headerPart;
    }

    @Override
    public Control createControl(Composite parent, FormToolkit toolkit) {
        this.toolkit = toolkit;

        ExpandableComposite expandableComposite = toolkit.createExpandableComposite(parent,
                ExpandableComposite.TWISTIE);
        expandableComposite.setText("Attributes");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(expandableComposite);

        parentComposite = new Composite(expandableComposite, SWT.NONE);
        toolkit.adapt(parentComposite);
        toolkit.paintBordersFor(parentComposite);
        GridLayoutFactory.fillDefaults().numColumns(4).applyTo(parentComposite);

        expandableComposite.setClient(parentComposite);
        expandableComposite.setExpanded(true);

        createAttributeName("Submitter:");
        sumbitterLabel = createLabelAttribute();

        createAttributeName("Reviewers").setForeground(
                parentComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        reviewersLabel = createLabelAttribute();

        createAttributeName("Branch:");
        branchText = createTextAttribute();

        createAttributeName("Groups");
        groupsText = createTextAttribute();

        createAttributeName("Bugs closed:");
        bugsText = createTextAttribute();

        createAttributeName("People:");
        peopleText = createTextAttribute();

        createAttributeName("Change number:");
        changeNumLabel = createLabelAttribute();

        createAttributeName("Repository:");
        repositoryLabel = createLabelAttribute();

        descriptionText = createMultiTextAttribute("Description:");
        testingDoneText = createMultiTextAttribute("Testing done:");

        Button updateAttributesButton = toolkit.createButton(parentComposite,
                "Update attributes", SWT.NONE);
        updateAttributesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                try {
                    ReviewRequest reviewRequest = getInput();
                    client.updateReviewRequest(reviewRequest);
                    setInput(reviewRequest);
                } catch (ReviewboardException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                }
            }
        });
        new Label(expandableComposite, SWT.NONE);
        new Label(expandableComposite, SWT.NONE);
        new Label(expandableComposite, SWT.NONE);

        setInput(reviewRequest);

        return expandableComposite;
    }

    private Text createMultiTextAttribute(String name) {
        Label descriptionLabel = createAttributeName(name);
        GridDataFactory.fillDefaults().span(4, 1).applyTo(descriptionLabel);

        Text text = toolkit.createText(parentComposite, "", SWT.MULTI | SWT.WRAP
                | SWT.V_SCROLL);
        GridDataFactory.swtDefaults().span(4, 1).hint(700, 100).applyTo(text);

        return text;
    }

    private Label createAttributeName(String name) {
        Label label = toolkit.createLabel(parentComposite, name);
        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
        label.setForeground(attributeNameColor);
        return label;
    }

    private Label createLabelAttribute() {
        return toolkit.createLabel(parentComposite, "");
    }

    private Text createTextAttribute() {
        Text text = toolkit.createText(parentComposite, "");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
        return text;
    }

    public void setInput(ReviewRequest reviewRequest) {
        sumbitterLabel.setText(reviewRequest.getSubmitter().getUsername());
        branchText.setText(reviewRequest.getBranch());

        bugsText.setText(ReviewboardUtil.joinList(reviewRequest.getBugsClosed()));
        groupsText.setText(ReviewboardUtil.joinList(reviewRequest.getTargetGroups()));
        peopleText.setText(ReviewboardUtil.joinList(reviewRequest.getTargetPeople()));

        if (reviewRequest.getChangeNumber() == null) {
            changeNumLabel.setText("None");
        } else {
            changeNumLabel.setText(String.valueOf(reviewRequest.getChangeNumber()));
        }

        repositoryLabel.setText(reviewRequest.getRepository().getName());
        descriptionText.setText(reviewRequest.getDescription());
        testingDoneText.setText(reviewRequest.getTestingDone());

        headerPart.setSummary(reviewRequest.getSummary());
    }

    public ReviewRequest getInput() {
        ReviewRequest reviewRequest = ReviewboardUtil.cloneEntity(this.reviewRequest);

        reviewRequest.setBugsClosed(marshallBugsClosed(bugsText.getText()));
        reviewRequest.setTargetGroups(marshallTargetGroups(groupsText.getText()));
        reviewRequest.setTargetPeople(marshallTargetPeople(peopleText.getText()));

        reviewRequest.setBranch(branchText.getText());
        reviewRequest.setDescription(descriptionText.getText());
        reviewRequest.setTestingDone(testingDoneText.getText());

        reviewRequest.setSummary(headerPart.getSummary());

        return reviewRequest;
    }

    private List<Integer> marshallBugsClosed(String bugsClosed) {
        List<Integer> result = new ArrayList<Integer>();

        for (String bugClosed : bugsClosed.split(",")) {
            try {
                int bug = Integer.parseInt(bugClosed.trim());
                result.add(bug);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return result;
    }

    private List<User> marshallTargetPeople(String targetPeople) {
        List<User> result = new ArrayList<User>();
        int index;

        for (String username : targetPeople.split(",")) {
            index = client.getClientData().getUsers().indexOf(new User(username.trim()));
            if (index >= 0) {
                result.add(client.getClientData().getUsers().get(index));
            }
        }

        return result;
    }

    private List<ReviewGroup> marshallTargetGroups(String targetGroups) {
        List<ReviewGroup> result = new ArrayList<ReviewGroup>();
        int index;

        for (String group : targetGroups.split(",")) {
            index = client.getClientData().getGroups().indexOf(new ReviewGroup(group.trim()));
            if (index >= 0) {
                result.add(client.getClientData().getGroups().get(index));
            }
        }

        return result;
    }

}
