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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.review_board.ereviewboard.ui.widget.CFileChooser;

/**
 * @author Markus Knittig
 *
 */
public class NewDiffPage extends WizardPage {

    private static final String TITLE = "New Diff";

    private static final String DESCRIPTION = "Enter data for new diff.";

    private Text baseDiffPathText;
    private CFileChooser diffFileChooser;
    private CFileChooser parentDiffFileChooser;

    protected NewDiffPage() {
        super(TITLE);

        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
        setControl(composite);

        GridDataFactory gridDataFactory = GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).copy();

        final Label baseDiffPathLabel = new Label(composite, SWT.NONE);
        baseDiffPathLabel.setText("Base Diff Path:");

        baseDiffPathText = new Text(composite, SWT.BORDER);
        gridDataFactory.applyTo(baseDiffPathText);
        baseDiffPathText.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                setPageComplete(isPageComplete());
            }
        });

        final Label diffLabel = new Label(composite, SWT.NONE);
        diffLabel.setText("Diff:");

        diffFileChooser = new CFileChooser(composite, SWT.NONE);
        gridDataFactory.applyTo(diffFileChooser);
        diffFileChooser.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                setPageComplete(isPageComplete());
            }
        });

        final Label parentDiffLabel = new Label(composite, SWT.NONE);
        parentDiffLabel.setText("Parent diff:");

        diffFileChooser = new CFileChooser(composite, SWT.NONE);
        gridDataFactory.applyTo(diffFileChooser);
    }

    @Override
    public boolean isPageComplete() {
        return (baseDiffPathText.getText().length() > 0) && (diffFileChooser.getFile() != null);
    }

    public String getBaseDiffPath() {
        return baseDiffPathText.getText();
    }

    public IFile getDiff() {
        return diffFileChooser.getFile();
    }

    public IFile getParentDiff() {
        return parentDiffFileChooser.getFile();
    }

}
