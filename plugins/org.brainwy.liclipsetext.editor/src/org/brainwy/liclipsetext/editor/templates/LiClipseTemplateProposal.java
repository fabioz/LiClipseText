/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.templates;

import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;

public class LiClipseTemplateProposal extends TemplateProposal implements ICompletionProposalExtension4 {

    public LiClipseTemplateProposal(LiClipseTemplate template, TemplateContext context, IRegion region, Image image,
            int relevance) {
        super(template, context, region, image, relevance);
    }

    public LiClipseTemplate getTemplateUsed() {
        return (LiClipseTemplate) super.getTemplate();
    }

    private String fDisplayString;

    @Override
    public String getDisplayString() {
        if (fDisplayString == null) {
            Template template = this.getTemplate();
            String name = template.getName();
            String description = template.getDescription();
            if (name.length() > 0) {
                if (description.length() > 0) {
                    fDisplayString = StringUtils.join(" - ", name, description);
                    return fDisplayString;
                }
                fDisplayString = name;
                return fDisplayString;
            }
            fDisplayString = description;
            return fDisplayString;
        }
        return fDisplayString;

    }

    public boolean isAutoInsertable() {
        return this.getTemplate().isAutoInsertable();
    }
}
