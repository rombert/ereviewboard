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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardRepositoryConnector;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.client.ReviewboardClientData;
import org.review_board.ereviewboard.core.model.AllReviewRequestQuery;
import org.review_board.ereviewboard.core.model.FromUserReviewRequestQuery;
import org.review_board.ereviewboard.core.model.GroupReviewRequestQuery;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryReviewRequestQuery;
import org.review_board.ereviewboard.core.model.ReviewRequestQuery;
import org.review_board.ereviewboard.core.model.ReviewRequestStatus;
import org.review_board.ereviewboard.core.model.StatusReviewRequestQuery;
import org.review_board.ereviewboard.core.model.ToUserReviewRequestQuery;
import org.review_board.ereviewboard.ui.ReviewboardUiUtil;

/**
 * @author Markus Knittig
 *
 */
public class ReviewboardQueryPage extends AbstractRepositoryQueryPage {

    private static final String TITLE = "Enter query parameters";

    private static final String DESCRIPTION = "Select options to create a query";

    private static final String TITLE_QUERY_TITLE = "Query title";

    private ReviewboardClient client;

    private IRepositoryQuery query;

    private Text titleText;

    private ReviewRequestQuery reviewRequestQuery;

    private ReviewRequestStatus status;

    private String changeNum = "";

    private RowLayout defaultRowLayout;

    private ComboViewer groupCombo;
    private ComboViewer fromUserCombo;
    private ComboViewer toUserCombo;
    private ComboViewer repositoryCombo;

