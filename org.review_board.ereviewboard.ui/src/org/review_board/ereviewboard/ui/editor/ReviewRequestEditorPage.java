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
 *     Robert Munteanu - added people part and rich text support for testing 
 *                       done                 
 *******************************************************************************/
package org.review_board.ereviewboard.ui.editor;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;

/**
 * @author Markus Knittig
 * 
 */
public class ReviewRequestEditorPage extends AbstractTaskEditorPage {

    private static final String ID_REVIEWBOARD_PART_TESTING_DONE = ReviewRequestEditorPage.class.getName() +".parts.testing_done";
    private static final String ID_REVIEWBOARD_DIFF = ReviewRequestEditorPage.class.getName() +".parts.diff";

    public ReviewRequestEditorPage(TaskEditor editor, String title) {
        super(editor, ReviewboardCorePlugin.REPOSITORY_KIND);
        
        setNeedsPrivateSection(true);
    }
    
    @Override
    protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
        Set<TaskEditorPartDescriptor> partDescriptors = super.createPartDescriptors();
        
        // we provide our own people part and we submit no data
        for ( Iterator<TaskEditorPartDescriptor> partDescriptorIterator = partDescriptors.iterator(); partDescriptorIterator.hasNext(); ) {
            TaskEditorPartDescriptor partDescriptor = partDescriptorIterator.next();
            if ( partDescriptor.getId().equals(ID_PART_PEOPLE) )
                partDescriptorIterator.remove();
        }
        
        // testing done
        partDescriptors = insertPart(partDescriptors,
                new TaskEditorPartDescriptor(ID_REVIEWBOARD_PART_TESTING_DONE) {
            @Override
            public AbstractTaskEditorPart createPart() {
                return new RichTextPart(ReviewboardAttributeMapper.Attribute.TESTING_DONE, true);
            }
        }.setPath(PATH_COMMENTS), ID_PART_DESCRIPTION);
        
        
        // Latest diff
        try {
            TaskData taskData = TasksUi.getTaskDataManager().getTaskData(getTask());
            
            if (taskData.getRoot().getAttribute(ReviewboardAttributeMapper.Attribute.LATEST_DIFF.toString()) != null) {
                
                partDescriptors = insertPart(partDescriptors, new TaskEditorPartDescriptor(ID_REVIEWBOARD_DIFF) {
                    
                    @Override
                    public AbstractTaskEditorPart createPart() {
                        
                        return new ReviewboardDiffPart();
                    }
                }.setPath(PATH_COMMENTS), ID_PART_ATTRIBUTES);
            }
        } catch (CoreException e) {
            ReviewboardUiPlugin.getDefault().getLog().log(new Status(Status.WARNING, ReviewboardUiPlugin.PLUGIN_ID, "Failed retrieving taskData ", e));
        }

        
        // people part
        partDescriptors.add(new TaskEditorPartDescriptor(ID_PART_PEOPLE) {
            @Override
            public AbstractTaskEditorPart createPart() {
                return new ReviewboardPeoplePart();
            }
        }.setPath(PATH_PEOPLE));

        return partDescriptors;
    }
    
    protected Set<TaskEditorPartDescriptor> insertPart(Set<TaskEditorPartDescriptor> originalDescriptors, TaskEditorPartDescriptor newDescriptor, String insertAfterId ) {
        
        Set<TaskEditorPartDescriptor> newDescriptors = new LinkedHashSet<TaskEditorPartDescriptor>();
        for (TaskEditorPartDescriptor taskEditorPartDescriptor : originalDescriptors) {
            newDescriptors.add(taskEditorPartDescriptor);
            if (taskEditorPartDescriptor.getId().equals(insertAfterId)) {
                newDescriptors.add(newDescriptor);
            }
        }
        
        return newDescriptors;
    }    
}
