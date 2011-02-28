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
package org.review_board.ereviewboard.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.ui.PlatformUI;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;

/**
 * @author Markus Knittig
 * 
 */
public final class ReviewboardUiUtil {

    private ReviewboardUiUtil() {
        super();
    }

    public static void selectDefaultComboItem(ComboViewer comboViewer) {
        if (comboViewer.getCombo().getItemCount() > 0 && comboViewer.getSelection().isEmpty()) {
            comboViewer.getCombo().select(0);
        }
    }
    
    public static void selectComboItemByValue(ComboViewer comboViewer, String value) {

        for (int i = 0; i < comboViewer.getCombo().getItemCount(); i++) {
            if (comboViewer.getCombo().getItem(i).equals(value)) {
                comboViewer.getCombo().select(i);
                break;
            }
        }
    }

    /**
     * Refreshes the repository data using the specified <tt>runnableContext</tt>
     * 
     * <p>If the <tt>runnableConext</tt> is null, a platform service will be used to indicate
     * that the runnable is executing.</p>
     * 
     * @param client the client, must not be <code>null</code>
     * @param runnableContext the runnable context, possibly <code>null</code>
     */
    public static void refreshRepositoryData(final ReviewboardClient client, final boolean force,
            IRunnableContext runnableContext) {
        try {
            IRunnableWithProgress runnable = new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                        InterruptedException {
                    try {
                        client.updateRepositoryData(force, monitor);
                    } catch (ReviewboardException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            };

            if (runnableContext != null)
                runnableContext.run(true, true, runnable);
            else
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            return;
        }
    }
}
