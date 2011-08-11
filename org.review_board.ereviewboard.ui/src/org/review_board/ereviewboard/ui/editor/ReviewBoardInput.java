/*******************************************************************************
 * Copyright (c) 2004, 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.ui.editor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.exception.ReviewboardException;

/**
 * @author Robert Munteanu
 *
 */
class ReviewBoardInput extends PatchCompareEditorInput {

    private final ReviewboardClient client;
    private int taskId;
    private int diffRevision;
    private int fileId;

    public ReviewBoardInput(IFile targetFile, CompareConfiguration configuration, ReviewboardClient client, int taskId, int diffRevision, int fileId) {
        super(new WorkspacePatcher(targetFile), configuration);
        
        this.client = client;
        this.taskId = taskId;
        this.diffRevision = diffRevision;
        this.fileId= fileId;
    }

    @Override
    public boolean canRunAsJob() {
        return true;
    }
    
    @Override
    protected void fillContextMenu(IMenuManager manager) {

    }
    
    @Override
    protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {

        try {
            byte[] rawFileDiff = client.getRawFileDiff(taskId, diffRevision, fileId, new NullProgressMonitor());
            getPatcher().parse(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(rawFileDiff))));

            // Refresh the patcher state
            getPatcher().refresh();
            
            // Build the diff tree
            processDiffs(getPatcher().getDiffs());
            
            return super.prepareInput(monitor);
        } catch (ReviewboardException e) {
            throw new InvocationTargetException(e);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
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