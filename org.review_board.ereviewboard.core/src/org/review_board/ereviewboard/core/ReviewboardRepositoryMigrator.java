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

import org.eclipse.mylyn.tasks.core.AbstractRepositoryMigrator;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Robert Munteanu
 */
public class ReviewboardRepositoryMigrator extends AbstractRepositoryMigrator {

    @Override
    public String getConnectorKind() {
        
        return ReviewboardCorePlugin.REPOSITORY_KIND;
    }
    
    @Override
    public boolean migrateRepository(TaskRepository repository) {
        
        return new ReviewboardRepositoryMapper(repository).setCategoryIfNotSet();
    }

}
