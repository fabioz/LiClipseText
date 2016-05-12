/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.partitioning;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

/**
 * Scanner that exclusively uses predicate rules.
 * <p>
 * If a partial range is set (see {@link #setPartialRange(IDocument, int, int, String, int)} with
 * content type that is not <code>null</code> then this scanner will first try the rules that match
 * the given content type.
 * </p>
 *
 * @since 2.0
 */
public class LiClipseRuleBasedPartitionScanner extends AbstractLiClipseRuleBasedScanner {

    /**
     * Disallow setting the rules since this scanner
     * exclusively uses predicate rules.
     *
     * @param rules the sequence of rules controlling this scanner
     */
    @Override
    public void setRules(IRule[] rules) {
        throw new UnsupportedOperationException();
    }

    /*
     * @see RuleBasedScanner#setRules(IRule[])
     */
    public void setPredicateRules(IPredicateRule[] rules) {
        super.setRules(rules);
    }

    /*
     * @see ITokenScanner#nextToken()
     */
    @Override
    public void nextToken(ScannerRange range) {
        if (range.fContentType == null || fRules == null) {
            //don't try to resume
            super.nextToken(range);
            return;
        }

        // inside a partition
        range.fColumn = ScannerRange.UNDEFINED;
        boolean resume = (range.fPartitionOffset > -1 && range.fPartitionOffset < range.fOffset);
        range.fTokenOffset = resume ? range.fPartitionOffset : range.fOffset;

        IPredicateRule rule;
        IToken token;

        for (int i = 0; i < fRules.length; i++) {
            rule = (IPredicateRule) fRules[i];
            token = rule.getSuccessToken();
            if (token == null) {
                Log.log("Rule: " + rule + " returned null as getSuccessToken.");
                continue;
            }
            if (range.fContentType.equals(token.getData())) {
                token = rule.evaluate(range, resume);
                if (!token.isUndefined()) {
                    range.fContentType = null;
                    range.setToken(token);
                    return;
                }
            }
        }

        // haven't found any rule for this type of partition
        range.fContentType = null;
        if (resume) {
            range.fOffset = range.fPartitionOffset;
        }
        super.nextToken(range);
    }

}
