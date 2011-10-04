package org.review_board.ereviewboard.subclipse.internal.actions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IActionDelegate;
import org.review_board.ereviewboard.core.ReviewboardClientManager;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Diff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.tigris.subversion.subclipse.core.*;
import org.tigris.subversion.subclipse.core.client.StatusAndInfoCommand;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * @author Robert Munteanu
 */
public class CreateReviewRequestAction implements IActionDelegate {

    private IProject currentProject;
    
    public void run(IAction action) {
        
        if ( currentProject == null )
            return;
        
        SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(currentProject, SVNProviderPlugin.getTypeId());
        
        Assert.isNotNull(svnProvider, "No " + SVNTeamProvider.class.getSimpleName() + " for " + currentProject);
        
        ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(currentProject);
        
        ReviewboardClientManager clientManager = ReviewboardCorePlugin.getDefault().getConnector().getClientManager();
        ReviewboardClient rbClient = null;
        Repository reviewBoardRepository = null;
        String taskRepositoryUrl = null;
        
        ISVNRepositoryLocation svnRepository = projectSvnResource.getRepository();
        
        System.out.println("Local repository is " + svnRepository.getRepositoryRoot().toString());
        
        for ( Map.Entry<String,ReviewboardClient> clientEntry : clientManager.getAllClients().entrySet() ) {
            
            ReviewboardClient client = clientEntry.getValue();
            for ( Repository repository : client.getClientData().getRepositories() ) {
                
                System.out.println("Considering repository of type " + repository.getTool()  + " and path " + repository.getPath());
                
                if ( repository.getTool() != RepositoryType.Subversion )
                    continue;
                
                if ( svnRepository.getRepositoryRoot().toString().equals(repository.getPath()) ) {
                    taskRepositoryUrl = clientEntry.getKey();
                    rbClient = client;
                    reviewBoardRepository = repository;
                    break;
                }
            }
        }

        
        File tmpFile = null;
        Reader reader = null;
        
        try {
            LocalResourceStatus status = projectSvnResource.getStatus();
            
            Assert.isNotNull(status, "No status for resource " + projectSvnResource);
            
            StatusAndInfoCommand command = new StatusAndInfoCommand(projectSvnResource, true, false, false);
            
            command.run(new NullProgressMonitor());
            
            ISVNStatus[] statuses = command.getStatuses();
            List<File> modified = new ArrayList<File>();
            List<File> added = new ArrayList<File>();
            List<File> unversioned = new ArrayList<File>();
            
            List<File> all = new ArrayList<File>();
            
            for ( ISVNStatus svnStatus : statuses ) {
                if ( SVNStatusKind.ADDED.equals(svnStatus.getTextStatus()))
                    added.add(svnStatus.getFile());
                else if ( SVNStatusKind.MODIFIED.equals(svnStatus.getTextStatus()))
                    modified.add(svnStatus.getFile());
                else if ( SVNStatusKind.UNVERSIONED.equals(svnStatus.getTextStatus()) ) {
                    unversioned.add(svnStatus.getFile());
                    continue;
                } else
                    Assert.isTrue(false, "Unhandled " + SVNStatusKind.class.getSimpleName()+ " " + svnStatus.getTextStatus() + " for file " + svnStatus.getFile());
                
                all.add(svnStatus.getFile());
            }

            ISVNClientAdapter svnClient = svnRepository.getSVNClient();
            
            tmpFile = File.createTempFile("ereviewboard", "diff");
            svnClient.createPatch(all.toArray(new File[all.size()]),currentProject.getLocation().toFile().getAbsoluteFile(), tmpFile, true);
            
            if ( reviewBoardRepository == null ) {
                MessageDialog.openError(null, "Failed creating review request", "Unable to find a matching SVN repository for " + svnRepository.getRepositoryRoot() + " .");
                return;
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            reader = new FileReader(tmpFile);
            IOUtils.copy(reader, outputStream);
            
            if ( rbClient != null && reviewBoardRepository != null ) {
                ReviewRequest reviewRequest = rbClient.createReviewRequest(reviewBoardRepository, new NullProgressMonitor());
                
                System.out.println("Created review request with id " + reviewRequest.getId());
                
                String basePath = projectSvnResource.getUrl().toString().substring(svnRepository.getRepositoryRoot().toString().length());
                
                System.out.println("Detected base path " + basePath);
                
                TaskRepository repository = TasksUi.getRepositoryManager().getRepository(ReviewboardCorePlugin.REPOSITORY_KIND, taskRepositoryUrl);
                
                Diff diff = rbClient.createDiff(reviewRequest.getId(), basePath, outputStream.toByteArray(), new NullProgressMonitor());
                
                System.out.println("Diff created.");
                
                boolean success = TasksUiUtil.openTask(repository, String.valueOf(reviewRequest.getId()));
                
                if ( !success ) {
                    MessageDialog.openWarning(null, "Failed opening task", "Review request with id " + reviewRequest.getId() + " created in repository " + reviewBoardRepository.getName() + " but the task editor could not be opened.");
                    return;
                }
            }
        } catch (SVNException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SVNClientException e) {
            throw new RuntimeException(e);
        } catch (ReviewboardException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(tmpFile);
            IOUtils.closeQuietly(reader);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {

        if ( selection instanceof IStructuredSelection ) {
            
            IStructuredSelection sel = (IStructuredSelection) selection;
            
            if ( sel.getFirstElement() instanceof IProject ) {
                currentProject = (IProject) sel.getFirstElement();
                
                return;
            }
        }
        
        currentProject = null;
    }
}
