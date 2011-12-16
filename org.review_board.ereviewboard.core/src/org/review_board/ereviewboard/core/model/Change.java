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

import java.util.Date;
import java.util.List;

public class Change {

    public enum Field {
        summary, description, testing_done, diff, bugs_closed, file_attachments, screenshots,
        target_people, target_groups, screenshot_captions, branch, status;
    }
    
    private final int _id;
    private final String _text;
    private final Date _timestamp;
    private final List<FieldChange> _fieldsChanged;
    
    public Change(int id, String text, Date timestamp, List<FieldChange> fieldsChanged) {
        _id = id;
        _text = text;
        _timestamp = timestamp;
        _fieldsChanged = fieldsChanged;
    }

    public int getId() {
        return _id;
    }

    public String getText() {
        return _text;
    }

    public Date getTimestamp() {
        return _timestamp;
    }

    public List<FieldChange> getFieldsChanged() {
        return _fieldsChanged;
    }

    public static class FieldChange {
    
        private final Field _field;
        private final ObjectLink _objectLink;
        private final String _old;
        private final String _new;
        private final String _added;
        private final String _removed;
        
        public FieldChange(Field field, ObjectLink objectLink, String old, String new1, String added,
                String removed) {
            
            _field = field;
            _objectLink = objectLink;
            _old = old;
            _new = new1;
            _added = added;
            _removed = removed;
        }

        public Field getField() {
            return _field;
        }

        public ObjectLink getObjectLink() {
            return _objectLink;
        }

        public String getOld() {
            return _old;
        }

        public String getNew() {
            return _new;
        }

        public String getAdded() {
            return _added;
        }

        public String getRemoved() {
            return _removed;
        }
        
        @Override
        public String toString() {
            return "Change [field : " +_field+"]";
        }
    }
    
    @Override
    public String toString() {
        return "Change [ id: " + _id + ", fieldsChanged : " + _fieldsChanged + "]";
    }
}
