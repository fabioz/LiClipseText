/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.List;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.LiClipseRuleBasedPartitionScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;

public class LiClipseContentTypeDefinitionScanner extends LiClipseRuleBasedPartitionScanner {

    private final ScannerHelper helper = new ScannerHelper();

    public LiClipseContentTypeDefinitionScanner(LiClipseLanguage language) {
        helper.setLanguage(language);

        List<ILiClipsePredicateRule> rules = language.rules;
        setRules(rules.toArray(new ILiClipsePredicateRule[rules.size()]));
    }

    public LiClipseContentTypeDefinitionScanner(ILiClipsePredicateRule... rules) {
        setRules(rules);
    }

    @Override
    public void setRules(ILiClipsePredicateRule[] rules) {
        rules = helper.getRulesWithBeforeAndAfterRules(helper.getTopLevelScope(), rules);
        super.setRules(rules);
    }

}
