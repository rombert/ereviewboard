package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
import org.review_board.ereviewboard.ui.util.ReviewboardImages;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * @author Robert Munteanu
 *
 */
public class PostReviewRequestWizard extends Wizard {
    
    static final int TEXT_WIDTH = 500;

    private final IProject _project;
    private DetectLocalChangesPage _detectLocalChangesPage;
    private PublishReviewRequestPage _publishReviewRequestPage;
    private final CreateReviewRequestWizardContext _context = new CreateReviewRequestWizardContext();

    private UpdateReviewRequestPage _updateReviewRequestPage;

    private ReviewRequest _reviewRequest;

    public PostReviewRequestWizard(IProject project) {

        _project = project;
        setWindowTitle("Create new review request");
        setDefaultPageImageDescriptor(ReviewboardImages.WIZARD_CREATE_REQUEST);
        setNeedsProgressMonitor(true);
    }
    
    public PostReviewRequestWizard(IProject project, ReviewRequest reviewRequest) {
        
        _project = project;
        _reviewRequest = reviewRequest;
        setWindowTitle("Update review request");
        setDefaultPageImageDescriptor(ReviewboardImages.WIZARD_CREATE_REQUEST);
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {

        _detectLocalChangesPage = new DetectLocalChangesPage(_project, _context, _reviewRequest);
        addPage(_detectLocalChangesPage);
        if ( _reviewRequest == null ) {
            _publishReviewRequestPage = new PublishReviewRequestPage(_context);
            addPage(_publishReviewRequestPage);
        } else {
            _updateReviewRequestPage = new UpdateReviewRequestPage(_context);
            addPage(_updateReviewRequestPage);
        }
    }
    
    @Override
    public boolean performFinish() {

        try {
            getContainer().run(false, true, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    
                    monitor.beginTask("Posting review request", 4);

                    SubMonitor sub;
                    
                    try {
                        ISVNRepositoryLocation svnRepository = _detectLocalChangesPage.getSvnRepositoryLocation();
                        ISVNClientAdapter svnClient = svnRepository.getSVNClient();
                        ReviewboardClient rbClient = _context.getReviewboardClient();
                        Repository reviewBoardRepository = _detectLocalChangesPage.getReviewBoardRepository();

                        ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(_project);
                        
                        sub = SubMonitor.convert(monitor, "Creating patch", 1);
                        
                        DiffCreator diffCreator = new DiffCreator();
                        
                        byte[] diffContent = diffCreator.createDiff(_detectLocalChangesPage.getSelectedFiles(), _project.getLocation().toFile(), svnClient);
                        
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
                            
                            rbClient.createDiff(reviewRequest.getId(), basePath, diffContent, monitor);
                            
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