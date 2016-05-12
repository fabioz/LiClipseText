/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import java.util.HashSet;
import java.util.Set;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.auto_edit.AutoCloseScopesAutoEditRule;
import org.brainwy.liclipsetext.editor.languages.auto_edit.LanguageAutoEdit;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyScopeCreationHelper.IScopeCreatingCharsProvider;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;

public final class DefaultScopeCreatingCharsProvider implements IScopeCreatingCharsProvider {
    private ILiClipseLanguageProvider languageProvider;

    public DefaultScopeCreatingCharsProvider(ILiClipseLanguageProvider languageProvider) {
        this.languageProvider = languageProvider;
    }

    public int getCharactersThatCreateScope(String contentType, char c) {
        Set<Character> ret = new HashSet<Character>();

        LanguageAutoEdit autoEdit = getAutoEdit();
        if (autoEdit == null) {
            return IScopeCreatingCharsProvider.CLOSE_SCOPE_NO;
        }

        int returnVal = IScopeCreatingCharsProvider.CLOSE_SCOPE_NO;
        AutoCloseScopesAutoEditRule[] autoCloseScopeRules = autoEdit.getAutoCloseScopeRules();
        if (autoCloseScopeRules != null) {
            for (int i = 0; i < autoCloseScopeRules.length; i++) {
                AutoCloseScopesAutoEditRule autoCloseScopesAutoEditRule = autoCloseScopeRules[i];
                ret.clear();
                autoCloseScopesAutoEditRule.fillWithCharsThatCreateScope(ret);
                if (ret.contains(c)) {
                    if (contentType.equals(autoCloseScopesAutoEditRule.getScope())) {
                        return IScopeCreatingCharsProvider.CLOSE_SCOPE;
                    } else {
                        returnVal = IScopeCreatingCharsProvider.CLOSE_SCOPE_IF_SELECTION;
                    }
                }
            }
        }
        return returnVal;
    }

    private LanguageAutoEdit getAutoEdit() {
        LiClipseLanguage language = languageProvider.getLanguage();
        if (language == null) {
            return null;
        }
        LanguageAutoEdit autoEdit = language.getAutoEdit();
        return autoEdit;
    }

    public Set<Tuple<String, String>> getMultiLineSequences() {
        LanguageAutoEdit autoEdit = getAutoEdit();
        Set<Tuple<String, String>> ret = new HashSet<Tuple<String, String>>();
        if (autoEdit == null) {
            return ret;
        }

        AutoCloseScopesAutoEditRule[] autoCloseScopeRules = autoEdit.getAutoCloseScopeRules();
        if (autoCloseScopeRules != null) {
            for (int i = 0; i < autoCloseScopeRules.length; i++) {
                ret.addAll(autoCloseScopeRules[i].getMultiLineStartEndSequences());
            }
        }
        return ret;
    }
}