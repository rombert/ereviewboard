/*******************************************************************************
 * Copyright (c) 2004, 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.ui.editor;

import java.util.List;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.*;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.reviews.core.model.IFileItem;
import org.eclipse.mylyn.reviews.core.model.IFileRevision;
import org.eclipse.mylyn.reviews.ui.ReviewUi;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.ReviewboardTaskMapper;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.reviews.ReviewModelFactory;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;
import org.review_board.ereviewboard.ui.editor.ext.SCMFileContentsLocator;
import org.review_board.ereviewboard.ui.editor.ext.TaskDiffAction;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardDiffPart extends AbstractTaskEditorPart {

    private static final String EXTENSION_POINT_TASK_DIFF_ACTIONS = "org.review_board.ereviewboard.ui.taskDiffActions";
    private static final String EXTENSION_POINT_SCM_FILE_CONTENTS_LOCATOR = "org.review_board.ereviewboard.ui.scmFileContentsLocator";

    public ReviewboardDiffPart() {
        
        setPartName("Latest Diff");
    }
    
    private void addDescriptiveRow(String name, String value, FormToolkit toolkit,Composite composite) {
        
        Label authorLabel = new Label(composite, SWT.NONE);
        authorLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        authorLabel.setText(name);

        Label authorText = new Label(composite, SWT.NONE);
        authorText.setText(value);
    }
    
    @Override
    public void createControl(Composite parent, FormToolkit toolkit) {
        
        Section section = createSection(parent, toolkit, true);
        Composite composite = toolkit.createComposite(section);
        GridLayoutFactory.createFrom(EditorUtil.createSectionClientLayout()).numColumns(2).applyTo(composite);
        
        final ReviewboardTaskMapper taskMapper = new ReviewboardTaskMapper(getTaskData());
        
        final ReviewboardDiffMapper diffMapper = new ReviewboardDiffMapper(getTaskData());
        
        ReviewModelFactory reviewModelFactory = new ReviewModelFactory(getClient());
        
        addDescriptiveRow("Author", reviewModelFactory.createUser(taskMapper.getReporter()).getDisplayName(), toolkit, composite);
        addDescriptiveRow("Revision", String.valueOf(diffMapper.getDiffRevision()), toolkit, composite);
        addDescriptiveRow("Created", diffMapper.getTimestamp(), toolkit, composite);
        
        TableViewer diffTableViewer = new TableViewer(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        diffTableViewer.setContentProvider(new ArrayContentProvider());
        diffTableViewer.setSorter(new ViewerSorter());
        
        GridDataFactory.fillDefaults().span(2,1).grab(true, true).hint(500, SWT.DEFAULT).applyTo(diffTableViewer.getControl());
        
        TableViewerColumn fileColumn = new TableViewerColumn(diffTableViewer, SWT.NONE);
        fileColumn.getColumn().setWidth(300);
        fileColumn.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                
                IFileItem fileDiff = (IFileItem) element;
                
                return fileDiff.getName();
            }
        });
        
        List<FileDiff> fileDiffs = diffMapper.getFileDiffs();
        

        List<IFileItem> fileItems= reviewModelFactory.createFileItems(taskMapper.getReporter(), diffMapper);
        diffTableViewer.setInput(fileItems.toArray(new IFileItem[fileItems.size()]));
        
        diffTableViewer.addOpenListener(new IOpenListener() {
            
            public void open(OpenEvent event) {
                
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                
                IFileItem item = (IFileItem) selection.getFirstElement();
                
                ReviewUi.setActiveReview(new ReviewboardReviewBehaviour(getTaskEditorPage().getTask()));
                
                SCMFileContentsLocator locator = getSCMFileContentsLocator(taskMapper, item.getBase());
                if ( locator == null ) {
                    MessageDialog.openWarning(null, "Unable to load base file", "Unable to load base file contents since no plug-in was able to handle the repository " + taskMapper.getRepository());
                    return;
                }
                
                ReviewboardClient client = getClient();
                
                int reviewRequestId = Integer.parseInt(getTaskData().getTaskId());
                int diffId = diffMapper.getDiffRevision();
                int fileDiffId = Integer.parseInt(item.getId());
                try {
                    if ( item.getTarget().getTopics().isEmpty() ) // do not add comments multiple times
                        new ReviewModelFactory(client).appendComments(item.getTarget(), client.readDiffComments(reviewRequestId, diffId, fileDiffId, new NullProgressMonitor()));
                } catch (ReviewboardException e) {
                    ReviewboardUiPlugin.getDefault().getLog().log(new Status(Status.ERROR, ReviewboardUiPlugin.PLUGIN_ID, "Failed retrieving diff comments ", e));
                }
                
                CompareUI.openCompareEditor(new ReviewboardCompareEditorInput(item, diffMapper, getTaskData(), locator));
            }

        });
                
        installExtensions(composite, taskMapper.getRepository(), diffMapper, fileDiffs);
        
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        setSection(toolkit, section);
    }

    private void installExtensions(Composite composite, Repository codeRepository, ReviewboardDiffMapper diffMapper, List<FileDiff> fileDiffs) {
        
        IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_TASK_DIFF_ACTIONS);
        
        int reviewRequestId = Integer.parseInt(getTaskData().getTaskId());
        Composite extensionsComposite = new Composite(composite, SWT.NONE);
        RowLayoutFactory.fillDefaults().type(SWT.HORIZONTAL).applyTo(extensionsComposite);
        
        for ( IConfigurationElement element : configurationElements ) {
            try {
                final TaskDiffAction taskDiffAction = (TaskDiffAction) element.createExecutableExtension("class");
                taskDiffAction.init(getTaskRepository(), reviewRequestId, codeRepository, fileDiffs);
                if ( !taskDiffAction.isEnabled() )
                    continue;
                
                final String label = element.getAttribute("label");
                
                Button button = new Button(extensionsComposite, SWT.NONE);
                button.setText(label);
                button.addSelectionListener(new SelectionListener() {
                    
                    public void widgetSelected(SelectionEvent e) {
                        
                        IStatus status;
                        try {
                            status = taskDiffAction.execute(new NullProgressMonitor());
                        } catch (Exception e1) {
                            status = new Status(IStatus.ERROR, ReviewboardUiPlugin.PLUGIN_ID, "Internal error while executing action '" + label+"' : " + e1.getMessage(), e1);
                            ReviewboardUiPlugin.getDefault().getLog().log(status);
                        }
                        
                        if ( !status.isOK() ) {
                            
                            int kind = MessageDialog.ERROR;
                            if ( status.getSeverity() == IStatus.WARNING )
                                kind = MessageDialog.WARNING;
                            
                            MessageDialog.open(kind, null, "Error performing action", status.getMessage(), SWT.SHEET);
                        }
                    }
                    
                    public void widgetDefaultSelected(SelectionEvent e) {
                        
                    }
                });
            } catch (CoreException e) {
                ReviewboardUiPlugin.getDefault().getLog().log(e.getStatus());
            }
        }
    }

    private TaskRepository getTaskRepository() {
        TaskRepository repository = TasksUi.getRepositoryManager().getRepository(ReviewboardCorePlugin.REPOSITORY_KIND, getTaskData().getRepositoryUrl());
        return repository;
    }
    
    private ReviewboardClient getClient() {
        return ReviewboardCorePlugin.getDefault().getConnector().getClientManager().getClient(getTaskRepository());
    }
    
    private SCMFileContentsLocator getSCMFileContentsLocator(ReviewboardTaskMapper taskMapper, IFileRevision fileRevision) {
        
        IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_SCM_FILE_CONTENTS_LOCATOR);

        for ( IConfigurationElement element : configurationElements ) { 
            try {
                SCMFileContentsLocator locator = (SCMFileContentsLocator) element.createExecutableExtension("class");
                locator.init(taskMapper.getRepository(), fileRevision.getPath(), fileRevision.getRevision());
                if ( locator.isEnabled() )
                    return locator;
            } catch (CoreException e) {
                ReviewboardUiPlugin.getDefault().getLog().log(e.getStatus());
            }
        }
        
        return null;
    }

}
