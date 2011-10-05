package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.RepositoryProvider;
import org.review_board.ereviewboard.core.ReviewboardClientManager;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.tigris.subversion.subclipse.core.*;
import org.tigris.subversion.subclipse.core.client.StatusAndInfoCommand;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * The <tt>DetectLocalChangesPage</tt> shows the local changes
 * 
 * <p>It also allows selection of the resources to be included in the review request.</p>
 * 
 * @author Robert Munteanu
 *
 */
class DetectLocalChangesPage extends WizardPage {

    private final IProject _project;
    private Table _table;
    private final Set<File> _selectedFiles = new HashSet<File>();
    private ISVNRepositoryLocation svnRepositoryLocation;
    private Repository _reviewBoardRepository;
    private TaskRepository _taskRepository;
    private Label _foundRbRepositoryLabel;
    private Label _foundSvnRepositoryLabel;
    private final CreateReviewRequestWizardContext _context;

    public DetectLocalChangesPage(IProject project, CreateReviewRequestWizardContext context) {

        super("Detect local changes");
        
        setMessage("Select the changes to submit for review. The ReviewBoard instance and the SVN repository have been auto-detected.", IMessageProvider.INFORMATION);
        _project = project;
        _context = context;
    }

    @Override
    public void createControl(Composite parent) {

        Composite layout = new Composite(parent, SWT.NONE);
        
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(layout);
        
        Label rbRepositoryLabel = new Label(layout, SWT.NONE);
        rbRepositoryLabel.setText("Reviewboard repository :");
        
        _foundRbRepositoryLabel = new Label(layout, SWT.NONE);
        _foundRbRepositoryLabel.setText("Pending...");
        
        Label svnRepositoryLabel = new Label(layout, SWT.NONE);
        svnRepositoryLabel.setText("SVN repository :");
        
        _foundSvnRepositoryLabel = new Label(layout, SWT.NONE);
        _foundSvnRepositoryLabel.setText("Pending...");
        
        _table = new Table(layout, SWT.BORDER | SWT.VIRTUAL);
        _table.setLinesVisible (true);
        _table.setHeaderVisible (true);

        GridDataFactory.fillDefaults().span(2, 1).hint(500, SWT.DEFAULT).applyTo(_table);
        TableColumn includeColumn = new TableColumn(_table, SWT.NONE);
        includeColumn.setText("Include");
        
        TableColumn typeColumn = new TableColumn(_table, SWT.NONE);
        typeColumn.setText("Change type");

        TableColumn fileColumn = new TableColumn(_table, SWT.NONE);
        fileColumn.setText("File");

        setControl(layout);
        
        populate();
    }
    
