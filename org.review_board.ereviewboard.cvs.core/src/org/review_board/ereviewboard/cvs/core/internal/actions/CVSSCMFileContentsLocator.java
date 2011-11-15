package org.review_board.ereviewboard.cvs.core.internal.actions;
/*******************************************************************************
 * Copyright (c) 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.review_board.ereviewboard.core.internal.scm.SCMFileContentsLocator;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.review_board.ereviewboard.cvs.core.internal.Activator;
import org.review_board.ereviewboard.cvs.core.internal.TraceLocation;

/**
 * @author Robert Munteanu
 *
 */
public class CVSSCMFileContentsLocator implements SCMFileContentsLocator {
    
    // copied from org.eclipse.mylyn.versions.core.ScmCore.findResource
    private static IResource findResource(String file) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IPath path = new Path(file);
        path.makeRelative();
        while (path.segmentCount() > 1) {
            IResource resource = root.findMember(path);
            if (resource != null) {
                return resource;
            }
            path = path.removeFirstSegments(1);
        }
        return null;
    }

    private Repository _codeRepository;
    private String _filePath;
    private String _revision;

    public void init(Repository codeRepository, String filePath, String revision) {

        _codeRepository = codeRepository;
        _filePath = filePath;
        _revision = revision;
    }

    public boolean isEnabled() {

        return _codeRepository != null && _codeRepository.getTool() == RepositoryType.CVS;
    }

    public byte[] getContents(IProgressMonitor monitor) throws CoreException {
        
        if ( FileDiff.PRE_CREATION.equals(_revision) )
            return new byte[0];
        
        CVSRepositoryLocation reviewBoardRepositoryLocation = CVSRepositoryLocation.fromString(_codeRepository.getPath());
        
        ICVSRepositoryLocation[] locations = CVSProviderPlugin.getPlugin().getKnownRepositories();
        
        for (ICVSRepositoryLocation repositoryLocation : locations) {
            
            String location = repositoryLocation.getLocation(false);
            
            Activator.getDefault().trace(TraceLocation.DIFF, "Considering " + location + " to match with " + reviewBoardRepositoryLocation);
            
            if ( !repositoryLocation.getMethod().getName().equals("pserver") )
                continue;
            
            if ( sameLocation(repositoryLocation, reviewBoardRepositoryLocation) ) {
                
                String unqualifiedPath = _filePath.substring(repositoryLocation.getRootDirectory().length()).replace(",v", "");
                
                Activator.getDefault().trace(TraceLocation.DIFF, NLS.bind("looking for {0} at {1}", new Object[] { unqualifiedPath, _revision }));
                
                IResource fileResource = findResource(unqualifiedPath);
                if ( fileResource == null )
                    throw new CoreException(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "No workspace resource found for  " + unqualifiedPath));
                
                RepositoryProvider provider = RepositoryProvider.getProvider(fileResource.getProject(), CVSProviderPlugin.getTypeId());
                IFileHistory history = provider.getFileHistoryProvider().getFileHistoryFor(fileResource, IFileHistoryProvider.NONE, monitor);
                
                IFileRevision revision = history.getFileRevision(_revision);
                if ( revision == null )
                    throw new CoreException(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "The file " + unqualifiedPath + " does not have any contents for revision " + _revision));
                IStorage storage = revision.getStorage(monitor);
                
                InputStream contents = storage.getContents();
                
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    
                    IOUtils.copy(contents, outputStream);
                
                    Activator.getDefault().trace(TraceLocation.DIFF, "Retrieved remote file history" + history);
                    
                    return outputStream.toByteArray();
                    
                } catch (IOException e) {
                    throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error communicating with the repository : " + e.getMessage(), e));
                } finally {
                    IOUtils.closeQuietly(contents);
                }
            }
        }

        throw new CoreException(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "No repository found matching " + reviewBoardRepositoryLocation.getLocation(true)));
        
    }

    private boolean sameLocation(ICVSRepositoryLocation repositoryLocation, CVSRepositoryLocation reviewBoardRepositoryLocation) {

        return repositoryLocation.getHost().equals(reviewBoardRepositoryLocation.getHost())
                && repositoryLocation.getPort() == reviewBoardRepositoryLocation.getPort()
                && repositoryLocation.getRootDirectory().equals(reviewBoardRepositoryLocation.getRootDirectory());
    }
}
