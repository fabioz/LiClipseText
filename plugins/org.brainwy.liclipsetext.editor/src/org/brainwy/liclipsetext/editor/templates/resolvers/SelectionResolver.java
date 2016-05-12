/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.templates.resolvers;

import org.brainwy.liclipsetext.editor.templates.LiClipseDocumentTemplateContext;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateContext;

public class SelectionResolver extends SimpleTemplateVariableResolver {

    public SelectionResolver() {
        super("selection", "Selection");
    }

    @Override
    protected String resolve(TemplateContext context) {
        LiClipseDocumentTemplateContext ctx = (LiClipseDocumentTemplateContext) context;

        TextSelectionUtils ts = ctx.markSelection();
        String selectedText = ts.getSelectedText();
        return selectedText;
    }
}
