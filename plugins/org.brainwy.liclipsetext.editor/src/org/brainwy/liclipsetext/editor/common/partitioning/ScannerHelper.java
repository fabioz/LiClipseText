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
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;

public class ScannerHelper {

    private LiClipseLanguage language;

    public void setLanguage(Object language) {
        this.language = (LiClipseLanguage) language;
    }

    public List<ILiClipsePredicateRule> getRulesBeforeScope(String scope) {
        if (this.language == null) {
            return null;
        }
        List<ILiClipsePredicateRule> ret = new ArrayList<ILiClipsePredicateRule>();

        List<ScopeSelector> injectionRules = this.language.injectionRules;
        for (ScopeSelector scopeSelector : injectionRules) {
            List<ILiClipsePredicateRule> rulesAfterScope = scopeSelector.getRulesBeforeScope(scope);
            if (rulesAfterScope != null) {
                ret.addAll(rulesAfterScope);
            }
        }

        return ret;
    }

    public List<ILiClipsePredicateRule> getRulesAfterScope(String scope) {
        if (this.language == null) {
            return null;
        }
        List<ILiClipsePredicateRule> ret = new ArrayList<ILiClipsePredicateRule>();

        List<ScopeSelector> injectionRules = this.language.injectionRules;
        for (ScopeSelector scopeSelector : injectionRules) {
            List<ILiClipsePredicateRule> rulesAfterScope = scopeSelector.getRulesAfterScope(scope);
            if (rulesAfterScope != null) {
                ret.addAll(rulesAfterScope);
            }
        }

        return ret;
    }

    public ILiClipsePredicateRule[] getRulesWithBeforeAndAfterRules(String scope, ILiClipsePredicateRule[] rules) {
        if (rules == null) {
            rules = new ILiClipsePredicateRule[0];
        }
        List<ILiClipsePredicateRule> rulesBeforeScope = getRulesBeforeScope(scope);
        List<ILiClipsePredicateRule> rulesAfterScope = getRulesAfterScope(scope);
        if (rulesBeforeScope != null && rulesBeforeScope.size() > 0) {
            if (rulesAfterScope != null && rulesAfterScope.size() > 0) {
                rules = ArrayUtils.concatArrays(rulesBeforeScope.toArray(new ILiClipsePredicateRule[0]), rules,
                        rulesAfterScope.toArray(new ILiClipsePredicateRule[0]));
            } else {
                rules = ArrayUtils.concatArrays(rulesBeforeScope.toArray(new ILiClipsePredicateRule[0]), rules);
            }
        } else if (rulesAfterScope != null && rulesAfterScope.size() > 0) {
            rules = ArrayUtils.concatArrays(rules, rulesAfterScope.toArray(new ILiClipsePredicateRule[0]));
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
