/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brainwy.liclipsetext.editor.templates.LiClipseTemplate;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * These snippets are considered 'global' as they may be valid to any existing
 * language.
 *
 *
 */
public class GlobalLanguageTemplates {

    private static GlobalLanguageTemplates instance = new GlobalLanguageTemplates();

    private GlobalLanguageTemplates() {

    }

    private final Map<String, List<LiClipseTemplate>> contextToTemplates = new HashMap<>();

    public static GlobalLanguageTemplates getInstance() {
        return instance;
    }

    public void clear() {
        contextToTemplates.clear();
        fResolvers.clear();
    }

    //Right now, the resolvers aren't dependent on the scope (just put all there).
    Map<String, LanguageTemplates.LiClipseVariableResolver> fResolvers = new HashMap<>();
    private int nextNameI;

    public TemplateVariableResolver[] getTemplateVariableResolvers(LiClipseLanguage liClipseLanguage) {
        synchronized (lock) {
            return fResolvers.values().toArray(new TemplateVariableResolver[0]);
        }
    }

    private final Object lock = new Object();

    public LanguageTemplates.LiClipseVariableResolver obtainValidTemplateVariableName(String sub) {
        LanguageTemplates.LiClipseVariableResolver ret;
        synchronized (lock) {
            ret = this.fResolvers.get(sub);
            if (ret == null) {

                boolean allValid = true;
                int length = sub.length();
                if (length == 0) {
                    allValid = false;
                } else {
                    for (int i = 0; i < length; i++) {
                        if (!StringUtils.isAsciiLetterOrUnderlineOrNumber(sub.charAt(i))) {
                            allValid = false;
                            break;
                        }
                    }
                }

                String type = sub;
                if (!allValid) {
                    type = nextName();
                }

                ret = new LanguageTemplates.LiClipseVariableResolver(type, Arrays.asList(sub));
                if (length != 0) {
                    fResolvers.put(sub, ret);
                } else {
                    fResolvers.put("__empty__" + ret.getType(), ret);
                }
            }
        }
        return ret;
    }

    private String nextName() {
        nextNameI++;
        return "var" + nextNameI;
    }

    public LiClipseTemplate[] getTemplates(String contextTypeId, LiClipseLanguage liClipseLanguage,
            String... otherContentTypes) {
        Set<String> set = new HashSet<>(Arrays.asList(otherContentTypes));
        set.add(liClipseLanguage.name);
        set.add(contextTypeId);

        List<LiClipseTemplate> allTemplates = new ArrayList<>();
        for (String s : set) {
            List<LiClipseTemplate> list = contextToTemplates.get(s);
            if (list != null) {
                allTemplates.addAll(list);
            }
        }
        return allTemplates.toArray(new LiClipseTemplate[0]);
    }

    private TemplateContextType contextType = new TemplateContextType();
    private final Object lock2 = new Object();

    public void addTemplate(LiClipseTemplate liClipseTemplate) throws TemplateException {
        contextType.validate(liClipseTemplate.getPattern());
        synchronized (lock2) {
            List<LiClipseTemplate> list = this.contextToTemplates.get(liClipseTemplate.contextType);
            if (list == null) {
                list = new ArrayList<LiClipseTemplate>();
                this.contextToTemplates.put(liClipseTemplate.contextType, list);
            }
            list.add(liClipseTemplate);
        }
    }

}
