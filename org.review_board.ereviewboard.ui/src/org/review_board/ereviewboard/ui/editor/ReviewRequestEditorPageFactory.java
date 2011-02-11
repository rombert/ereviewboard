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

import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;

/**
 * @author Markus Knittig
 *
 */
public class ReviewRequestEditorPageFactory extends AbstractTaskEditorPageFactory {

    private static final String TITLE = "Review Request";

    @Override
    public boolean canCreatePageFor(TaskEditorInput input) {
        if (input.getTask().getConnectorKind().equals(ReviewboardCorePlugin.REPOSITORY_KIND)) {
            return true;
        } else if (TasksUiUtil.isOutgoingNewTask(input.getTask(),
                ReviewboardCorePlugin.REPOSITORY_KIND)) {
            return true;
        }
        return false;
    }

    @Override
    public int getPriority() {
        return PRIORITY_TASK;
    }

    @Override
    public Image getPageImage() {
        return null;
    }

    @Override
    public String getPageText() {
        return TITLE;
    }

    @Override
    public IFormPage createPage(TaskEditor parentEditor) {
        return new ReviewRequestEditorPage(parentEditor, TITLE);
    }

    @Override
    public String[] getConflictingIds(TaskEditorInput input) {
        
        return new String[] { ITasksUiConstants.ID_PAGE_PLANNING };
    }
}
