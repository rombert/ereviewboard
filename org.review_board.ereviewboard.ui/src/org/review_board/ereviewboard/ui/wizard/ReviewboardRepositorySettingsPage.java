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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardRepositoryConnector;
import org.review_board.ereviewboard.core.ReviewboardRepositoryMapper;
import org.review_board.ereviewboard.core.client.ReviewboardClient;

/**
 * @author Markus Knittig
 *
 */
public class ReviewboardRepositorySettingsPage extends AbstractRepositorySettingsPage {

    private static final String TITLE = "Reviewboard Repository Settings";

    private static final String DESCRIPTION = "Example: reviews.your-domain.org";

    private String checkedUrl = null;

    private boolean authenticated;

    private String username = "";
    private String password = "";

    public ReviewboardRepositorySettingsPage(TaskRepository taskRepository) {
        super(TITLE, DESCRIPTION, taskRepository);

        setNeedsAnonymousLogin(false);
        setNeedsEncoding(false);
        setNeedsTimeZone(false);
        setNeedsValidation(true);
        setNeedsHttpAuth(true);
        setNeedsAdvanced(false);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        checkedUrl = getRepositoryUrl();
    }
    
    @Override
    public boolean isPageComplete() {
        return super.isPageComplete() && checkedUrl != null
                && checkedUrl.equals(getRepositoryUrl())
                && username.equals(getUserName())
                && password.equals(getPassword())
                && authenticated;
    }

    @Override
    protected void createAdditionalControls(Composite parent) {
    }

    @Override
    public String getConnectorKind() {
        return ReviewboardCorePlugin.REPOSITORY_KIND;
    }

    @Override
    protected Validator getValidator(final TaskRepository repository) {
        username = getUserName();
        password = getPassword();

        return new Validator() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                authenticated = false;

                ReviewboardRepositorySettingsPage.this.checkedUrl = repository.getRepositoryUrl();

                ReviewboardRepositoryConnector connector = (ReviewboardRepositoryConnector) TasksUi
                        .getRepositoryManager().getRepositoryConnector(
                        ReviewboardCorePlugin.REPOSITORY_KIND);

                ReviewboardClient client = connector.getClientManager().getClient(repository);
                
                IStatus status = client.validate(username, password, monitor);
                
                if (!status.isOK())
                    throw new CoreException(status);

                authenticated = true;
            }
        };
    }

    @Override
    protected boolean isValidUrl(String url) {
        if ((url.startsWith(URL_PREFIX_HTTPS) || url.startsWith(URL_PREFIX_HTTP))
                && !url.endsWith("/")) {
            try {
                new URL(url);
                return true;
            } catch (MalformedURLException e) {
                // ignore
            }
        }
        return false;
    }

    @Override
    public void applyTo(TaskRepository repository) {
        
        super.applyTo(repository);
        
        new ReviewboardRepositoryMapper(repository).setCategoryIfNotSet();
    }
}
