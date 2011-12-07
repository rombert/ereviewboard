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
 *     Robert Munteanu - improvements
 *******************************************************************************/
package org.review_board.ereviewboard.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.User;
import org.review_board.ereviewboard.ui.internal.control.Proposal;

/**
 * @author Markus Knittig
 *
 */
public class UiUtils {
    
    public static final int FULL_TEXT_WIDTH = 500;

    public static Button createRadioButton(Composite parent, String text) {
        Button radioButton = new Button(parent, SWT.RADIO);
        radioButton.setText(text);
        return radioButton;
    }
    
    public static List<Proposal> adaptUsers(Collection<User> users) {
        
        List<Proposal> proposals = new ArrayList<Proposal>(users.size());
        for ( User user : users )
            proposals.add(adaptUser(user));
        
        return proposals;
    }
    
    private static Proposal adaptUser(User user) {
        
        return new Proposal(user.getUsername(), user.getFullName());
    }

    public static List<Proposal> adaptGroups(List<ReviewGroup> groups) {
        
        List<Proposal> proposals = new ArrayList<Proposal>(groups.size());
        for ( ReviewGroup group : groups )
            proposals.add(adaptGroup(group));
        
        return proposals;
    }

    private static Proposal adaptGroup(ReviewGroup group) {
        
        return new Proposal(group.getName(), group.getDisplayName());
    }
    
    public static ControlDecoration installContentAssistControlDecoration(Control control) {
        
        ControlDecoration controlDecoration = new ControlDecoration(control, (SWT.TOP | SWT.LEFT));
        controlDecoration.setShowOnlyOnFocus(true);
        FieldDecoration contentProposalImage = FieldDecorationRegistry.getDefault().getFieldDecoration(
                FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
        controlDecoration.setImage(contentProposalImage.getImage());
        controlDecoration.setDescriptionText("Content Assist Available");
        return controlDecoration;
    }

    
    public static StyledText newMultilineText(Composite layout) {
        
        return newSpellcheckedText(layout, true);
    }
    
    public static StyledText newSpellcheckedText(Composite layout, boolean multiline) {
        
        int style = SWT.BORDER;
        if ( multiline )
            style |= SWT.MULTI | SWT.V_SCROLL | SWT.WRAP;
        else
            style |= SWT.SINGLE;
        
        final SourceViewer sourceViewer = new SourceViewer(layout, null, null, true, style);
        StyledText text = sourceViewer.getTextWidget();
        if ( multiline )
            GridDataFactory.swtDefaults().hint(FULL_TEXT_WIDTH, 60).applyTo(text);
        else
            GridDataFactory.swtDefaults().hint(FULL_TEXT_WIDTH, SWT.DEFAULT).applyTo(text);    

        Document document = new Document();
        SourceViewerConfiguration config = new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore());
        sourceViewer.configure(config);
        sourceViewer.setDocument(document, new AnnotationModel());
        final IAnnotationAccess access = new DefaultMarkerAnnotationAccess();

        SourceViewerDecorationSupport decorationSupport = new SourceViewerDecorationSupport(
                sourceViewer, null, access, EditorsUI.getSharedTextColors());
        configureSourceViewerDecorationSupport(decorationSupport);

        return text;
    }
    
    public static StyledText newSinglelineText(Composite layout) {
        
        return newSpellcheckedText(layout, false);
    }

    
    private static void configureSourceViewerDecorationSupport(
            SourceViewerDecorationSupport support) {

        for(Object o :new MarkerAnnotationPreferences() .getAnnotationPreferences())
            support.setAnnotationPreference((AnnotationPreference)o);
        
        support.install(EditorsUI.getPreferenceStore());
    }
}
