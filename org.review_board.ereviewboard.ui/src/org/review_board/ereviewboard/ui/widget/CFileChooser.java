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
package org.review_board.ereviewboard.ui.widget;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @author Markus Knittig
 *
 */
public class CFileChooser extends Composite {

    private Text text;

    public CFileChooser(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));

        text = new Text(this, SWT.BORDER);
        text.setEnabled(false);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        text.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                notifyListeners(SWT.Modify, event);
            }
        });

        final Button browseButton = new Button(this, SWT.NONE);
        browseButton.setText("Browse...");
        browseButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FileDialog fileDialog = new FileDialog(getShell());
                fileDialog.setFilterExtensions(new String[] {
                        "Diffs/Patches", "All Files"
                });
                fileDialog.setFilterExtensions(
                        new String[] { "*.diff; *.patch", "*.*" });
                String result = fileDialog.open();
                if (result != null) {
                    text.setText(result);
                }
            }
        });
    }

    public IFile getFile() {
        if (text.getText() != null) {
            IPath path = new Path(text.getText());
            return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        }
        return null;
    }

}
