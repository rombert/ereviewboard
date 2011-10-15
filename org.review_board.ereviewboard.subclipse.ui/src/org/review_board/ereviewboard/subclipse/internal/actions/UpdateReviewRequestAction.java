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
package org.review_board.ereviewboard.subclipse.internal.actions;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.ReviewboardRepositoryConnector;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.subclipse.Activator;
import org.review_board.ereviewboard.subclipse.TraceLocation;
import org.review_board.ereviewboard.subclipse.internal.wizards.PostReviewRequestWizard;
import org.review_board.ereviewboard.ui.editor.ext.TaskDiffAction;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils;

/**
 * @author Robert Munteanu
 */
public class UpdateReviewRequestAction implements TaskDiffAction {

    private TaskRepository repository;
    private int reviewRequestId;
    private List<FileDiff> fileDiffs;
    private Repository codeRepository;

    public void init(TaskRepository repository, int reviewRequestId, Repository codeRepository, List<FileDiff> fileDiffs) {
        
        this.repository = repository;
        this.reviewRequestId = reviewRequestId;
        this.codeRepository = codeRepository;
        this.fileDiffs = fileDiffs;
    }


    public boolean isEnabled() {

        return codeRepository != null && codeRepository.getTool() == RepositoryType.Subversion;
    }

    public IStatus execute(IProgressMonitor monitor) {
        
        try {
            
            ReviewboardRepositoryConnector connector = ReviewboardCorePlugin.getDefault().getConnector();
            
            ReviewboardClient client = connector.getClientManager().getClient(repository);
            
            ReviewRequest reviewRequest = client.getReviewRequest(reviewRequestId, monitor);
            
            IWorkspace workspace = ResourcesPlugin.getWorkspace();

            IProject matchingProject = null;
            
            projectLoop: for ( IProject project : workspace.getRoot().getProjects() ) {
                
                SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(project, SVNProviderPlugin.getTypeId());
                
                if ( svnProvider == null )
                    continue;
                
                ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(project);
                
                String projectRelativePath = SVNUrlUtils.getRelativePath(projectSvnResource.getRepository().getRepositoryRoot(), projectSvnResource.getUrl(), true);
                
                for ( FileDiff fileDiff : fileDiffs  ) {
                    
                    if ( !fileDiff.getDestinationFile().startsWith(projectRelativePath) ) {
                        continue projectLoop;
                    }
                }
                
                matchingProject = project;
                break;
            }

            Activator.getDefault().trace(TraceLocation.MAIN, "Matched review request with id " + reviewRequestId + " with project " + matchingProject);
            
            if ( matchingProject == null )
                return new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Could not find a matching project for the resources in the review request.");
            
            IWorkbench wb = PlatformUI.getWorkbench();
            
            IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
            
            new WizardDialog(win.getShell(), new PostReviewRequestWizard(matchingProject, reviewRequest)).open();

            return Status.OK_STATUS;
        } catch (ReviewboardException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed updating the diff : " + e.getMessage(), e);
        }
    }
}