    public ReviewboardQueryPage(TaskRepository taskRepository, IRepositoryQuery query) {
        super(TITLE, taskRepository, query);

        this.query = query;

        ReviewboardRepositoryConnector connector = (ReviewboardRepositoryConnector) TasksUi
                .getRepositoryManager().getRepositoryConnector(
                        ReviewboardCorePlugin.REPOSITORY_KIND);
        client = connector.getClientManager().getClient(getTaskRepository());

        defaultRowLayout = new RowLayout();
        defaultRowLayout.center = true;
        defaultRowLayout.wrap = false;

        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    public ReviewboardQueryPage(TaskRepository repository) {
        this(repository, null);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updateRepositoryData(false);

            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    if (query != null) {
                        restoreQuery(query);
                    } else {
                        reviewRequestQuery = new AllReviewRequestQuery(ReviewRequestStatus.PENDING);
                    }
                }
            });
        }
    }

    private void updateRepositoryData(final boolean force) {
        if (force || !client.hasRepositoryData()) {
            try {
                IRunnableWithProgress runnable = new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException,
                            InterruptedException {
                        try {
                            client.updateRepositoryData(force, monitor);
                        } catch (Exception e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                };

                if (getContainer() != null) {
                    getContainer().run(true, true, runnable);
                } else if (getSearchContainer() != null) {
                    getSearchContainer().getRunnableContext().run(true, true, runnable);
                } else {
                    IProgressService service = PlatformUI.getWorkbench().getProgressService();
                    service.busyCursorWhile(runnable);
                }
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                return;
            }
        }

        ReviewboardClientData clientData = client.getClientData();
        groupCombo.setInput(ReviewboardUiUtil.getStringList(clientData.getGroups()));
        repositoryCombo.setInput(ReviewboardUiUtil.getStringList(clientData.getRepositories()));

        // TODO replace with another control
        toUserCombo.setInput(ReviewboardUiUtil.getStringList(clientData.getUsers()));
        fromUserCombo.setInput(ReviewboardUiUtil.getStringList(clientData.getUsers()));

        ReviewboardUiUtil.selectDefaultComboItem(groupCombo);
        ReviewboardUiUtil.selectDefaultComboItem(toUserCombo);
        ReviewboardUiUtil.selectDefaultComboItem(fromUserCombo);
        ReviewboardUiUtil.selectDefaultComboItem(repositoryCombo);
    }

    private void restoreQuery(IRepositoryQuery query) {
        titleText.setText(query.getSummary());
        changeNum = query.getAttribute("changeNum");
        reviewRequestQuery = StatusReviewRequestQuery.fromQueryString(query.getUrl());
    }

    @Override
    public void applyTo(IRepositoryQuery query) {
        query.setSummary(getQueryTitle());
        query.setUrl(reviewRequestQuery.getQuery());
        query.setAttribute("changeNum", changeNum);
    }

    @Override
    public String getQueryTitle() {
        if (titleText == null) {
            return "<search>";
        } else {
            return titleText.getText();
        }
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    @Override
    public boolean isPageComplete() {
        return validate();
    }

    private boolean validate() {
        boolean valid = true;
        if (reviewRequestQuery instanceof RepositoryReviewRequestQuery) {
            RepositoryReviewRequestQuery repositoryQuery = (RepositoryReviewRequestQuery) reviewRequestQuery;
            valid = repositoryQuery.isValid();
        }

        return (titleText != null && titleText.getText().length() > 0) && valid;
    }

    public void createControl(Composite parent) {
        Composite control = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(4).applyTo(control);
        GridDataFactory.fillDefaults().applyTo(control);

        createTitleGroup(control);

        Composite parentRadioComposite = new Composite(control, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(4).applyTo(parentRadioComposite);
        GridDataFactory.fillDefaults().span(4, 1).applyTo(parentRadioComposite);

        Composite radioComposite = new Composite(parentRadioComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(radioComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(radioComposite);

        Button all = createRadioButton(radioComposite, "All");
        all.setSelection(true);
        all.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                reviewRequestQuery = new AllReviewRequestQuery(status);
                getContainer().updateButtons();
            }
        });
        createRadioComposite(radioComposite);

        Composite groupComposite = createRadioCompositeWithCombo(radioComposite, "With group");
        groupCombo = createGroupCombo(groupComposite);

        Composite fromUserComposite = createRadioCompositeWithCombo(radioComposite, "From the user");
        fromUserCombo = createFromUserCombo(fromUserComposite);

        Composite toUserComposite = createRadioCompositeWithCombo(radioComposite, "To the user");
        toUserCombo = createToUserCombo(toUserComposite);

        Composite repositoryComposite = createRadioCompositeWithCombo(radioComposite,
                "From repository");
        repositoryCombo = createRepositoryCombo(repositoryComposite);
        Label changeNumLabel = new Label(repositoryComposite, SWT.NONE);
        changeNumLabel.setText("with change number:");
        Text changeNumText = new Text(repositoryComposite, SWT.BORDER);
        changeNumText.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                changeNum = ((Text) event.widget).getText();
                event.widget = repositoryCombo.getCombo();
                repositoryCombo.getCombo().notifyListeners(SWT.Modify, event);
                getContainer().updateButtons();
            }
        });

        Composite statusComposite = new Composite(control, SWT.NONE);
        RowLayoutFactory.createFrom(defaultRowLayout).applyTo(statusComposite);

        Label statusLabel = new Label(statusComposite, SWT.NONE);
        statusLabel.setText("With Status");
        ComboViewer statusCombo = createCombo(statusComposite);
        for (ReviewRequestStatus status : ReviewRequestStatus.values()) {
            statusCombo.add(status.getDisplayname());
        }
        statusCombo.getCombo().select(0);
        statusCombo.getCombo().addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                if (reviewRequestQuery != null) {
                    reviewRequestQuery.setStatus(ReviewRequestStatus.valueOf(((Combo) event.widget)
                            .getText().toUpperCase()));
                }
            }
        });

        setControl(control);
    }

    private Composite createRadioComposite(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        RowLayoutFactory.createFrom(defaultRowLayout).applyTo(composite);

        return composite;
    }

    private Composite createRadioCompositeWithCombo(final Composite parent, String text) {
        Button button = createRadioButton(parent, text);
        final Composite composite = createRadioComposite(parent);
        composite.setEnabled(false);

        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                composite.setEnabled(!composite.getEnabled());
                composite.notifyListeners(SWT.Modify, event);
            }
        });

        return composite;
    }

    private ComboViewer createCombo(Composite parent) {
        final ComboViewer combo = new ComboViewer(parent, SWT.DROP_DOWN | SWT.BORDER
                | SWT.READ_ONLY);

        parent.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                event.widget = combo.getCombo();
                combo.getCombo().notifyListeners(SWT.Modify, event);
            }
        });

        return combo;
    }

    private ComboViewer createGroupCombo(Composite parent) {
        ComboViewer combo = createCombo(parent);
        combo.setContentProvider(new ArrayContentProvider());

        combo.getCombo().addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                reviewRequestQuery = new GroupReviewRequestQuery(status, ((Combo) event.widget)
                        .getText());
                getContainer().updateButtons();
            }
        });

        return combo;
    }

    private ComboViewer createFromUserCombo(Composite parent) {
        ComboViewer combo = createCombo(parent);

        combo.getCombo().addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                reviewRequestQuery = new FromUserReviewRequestQuery(status, ((Combo) event.widget)
                        .getText());
                getContainer().updateButtons();
            }
        });

        return combo;
    }

    private ComboViewer createToUserCombo(Composite parent) {
        ComboViewer combo = createCombo(parent);

        combo.getCombo().addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                reviewRequestQuery = new ToUserReviewRequestQuery(status, ((Combo) event.widget)
                        .getText());
                getContainer().updateButtons();
            }
        });

        return combo;
    }

    private ComboViewer createRepositoryCombo(Composite parent) {
        ComboViewer combo = createCombo(parent);

        combo.getCombo().addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                Repository repository = client.getClientData().getRepositories().get(
                        ((Combo) event.widget).getSelectionIndex());

                int changeNumInt = 0;
                try {
                    changeNumInt = Integer.valueOf(changeNum);
                } catch (NumberFormatException e) {
                    // ignore
                }

                reviewRequestQuery = new RepositoryReviewRequestQuery(status, repository.getId(),
                        changeNumInt);
                getContainer().updateButtons();
            }
        });

        return combo;
    }

    private Button createRadioButton(Composite parent, String text) {
        Button radioButton = new Button(parent, SWT.RADIO);
        radioButton.setText(text);
        return radioButton;
    }

    private void createTitleGroup(Composite control) {
        if (inSearchContainer()) {
            return;
        }

        Label titleLabel = new Label(control, SWT.NONE);
        titleLabel.setText(TITLE_QUERY_TITLE);

        titleText = new Text(control, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(titleText);
        titleText.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                getContainer().updateButtons();
            }
        });
    }

}
