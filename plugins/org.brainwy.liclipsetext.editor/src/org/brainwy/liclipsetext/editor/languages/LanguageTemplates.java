/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.swt.graphics.Image;
import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.images.LiClipseImageProvider;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplate;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateContextType;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_ui.UIConstants;

public class LanguageTemplates extends LanguageConfig {

    private LiClipseTemplate[] templates = new LiClipseTemplate[0];
    private TemplateVariableResolver[] resolvers;

    public LanguageTemplates(LiClipseLanguage liClipseLanguage) {
        super(liClipseLanguage);
    }

    public LiClipseTemplate[] getTemplates(String contextTypeId, String contentType) {
        if (!LiClipseTemplateContextType.LICLIPSE_TEMPLATES_CONTEXT_TYPE_ID.equals(contextTypeId)) {
            Log.log("Expected id: " + LiClipseTemplateContextType.LICLIPSE_TEMPLATES_CONTEXT_TYPE_ID + ". Found: "
                    + contextTypeId);
            return new LiClipseTemplate[0];
        }
        int length = templates.length;
        ArrayList<LiClipseTemplate> lst = new ArrayList<LiClipseTemplate>(length);

        for (int i = 0; i < length; i++) {
            LiClipseTemplate t = templates[i];
            if (t.contextType == null || t.contextType.equals(contentType)) {
                lst.add(t);
            }
        }
        return lst.toArray(new LiClipseTemplate[lst.size()]);
    }

    public void setTemplates(LiClipseTemplate[] templates) {
        this.templates = templates;

    }

    public Object getTemplatesDump() {
        ArrayList<Map<String, Object>> lst = new ArrayList<Map<String, Object>>(templates.length);
        for (int i = 0; i < templates.length; i++) {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            Template t = templates[i];
            m.put("name", t.getName());
            m.put("description", t.getDescription());
            m.put("pattern", t.getPattern());
            m.put("auto_insert", t.isAutoInsertable());
            lst.add(m);
        }
        return lst;
    }

    @SuppressWarnings("rawtypes")
    public void load(List<Map<String, Object>> templatesToLoad, Map<String, Object> templateVariables,
            List<IStatus> errorList) {
        if (templatesToLoad == null) {
            return; //Nothing to load!
        }
        ArrayList<LiClipseTemplate> lst = new ArrayList<LiClipseTemplate>(templatesToLoad.size());
        for (Map m : templatesToLoad) {
            String name = "N/A";
            try {
                name = (String) m.remove("name");
                String description = (String) m.remove("description");
                if (description == null) {
                    description = name;
                }
                String pattern = (String) m.remove("pattern");
                Object autoinsertOnMap = m.remove("auto_insert");
                boolean autoInsert = true;
                if (autoinsertOnMap instanceof Boolean) {
                    autoInsert = (Boolean) autoinsertOnMap;
                } else if (autoinsertOnMap instanceof String) {
                    if (autoinsertOnMap.equals("false")) {
                        autoInsert = false;
                    }
                }

                List matchPreviousSubScope = (List) m.remove("match_previous_sub_scope");
                if (matchPreviousSubScope != null) {
                    if (matchPreviousSubScope.size() != 2) {
                        LiClipseTextEditorPlugin
                                .createWarning(
                                        "Expected match_previous_sub_scope size to be == 2 (the scope and the value to match).",
                                        errorList);
                        matchPreviousSubScope = null;

                    } else {
                        for (Object o : matchPreviousSubScope) {
                            if (!(o instanceof String)) {
                                LiClipseTextEditorPlugin.createWarning("Expected match_previous_sub_scope in: " + name
                                        + " to be a List with 2 Strings.", errorList);
                                matchPreviousSubScope = null;
                                break;
                            }
                        }
                    }
                }

                List matchCurrentSubScope = (List) m.remove("match_current_sub_scope");
                if (matchCurrentSubScope != null) {
                    if (matchCurrentSubScope.size() != 2) {
                        LiClipseTextEditorPlugin
                                .createWarning(
                                        "Expected match_current_sub_scope size to be == 2 (the scope and the value to match).",
                                        errorList);
                        matchCurrentSubScope = null;

                    } else {
                        Object part1 = matchCurrentSubScope.get(0);
                        Object part2 = matchCurrentSubScope.get(1);
                        if (!(part1 instanceof String)) {
                            LiClipseTextEditorPlugin.createWarning("Expected match_current_sub_scope in: " + name
                                    + " to be a List with 2 Strings.", errorList);
                            matchCurrentSubScope = null;
                            break;
                        }
                        if (!(part2 instanceof String)) {
                            LiClipseTextEditorPlugin.createWarning("Expected match_current_sub_scope in: " + name
                                    + " to be a List with 2 Strings.", errorList);
                            matchCurrentSubScope = null;
                            break;
                        }
                    }
                }

                String contextType = (String) m.remove("scope");
                if ("default".equals(contextType)) {
                    contextType = IDocument.DEFAULT_CONTENT_TYPE;
                }
                LiClipseTemplate template = new LiClipseTemplate(name, description,
                        LiClipseTemplateContextType.LICLIPSE_TEMPLATES_CONTEXT_TYPE_ID,
                        pattern, autoInsert, contextType);
                if (matchPreviousSubScope != null) {
                    template.matchPreviousSubScope = new Tuple<String, String>((String) matchPreviousSubScope.get(0),
                            (String) matchPreviousSubScope.get(1));
                }
                if (matchCurrentSubScope != null) {
                    template.matchCurrentSubScope = new Tuple<String, String>((String) matchCurrentSubScope.get(0),
                            (String) matchCurrentSubScope.get(1));
                }

                String icon = (String) m.remove("icon");
                if (icon != null) {
                    Image image = LiClipseImageProvider.getImage(icon, UIConstants.COMPLETION_TEMPLATE);
                    template.icon = image;
                }
                lst.add(template);

                if (!m.isEmpty()) {
                    LiClipseTextEditorPlugin.createWarning("Fields not treated in templates: "
                            + StringUtils.join(", ", m.keySet()), errorList);
                }
            } catch (Exception e) {
                Log.log(e);
                LiClipseTextEditorPlugin.createWarning("Error creating template: " + name + ": " + e.getMessage(),
                        errorList);
            }
        }
        this.templates = lst.toArray(new LiClipseTemplate[lst.size()]);
        if (templateVariables != null) {
            Set<Entry<String, Object>> entrySet = templateVariables.entrySet();
            List<TemplateVariableResolver> resolvers = new ArrayList<TemplateVariableResolver>();

            for (Entry<String, Object> entry : entrySet) {
                String key = entry.getKey();
                Object value = entry.getValue();
                resolvers.add(new LiClipseVariableResolver(key, (List<String>) value));
            }
            this.resolvers = resolvers.toArray(new TemplateVariableResolver[resolvers.size()]);
        }
    }

    public static final class LiClipseVariableResolver extends TemplateVariableResolver {

        public final String[] value;

        public LiClipseVariableResolver(String key, List<String> value) {
            super(key, key);
            this.value = value.toArray(new String[value.size()]);
        }

        @Override
        protected String[] resolveAll(TemplateContext context) {
            return this.value;
        }

        @Override
        public String toString() {
            return StringUtils.join("", "LiClipseVariableResolver[", this.getType(), "]");
        }

    }

    /**
     * May be null.
     */
    public TemplateVariableResolver[] getVariableResolvers() {
        return this.resolvers;
    }
}
