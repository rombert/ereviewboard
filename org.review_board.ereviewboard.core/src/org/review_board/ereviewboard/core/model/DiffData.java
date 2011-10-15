/*******************************************************************************
 * Copyright (c) 2004, 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert Munteanu
 * 
 */
public class DiffData implements Serializable {

    private boolean _binary;
    private List<Chunk> _chunks = new ArrayList<Chunk>();
    private List<Integer> _changedChunkIndexes = new ArrayList<Integer>();
    private boolean _newFile;
    private int _numChanges;

    public DiffData() {

    }

    public DiffData(boolean binary, List<Chunk> chunks, List<Integer> changedChunkIndexes,
            boolean newFile, int numChanges) {
        _binary = binary;
        _chunks = chunks;
        _changedChunkIndexes = changedChunkIndexes;
        _newFile = newFile;
        _numChanges = numChanges;
    }

    public boolean isBinary() {
        return _binary;
    }

    public void setBinary(boolean binary) {
        _binary = binary;
    }

    public List<Chunk> getChunks() {
        return _chunks;
    }

    public void setChunks(List<Chunk> chunks) {
        _chunks = chunks;
    }

    public List<Integer> getChangedChunkIndexes() {
        return _changedChunkIndexes;
    }

    public void setChangedChunkIndexes(List<Integer> changedChunkIndexes) {
        _changedChunkIndexes = changedChunkIndexes;
    }

    public boolean isNewFile() {
        return _newFile;
    }

    public void setNewFile(boolean newFile) {
        _newFile = newFile;
    }

    public int getNumChanges() {
        return _numChanges;
    }

    public void setNumChanges(int numChanges) {
        _numChanges = numChanges;
    }

    /**
     * A chunk contained in a {@linkplain DiffData}
     * 
     * Does not contain the meta information from ReviewBoard's API
     * 
     * @author Robert Munteanu
     */
    public static class Chunk {

        private Type _change;
        private boolean _collapsable;
        private int _index;
        private List<Line> _lines = new ArrayList<Line>();
        private int _numLines;

        public Chunk() {

        }

        public Chunk(Type change, boolean collapsable, int index, List<Line> lines, int numLines) {
            _change = change;
            _collapsable = collapsable;
            _index = index;
            _lines = lines;
            _numLines = numLines;
        }

        public Type getChange() {
            return _change;
        }

        public void setChange(Type change) {
            _change = change;
        }

        public boolean isCollapsable() {
            return _collapsable;
        }

        public void setCollapsable(boolean collapsable) {
            _collapsable = collapsable;
        }

        public int getIndex() {
            return _index;
        }

        public void setIndex(int index) {
            _index = index;
        }

        public List<Line> getLines() {
            return _lines;
        }

        public void setLines(List<Line> lines) {
            _lines = lines;
        }

        public int getNumLines() {
            return _numLines;
        }

        public void setNumLines(int numLines) {
            _numLines = numLines;
        }

    }

    /**
     * A line contained in a {@link Chunk}
     * 
     * Does not contain the text replacement indices from ReviewBoard's API
     * 
     * @author Robert Munteanu
     */
    public static class Line {

        private int _diffRowNumber;
        private int _leftFileRowNumber;
        private String _leftLineText;
        private int _rightFileRowNumber;
        private String _rightLineText;
        private boolean _whitespaceOnly;

        public Line() {

        }

        public Line(int diffRowNumber, int leftFileRowNumber, String leftLineText,
                int rightFileRowNumber, String rightLineText, boolean whitespaceOnly) {
            _diffRowNumber = diffRowNumber;
            _leftFileRowNumber = leftFileRowNumber;
            _leftLineText = leftLineText;
            _rightFileRowNumber = rightFileRowNumber;
            _rightLineText = rightLineText;
            _whitespaceOnly = whitespaceOnly;
        }

        public int getDiffRowNumber() {
            return _diffRowNumber;
        }

        public void setDiffRowNumber(int diffRowNumber) {
            _diffRowNumber = diffRowNumber;
        }

        public int getLeftFileRowNumber() {
            return _leftFileRowNumber;
        }

        public void setLeftFileRowNumber(int leftFileRowNumber) {
            _leftFileRowNumber = leftFileRowNumber;
        }

        public String getLeftLineText() {
            return _leftLineText;
        }

        public void setLeftLineText(String leftLineText) {
            _leftLineText = leftLineText;
        }

        public int getRightFileRowNumber() {
            return _rightFileRowNumber;
        }

        public void setRightFileRowNumber(int rightFileRowNumber) {
            _rightFileRowNumber = rightFileRowNumber;
        }

        public String getRightLineText() {
            return _rightLineText;
        }

        public void setRightLineText(String rightLineText) {
            _rightLineText = rightLineText;
        }

        public boolean isWhitespaceOnly() {
            return _whitespaceOnly;
        }

        public void setWhitespaceOnly(boolean whitespaceOnly) {
            _whitespaceOnly = whitespaceOnly;
        }

    }

    public enum Type {
        EQUAL, DELETE, INSERT, REPLACE;

        public static Type fromString(String type) {

            for (Type typeEnum : Type.values())
                if (typeEnum.toString().equalsIgnoreCase(type))
                    return typeEnum;

            throw new IllegalArgumentException("No Type for value " + type);
        }
    }
}
