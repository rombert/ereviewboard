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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.ExtConnectionMethod;
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
                
                CVSTag tag = new CVSTag(_revision, CVSTag.VERSION);
                
                Activator.getDefault().trace(TraceLocation.DIFF, NLS.bind("looking for {0} at {1}", new Object[] { unqualifiedPath, tag }));
                
                ICVSRemoteFile remoteFile = repositoryLocation.getRemoteFile(unqualifiedPath, tag);
                
                if ( !remoteFile.exists(monitor) )
                    continue;
                
                InputStream contents = remoteFile.getContents(monitor);
                
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    
                    IOUtils.copy(contents, outputStream);
                
                    Activator.getDefault().trace(TraceLocation.DIFF, "Retrieved remote file " + remoteFile);
                    
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
