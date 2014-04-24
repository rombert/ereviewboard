package org.review_board.ereviewboard.subclipse.internal.wizards;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.subversion.javahl.ClientException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class DiffCreatorTest {

    @Rule
    public TemporaryFolder repo = new TemporaryFolder();

    @Test
    public void diffCreatorStuff() throws IOException, ClientException, SVNClientException, SVNException,
            ParseException {

        ISVNClientAdapter svnClient = SVNProviderPlugin.getPlugin().getSVNClient();

        // create svn repo
        File repos = new File(repo.getRoot(), "repo");
        svnClient.createRepository(repos, "force");

        // checkout repo
        File workingCopy = repo.newFolder("wc");
        svnClient.checkout(new SVNUrl("file://" + repos.getAbsolutePath()), workingCopy, SVNRevision.HEAD,
                true);

        System.out.println("Checked out repo at " + workingCopy);

        // add new file
        File parentDir = new File(workingCopy, "dir");
        assertTrue("Failed creating dir", parentDir.mkdir());

        File file = new File(parentDir, "first.txt");
        assertTrue("Failed creating " + file, file.createNewFile());
        IOUtils.write("Some data\n", new FileOutputStream(file));

        svnClient.addDirectory(parentDir, true);
        long rev = svnClient.commit(new File[] { parentDir, file }, "Initial import", true);

        System.out.println("Commited a file in r" + rev);

        // update file
        IOUtils.write("Some other data\n", new FileOutputStream(file));

        // create diff
        DiffCreator dc = new DiffCreator();
        byte[] diff = dc.createDiff(Collections.singleton(new ChangedFile(file, null, ".")), parentDir, svnClient);

        System.out.println("Got diff " + new String(diff));

        assertNotNull("diff", diff);
    }

}
