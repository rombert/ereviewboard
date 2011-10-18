package org.review_board.ereviewboard.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.reviews.ui.ProgressDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.review_board.ereviewboard.core.exception.ReviewboardException;

public abstract class ReviewboardDialog extends ProgressDialog {

    public ReviewboardDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public boolean close() {
        if (getReturnCode() == OK) {
            boolean shouldClose = execute();
            if (!shouldClose) {
                return false;
            }
        }
        return super.close();
    }

    private boolean execute() {
        try {
            run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                        InterruptedException {
                    try {
                        executeAction(monitor);
                    } catch (ReviewboardException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
            
            return true;
        } catch (InvocationTargetException e1) {
            setMessage(e1.getCause().getMessage(), IMessageProvider.ERROR);
            return false;
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            return true;
        }
    }
    
    @Override
    protected Control createPageControls(Composite parent) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(1).margins(4, 4).applyTo(composite);
        
        createPageControlsWithComposite(composite);
        
        return composite;
    }

    protected abstract void createPageControlsWithComposite(Composite composite);

    protected abstract void executeAction(IProgressMonitor monitor) throws ReviewboardException;

}