/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class LiClipseTextAttribute extends TextAttribute {

    public final String contentType;
    public final String colorName;

    public LiClipseTextAttribute(String contentType, String colorName,
            Color foreground, Color background, int style) {
        super(foreground, background, style);
        this.contentType = contentType;
        this.colorName = colorName;
    }

    public LiClipseTextAttribute(String contentType, String colorName,
            Color foreground, Color background, int style, Font font) {
        super(foreground, background, style, font);
        this.contentType = contentType;
        this.colorName = colorName;
    }

    public static String getContentTypeFromToken(IToken token) {
        Object data = token.getData();
        if (data instanceof LiClipseTextAttribute) {
            LiClipseTextAttribute liClipseTextAttribute = (LiClipseTextAttribute) data;
            return liClipseTextAttribute.contentType;
        }
        return (String) data;

    }

    @Override
    public String toString() {
        return "LiClipseTextAttribute: " + this.contentType + " color: " + this.colorName;
    }

    public boolean isDisposed() {
        Color background = this.getBackground();
        if (background != null && background.isDisposed()) {
            return true;
        }
        Color foreground = this.getForeground();
        if (foreground != null && foreground.isDisposed()) {
            return true;
        }
        return false;
    }
}