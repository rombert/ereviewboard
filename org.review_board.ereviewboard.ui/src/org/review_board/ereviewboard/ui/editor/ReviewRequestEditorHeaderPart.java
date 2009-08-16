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
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Markus Knittig
 *
 */
public class ReviewRequestEditorHeaderPart extends AbstractFormPagePart {

    private TaskEditor editor;

    private Composite parentComposite;

    private FormToolkit toolkit;

    private Text summaryText;

    private Label lblUpdatedWeek;

    public ReviewRequestEditorHeaderPart(TaskEditor editor) {
        this.editor = editor;
    }

    private ITask getTask() {
        return editor.getTaskEditorInput().getTask();
    }

    @Override
    public Control createControl(Composite parent, FormToolkit toolkit) {
        this.toolkit = toolkit;

        parentComposite = toolkit.createComposite(parent);
        toolkit.paintBordersFor(parentComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parentComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(parentComposite);

        createSummaryLabel();
        createSummaryText();
        createLastUpdatedLabel();

        Label label = toolkit.createSeparator(parent, SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

        return parentComposite;
    }

    private void createSummaryLabel() {
        Label lblSummary = toolkit.createLabel(parentComposite, "Summary:");
        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblSummary);
        lblSummary.setFont(titleFont);
        lblSummary.setForeground(attributeNameColor);
    }

    private void createSummaryText() {
        summaryText = toolkit.createText(parentComposite, getTask().getSummary());
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(600, 15).applyTo(
                summaryText);
        summaryText.setFont(titleFont);
    }

    private void createLastUpdatedLabel() {
        lblUpdatedWeek = toolkit.createLabel(parentComposite, NLS.bind("Updated on {0}", getTask()
                .getModificationDate()));
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).span(2, 1).applyTo(
                lblUpdatedWeek);
        lblUpdatedWeek.setForeground(attributeNameColor);
    }

    public String getSummary() {
        return summaryText.getText();
    }

    public void setSummary(String summary) {
        summaryText.setText(summary);
    }

}