    private void populate() {

        try {
            getWizard().getContainer().run(false, true, new IRunnableWithProgress() {
                
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                
                    SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(_project, SVNProviderPlugin.getTypeId());
                    
                    Assert.isNotNull(svnProvider, "No " + SVNTeamProvider.class.getSimpleName() + " for " + _project);
                    
                    ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(_project);
                    
                    ReviewboardClientManager clientManager = ReviewboardCorePlugin.getDefault().getConnector().getClientManager();
                    ReviewboardClient rbClient = null;
                    Repository reviewBoardRepository = null;
                    TaskRepository taskRepository = null;
                    
                    setSvnRepositoryLocation(projectSvnResource.getRepository());
                    
                    System.out.println("Local repository is " + getSvnRepositoryLocation().getRepositoryRoot().toString());
                    
                    for ( String clientUrl : clientManager.getAllClientUrl() ) {
                        
                        TaskRepository repositoryCandidate = TasksUi.getRepositoryManager().getRepository(ReviewboardCorePlugin.REPOSITORY_KIND, clientUrl);
                        ReviewboardClient client = clientManager.getClient(repositoryCandidate);
                        
                        for ( Repository repository : client.getClientData().getRepositories() ) {
                            
                            System.out.println("Considering repository of type " + repository.getTool()  + " and path " + repository.getPath());
                            
                            if ( repository.getTool() != RepositoryType.Subversion )
                                continue;
                            
                            if ( getSvnRepositoryLocation().getRepositoryRoot().toString().equals(repository.getPath()) ) {
                                reviewBoardRepository = repository;
                                taskRepository = repositoryCandidate;
                                rbClient = client;
                                break;
                            }
                        }
                    }
                    
                    setReviewboardClient(rbClient);
                    setReviewboardRepository(reviewBoardRepository);
                    setTaskRepository(taskRepository);
                    
                    if ( taskRepository != null ) {
                        _foundRbRepositoryLabel.setText(taskRepository.getRepositoryLabel());
                        _foundRbRepositoryLabel.setToolTipText(taskRepository.getUrl());
                    } else {
                        _foundRbRepositoryLabel.setText("Not found.");
                        _foundSvnRepositoryLabel.setText("Not found");
                        setErrorMessage("No repository found for SVN path " +  getSvnRepositoryLocation().getRepositoryRoot());
                        return;
                    }
                    
                    if ( reviewBoardRepository != null ) {
                        _foundSvnRepositoryLabel.setText(reviewBoardRepository.getName());
                        _foundSvnRepositoryLabel.setToolTipText(reviewBoardRepository.getPath());
                    } else {
                        _foundSvnRepositoryLabel.setText("Not found");
                        setErrorMessage("No repository found for SVN path " +  getSvnRepositoryLocation().getRepositoryRoot());
                        return;
                    }
                    

                    try {
                        LocalResourceStatus status = projectSvnResource.getStatus();
                        
                        Assert.isNotNull(status, "No status for resource " + projectSvnResource);
                        
                        ISVNClientAdapter svnClient = getSvnRepositoryLocation().getSVNClient();
                        
                        ISVNStatus[] statuses = svnClient.getStatus(_project.getLocation().toFile(), true, false);
                        
                        for ( ISVNStatus svnStatus : statuses ) {
                            
                            if ( SVNStatusKind.UNVERSIONED.equals(svnStatus.getTextStatus()) )
                                continue;
                            
                            TableItem item = new TableItem (_table, SWT.NONE);
                            
                            TableEditor editor = new TableEditor(_table);
                            Button checkbox = new Button(_table, SWT.CHECK);
                            checkbox.setData(svnStatus.getFile());
                            _selectedFiles.add(svnStatus.getFile());
                            checkbox.addSelectionListener(new SelectionListener() {
                                
                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                
                                    Button source =  (Button) e.getSource();
                                    
                                    if ( source.getSelection() )
                                        _selectedFiles.add((File) source.getData());
                                    else
                                        _selectedFiles.remove(source.getData());
                                    
                                    System.out.println("Now we have " + _selectedFiles.size() + " selected files.");
                                    
                                    if ( _selectedFiles.isEmpty() ) {
                                        setErrorMessage("Please select at least one change to submit for review.");
                                    } else {
                                        setErrorMessage(null);
                                    }
                                    
                                    getContainer().updateButtons();
                                    
                                }
                                
                                @Override
                                public void widgetDefaultSelected(SelectionEvent e) {
                                    widgetSelected(e);
                                }
                            });
                            checkbox.setSelection(true);
                            checkbox.pack();
                            editor.minimumWidth = checkbox.getSize ().x;
                            editor.horizontalAlignment = SWT.LEFT;
                            editor.setEditor(checkbox, item, 0);
                            item.setText(1, svnStatus.getTextStatus().toString());
                            item.setText(2, svnStatus.getUrlString().substring(projectSvnResource.getUrl().toString().length()));
                        }
                        
                        for ( int i = 0 ; i < _table.getColumnCount(); i ++ )
                            _table.getColumn(i).pack();

                        if ( _selectedFiles.isEmpty() ) {
                            setErrorMessage("No changes found in the repository which can be used to create a diff.");
                            return;
                        }
                    } catch (SVNException e) {
                        throw new InvocationTargetException(e);
                    } catch (SVNClientException e) {
                        throw new InvocationTargetException(e);
                    }
                }

            });
        } catch (InvocationTargetException e) {
            setErrorMessage(e.getMessage());
        } catch (InterruptedException e) {
            setErrorMessage(e.getMessage());
        }
    }
    
    @Override
    public boolean isPageComplete() {
    
        return super.isPageComplete() && getTaskRepository() != null && getReviewBoardRepository() != null && getSelectedFiles().size() > 0 ;
    }
    
    public Set<File> getSelectedFiles() {
        
        return _selectedFiles;
    }

    public ISVNRepositoryLocation getSvnRepositoryLocation() {

        return svnRepositoryLocation;
    }
    
    public Repository getReviewBoardRepository() {

        return _reviewBoardRepository;
    }
    
    public TaskRepository getTaskRepository() {

        return _taskRepository;
    }

    void setSvnRepositoryLocation(ISVNRepositoryLocation svnRepositoryLocation) {

        this.svnRepositoryLocation = svnRepositoryLocation;
    }

    void setReviewboardClient(ReviewboardClient rbClient) {
        
        _context.setReviewboardClient(rbClient);
    }
    
    void setReviewboardRepository(Repository reviewBoardRepository) {

        _reviewBoardRepository = reviewBoardRepository;
    }
    
    void setTaskRepository(TaskRepository taskRepository) {

        _taskRepository = taskRepository;
    }
}
