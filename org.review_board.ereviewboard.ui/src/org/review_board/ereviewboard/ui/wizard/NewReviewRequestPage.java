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
package org.review_board.ereviewboard.ui.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.review_board.ereviewboard.core.client.ReviewboardClientData;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.ui.ReviewboardUiUtil;

/**
 * @author Markus Knittig
 *
 */
public class NewReviewRequestPage extends WizardPage {

    private static final String TITLE = "New Review Request";

    private static final String DESCRIPTION = "Enter data for new review request.";

    private Combo repositoryCombo;
    private Text changeNumText;

    private ReviewboardClientData clientData;

    public NewReviewRequestPage(ReviewboardClientData clientData) {
        super(TITLE);
        this.clientData = clientData;

        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
        setControl(composite);

        GridDataFactory gridDataFactory = GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).copy();

        final Label label = new Label(composite, SWT.NONE);
        label.setText("Repository:");

        final ComboViewer comboViewer = new ComboViewer(composite, SWT.BORDER | SWT.READ_ONLY);
        comboViewer.setContentProvider(new ArrayContentProvider());
        repositoryCombo = comboViewer.getCombo();
        gridDataFactory.applyTo(repositoryCombo);
        comboViewer.setInput(ReviewboardUiUtil.getStringList(clientData.getRepositories()));
        repositoryCombo.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                validateChangeNumberTextField();
                getContainer().updateButtons();
            }
        });

        final Label changeNumLabel = new Label(composite, SWT.NONE);
        changeNumLabel.setText("Change number:");

        changeNumText = new Text(composite, SWT.BORDER);
        gridDataFactory.applyTo(changeNumText);
        validateChangeNumberTextField();
    }

    @Override
    public boolean isPageComplete() {
        return repositoryCombo.getSelectionIndex() >= 0;
    }

    public ReviewRequest getReviewRequest() {
        ReviewRequest reviewRequest = new ReviewRequest();

        reviewRequest.setRepository(getSelectedRepository());
        if (changeNumText.getEnabled() && changeNumText.getText().length() > 0) {
            reviewRequest.setChangeNumber(Integer.parseInt(changeNumText.getText()));
        }

        return reviewRequest;
    }

    /**
     * Validates if the change number text field is activated. This is only the
     * case if the type of the choosen repository is "Perforce", otherwise the
     * text field is disabled.
     */
    private void validateChangeNumberTextField() {
        changeNumText.setEnabled(false);
        //sag 11/03/2009 avoid NPE
        Repository selectedRepo = getSelectedRepository();         
        if (selectedRepo!=null && 
            selectedRepo.getTool()!=null&&
            selectedRepo.getTool().equals("Perforce")) {
            changeNumText.setEnabled(true);
        }
    }

    private Repository getSelectedRepository() {
        if (repositoryCombo.getSelectionIndex() == -1) {
            return null;
        }
        return clientData.getRepositories().get(repositoryCombo.getSelectionIndex());
    }


}
