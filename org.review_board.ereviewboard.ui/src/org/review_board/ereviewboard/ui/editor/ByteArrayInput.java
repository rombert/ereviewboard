package org.review_board.ereviewboard.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * @author Robert Munteanu
 *
 */
// Copied from org.eclipse.mylyn.internal.reviews.ui.operations.ByteArrayInput
class ByteArrayInput implements ITypedElement, IStreamContentAccessor {

    private final byte[] content;

    private final String name;

    public ByteArrayInput(byte[] content, String name) {
        this.content = content;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Image getImage() {
        return null;
    }

    public String getType() {
        String extension = FilenameUtils.getExtension(name);
        return extension != null && extension.length() > 0 ? extension : ITypedElement.TEXT_TYPE;
    }

    public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(content);
    }

}