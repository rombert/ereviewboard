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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskFormPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardRepositoryConnector;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;

/**
 * @author Markus Knittig
 *
 */
public class ReviewRequestEditorPage extends TaskFormPage {

    private TaskEditor editor;

    private ScrolledForm form;

    private FormToolkit toolkit;

    private Composite editorComposite;

    private List<AbstractFormPagePart> parts;

    private ReviewRequest reviewRequest;

    private ReviewboardClient client;

    public ReviewRequestEditorPage(TaskEditor editor, String title) {
        super(editor, ReviewboardCorePlugin.REPOSITORY_KIND, title);
        this.editor = editor;
        parts = new ArrayList<AbstractFormPagePart>();

        ReviewboardRepositoryConnector connector = ReviewboardCorePlugin.getDefault()
                .getConnector();
        client = connector.getClientManager().getClient(getTaskRepository());
    }

    @Override
    public TaskEditor getEditor() {
        return (TaskEditor) super.getEditor();
    }

    private ITask getTask() {
        return getEditor().getTaskEditorInput().getTask();
    }

    private TaskRepository getTaskRepository() {
        return getEditor().getTaskEditorInput().getTaskRepository();
    }

    @Override
    protected void fillToolBar(IToolBarManager toolBarManager) {
        // TODO Auto-generated method stub
        super.fillToolBar(toolBarManager);
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        form = managedForm.getForm();
        toolkit = managedForm.getToolkit();
        editorComposite = form.getBody();

        // TODO consider using TableWrapLayout, it makes resizing much faster
        GridLayout editorLayout = new GridLayout();
        editorComposite.setLayout(editorLayout);
        editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                downloadReviewRequestAndRefresh();
            }
        });
    }

    private void createFormParts() {
        ReviewRequestEditorHeaderPart headerPart = new ReviewRequestEditorHeaderPart(editor);
        parts.add(headerPart);
        if (reviewRequest != null) {
            parts.add(new ReviewRequestEditorAttributesPart(reviewRequest, client, headerPart));
        }
    }

    private void clearFormContent() {
        for (AbstractFormPagePart part : parts) {
            getManagedForm().removePart(part);
        }
        parts.clear();

        // remove all of the old widgets so that we can redraw the editor
        for (Control child : editorComposite.getChildren()) {
            child.dispose();
        }
    }

    private void setBusy(boolean busy) {
        getEditor().showBusy(busy);
    }

    private void downloadReviewRequestAndRefresh() {
        Job job = new Job(NLS.bind("Retrieving review request {0}...", getTask().getTaskId())) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.subTask("Retrieving review request");
                try {
                    reviewRequest = client.getReviewRequest(Integer.valueOf(getTask().getTaskId()));
                } catch (Exception e) {
                    return new Status(IStatus.ERROR, ReviewboardUiPlugin.PLUGIN_ID, e.getMessage());
                }
                return new Status(IStatus.OK, ReviewboardUiPlugin.PLUGIN_ID, null);
            }
        };
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        reviewRequestUpdateCompleted();
                    }
                });
            }
        });
        job.schedule();
        setBusy(true);
    }

    private void reviewRequestUpdateCompleted() {
        setBusy(false);
        createInitialFormContent();
    }

    private void createInitialFormContent() {
        clearFormContent();
        createFormParts();

        for (AbstractFormPagePart part : parts) {
            getManagedForm().addPart(part);
            part.initialize(getManagedForm());
            part.createControl(editorComposite, toolkit);
        }

        form.layout(true, true);
        form.reflow(true);
        form.setRedraw(true);
    }

}
