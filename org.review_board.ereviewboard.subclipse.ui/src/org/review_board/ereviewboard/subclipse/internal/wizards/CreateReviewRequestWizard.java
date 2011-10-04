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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Diff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

public class CreateReviewRequestWizard extends Wizard {

    private final IProject _project;
    private DetectLocalChangesPage _detectLocalChangesPage;

    public CreateReviewRequestWizard(IProject project) {

        _project = project;
        setWindowTitle("Create new review request");
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {

        _detectLocalChangesPage = new DetectLocalChangesPage(_project);
        addPage(_detectLocalChangesPage);
    }

    @Override
    public boolean performFinish() {

        try {
            getContainer().run(false, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    File tmpFile = null;
                    FileReader reader = null;
                    try {
                        ISVNRepositoryLocation svnRepository = _detectLocalChangesPage.getSvnRepositoryLocation();
                        ISVNClientAdapter svnClient = svnRepository.getSVNClient();
                        ReviewboardClient rbClient = _detectLocalChangesPage.getReviewboardClient();
                        Repository reviewBoardRepository = _detectLocalChangesPage.getReviewBoardRepository();
                        
                        ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(_project);

                        tmpFile = File.createTempFile("ereviewboard", "diff");
                        svnClient.createPatch(_detectLocalChangesPage.getSelectedFiles().toArray(new File[_detectLocalChangesPage.getSelectedFiles().size()]),
                                _project.getLocation().toFile().getAbsoluteFile(), tmpFile, true);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        reader = new FileReader(tmpFile);
                        IOUtils.copy(reader, outputStream);

                        if (rbClient != null && reviewBoardRepository != null) {
                            ReviewRequest reviewRequest = rbClient.createReviewRequest(reviewBoardRepository,
                                    monitor);

                            System.out.println("Created review request with id " + reviewRequest.getId());

                            String basePath = projectSvnResource.getUrl().toString()
                                    .substring(svnRepository.getRepositoryRoot().toString().length());

                            System.out.println("Detected base path " + basePath);

                            TaskRepository repository = _detectLocalChangesPage.getTaskRepository();

                            Diff diff = rbClient.createDiff(reviewRequest.getId(), basePath,
                                    outputStream.toByteArray(), new NullProgressMonitor());

                            System.out.println("Diff created.");

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
                    }
                }
            });
        } catch (InvocationTargetException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

}
