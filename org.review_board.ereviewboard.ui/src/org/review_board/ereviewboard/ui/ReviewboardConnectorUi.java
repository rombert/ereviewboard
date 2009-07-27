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
package org.review_board.ereviewboard.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardRepositoryConnector;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.ui.wizard.NewReviewboardReviewRequestWizard;
import org.review_board.ereviewboard.ui.wizard.ReviewboardQueryPage;
import org.review_board.ereviewboard.ui.wizard.ReviewboardRepositorySettingsPage;

/**
 * @author Markus Knittig
 *
 */
public class ReviewboardConnectorUi extends AbstractRepositoryConnectorUi {

    @Override
    public String getConnectorKind() {
        return ReviewboardCorePlugin.REPOSITORY_KIND;
    }

    @Override
    public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
        return new ReviewboardRepositorySettingsPage(taskRepository);
    }

    @Override
    public boolean hasSearchPage() {
        return false;
    }

    @Override
    public IWizard getNewTaskWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
        ReviewboardRepositoryConnector connector = (ReviewboardRepositoryConnector) TasksUi
                .getRepositoryManager().getRepositoryConnector(
                        ReviewboardCorePlugin.REPOSITORY_KIND);
        final ReviewboardClient client = connector.getClientManager().getClient(taskRepository);

        return new NewReviewboardReviewRequestWizard(taskRepository, client);
    }

    @Override
    public IWizard getQueryWizard(TaskRepository taskRepository, IRepositoryQuery queryToEdit) {
        RepositoryQueryWizard wizard = new RepositoryQueryWizard(taskRepository);
        wizard.addPage(new ReviewboardQueryPage(taskRepository, queryToEdit));
        return wizard;
    }

    @Override
    public String getTaskKindLabel(ITask task) {
        return "Review Request";
    }

    @Override
    public String getAccountCreationUrl(TaskRepository taskRepository) {
        return taskRepository.getRepositoryUrl() + "/account/login/"; //$NON-NLS-1$
    }

    @Override
    public String getAccountManagementUrl(TaskRepository taskRepository) {
        return taskRepository.getRepositoryUrl() + "/account/preferences/"; //$NON-NLS-1$
    }

}
