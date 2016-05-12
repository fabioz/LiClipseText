/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.auto_edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.LanguageConfig;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmIndentPart;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyHelper;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

public class LanguageAutoEdit extends LanguageConfig {

    private final List<ILanguageAutoEditRule> rules = new ArrayList<ILanguageAutoEditRule>();
    private AutoCloseScopesAutoEditRule[] autoCloseScopesRules;

    public LanguageAutoEdit(LiClipseLanguage liClipseLanguage) {
        super(liClipseLanguage);
    }

    /**
     * Rule examples
     *
     * Default
     * - {after: ':', trigger: '\n', action: indent, scope: default} #in a new line, indent after ':'
     * - {before: ':', trigger: ':', action: skip, scope: default} #skip ':' if already there
     * - {after_scope: class.method, trigger: '(', action: insert, text: (self):} #Note: if scope is dotted, check 'hierarchy' -- as given by outline.
     *
     * Custom
     * - {auto_close_scopes: [multiLineComment, singleQuotedString, doubleQuotedString], scope: default} #To work, the scopes must be: MultiLineRule (start, end) or SingleLineRule(sequence used for start/end)
     *
     * Custom
     * - {auto_close_parens: ['(', '[', '{'], scope: default} #Will close the parens if it's not properly balanced
     *
     * Custom
     * - {auto_skip_parens: [')', ']', '}'], scope: default} #Will skip the close parens if it's properly balanced
     */
    public void load(List autoEdit, List<IStatus> errorList) {
        List<AutoCloseScopesAutoEditRule> temp = new ArrayList<AutoCloseScopesAutoEditRule>();
        for (int i = 0; i < autoEdit.size(); i++) {
            Map map = (Map) autoEdit.get(i);
            if (map.containsKey(AutoCloseScopesAutoEditRule.AUTO_CLOSE_SCOPES)) {
                AutoCloseScopesAutoEditRule rule = new AutoCloseScopesAutoEditRule(map, errorList,
                        this.liClipseLanguage);
                this.rules.add(rule);
                temp.add(rule);

            } else if (map.containsKey(AutoCloseParensAutoEditRule.AUTO_CLOSE_PARENS)) {
                this.rules.add(new AutoCloseParensAutoEditRule(map, errorList));

            } else if (map.containsKey(AutoSkipParensAutoEditRule.AUTO_SKIP_PARENS)) {
                this.rules.add(new AutoSkipParensAutoEditRule(map, errorList));

            } else {
                //default
                this.rules.add(new DefaultAutoEditRule(map));
            }

            if (!map.isEmpty()) {
                LiClipseTextEditorPlugin.createWarning("Fields not treated in auto_edit: "
                        + StringUtils.join(", ", map.keySet()), errorList);
            }
        }

        autoCloseScopesRules = temp.toArray(new AutoCloseScopesAutoEditRule[temp.size()]);
    }

    public void loadGlobals() {
        LiClipseLanguage language = liClipseLanguage.get();
        if (language != null) {
            String name = language.name;
            LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
            if (languagesManager != null) {
                Map<String, TmIndentPart> scopeToTmIndent = languagesManager.scopeToTmIndent;
                TmIndentPart tmIndentPart = scopeToTmIndent.get(name);
                if (tmIndentPart != null) {
                    this.rules.add(new IndentAutoEditRule(tmIndentPart));
                }
            }
        }
    }

    /**
     * @return true if the command was properly handled and false otherwise.
     */
    public boolean customizeDocumentCommand(IDocument document, DocumentCommand command, AutoEditStrategyHelper helper,
            String indentString) {
        String contentType = AutoEditStrategyHelper.getContentType(document, command);
        for (ILanguageAutoEditRule rule : this.rules) {
            try {
                if (rule.customizeDocumentCommand(document, command, helper, indentString, contentType)) {
                    return true;
                }
            } catch (Exception e) {
                Log.log(e);
            }
        }
        return false;
    }

    /**
     * Can be null if not properly loaded. Should not be modified.
     */
    public AutoCloseScopesAutoEditRule[] getAutoCloseScopeRules() {
        return autoCloseScopesRules;
    }

}
