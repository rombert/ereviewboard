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

import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.util.ResourceUtil;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardDiffPart extends AbstractTaskEditorPart {

    public ReviewboardDiffPart() {
        
        setPartName("Latest Diff");
    }
    
    @Override
    public void createControl(Composite parent, FormToolkit toolkit) {

        Section section = createSection(parent, toolkit, true);
        Composite composite = toolkit.createComposite(section);
        composite.setLayout(EditorUtil.createSectionClientLayout());
        
        final ReviewboardDiffMapper diffMapper = new ReviewboardDiffMapper(getTaskData());
        
        for ( final TaskAttribute child : diffMapper.getFileDiffs() ) {
            final String sourcePath = child.getAttribute(SOURCE_FILE.toString()).getValue();
            final String sourceRevision = child.getAttribute(SOURCE_REVISION.toString()).getValue();
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
        
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        setSection(toolkit, section);
    }

}
