/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.graphics.Color;

public class DummyColorCache extends LiClipseColorCache {

    private boolean forceDisposeOfColors;

    public DummyColorCache() {
        this(true);
    }

    public DummyColorCache(boolean forceDisposeOfColors) {
        super(new PreferenceStore());
        this.forceDisposeOfColors = forceDisposeOfColors;
    }

    @Override
    protected Color getNamedColor(String name) {
        Color color = super.getNamedColor("RED");
        if (forceDisposeOfColors) {
            color.dispose(); //Should never be used (as it's just a dummy).
        }
        return color;
    }

    @Override
    protected LiClipseTextAttribute getTextAttribute(String contentType, String colorName) {
        return new LiClipseTextAttribute(contentType, colorName, null, null, 0);
    }

}
