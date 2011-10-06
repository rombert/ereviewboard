package org.review_board.ereviewboard.ui.editor.ext;

/**
 * @author Robert Munteanu
 */
public class DiffResource {

    private final String path;
    private final String revision;

    public DiffResource(String path, String revision) {
        this.path = path;
        this.revision = revision;
    }

    public String getPath() {
        return path;
    }

    public String getRevision() {
        return revision;
    }

}