/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.navigation;

import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.IRegion;

public final class RegionAndText {

    public final IRegion region;

    public final String text;

    /**
     * 0 - based
     */
    public final int beginLine;

    /**
     * 0 - based
     */
    public final int beginCol;

    public final String kind;

    public RegionAndText(IRegion region, String text, int beginLine, int beginCol) {
        this(region, text, beginLine, beginCol, null);
    }

    public RegionAndText(IRegion region, String text, int beginLine, int beginCol, String kind) {
        this.region = region;
        this.text = text;
        this.beginLine = beginLine;
        this.beginCol = beginCol;
        this.kind = kind;
    }

    @Override
    public String toString() {
        return StringUtils.join(" ", "RegionAndText[ text:", text, "region:", region, "kind:", kind, "]");
    }

}