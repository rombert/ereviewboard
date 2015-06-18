package org.review_board.ereviewboard.subclipse.core.internal.actions;
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


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.review_board.ereviewboard.core.internal.scm.SCMFileContentsLocator;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.review_board.ereviewboard.subclipse.core.internal.Activator;
import org.review_board.ereviewboard.subclipse.core.internal.TraceLocation;
import org.tigris.subversion.subclipse.core.repo.SVNRepositoryLocation;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Robert Munteanu
 *
 */
public class SubclipseSCMFileContentsLocator implements SCMFileContentsLocator {

    private Repository _codeRepository;
    private String _filePath;
    private String _revision;

    public void init(Repository codeRepository, String filePath, String revision) {

        _codeRepository = codeRepository;
        _filePath = filePath;
        _revision = revision;
    }

    public boolean isEnabled() {

        return _codeRepository != null && _codeRepository.getTool() == RepositoryType.Subversion;
    }

    public byte[] getContents(IProgressMonitor monitor) throws CoreException{
        
        if ( FileDiff.PRE_CREATION.equals(_revision) )
            return new byte[0];

        try {
            SVNRepositoryLocation repoLocation = SVNRepositoryLocation.fromString(_codeRepository.getPath());
            
            Activator.getDefault().trace(TraceLocation.MAIN, "Retrieving file " + _filePath + " @ " + _revision + " from repo " + repoLocation.getLabel() + " ( " + repoLocation.getLocation() + " )");
            
            SVNUrl resourceUrl;
            try {
            	new URL(_filePath);
            	resourceUrl = new SVNUrl(_filePath);
            }
            catch (MalformedURLException ex) {
            	resourceUrl = repoLocation.getUrl().appendPath(_filePath);
            }
            SVNRevision revision = SVNRevision.getRevision(_revision);
            InputStream content = repoLocation.getSVNClient().getContent(resourceUrl, revision, revision);
            
            return IOUtils.toByteArray(content);
        } catch (ParseException e) {
            throw toCoreException(e);
        } catch (SVNClientException e) {
            throw toCoreException(e);
        } catch (IOException e) {
            throw toCoreException(e);
        }
        
    }

    private CoreException toCoreException(Exception e) {

        return new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed getting contents of " + _filePath + " @ " +_revision + " : " + e.getMessage(), e));
    }
}
