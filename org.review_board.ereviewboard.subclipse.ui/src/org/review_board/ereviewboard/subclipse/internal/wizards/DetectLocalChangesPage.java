package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.team.core.RepositoryProvider;
import org.review_board.ereviewboard.core.ReviewboardClientManager;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.subclipse.Activator;
import org.review_board.ereviewboard.subclipse.TraceLocation;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

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
    private final Set<ChangedFile> _selectedFiles = new HashSet<ChangedFile>();
    private ISVNRepositoryLocation svnRepositoryLocation;
    private Repository _reviewBoardRepository;
    private TaskRepository _taskRepository;
    private Label _foundRbRepositoryLabel;
    private Label _foundSvnRepositoryLabel;
    private final CreateReviewRequestWizardContext _context;
    private boolean _alreadyPopulated;
    private final ReviewRequest _reviewRequest;

    public DetectLocalChangesPage(IProject project, CreateReviewRequestWizardContext context, ReviewRequest reviewRequest) {

        super("Detect local changes", "Detect local changes", null);
        
        setMessage("Select the changes to submit for review. The ReviewBoard instance and the SVN repository have been auto-detected.", IMessageProvider.INFORMATION);
        _project = project;
        _context = context;
        _reviewRequest = reviewRequest;
    }

    public void createControl(Composite parent) {

        Composite layout = new Composite(parent, SWT.NONE);
        
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(layout);
        
        Label rbRepositoryLabel = new Label(layout, SWT.NONE);
        rbRepositoryLabel.setText("Reviewboard repository :");
        
        _foundRbRepositoryLabel = new Label(layout, SWT.NONE);
        _foundRbRepositoryLabel.setText("Unknown");
        
        Label svnRepositoryLabel = new Label(layout, SWT.NONE);
        svnRepositoryLabel.setText("SVN repository :");
        
        _foundSvnRepositoryLabel = new Label(layout, SWT.NONE);
        _foundSvnRepositoryLabel.setText("Unknown");
        
        if ( _reviewRequest != null ) {
            Label reviewRequestLabel = new Label(layout, SWT.NONE);
            reviewRequestLabel.setText("Review request :");
            
            Label reviewRequestName = new Label(layout, SWT.NONE);
            reviewRequestName.setText(_reviewRequest.getSummary());
        }
        
        _table = new Table(layout, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK);
        _table.setLinesVisible (true);
        _table.setHeaderVisible (true);

        GridDataFactory.fillDefaults().span(2, 1).hint(500, 300).grab(true, true).applyTo(_table);
        TableColumn includeColumn = new TableColumn(_table, SWT.NONE);
        includeColumn.setText("Include");
        
        TableColumn typeColumn = new TableColumn(_table, SWT.NONE);
        typeColumn.setText("Change type");

        TableColumn fileColumn = new TableColumn(_table, SWT.NONE);
        fileColumn.setText("File");
        
        _table.addListener(SWT.Selection, new Listener() {
            
            public void handleEvent(Event event) {
                
                if ( event.detail == SWT.CHECK ) {
                    
                    ChangedFile eventData = (ChangedFile) event.item.getData();
                    
                    if ( _selectedFiles.contains(eventData) )
                        _selectedFiles.remove(eventData);
                    else
                        _selectedFiles.add(eventData);
                    
                    Activator.getDefault().trace(TraceLocation.MAIN, "Number of selected files is " + _selectedFiles.size()); 
                    
                    if ( _selectedFiles.isEmpty() )
                        setErrorMessage("Please select at least one change to submit for review.");
                    else
                        setErrorMessage(null);
                    
                    getContainer().updateButtons();
                }
            }
        });

        setControl(layout);
    }
    
    @Override
    public void setVisible(boolean visible) {
    
        super.setVisible(visible);
        
        if ( visible )
            populate();
    }
    
    private void populate() {
        
        if ( _alreadyPopulated )
            return;

        try {
            getWizard().getContainer().run(false, true, new IRunnableWithProgress() {
                
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                
                    SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(_project, SVNProviderPlugin.getTypeId());
                    
                    Assert.isNotNull(svnProvider, "No " + SVNTeamProvider.class.getSimpleName() + " for " + _project);
                    
                    ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(_project);
                    
                    ReviewboardClientManager clientManager = ReviewboardCorePlugin.getDefault().getConnector().getClientManager();
                    ReviewboardClient rbClient = null;
                    Repository reviewBoardRepository = null;
                    TaskRepository taskRepository = null;
                    
                    setSvnRepositoryLocation(projectSvnResource.getRepository());
                    
                    Activator.getDefault().trace(TraceLocation.MAIN, "Local repository is " + getSvnRepositoryLocation().getRepositoryRoot().toString());
                    
                    List<String> clientUrls = clientManager.getAllClientUrl();
                    if ( clientUrls.isEmpty() ) {
                        setMessage("No Reviewboard repositories are defined. Please add one using the Task Repositories view.", IMessageProvider.WARNING);
                        return;
                    }
                    
                    boolean hasSvnRepos = false;
                    
                    for ( String clientUrl : clientUrls ) {
                        
                        TaskRepository repositoryCandidate = TasksUi.getRepositoryManager().getRepository(ReviewboardCorePlugin.REPOSITORY_KIND, clientUrl);
                        
                        if ( repositoryCandidate == null) {
                            Activator.getDefault().log(IStatus.WARNING, "No repository for clientUrl " + clientUrl +" skipping.");
                            continue;
                        }
                        
                        Activator.getDefault().trace(TraceLocation.MAIN, "Checking repository candidate " + repositoryCandidate.getRepositoryLabel());
                        
                        ReviewboardClient client = clientManager.getClient(repositoryCandidate);
                        
                        Activator.getDefault().trace(TraceLocation.MAIN, "Got reviewboardClient " + client);
                        
                        try {
                            client.updateRepositoryData(false, monitor);
                        } catch (ReviewboardException e) {
                            throw new InvocationTargetException(e, "Failed updating the repository data for " + repositoryCandidate.getRepositoryLabel() + " : " + e.getMessage());
                        } catch (RuntimeException e) {
                            throw new InvocationTargetException(e, "Failed updating the repository data for " + repositoryCandidate.getRepositoryLabel() + " : " + e.getMessage());
                        }
                        
                        Activator.getDefault().trace(TraceLocation.MAIN, "Refreshed repository data , got " + client.getClientData().getRepositories().size() + " repositories.");
                        
                        for ( Repository repository : client.getClientData().getRepositories() ) {
                            
                            Activator.getDefault().trace(TraceLocation.MAIN, "Considering repository of type " + repository.getTool()  + " and path " + repository.getPath());
                            
                            if ( repository.getTool() != RepositoryType.Subversion )
                                continue;
                            
                            hasSvnRepos = true;
                            
                            if ( getSvnRepositoryLocation().getRepositoryRoot().toString().equals(repository.getPath()) ) {
                                reviewBoardRepository = repository;
                                taskRepository = repositoryCandidate;
                                rbClient = client;
                                break;
                            }
                        }
                    }
                    
                    if ( !hasSvnRepos ) {
                        setMessage("No Subversion repositories are defined in the configured ReviewBoard servers. Please add the correspoding repositories to ReviewBoard.");
                        return;
                    }
                    
                    setReviewboardClient(rbClient);
                    setReviewboardRepository(reviewBoardRepository);
                    setTaskRepository(taskRepository);
                    
                    if ( taskRepository != null && reviewBoardRepository != null) {
                        _foundRbRepositoryLabel.setText(taskRepository.getRepositoryLabel());
                        _foundRbRepositoryLabel.setToolTipText(taskRepository.getUrl());
                        
                        _foundSvnRepositoryLabel.setText(reviewBoardRepository.getName());
                        _foundSvnRepositoryLabel.setToolTipText(reviewBoardRepository.getPath());

                    } else {
                        setErrorMessage("No SVN repository defined in ReviewBoard for path " +  getSvnRepositoryLocation().getRepositoryRoot() + ". Please ensure that the repository URL from Eclipse matches the one from ReviewBoard.");
                        return;
                    }
                    
                    try {
                        LocalResourceStatus status = projectSvnResource.getStatus();
                        
                        Activator.getDefault().trace(TraceLocation.MAIN, "SVN repository status is " + status);
                        
                        Assert.isNotNull(status, "No status for resource " + projectSvnResource);
                        
                        ISVNClientAdapter svnClient = getSvnRepositoryLocation().getSVNClient();
                        
                        ChangedFileFinder changedFileFinder = new ChangedFileFinder(projectSvnResource, svnClient);
                        
                        List<ChangedFile> changedFiles = changedFileFinder.findChangedFiles();
                        
                        Activator.getDefault().trace(TraceLocation.MAIN, "Found " + changedFiles.size() + " changed files.");
                        
                        for ( ChangedFile changedFile : changedFiles ) {
                            
                            TableItem item = new TableItem (_table, SWT.NONE);
                            
                            /*       TableEditor editor = new TableEditor(_table);
                            Button checkbox = new Button(_table, SWT.CHECK);
                            checkbox.setData(changedFile);
                            _selectedFiles.add(changedFile);
                            checkbox.addSelectionListener(new SelectionListener() {
                                
                                public void widgetSelected(SelectionEvent e) {
                                
                                    Button source =  (Button) e.getSource();
                                    
                                    if ( source.getSelection() )
                                        _selectedFiles.add((ChangedFile) source.getData());
                                    else
                                        _selectedFiles.remove(source.getData());
                                    
                                    Activator.getDefault().trace(TraceLocation.MAIN, "Now we have " + _selectedFiles.size() + " selected files.");
                                    
                                    if ( _selectedFiles.isEmpty() ) {
                                        setErrorMessage("Please select at least one change to submit for review.");
                                    } else {
                                        setErrorMessage(null);
                                    }
                                    
                                    getContainer().updateButtons();
                                    
                                }
                                
                                public void widgetDefaultSelected(SelectionEvent e) {
                                    widgetSelected(e);
                                }
                            });
                            checkbox.setSelection(true);
                            checkbox.pack();
                            editor.minimumWidth = checkbox.getSize ().x;
                            editor.horizontalAlignment = SWT.LEFT;
                            editor.setEditor(checkbox, item, 0);*/
                            item.setData(changedFile);
                            item.setText(0, "");
                            item.setText(1, changedFile.getStatusKind().toString());
                            item.setText(2, changedFile.getPathRelativeToProject());
                            
                            item.setChecked(true);
                            
                            _selectedFiles.add(changedFile);
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
        } catch ( RuntimeException e ) {
            setErrorMessage(getErrorMessage());
            Activator.getDefault().log(IStatus.ERROR, e.getMessage(), e);
        } finally {
            _alreadyPopulated = true;
        }
    }
    
    @Override
    public boolean isPageComplete() {
    
        return super.isPageComplete() && getTaskRepository() != null && getReviewBoardRepository() != null && getSelectedFiles().size() > 0 ;
    }
    
    public Set<ChangedFile> getSelectedFiles() {
        
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
