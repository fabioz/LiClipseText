/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.IRuleWithSubRules2;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseTextAttribute;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.IScannerWithOffPartition;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionCodeReader;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * The CompositeRule must match all the sub-rules it contains to be valid.
 */
public class CompositeRule implements ILiClipsePredicateRule, IRuleWithSubRules, IRuleWithSubRules2, IChangeTokenRule {

    private ILiClipsePredicateRule[] subRules;
    private boolean[] offPartition;
    private IToken fToken;

    public CompositeRule(ILiClipsePredicateRule... subRules) {
        setSubRules(subRules);
    }

    public CompositeRule(List<ILiClipsePredicateRule> subRules) {
        setSubRules(subRules.toArray(new ILiClipsePredicateRule[subRules.size()]));
    }

    private void setSubRules(ILiClipsePredicateRule[] subRules) {
        this.subRules = subRules;
        int len = this.subRules.length;

        if (len == 0) {
            throw new AssertionFailedException("Sub rules in CompositeRule must have at least 1 rule defined.");
        }

        fToken = this.subRules[0].getSuccessToken();

        this.offPartition = new boolean[len];
        boolean last = false;
        for (int i = 0; i < len; i++) {
            boolean isOffPartition = LiClipseTextAttribute.getContentTypeFromToken(this.subRules[i].getSuccessToken())
                    .equals("OFF_PARTITION");
            if (last && !isOffPartition) {
                isOffPartition = true;
                Log.log("subRule: " + this.subRules[i]
                        + " is after an OFF_PARTITION, so, considering it an OFF_PARTITION too.");
            }
            this.offPartition[i] = isOffPartition;
            last = isOffPartition;
        }
    }

    public ILiClipsePredicateRule[] getSubRules() {
        return subRules;
    }

    /**
     * Either will return null if it did not match or a list with the sub tokens matched.
     * @throws DocumentTimeStampChangedException
     */
    @Override
    public SubRuleToken evaluateSubRules(final ScannerRange scanner, boolean generateSubRuleTokens)
            throws DocumentTimeStampChangedException {
        IMarkScanner markScanner = scanner;
        final int mark = markScanner.getMark();

        SubRuleToken wholeMatch = null;
        int length = subRules.length;
        for (int i = 0; i < length; i++) {

            int currOffset = markScanner.getMark();

            ILiClipsePredicateRule subRule = subRules[i];
            boolean isOffPartition = this.offPartition[i];
            if (isOffPartition) {
                //Ok, this means that from now on, we'll have a different scanner as we matched
                //everything we could at this point.
                IScannerWithOffPartition scannerWithOffPartition = scanner;
                PartitionCodeReader codeReader = scannerWithOffPartition.getOffPartitionCodeReader(currOffset);
                if (codeReader == null) {
                    markScanner.setMark(mark);
                    return null;
                }

                for (; i < length; i++) {
                    subRule = subRules[i];
                    IToken token = subRule.evaluate(codeReader);
                    if (token.isUndefined()) {
                        markScanner.setMark(mark);
                        return null;
                    }
                }
                //Ok, leave it regularly...
            } else {
                IToken token = subRule.evaluate(scanner);
                if (!token.isUndefined()) {
                    int foundLen = markScanner.getMark() - currOffset;
                    if (foundLen > 0) {
                        //ignore zero len partitions here.
                        int finalOffset = markScanner.getMark();
                        if (wholeMatch == null) {
                            wholeMatch = new SubRuleToken(this.fToken, mark, -1); //len set afterwards
                        }
                        if (generateSubRuleTokens) {
                            wholeMatch.addChild(new SubRuleToken(token, currOffset, finalOffset - currOffset));
                        }
                    }
                } else {
                    markScanner.setMark(mark);
                    return null;
                }
            }
        }
        if (wholeMatch != null) {
            wholeMatch.len = markScanner.getMark() - wholeMatch.offset;
        }
        return wholeMatch;

    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) throws DocumentTimeStampChangedException {
        return evaluate(scanner, false);
    }

    @Override
    public IToken getSuccessToken() {
        return fToken;
    }

    @Override
    public void setToken(IToken token) {
        fToken = token;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {
        if (resume) {
            return Token.UNDEFINED;
        }
        SubRuleToken evaluateSubRules = this.evaluateSubRules((ScannerRange) scanner, false);
        if (evaluateSubRules == null) {
            return Token.UNDEFINED;
        }
        return fToken;
    }

    @Override
    public String toString() {
        FastStringBuffer buf = new FastStringBuffer("CompositeRule[", this.subRules.length * 20);
        for (ILiClipsePredicateRule rule : this.subRules) {
            buf.append("  ").append(rule.toString()).append('\n');
        }
        buf.append("]");
        return buf.toString();
    }

}
