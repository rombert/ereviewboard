/*******************************************************************************
 * Copyright (c) 2004 - 2009 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers, Atlassian, Sven Krzyzak
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2009 Markus Knittig
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Markus Knittig - adapted Trac, Redmine & Atlassian implementations for
 *                      Review Board
 *******************************************************************************/
package org.review_board.ereviewboard.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.review_board.ereviewboard.core.client.RestfulReviewboardClient;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.client.ReviewboardClientData;

/**
 * @author Markus Knittig
 *
 */
public class ReviewboardClientManager implements IRepositoryListener {

    private Map<String, ReviewboardClient> clientByUrl = new HashMap<String, ReviewboardClient>();

    private Map<String, ReviewboardClientData> dataByUrl = new HashMap<String, ReviewboardClientData>();

    private TaskRepositoryLocationFactory taskRepositoryLocationFactory;

    private File cacheFile;

    public ReviewboardClientManager(File cacheFile) {
        this.cacheFile = cacheFile;
        readCache();
    }

    public synchronized ReviewboardClient getClient(TaskRepository taskRepository) {
        String repositoryUrl = taskRepository.getRepositoryUrl();
        ReviewboardClient repository = clientByUrl.get(repositoryUrl);

        if (repository == null) {
            AbstractWebLocation location =
                    taskRepositoryLocationFactory.createWebLocation(taskRepository);

            ReviewboardClientData data = dataByUrl.get(repositoryUrl);
            if (data == null) {
                data = new ReviewboardClientData();
                dataByUrl.put(repositoryUrl, data);
            }

            repository = new RestfulReviewboardClient(location, data, taskRepository);
            clientByUrl.put(taskRepository.getRepositoryUrl(), repository);
        }

        return repository;
    }

    public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
        return taskRepositoryLocationFactory;
    }

    public void setTaskRepositoryLocationFactory(
            TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        this.taskRepositoryLocationFactory = taskRepositoryLocationFactory;
    }

    public void repositoryAdded(TaskRepository repository) {
        repositorySettingsChanged(repository);
    }

    public void repositoryRemoved(TaskRepository repository) {
        clientByUrl.remove(repository.getRepositoryUrl());
        dataByUrl.remove(repository.getRepositoryUrl());
    }

    public void repositorySettingsChanged(TaskRepository repository) {
        
        
        
        ReviewboardClient client = clientByUrl.get(repository.getRepositoryUrl());

        if (client != null) {
            client.refreshRepositorySettings(repository);
        }
    }

    public void repositoryUrlChanged(TaskRepository repository, String oldUrl) {
        clientByUrl.put(repository.getRepositoryUrl(), clientByUrl.remove(oldUrl));
        dataByUrl.put(repository.getRepositoryUrl(), dataByUrl.remove(oldUrl));
    }

    private void readCache() {
        if (cacheFile == null || !cacheFile.exists()) {
            return;
        }

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(cacheFile));

            for (int count = in.readInt(); count > 0; count--) {
                dataByUrl.put(in.readObject().toString(), (ReviewboardClientData) in.readObject());
            }
        } catch (Throwable e) {
            StatusHandler.log(new Status(IStatus.WARNING, ReviewboardCorePlugin.PLUGIN_ID,
                    "The Reviewboard respository data cache could not be read", e));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    void writeCache() {
        if (cacheFile == null) {
            return;
        }

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(cacheFile));

            out.writeInt(dataByUrl.size());
            for (Entry<String, ReviewboardClientData> entry : dataByUrl.entrySet()) {
                out.writeObject(entry.getKey());
                out.writeObject(entry.getValue());
            }

            out.flush();
        } catch (Throwable e) {
            StatusHandler.log(new Status(IStatus.WARNING, ReviewboardCorePlugin.PLUGIN_ID,
                    "The Reviewboard respository data cache could not be written", e));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

}
