/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.templates;

import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;

public class LiClipseTemplate extends Template {

    public final String contextType;

    /**
     * Matches it regardless of the place.
     */
    public Tuple<String, String> matchPreviousSubScope;

    /**
     * Matches it only inside the current partition.
     */
    public Tuple<String, String> matchCurrentSubScope;
    public Image icon;

    public LiClipseTemplate(String name, String description, String liclipseTemplatesContextTypeId, String pattern,
            boolean autoInsert, String contextType) {
        super(name, description, liclipseTemplatesContextTypeId, pattern, autoInsert);
        this.contextType = contextType;
    }

}
