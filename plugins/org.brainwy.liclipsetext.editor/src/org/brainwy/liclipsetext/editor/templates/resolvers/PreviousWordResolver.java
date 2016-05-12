/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.templates.resolvers;

import org.brainwy.liclipsetext.editor.templates.LiClipseDocumentTemplateContext;
import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateContext;

public class PreviousWordResolver extends SimpleTemplateVariableResolver {

    public PreviousWordResolver() {
        super("previous_word", "Previous Word");
    }

    @Override
    protected String resolve(TemplateContext context) {
        LiClipseDocumentTemplateContext ctx = (LiClipseDocumentTemplateContext) context;

        return ctx.prefix;
    }
}
