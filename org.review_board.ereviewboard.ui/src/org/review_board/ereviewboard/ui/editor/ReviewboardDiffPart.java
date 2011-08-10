package org.review_board.ereviewboard.ui.editor;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.FileEditorInput;
import org.review_board.ereviewboard.core.ReviewboardAttributeMapper;
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
                    
                    try {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                        
                        IEditorDescriptor desc = editorRegistry.getDefaultEditor(file.getName());
                        if ( desc == null )
                            desc = editorRegistry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
                            
                        page.openEditor(new FileEditorInput(file), desc.getId());
                    } catch (PartInitException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            });
        }
        
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        setSection(toolkit, section);
    }
    
}
