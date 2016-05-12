/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.templates;

import org.brainwy.liclipsetext.editor.languages.GlobalLanguageTemplates;
import org.brainwy.liclipsetext.editor.languages.LanguageTemplates;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.LanguageTemplates.LiClipseVariableResolver;
import org.brainwy.liclipsetext.editor.templates.resolvers.IndentedBlockResolver;
import org.brainwy.liclipsetext.editor.templates.resolvers.PreviousWordResolver;
import org.brainwy.liclipsetext.editor.templates.resolvers.SelectionResolver;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class LiClipseTemplateContextType extends TemplateContextType {

    public static final String LICLIPSE_TEMPLATES_CONTEXT_TYPE_ID = "org.brainwy.liclipsetext.editor.templates.context_type.id";
    private final LiClipseLanguage liClipseLanguage;

    public LiClipseTemplateContextType(LiClipseLanguage liClipseLanguage) {
        setId(LICLIPSE_TEMPLATES_CONTEXT_TYPE_ID);
        this.liClipseLanguage = liClipseLanguage;

        LanguageTemplates templates = liClipseLanguage.getTemplates();
        TemplateVariableResolver[] variableResolvers = templates.getVariableResolvers();
        if (variableResolvers != null) {
            for (int i = 0; i < variableResolvers.length; i++) {
                addResolver(variableResolvers[i]);
            }
        }

        GlobalLanguageTemplates instance = GlobalLanguageTemplates.getInstance();
        variableResolvers = instance.getTemplateVariableResolvers(liClipseLanguage);
        if (variableResolvers != null) {
            for (int i = 0; i < variableResolvers.length; i++) {
                LiClipseVariableResolver resolver = (LiClipseVariableResolver) variableResolvers[i];
                addResolver(resolver);
            }
        }

        // The ones below have higher priority (so, add them later as they'll override the
        // previous ones).
        addResolver(new GlobalTemplateVariables.Cursor());
        //addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new SelectionResolver());
        addResolver(new IndentedBlockResolver());
        addResolver(new PreviousWordResolver());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
    }

}
