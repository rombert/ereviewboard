package org.review_board.ereviewboard.subclipse.internal.wizards;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.subversion.javahl.ClientException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class DiffCreatorTest {

    @Rule
    public TemporaryFolder repo = new TemporaryFolder();
    private File repos;
    private File workingCopy;
    private ISVNClientAdapter svnClient;
    private File parentDir;
    private File nestedParentDir;
    private File file;
    private long rev;

    @Before
    public void createRepo() throws SVNException, SVNClientException, MalformedURLException, IOException,
            FileNotFoundException {

        svnClient = SVNProviderPlugin.getPlugin().getSVNClient();

        repos = new File(repo.getRoot(), "repo");
        svnClient.createRepository(repos, "force");

        workingCopy = repo.newFolder("wc");
        svnClient.checkout(new SVNUrl("file://" + repos.getAbsolutePath()), workingCopy, SVNRevision.HEAD,
                true);

        parentDir = new File(workingCopy, "dir");
        assertTrue("Failed creating dir", parentDir.mkdir());

        nestedParentDir = new File(parentDir, "nested");
        assertTrue("Failed creating dir", nestedParentDir.mkdir());

        file = new File(nestedParentDir, "first.txt");
        assertTrue("Failed creating " + file, file.createNewFile());
        IOUtils.write("Some data\n", new FileOutputStream(file));

        svnClient.addDirectory(parentDir, true);
        rev = svnClient.commit(new File[] { parentDir, nestedParentDir, file }, "Initial import", true);
    }

    @Test
    public void createDiffForSimpleChange() throws IOException, ClientException, SVNClientException, SVNException,
            ParseException {

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
        assertThat("Patch has invalid --- line", removedLine, equalTo("--- dir/nested/first.txt\t(revision " + rev
                + ")"));
        assertThat("Patch has invalid +++ line", addedLine, equalTo("+++ dir/nested/first.txt\t(working copy)"));
    }

    @Test
    public void createDiffForRename() throws SVNClientException, IOException {

        File newFile = new File(nestedParentDir, "moved.txt");

        svnClient.move(file, newFile, false);
        IOUtils.write("Some data\n", new FileOutputStream(newFile));

        DiffCreator dc = new DiffCreator();
        Set<ChangedFile> changes = new HashSet<ChangedFile>();
        changes.add(new ChangedFile(newFile, SVNStatusKind.ADDED, "dir/nested/moved.txt", "dir/nested/first.txt"));
        changes.add(new ChangedFile(file, SVNStatusKind.DELETED, "dir/nested/first.txt"));

        byte[] diff = dc.createDiff(changes, workingCopy, svnClient);

        assertNotNull("diff", diff);
        String stringDiff = new String(diff);

        String[] diffLines = stringDiff.split("\\n");
        String indexLine = diffLines[0];
        String removedLine = diffLines[2];
        String addedLine = diffLines[3];

        assertThat("Patch has invalid index line", indexLine, equalTo("Index: dir/nested/first.txt"));
        assertThat("Patch has invalid --- line", removedLine, equalTo("--- dir/nested/first.txt\t(revision 0)"));
        assertThat("Patch has invalid +++ line", addedLine, equalTo("+++ dir/nested/moved.txt\t(working copy)"));

    }
}
