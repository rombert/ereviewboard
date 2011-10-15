package org.review_board.ereviewboard.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.compare.patch.ApplyPatchOperation;
import org.eclipse.compare.patch.IFilePatch;
import org.eclipse.compare.patch.IHunk;
import org.eclipse.core.runtime.CoreException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.review_board.ereviewboard.core.util.ByteArrayStorage;

/**
 * @author Robert Munteanu
 */
public class DiffCommentLineMapperTest {
    //
    // This tests avoids becoming a plug-in based test by building stub hunks instead of parsing diff files
    // 
    // To reliably verify that hunks are generated correctly:
    // 1. write a diff file under /testdata/diffs/
    // 2. build your stub hunks using the HunkBuilder
    // 3. compare the logging output with
    //    printHunkOffsets(parseHunks("new_file.diff"));
    //    printHunkOffsets(new HunkBuilder().newHunk(... , ...).getHunks());

    @Test
    public void diffWithChangedLine() throws CoreException, IOException {

        // based on changed_line.diff
        IHunk[] hunks = new HunkBuilder().newHunk(0, ' ', '-', '+', ' ').getHunks();
        
        DiffCommentLineMapper mapper = new DiffCommentLineMapper(hunks);
        Assert.assertThat(mapper.getLineMappings(0), CoreMatchers.is(new int[] { 0, 0 }));
        Assert.assertThat(mapper.getLineMappings(1), CoreMatchers.is(new int[] { 1, 1 }));
        Assert.assertThat(mapper.getLineMappings(2), CoreMatchers.is(new int[] { 2, 2 }));
    }

    @Test
    @Ignore
    public void diffWithLineAddedAtTheEnd() throws CoreException, IOException {

        // based on line_added_at_end.diff
        IHunk[] hunks = new HunkBuilder().newHunk(0, ' ', ' ', '+').getHunks();
        
        DiffCommentLineMapper mapper = new DiffCommentLineMapper(hunks);
        Assert.assertThat(mapper.getLineMappings(0), CoreMatchers.is(new int[] { 0, 0 }));
        Assert.assertThat(mapper.getLineMappings(1), CoreMatchers.is(new int[] { 1, 1 }));
        Assert.assertThat(mapper.getLineMappings(2), CoreMatchers.is(new int[] { -1, 2 }));
    }
    
    @Test
    @Ignore
    public void diffWithLineRemovedInTheMiddle() throws CoreException, IOException {
      
        // based on line_removed_in_the_middle.diff
        IHunk[] hunks = new HunkBuilder().newHunk(0, ' ', '-', ' ').getHunks();
        
        DiffCommentLineMapper mapper = new DiffCommentLineMapper(hunks);
        Assert.assertThat(mapper.getLineMappings(0), CoreMatchers.is(new int[] { 0, 0 }));
        Assert.assertThat(mapper.getLineMappings(1), CoreMatchers.is(new int[] { 1, -1 }));
        Assert.assertThat(mapper.getLineMappings(2), CoreMatchers.is(new int[] { 2, 1 }));
    }
    
    @SuppressWarnings("unused")
    private void printHunkOffsets(IHunk[] hunks) {

        for (IHunk hunk : hunks) {
            System.out.println("Hunk START " + hunk.getStartPosition());
            int startPosition = hunk.getStartPosition();

            for (int linePosition = 0; linePosition < hunk.getUnifiedLines().length; linePosition++) {
                char firstChar = hunk.getUnifiedLines()[linePosition].charAt(0);
                System.out.println((startPosition + linePosition) + " - " + firstChar);
            }
        }
    }

    @SuppressWarnings("unused")
    private static IHunk[] parseHunks(String diffFile) throws CoreException, IOException {

        InputStream resourceAsStream = DiffCommentLineMapperTest.class.getResourceAsStream("/diffs/" + diffFile);
        if ( resourceAsStream == null )
            throw new IllegalArgumentException("Could not locate resource '/diffs/" + diffFile+ "' in the classpath");
        
        IFilePatch[] patch = ApplyPatchOperation.parsePatch(new ByteArrayStorage(
                IOUtils.toByteArray(resourceAsStream)));

        if (patch.length != 1)
            throw new IllegalArgumentException("Expected 1 patch, got " + patch.length);

        return patch[0].getHunks();
    }

    static class HunkBuilder {

        private final List<IHunk> hunks = new ArrayList<IHunk>();

        public HunkBuilder newHunk(int start, char... unifiedLinePrefixes) {

            hunks.add(new StubHunk(start, unifiedLinePrefixes));

            return this;
        }

        public IHunk[] getHunks() {

            return hunks.toArray(new IHunk[hunks.size()]);
        }
    }

    static class StubHunk implements IHunk {

        private final int startPosition;
        private final String[] unifiedLines;

        public StubHunk(int startPosition, char... unifiedLinechars) {
            this.startPosition = startPosition;
            this.unifiedLines = new String[unifiedLinechars.length];
            for (int i = 0; i < unifiedLinechars.length; i++)
                unifiedLines[i] = unifiedLinechars[i] + "asdf";
        }

        public String getLabel() {
            return "Stub Hunk at " + startPosition;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public String[] getUnifiedLines() {

            return unifiedLines;
        }

        public InputStream getOriginalContents() {
            throw new UnsupportedOperationException();
        }

        public InputStream getPatchedContents() {
            throw new UnsupportedOperationException();
        }

        public String getCharset() throws CoreException {
            return "UTF-8";
        }

    }

}
