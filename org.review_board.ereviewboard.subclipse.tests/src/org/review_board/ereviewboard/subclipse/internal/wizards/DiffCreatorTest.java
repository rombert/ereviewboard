package org.review_board.ereviewboard.subclipse.internal.wizards;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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

        // add new file
        File parentDir = new File(workingCopy, "dir");
        assertTrue("Failed creating dir", parentDir.mkdir());

        File nestedParentDir = new File(parentDir, "nested");
        assertTrue("Failed creating dir", nestedParentDir.mkdir());

        File file = new File(nestedParentDir, "first.txt");
        assertTrue("Failed creating " + file, file.createNewFile());
        IOUtils.write("Some data\n", new FileOutputStream(file));

        svnClient.addDirectory(parentDir, true);
        long rev = svnClient.commit(new File[] { parentDir, nestedParentDir, file }, "Initial import", true);

        // update file
        IOUtils.write("Some other data\n", new FileOutputStream(file));

        // create diff
        DiffCreator dc = new DiffCreator();
        byte[] diff = dc.createDiff(Collections.singleton(new ChangedFile(file, null, ".")), workingCopy, svnClient);

        assertNotNull("diff", diff);
        String stringDiff = new String(diff);
        String[] diffLines = stringDiff.split("\\n");
        String indexLine = diffLines[0];
        String removedLine = diffLines[2];
        String addedLine = diffLines[3];

        assertThat("Patch has invalid index line", indexLine, equalTo("Index: dir/nested/first.txt"));
        assertThat("Patch has invalid --- line", removedLine, equalTo("--- dir/nested/first.txt\t(revision " + rev+")"));
        assertThat("Patch has invalid +++ line", addedLine, equalTo("+++ dir/nested/first.txt\t(working copy)"));
    }

}
