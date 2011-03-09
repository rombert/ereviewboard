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
package org.review_board.ereviewboard.core;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 * @author Robert Munteanu
 */
public class ReviewboardCommentMapper {

    private IRepositoryPerson author;
    private String heading;
    private String top;
    private String body;
    private String bottom;

    public void setAuthor(IRepositoryPerson author) {

        this.author = author;
    }

    public void setHeading(String heading) {

        this.heading = heading;
    }

    public void setTop(String top) {

        this.top = top;
    }

    public void setBody(String body) {

        this.body = body;
    }

    public void setBottom(String bottom) {

        this.bottom = bottom;
    }

    public void applyTo(TaskData taskData, int index, Date creationDate) {

        TaskAttribute attribute = taskData.getRoot().createAttribute(
                TaskAttribute.PREFIX_COMMENT + index);

        TaskCommentMapper mapper = new TaskCommentMapper();
        mapper.setCommentId(String.valueOf(index));
        mapper.setCreationDate(creationDate);
        mapper.setAuthor(author);
        mapper.setText(buildText().toString());
        mapper.setNumber(index);

        mapper.applyTo(attribute);
    }

    /** Visible for testing only */
    public StringBuilder buildText() {

        StringBuilder text = new StringBuilder();

        boolean previousPartHadText = false;

        for (String part : new String[] { heading, top, body, bottom }) {

            if (StringUtils.isNotBlank(part)) {

                if (previousPartHadText)
                    text.append("\n\n");

                text.append(part);

                previousPartHadText = true;
            }
        }

        return text;
    }
}