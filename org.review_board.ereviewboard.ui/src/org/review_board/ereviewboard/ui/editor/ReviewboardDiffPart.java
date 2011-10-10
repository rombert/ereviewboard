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

import static org.review_board.ereviewboard.core.ReviewboardAttributeMapper.Attribute.SOURCE_FILE;
import static org.review_board.ereviewboard.core.ReviewboardAttributeMapper.Attribute.SOURCE_REVISION;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.ReverbType;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.ReviewboardTaskMapper;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.util.ResourceUtil;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;
import org.review_board.ereviewboard.ui.editor.ext.DiffResource;
import org.review_board.ereviewboard.ui.editor.ext.TaskDiffAction;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardDiffPart extends AbstractTaskEditorPart {

    private static final String EXTENSION_POINT_TASK_DIFF_ACTIONS = "org.review_board.ereviewboard.ui.taskDiffActions";

    public ReviewboardDiffPart() {
        
        setPartName("Latest Diff");
    }
    
    @Override
    public void createControl(Composite parent, FormToolkit toolkit) {
        
        Section section = createSection(parent, toolkit, true);
        Composite composite = toolkit.createComposite(section);
        composite.setLayout(EditorUtil.createSectionClientLayout());
        
        final ReviewboardDiffMapper diffMapper = new ReviewboardDiffMapper(getTaskData());
        
        List<DiffResource> diffResources = new ArrayList<DiffResource>();
        
        for ( final TaskAttribute child : diffMapper.getFileDiffs() ) {
            
            final String sourcePath = child.getAttribute(SOURCE_FILE.toString()).getValue();
            final String sourceRevision = child.getAttribute(SOURCE_REVISION.toString()).getValue();
            
            diffResources.add(new DiffResource(sourcePath, sourceRevision));
            
            Hyperlink link = toolkit.createHyperlink(composite, sourcePath + " ( " + sourceRevision + " )", SWT.NONE);
            link.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    
                    List<String> paths = ResourceUtil.getResourcePathPermutations(sourcePath);
                    
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    IResource resource = null;
                    for ( String path : paths ) {
                        resource = workspace.getRoot().findMember(path);
                        if ( resource != null )
                            break;
                    }
                    
                    if ( resource == null  && ! ReviewboardDiffMapper.REVISION_PRE_CREATION.equals(sourceRevision) ) {
                        MessageDialog.openWarning(null, "Unable to find file", "Unable to find a file for " + sourcePath + " in the workspace.");
                        ReviewboardUiPlugin.getDefault().getLog().log(new Status(Status.WARNING, ReviewboardUiPlugin.PLUGIN_ID, "Unable to find a matching file for " + child.getValue() + " tried " + paths ));
                        return;
                    }
                    
                    IFile file = (IFile) resource;
                    
                    CompareConfiguration configuration = new CompareConfiguration();
                    ReviewboardClient client = ReviewboardCorePlugin.getDefault().getConnector().getClientManager().getClient(new TaskRepository(ReviewboardCorePlugin.PLUGIN_ID, getTaskData().getRepositoryUrl()));
                    ReviewBoardInput input = new ReviewBoardInput(file, configuration, client, Integer.parseInt(getTaskData().getTaskId()), diffMapper.getDiffRevision(), Integer.parseInt(child.getValue()));
                    
                    CompareUI.openCompareEditor(input);
                }
            });
        }
        
        ReviewboardTaskMapper taskMapper = new ReviewboardTaskMapper(getTaskData());
        
        installExtensions(composite, taskMapper.getRepository(), diffMapper, diffResources);

        
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        setSection(toolkit, section);
    }

    private void installExtensions(Composite composite, Repository codeRepository, ReviewboardDiffMapper diffMapper, List<DiffResource> diffResources) {
        
        IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_TASK_DIFF_ACTIONS);
        
        int reviewRequestId = Integer.parseInt(getTaskData().getTaskId());
        TaskRepository repository = TasksUi.getRepositoryManager().getRepository(ReviewboardCorePlugin.REPOSITORY_KIND, getTaskData().getRepositoryUrl());
        
        for ( IConfigurationElement element : configurationElements ) {
            try {
                final TaskDiffAction taskDiffAction = (TaskDiffAction) element.createExecutableExtension("class");
                taskDiffAction.init(repository, reviewRequestId, codeRepository, diffResources);
                if ( !taskDiffAction.isEnabled() )
                    continue;
                
                final String label = element.getAttribute("label");
                
                Button button = new Button(composite, SWT.NONE);
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

}
