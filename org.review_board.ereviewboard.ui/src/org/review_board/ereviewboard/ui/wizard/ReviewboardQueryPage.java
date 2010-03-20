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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
import org.eclipse.swt.SWT;
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
import org.review_board.ereviewboard.core.util.ReviewboardUtil;
import org.review_board.ereviewboard.ui.ReviewboardUiUtil;
import org.review_board.ereviewboard.ui.util.UiUtils;

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

    private ComboViewer groupCombo;
    private Text fromUserText;
    private Text toUserText;
    private ComboViewer repositoryCombo;

    private AutoCompleteField fromUserAutoCompleteField;
    private AutoCompleteField toUserComboAutoCompleteField;

    private List<String> fromUsers;
    private List<String> toUsers;

    public ReviewboardQueryPage(TaskRepository taskRepository, IRepositoryQuery query) {
        super(TITLE, taskRepository, query);

        this.query = query;

        ReviewboardRepositoryConnector connector = (ReviewboardRepositoryConnector) TasksUi
                .getRepositoryManager().getRepositoryConnector(
                        ReviewboardCorePlugin.REPOSITORY_KIND);
        client = connector.getClientManager().getClient(getTaskRepository());

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

        groupCombo.setInput(ReviewboardUtil.toStringList(clientData.getGroups()));
        repositoryCombo.setInput(ReviewboardUtil.toStringList(clientData.getRepositories()));
        ReviewboardUiUtil.selectDefaultComboItem(groupCombo);
        ReviewboardUiUtil.selectDefaultComboItem(repositoryCombo);

        fromUsers = ReviewboardUtil.toStringList(clientData.getUsers());
        toUsers = ReviewboardUtil.toStringList(clientData.getUsers());
        fromUserAutoCompleteField.setProposals(fromUsers.toArray(new String[fromUsers.size()]));
        toUserComboAutoCompleteField.setProposals(toUsers.toArray(new String[toUsers.size()]));
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

        if (reviewRequestQuery instanceof ToUserReviewRequestQuery) {
            valid = toUsers.contains(toUserText.getText());
        }

        if (reviewRequestQuery instanceof FromUserReviewRequestQuery) {
            valid = fromUsers.contains(fromUserText.getText());
        }

        return (titleText != null && titleText.getText().length() > 0) && valid;
    }

    public void createControl(Composite parent) {
        Composite control = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(4).applyTo(control);

        createTitleGroup(control);

        Composite parentRadioComposite = new Composite(control, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parentRadioComposite);
        GridDataFactory.fillDefaults().span(4, 1).applyTo(parentRadioComposite);

        Composite radioComposite = new Composite(parentRadioComposite, SWT.NONE);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(radioComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(radioComposite);

        createAllButton(radioComposite);

        Composite groupComposite = createRadioCompositeWithCombo(radioComposite, "With group");
        groupCombo = createGroupCombo(groupComposite);

        Composite fromUserComposite = createRadioCompositeWithCombo(radioComposite, "From the user");
        createFromUserText(fromUserComposite);

        Composite toUserComposite = createRadioCompositeWithCombo(radioComposite, "To the user");
        createToUserText(toUserComposite);

        Composite repositoryComposite = createRadioCompositeWithCombo(radioComposite, "From repository");
        repositoryCombo = createRepositoryCombo(repositoryComposite);
        Label changeNumLabel = new Label(repositoryComposite, SWT.FILL);
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

        Composite statusComposite =  new Composite(parentRadioComposite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(statusComposite);
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

    private void createAllButton(Composite radioComposite) {
        Button button = UiUtils.createRadioButton(radioComposite, "All");
        button.setSelection(true);
        final Composite allComposite = createRadioComposite(radioComposite);
        allComposite.setEnabled(false);
        new Label(allComposite, SWT.NONE);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                reviewRequestQuery = new AllReviewRequestQuery(status);
                getContainer().updateButtons();
            }
        });
    }

    private Composite createRadioComposite(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

        return composite;
    }

    private Composite createRadioCompositeWithCombo(final Composite parent, String text) {
        Button button = UiUtils.createRadioButton(parent, text);
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
        combo.setContentProvider(new ArrayContentProvider());

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

        combo.getCombo().addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                reviewRequestQuery = new GroupReviewRequestQuery(status, ((Combo) event.widget)
                        .getText());
                getContainer().updateButtons();
            }
        });

        return combo;
    }

    private void createFromUserText(Composite fromUserComposite) {
        fromUserText = new Text(fromUserComposite, SWT.BORDER);
        fromUserAutoCompleteField = new AutoCompleteField(fromUserText, new TextContentAdapter(), new String[] {});
        fromUserComposite.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                fromUserText.notifyListeners(SWT.Modify, event);
            }
        });
        fromUserText.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                reviewRequestQuery = new FromUserReviewRequestQuery(status, ((Text) event.widget).getText());
                getContainer().updateButtons();
            }
        });
    }

    private void createToUserText(Composite toUserComposite) {
        toUserText = new Text(toUserComposite, SWT.BORDER);
        toUserComboAutoCompleteField = new AutoCompleteField(toUserText, new TextContentAdapter(), new String[] {});
        toUserComposite.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                toUserText.notifyListeners(SWT.Modify, event);
            }
        });
        toUserComposite.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                reviewRequestQuery = new ToUserReviewRequestQuery(status, ((Text) event.widget).getText());
                getContainer().updateButtons();
            }
        });
    }

    private ComboViewer createRepositoryCombo(Composite parent) {
        ComboViewer combo = createCombo(parent);

        combo.getCombo().addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                int selectedIndex =  ((Combo) event.widget).getSelectionIndex();
                if (((Combo) event.widget).getItemCount() > 0 && selectedIndex > -1 ){

                    Repository repository = client.getClientData().getRepositories().get(selectedIndex);

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
            }
        });

        return combo;
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
