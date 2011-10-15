/*******************************************************************************
 * Copyright (c) 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.subclipse.internal.wizards;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * @author Robert Munteanu
 *
 */
class UpdateReviewRequestPage extends WizardPage {

    private Text _text;

    public UpdateReviewRequestPage() {

        super("Update Review Request", "Update Review Request", null);
        setMessage("Provide an optional description for the changes you are about to publish.");
    }
    
    public void createControl(Composite parent) {
        
        Composite control = new Composite(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(control);
        
        Label label = new Label( control, SWT.WRAP);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(label);
        label.setText("Change description\n(optional)");
        
        _text= new Text(control, SWT.MULTI| SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        GridDataFactory.swtDefaults().hint(PostReviewRequestWizard.TEXT_WIDTH, 60).applyTo(_text);

        setControl(control);
    }
    
    public String getChangeDescription() {
        
        return _text.getText();
    }
}
