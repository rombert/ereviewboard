package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Finds local changes for a specified <tt>location</tt>
 * 
 * <p>
 * Does not handle {@link SVNStatusKind#UNVERSIONED unversioned} files.
 * </p>
 * 
 * @author Robert Munteanu
 * 
 */
public class ChangedFileFinder {

    private final IPath _location;
    private final ISVNClientAdapter _svnClient;
    private SVNUrl _baseUrl;

    public ChangedFileFinder(ISVNLocalResource projectSvnResource, ISVNClientAdapter svnClient) {

        _location = projectSvnResource.getResource().getLocation();
        _baseUrl = projectSvnResource.getUrl();
        _svnClient = svnClient;
    }

    public List<ChangedFile> findChangedFiles() throws SVNClientException {

        ISVNStatus[] statuses = _svnClient.getStatus(_location.toFile(), true, false);

        List<ChangedFile> changedFiles = new ArrayList<ChangedFile>(statuses.length);

        for (ISVNStatus svnStatus : statuses) {

            if (SVNStatusKind.UNVERSIONED.equals(svnStatus.getTextStatus()))
                continue;

            boolean copied = svnStatus.isCopied();
            String relativePath = svnStatus.getUrlString().substring(_baseUrl.toString().length());
            if (!copied) {
                changedFiles.add(new ChangedFile(svnStatus.getFile(), svnStatus.getTextStatus(), relativePath));
            } else {
                ISVNInfo info = _svnClient.getInfoFromWorkingCopy(svnStatus.getFile());
                String copiedFromRelativePath = info.getCopyUrl().toString().substring(_baseUrl.toString().length());
                changedFiles.add(new ChangedFile(svnStatus.getFile(), svnStatus.getTextStatus(), relativePath, copiedFromRelativePath));
            }
        }

        return changedFiles;

    }
}
