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

import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * Maps between Mylyn and Reviewboard repository properties
 * 
 * @author Robert Munteanu
 */
public class ReviewboardRepositoryMapper {

    private final TaskRepository repository;

    public ReviewboardRepositoryMapper(TaskRepository repository) {
        this.repository = repository;
    }
    
    /**
     * @return true if a new category was set, false otherwise
     */
    public boolean setCategoryIfNotSet() {
        
        if ( repository.getProperty(IRepositoryConstants.PROPERTY_CATEGORY) == null ) {
            repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY, IRepositoryConstants.CATEGORY_REVIEW);
            return true;
        }
        
        return false;
    }
}
