/*******************************************************************************
 * Copyright (c) 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.ui.util;


import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.review_board.ereviewboard.ui.ReviewboardUiPlugin;

/**
 * @author Robert Munteanu
 */
public class ReviewboardImages {

	private static final URL baseURL = ReviewboardUiPlugin.getDefault().getBundle().getEntry("/icons/");
	
	public static final String T_VIEW = "eview16";

	public static final String T_WIZ = "wizard";
	
	public static final ImageDescriptor OVERLAY_REVIEWBOARD = create(T_VIEW, "reviewboard-overlay-icon.png");
	
	public static final ImageDescriptor ICON_REVIEWBOARD = create(T_VIEW, "reviewboard-icon.png");

	public static final ImageDescriptor WIZARD_CREATE_REQUEST = create(T_WIZ, "reviewboard-wizard.png");


	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
   private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {

        StringBuffer buffer = new StringBuffer(prefix);
        if (!"".equals(prefix))
            buffer.append('/');
        buffer.append(name);
        return new URL(baseURL, buffer.toString());
    }
}
