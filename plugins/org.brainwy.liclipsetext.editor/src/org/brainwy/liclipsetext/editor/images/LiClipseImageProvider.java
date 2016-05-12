/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.images;

import org.brainwy.liclipsetext.shared_ui.ImageCache;
import org.brainwy.liclipsetext.shared_ui.SharedUiPlugin;
import org.brainwy.liclipsetext.shared_ui.UIConstants;
import org.eclipse.swt.graphics.Image;

public class LiClipseImageProvider {

    public static String CLASS_KIND = "class";
    public static String METHOD_KIND = "method";
    public static String COMMENT_KIND = "comment";
    public static String ATTRIBUTE_KIND = "attribute";
    public static String TAG_KIND = "tag";
    public static String ACTION_KIND = "action";

    public static Image getImage(String requestedIcon, String defaultIcon) {
        String icon = defaultIcon; //default
        String dataIcon = requestedIcon;
        if (dataIcon != null) {
            if (CLASS_KIND.equals(dataIcon)) {
                icon = UIConstants.CLASS_ICON;

            } else if (METHOD_KIND.equals(dataIcon)) {
                icon = UIConstants.METHOD_ICON;

            } else if (COMMENT_KIND.equals(dataIcon)) {
                icon = UIConstants.COMMENT;

            } else if (ATTRIBUTE_KIND.equals(dataIcon)) {
                icon = UIConstants.PUBLIC_ATTR_ICON;

            } else if (TAG_KIND.equals(dataIcon)) {
                icon = UIConstants.XML_TAG_ICON;

            } else if (ACTION_KIND.equals(dataIcon)) {
                icon = UIConstants.ACTION_ICON;

            }
        }
        ImageCache imageCache = SharedUiPlugin.getImageCache();
        if (imageCache == null) {
            return null;
        }
        return imageCache.get(icon);
    }

}
