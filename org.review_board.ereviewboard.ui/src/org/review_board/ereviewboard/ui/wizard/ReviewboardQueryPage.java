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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
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
    
    private enum Selection {
        
        ALL, GROUP, FROM_USER, TO_USER, REPOSITORY;
    }

    private static final String TITLE = "Enter query parameters";

    private static final String DESCRIPTION = "Select options to create a query";

    private static final String TITLE_QUERY_TITLE = "Query title";
    
    private final UpdateButtonsListener updateButtonsListener = new UpdateButtonsListener();
    
    private ReviewboardClient client;

    private IRepositoryQuery query;

    private Text titleText;

    private String changeNum = "";

    private ComboViewer groupCombo;
    private Text fromUserText;
    private Text toUserText;
    private ComboViewer repositoryCombo;

    private AutoCompleteField fromUserAutoCompleteField;
    private AutoCompleteField toUserComboAutoCompleteField;

    private List<String> fromUsers;
    private List<String> toUsers;

    private ComboViewer statusCombo;

    private Text changeNumText;

    private Map<Integer, String> repositories;

    private List<Repository> repositoryList;
    
    private Text maxResultsText;
    
    private Map<Button, SelectionRunnable> radioButtons = new HashMap<Button, SelectionRunnable>();

    private Selection selection = Selection.ALL;

    private ReviewRequestQuery reviewRequestQuery;

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

    private void updateRepositoryData(final boolean force) {
        
        if (force || !client.hasRepositoryData())
            ReviewboardUiUtil.refreshRepositoryData(client, force, getRunnableContext());

        ReviewboardClientData clientData = client.getClientData();

        groupCombo.setInput(ReviewboardUtil.toStringList(clientData.getGroups()));
        repositoryCombo.setInput(ReviewboardUtil.toStringList(clientData.getRepositories()));

        fromUsers = ReviewboardUtil.toStringList(clientData.getUsers());
        toUsers = ReviewboardUtil.toStringList(clientData.getUsers());
        fromUserAutoCompleteField.setProposals(fromUsers.toArray(new String[fromUsers.size()]));
        toUserComboAutoCompleteField.setProposals(toUsers.toArray(new String[toUsers.size()]));
        
        repositories = new HashMap<Integer, String>();
        repositoryList = clientData.getRepositories();
        for ( Repository repository : clientData.getRepositories() )
            repositories.put(repository.getId(), repository.getName());
    }
    
    private IRunnableContext getRunnableContext() {
        
        if ( getContainer() != null )
            return getContainer();
        
        ITaskSearchPageContainer container = getSearchContainer();
        if ( container != null )
            return container.getRunnableContext();
        
        return null;
    }

    private void restoreQuery(IRepositoryQuery query) {
        titleText.setText(query.getSummary());
        changeNum = query.getAttribute("changeNum");
        ReviewRequestQuery reviewRequestQuery = StatusReviewRequestQuery.fromQueryString(query.getUrl());
        selection = Selection.ALL;
        
        if (reviewRequestQuery instanceof StatusReviewRequestQuery) {
            
            ReviewRequestStatus status = ((StatusReviewRequestQuery) reviewRequestQuery).getStatus();
            ReviewboardUiUtil.selectComboItemByValue(statusCombo, status.getDisplayname());
            maxResultsText.setText(String.valueOf(((StatusReviewRequestQuery) reviewRequestQuery).getMaxResults()));
            
            if (reviewRequestQuery instanceof GroupReviewRequestQuery) {
                GroupReviewRequestQuery specificQuery = (GroupReviewRequestQuery) reviewRequestQuery;
                ReviewboardUiUtil.selectComboItemByValue(groupCombo, specificQuery.getGroupname());
                selection = Selection.GROUP;
            } else if ( reviewRequestQuery instanceof FromUserReviewRequestQuery) {
                FromUserReviewRequestQuery specificQuery = (FromUserReviewRequestQuery) reviewRequestQuery;
                fromUserText.setText(specificQuery.getUsername());
                selection = Selection.FROM_USER;
            } else if ( reviewRequestQuery instanceof ToUserReviewRequestQuery) {
                ToUserReviewRequestQuery specificQuery = (ToUserReviewRequestQuery) reviewRequestQuery;
                toUserText.setText(specificQuery.getUsername());
                selection = Selection.TO_USER;
            } else if ( reviewRequestQuery instanceof RepositoryReviewRequestQuery ) {
                RepositoryReviewRequestQuery specificQuery = (RepositoryReviewRequestQuery) reviewRequestQuery;
                changeNumText.setText(String.valueOf(specificQuery.getChangeNum()));
                ReviewboardUiUtil.selectComboItemByValue(repositoryCombo, repositories.get(specificQuery.getRepositoryId()));
                selection = Selection.REPOSITORY;
            }
        }
        
        // toggle buttons
        for ( Iterator<Map.Entry<Button, SelectionRunnable>> iterator = radioButtons.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<Button, SelectionRunnable> entry = iterator.next();
            boolean needsSelection = ( entry.getValue().getSelection() == selection );
            entry.getKey().setSelection(needsSelection);
            if ( needsSelection )
                entry.getValue().run();
        }
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
        
        ReviewRequestQuery query;
        ReviewRequestStatus status = getSelectedStatus();
        
        int maxResults;
        try {
            maxResults = Integer.parseInt(maxResultsText.getText());
            if ( maxResults <= 0 )
                return false;
        } catch ( NumberFormatException e) {
            return false;
        }
        
        switch ( selection ) {
        
        case ALL:
            query = new AllReviewRequestQuery(status, maxResults);
            break;
        
        case GROUP:
            query = new GroupReviewRequestQuery(status, maxResults, groupCombo.getCombo().getText());
            break;
            
        case REPOSITORY:
            String text =  changeNumText.getText();
            try {
                int changeNumInt = Integer.parseInt(text);
                if ( changeNumInt<= 0 )
                    return false;
                
                int repositoryIndex = repositoryCombo.getCombo().getSelectionIndex();
                int repositoryId = repositoryList.get(repositoryIndex).getId();
                
                query = new RepositoryReviewRequestQuery(status, maxResults, repositoryId, changeNumInt);
            } catch ( NumberFormatException e ) {
                return false;
            }
            
            break;
            
        case TO_USER:
            if ( !toUsers.contains(toUserText.getText()) )
                return false;
            
            query = new ToUserReviewRequestQuery(status, maxResults, toUserText.getText());
            break;
            
        case FROM_USER:
            if ( ! fromUsers.contains(fromUserText.getText() ) )
                return false;

            query = new FromUserReviewRequestQuery(status, maxResults, fromUserText.getText());
            break;
            
            default: 
                return false;
        }

        // assign at the end to benefit from 'definite assignment' compiler analysis
        reviewRequestQuery = query;
        
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

        Composite groupComposite = createRadioCompositeWithCombo(radioComposite, "With group", Selection.GROUP);
        groupCombo = createGroupCombo(groupComposite);

        Composite fromUserComposite = createRadioCompositeWithCombo(radioComposite, "From the user", Selection.FROM_USER);
        createFromUserText(fromUserComposite);

        Composite toUserComposite = createRadioCompositeWithCombo(radioComposite, "To the user", Selection.TO_USER);
        createToUserText(toUserComposite);

        Composite repositoryComposite = createRadioCompositeWithCombo(radioComposite, "From repository", Selection.REPOSITORY);
        repositoryCombo = createRepositoryCombo(repositoryComposite);
        Label changeNumLabel = new Label(repositoryComposite, SWT.FILL);
        changeNumLabel.setText("with change number:");
        changeNumText = new Text(repositoryComposite, SWT.BORDER);
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
        statusCombo = createCombo(statusComposite);
        for (ReviewRequestStatus status : ReviewRequestStatus.values())
            if ( status != ReviewRequestStatus.NONE )
                statusCombo.add(status.getDisplayname());
        
        statusCombo.getCombo().select(0);
        statusCombo.getCombo().addListener(SWT.Modify, updateButtonsListener);

        Composite maxResultComposite = new Composite(parentRadioComposite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(maxResultComposite);
        
        Label maxResultsLabel = new Label(maxResultComposite , SWT.FILL);
        maxResultsLabel.setText("Maximum results");
        
        maxResultsText = new Text(maxResultComposite, SWT.BORDER);
        GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(maxResultsText);
        maxResultsText.addListener(SWT.Modify, updateButtonsListener);

        Button button = new Button(control, SWT.NONE);
        GridDataFactory.swtDefaults().span(4, 1).applyTo(button);
        button.setText("Refresh repository configuration");
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateRepositoryData(true);
            }
        });
        
        setControl(control);
        
        Dialog.applyDialogFont(control);
        
        updateRepositoryData(false);
        
        ReviewboardUiUtil.selectDefaultComboItem(groupCombo);
        ReviewboardUiUtil.selectDefaultComboItem(repositoryCombo);
        maxResultsText.setText(String.valueOf(ReviewRequestQuery.DEFAULT_MAX_RESULTS));
        
        if (query != null)
            restoreQuery(query);
    }
    
    private ReviewRequestStatus getSelectedStatus() {
        
        return ReviewRequestStatus.valueOf(statusCombo.getCombo().getText().toUpperCase());
    }

    private void createAllButton(Composite radioComposite) {
        Button button = UiUtils.createRadioButton(radioComposite, "All");
        final SelectionRunnable toggleRunnable = new SelectionRunnable(Selection.ALL) {
          public void run0() {
              getContainer().updateButtons();
          }
        };
        radioButtons.put(button, toggleRunnable);
        button.setSelection(true);
        final Composite allComposite = createRadioComposite(radioComposite);
        allComposite.setEnabled(false);
        new Label(allComposite, SWT.NONE);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                toggleRunnable.run();
            }
        });
    }

    private Composite createRadioComposite(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

        return composite;
    }

    private Composite createRadioCompositeWithCombo(final Composite parent, String text, final Selection selection) {
        final Button button = UiUtils.createRadioButton(parent, text);
        final Composite composite = createRadioComposite(parent);
        composite.setEnabled(false);
        final SelectionRunnable toggleRunnable = new SelectionRunnable(selection) {
            protected void run0() {
                composite.setEnabled(button.getSelection());
                getContainer().updateButtons();
            }  
          };
        radioButtons.put(button, toggleRunnable);


        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                toggleRunnable.run();
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

        combo.getCombo().addListener(SWT.Modify, updateButtonsListener);

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
        fromUserText.addListener(SWT.Modify, updateButtonsListener);
    }

    private void createToUserText(Composite toUserComposite) {
    
        toUserText = new Text(toUserComposite, SWT.BORDER);
        toUserComboAutoCompleteField = new AutoCompleteField(toUserText, new TextContentAdapter(), new String[] {});
        toUserComposite.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                toUserText.notifyListeners(SWT.Modify, event);
            }
        });
        toUserComposite.addListener(SWT.Modify, updateButtonsListener);
    }

    private ComboViewer createRepositoryCombo(Composite parent) {

        ComboViewer combo = createCombo(parent);

        combo.getCombo().addListener(SWT.Modify, updateButtonsListener);
        
        return combo;
    }

    private void createTitleGroup(Composite control) {
        
        if (inSearchContainer())
            return;

        Label titleLabel = new Label(control, SWT.NONE);
        titleLabel.setText(TITLE_QUERY_TITLE);

        titleText = new Text(control, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(titleText);
        titleText.addListener(SWT.Modify, updateButtonsListener);
    }

    private abstract class SelectionRunnable implements Runnable {

        private final Selection selection;
        
        
        public SelectionRunnable(Selection selection) {
            this.selection = selection;
        }

        public Selection getSelection() {
            
            return selection;
        }

        public void run() {
            
            ReviewboardQueryPage.this.selection = selection;
            run0();
        }
        
        protected abstract void run0();
        
    }
    
    private final class UpdateButtonsListener implements Listener {
        public void handleEvent(Event event) {
            getContainer().updateButtons();
        }
    }
}
