package org.review_board.ereviewboard.ui.editor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.internal.core.patch.FileDiffResult;
import org.eclipse.compare.internal.core.patch.FilePatch2;
import org.eclipse.compare.internal.core.patch.HunkResult;
import org.eclipse.compare.internal.patch.HunkDiffNode;
import org.eclipse.compare.internal.patch.PatchCompareEditorInput;
import org.eclipse.compare.internal.patch.PatchFileDiffNode;
import org.eclipse.compare.internal.patch.UnmatchedHunkTypedElement;
import org.eclipse.compare.internal.patch.WorkspacePatcher;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.util.ResourceUtil;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;

/**
 * @author Robert Munteanu
 *
 */
public class ReviewboardDiffPart extends AbstractTaskEditorPart {

    public ReviewboardDiffPart() {
        
        setPartName("Latest Diff");
    }
    
    @Override
    public void createControl(Composite parent, FormToolkit toolkit) {

        Section section = createSection(parent, toolkit, true);
        Composite composite = toolkit.createComposite(section);
        composite.setLayout(EditorUtil.createSectionClientLayout());
        
        final TaskAttribute attribute = getTaskData().getRoot().getAttribute(ReviewboardAttributeMapper.Attribute.LATEST_DIFF.toString());
        
        for ( final TaskAttribute child : attribute.getAttributes().values() ) {
            final String sourcePath = child.getAttribute("sourceFile").getValue();
            final String sourceRevision = child.getAttribute("sourceRevision").getValue();
            Hyperlink link = toolkit.createHyperlink(composite, sourcePath + " ( " + sourceRevision + " )", SWT.NONE);
            link.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    
                    List<String> paths = ResourceUtil.getResourcePathPermutations(sourcePath);
                    
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    IResource resource = null;
                    for ( String path : paths ) {
                        resource = workspace.getRoot().findMember(path);
                        if ( resource != null )
                            break;
                    }
                    
                    if ( resource == null ) {
                        MessageDialog.openWarning(null, "Unable to find file", "Unable to find a file for " + sourcePath + " in the workspace.");
                        ReviewboardUiPlugin.getDefault().getLog().log(new Status(Status.WARNING, ReviewboardUiPlugin.PLUGIN_ID, "Unable to find a matching file for " + child.getValue() + " tried " + paths ));
                        return;
                    }
                    IFile file = (IFile) resource;
                    
                    CompareConfiguration configuration = new CompareConfiguration();
                    WorkspacePatcher patcher = new WorkspacePatcher(file);
                    ReviewBoardInput input = new ReviewBoardInput(patcher, configuration);
                    ReviewboardClient client = ReviewboardCorePlugin.getDefault().getConnector().getClientManager().getClient(new TaskRepository(ReviewboardCorePlugin.PLUGIN_ID, getTaskData().getRepositoryUrl()));
                    try {
                        // TODO move to a Job
                        byte[] rawFileDiff = client.getRawFileDiff(Integer.parseInt(getTaskData().getTaskId()), Integer.parseInt( attribute.getValue()), Integer.parseInt(child.getValue()), new NullProgressMonitor());
                        patcher.parse(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(rawFileDiff))));
                    } catch (NumberFormatException e1) {
                        ReviewboardUiPlugin.getDefault().getLog().log(new Status(Status.ERROR, ReviewboardUiPlugin.PLUGIN_ID, "Failed loading diff ", e1));
                        return;
                    } catch (ReviewboardException e1) {
                        ReviewboardUiPlugin.getDefault().getLog().log(new Status(Status.ERROR, ReviewboardUiPlugin.PLUGIN_ID, "Failed loading diff ", e1));
                        return;
                    } catch (IOException e1) {
                        ReviewboardUiPlugin.getDefault().getLog().log(new Status(Status.ERROR, ReviewboardUiPlugin.PLUGIN_ID, "Failed loading diff ", e1));
                        return;
                    }
                    
                    CompareUI.openCompareEditor(input);
                }
            });
        }
        
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        setSection(toolkit, section);
    }
    
    private static class ReviewBoardInput extends PatchCompareEditorInput {

        public ReviewBoardInput(WorkspacePatcher patcher, CompareConfiguration configuration) {
            super(patcher, configuration);
        }

        @Override
        protected void fillContextMenu(IMenuManager manager) {

        }
        
        @Override
        protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException,
                InterruptedException {
            
            // Refresh the patcher state
            getPatcher().refresh();
            
            // Build the diff tree
            processDiffs(getPatcher().getDiffs());
            
            return  super.prepareInput(monitor);
        }
        
        // Copied from superclass
        private void processDiffs(FilePatch2[] diffs) { 
            for (int i = 0; i < diffs.length; i++) {
                processDiff(diffs[i], getRoot());
            }
        }
        
        // Copied from superclass
        private void processDiff(FilePatch2 diff, DiffNode parent) {
            FileDiffResult diffResult = getPatcher().getDiffResult(diff);
            PatchFileDiffNode node = PatchFileDiffNode.createDiffNode(parent, diffResult);
            HunkResult[] hunkResults = diffResult.getHunkResults();
            for (int i = 0; i < hunkResults.length; i++) {
                HunkResult hunkResult = hunkResults[i];
                if (!hunkResult.isOK()) {
                    HunkDiffNode hunkNode = HunkDiffNode.createDiffNode(node, hunkResult, true);
                    Object left = hunkNode.getLeft();
                    if (left instanceof UnmatchedHunkTypedElement) {
                        UnmatchedHunkTypedElement element = (UnmatchedHunkTypedElement) left;
                        element.addContentChangeListener(new IContentChangeListener() {
                            public void contentChanged(IContentChangeNotifier source) {
                                if (getViewer() == null || getViewer().getControl().isDisposed())
                                    return;
                                getViewer().refresh(true);
                            }
                        });
                    }
                } else if (isShowMatched()) {
                    HunkDiffNode.createDiffNode(node, hunkResult, false, true, false);
                }
            }
        }
    }

}
