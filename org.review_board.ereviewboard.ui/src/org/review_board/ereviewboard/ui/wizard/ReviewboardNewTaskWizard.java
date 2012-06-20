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
package org.review_board.ereviewboard.ui.wizard;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;


/**
 * @author Robert Munteanu
 * 
 */
public class ReviewboardNewTaskWizard extends NewTaskWizard {

    public ReviewboardNewTaskWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
        super(taskRepository, taskSelection);
    }

    @Override
    public void addPages() {
        super.addPages();

        addPage(new ReviewboardUnsupportedCreationWizardPage());
    }

    @Override
    public boolean canFinish() {
        return false;
    }

    static final class ReviewboardUnsupportedCreationWizardPage extends WizardPage {
        public ReviewboardUnsupportedCreationWizardPage() {
            super("Creation not supported");
            setTitle("Direct creation is not supported.");
            setMessage("Direct creation is not supported.", IMessageProvider.WARNING);
        }

        public void createControl(Composite parent) {

            Composite control = new Composite(parent, SWT.NONE);
            GridLayoutFactory.swtDefaults().applyTo(control);

            Link link = new Link(control, SWT.NONE);
            link.setText("Review requests may be created for specific SCM plugins.\n\nPlease visit the <a>SCM integrations wiki</a> for more details.");
            link.addSelectionListener(new SelectionListener() {
                
                public void widgetSelected(SelectionEvent e) {
                    
                    BrowserUtil.openUrl("https://github.com/rombert/ereviewboard/wiki/SCM-Integrations", IWorkbenchBrowserSupport.AS_EXTERNAL);
                }
                
                public void widgetDefaultSelected(SelectionEvent e) {
                    
                }
            });

            setControl(control);
            
            
        }
    }
}
