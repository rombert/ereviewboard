package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.review_board.ereviewboard.ui.util.*;

public class CreateReviewRequestWizard extends Wizard {

    private final IProject _project;
    private DetectLocalChangesPage _detectLocalChangesPage;
    private PublishReviewRequestPage _publishReviewRequestPage;
    private final CreateReviewRequestWizardContext _context = new CreateReviewRequestWizardContext();

    public CreateReviewRequestWizard(IProject project) {

        _project = project;
        setWindowTitle("Create new review request");
        setDefaultPageImageDescriptor(ReviewboardImages.WIZARD_CREATE_REQUEST);
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {

        _detectLocalChangesPage = new DetectLocalChangesPage(_project, _context);
        addPage(_detectLocalChangesPage);
        _publishReviewRequestPage = new PublishReviewRequestPage(_context);
        addPage(_publishReviewRequestPage);
    }

    @Override
    public boolean performFinish() {

        try {
            getContainer().run(false, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    
                    monitor.beginTask("Posting review request", 4);

                    File tmpFile = null;
                    FileReader reader = null;
                    
                    SubMonitor sub;
                    
                    try {
                        ISVNRepositoryLocation svnRepository = _detectLocalChangesPage.getSvnRepositoryLocation();
                        ISVNClientAdapter svnClient = svnRepository.getSVNClient();
                        ReviewboardClient rbClient = _context.getReviewboardClient();
                        Repository reviewBoardRepository = _detectLocalChangesPage.getReviewBoardRepository();

                        ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(_project);

                        tmpFile = File.createTempFile("ereviewboard", "diff");
                        
                        sub = SubMonitor.convert(monitor, "Creating patch", 1);
                        
                        
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                        File[] changes = _detectLocalChangesPage.getSelectedFiles().toArray(new File[_detectLocalChangesPage.getSelectedFiles().size()]);
                        svnClient.createPatch(changes, _project.getLocation().toFile(), tmpFile, false);
                        reader = new FileReader(tmpFile);
                        IOUtils.copy(reader, outputStream);

                        sub.done();
                        
                        if (rbClient != null && reviewBoardRepository != null) {
                            
                            sub = SubMonitor.convert(monitor, "Creating initial review request", 1);
                            
                            ReviewRequest reviewRequest = rbClient.createReviewRequest(reviewBoardRepository,
                                    sub);
                            
                            sub.done();

                            System.out.println("Created review request with id " + reviewRequest.getId());

                            String basePath = projectSvnResource.getUrl().toString()
                                    .substring(svnRepository.getRepositoryRoot().toString().length());

                            System.out.println("Detected base path " + basePath);

                            TaskRepository repository = _detectLocalChangesPage.getTaskRepository();

                            sub = SubMonitor.convert(monitor, "Posting diff patch", 1);
                            
                            rbClient.createDiff(reviewRequest.getId(), basePath, outputStream.toByteArray(), monitor);
                            
                            sub.done();

                            System.out.println("Diff created.");

                            ReviewRequest reviewRequestForUpdate = _publishReviewRequestPage.getReviewRequest();
                            reviewRequestForUpdate.setId(reviewRequest.getId());

                            sub = SubMonitor.convert(monitor, "Publishing review request", 1);
                            
                            rbClient.updateReviewRequest(reviewRequestForUpdate, true, monitor);
                            
                            sub.done();

                            boolean success = TasksUiUtil.openTask(repository, String.valueOf(reviewRequest.getId()));

                            if (!success) {
                                MessageDialog.openWarning(null, "Failed opening task",
                                        "Review request with id " + reviewRequest.getId() + " created in repository "
                                                + reviewBoardRepository.getName()
                                                + " but the task editor could not be opened.");
                                return;
                            }
                        }
                    } catch (SVNException e) {
                        throw new InvocationTargetException(e);
                    } catch (IOException e) {
                        throw new InvocationTargetException(e);
                    } catch (SVNClientException e) {
                        throw new InvocationTargetException(e);
                    } catch (ReviewboardException e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        FileUtils.deleteQuietly(tmpFile);
                        IOUtils.closeQuietly(reader);
                        monitor.done();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            ((WizardPage) getContainer().getCurrentPage()).setErrorMessage("Failed creating new review request : " + e.getCause().getMessage());
            return false;
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

}
