/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.ScopeSelector;
import org.brainwy.liclipsetext.shared_core.utils.ArrayUtils;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;

public class ScannerHelper {

    private LiClipseLanguage language;

    public void setLanguage(Object language) {
        this.language = (LiClipseLanguage) language;
    }

    public List<IPredicateRule> getRulesBeforeScope(String scope) {
        if (this.language == null) {
            return null;
        }
        List<IPredicateRule> ret = new ArrayList<IPredicateRule>();

        List<ScopeSelector> injectionRules = this.language.injectionRules;
        for (ScopeSelector scopeSelector : injectionRules) {
            List<IPredicateRule> rulesAfterScope = scopeSelector.getRulesBeforeScope(scope);
            if (rulesAfterScope != null) {
                ret.addAll(rulesAfterScope);
            }
        }

        return ret;
    }

    public List<IPredicateRule> getRulesAfterScope(String scope) {
        if (this.language == null) {
            return null;
        }
        List<IPredicateRule> ret = new ArrayList<IPredicateRule>();

        List<ScopeSelector> injectionRules = this.language.injectionRules;
        for (ScopeSelector scopeSelector : injectionRules) {
            List<IPredicateRule> rulesAfterScope = scopeSelector.getRulesAfterScope(scope);
            if (rulesAfterScope != null) {
                ret.addAll(rulesAfterScope);
            }
        }

        return ret;
    }

    public IPredicateRule[] getRulesWithBeforeAndAfterRules(String scope, IPredicateRule[] rules) {
        if (rules == null) {
            rules = new IPredicateRule[0];
        }
        List<IPredicateRule> rulesBeforeScope = getRulesBeforeScope(scope);
        List<IPredicateRule> rulesAfterScope = getRulesAfterScope(scope);
        if (rulesBeforeScope != null && rulesBeforeScope.size() > 0) {
            if (rulesAfterScope != null && rulesAfterScope.size() > 0) {
                rules = ArrayUtils.concatArrays(rulesBeforeScope.toArray(new IPredicateRule[0]), rules,
                        rulesAfterScope.toArray(new IPredicateRule[0]));
            } else {
                rules = ArrayUtils.concatArrays(rulesBeforeScope.toArray(new IPredicateRule[0]), rules);
            }
        } else if (rulesAfterScope != null && rulesAfterScope.size() > 0) {
            rules = ArrayUtils.concatArrays(rules, rulesAfterScope.toArray(new IPredicateRule[0]));
        }
        return rules;
    }

    public String getTopLevelScope() {
        if (this.language == null) {
            return IDocument.DEFAULT_CONTENT_TYPE;
        }
        return this.language.name;
    }

}
