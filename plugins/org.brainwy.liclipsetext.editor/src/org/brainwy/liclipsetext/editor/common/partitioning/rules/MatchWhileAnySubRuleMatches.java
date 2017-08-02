/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.IRuleWithSubRules2;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class MatchWhileAnySubRuleMatches
        implements ITextMateRule, ILiClipsePredicateRule, IRuleWithSubRules, IRuleWithSubRules2,
        IChangeTokenRule, IPrintableRule {

    final private ILiClipsePredicateRule[] subRules;
    private IToken fToken;

    public MatchWhileAnySubRuleMatches(List<ILiClipsePredicateRule> rules, IToken token) {
        this.subRules = rules.toArray(new ILiClipsePredicateRule[0]);

        this.fToken = token;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) throws DocumentTimeStampChangedException {
        return evaluate(scanner, false);
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
    public IToken getSuccessToken() {
        return fToken;
    }

    @Override
    public void setToken(IToken token) {
        fToken = token;
    }

    @Override
    public String toString() {
        return "MatchWhileAnySubRuleMatches";
    }

    @Override
    public SubRuleToken evaluateSubRules(ScannerRange scanner, boolean generateSubRuleTokens)
            throws DocumentTimeStampChangedException {
        final int mark = scanner.getMark();
        SubRuleToken wholeMatch = null;
        boolean matchedSubRule = true;
        int length = subRules.length;

        if (scanner.getEndRuleMatchFromStack() != null) {
            return null;
        }

        OUT: while (matchedSubRule) {
            if (scanner.getEndRuleMatchFromStack() != null) {
                break;
            }

            // Check if we have a pending end rule in the stack. If we do, match it!
            int initialMark = scanner.getMark();
            if (!scanner.beginEndRuleStack.isEmpty()) { // Note: just check the topmost match!
                TmMatchRule endRuleInStack = (TmMatchRule) scanner.beginEndRuleStack.peek();
                SubRuleToken endRuleRegion = endRuleInStack.evaluateSubRules(scanner, generateSubRuleTokens);
                if (endRuleRegion != null) {
                    scanner.setEndRuleMatchFromStack(initialMark, scanner.getMark(), endRuleInStack, endRuleRegion);

                    // If we don't do that, the region we'll generate will be wrong... what we're doing is
                    // marking what we found in this rule and the backing up until the begin/end rule for
                    // which we found the end now is reached.
                    scanner.setMark(initialMark);
                    break OUT;
                }
            }

            matchedSubRule = false;
            for (int i = 0; i < length; i++) {
                ILiClipsePredicateRule subRule = subRules[i];
                if (subRule instanceof IRuleWithSubRules) {
                    IRuleWithSubRules iRuleWithSubRules = (IRuleWithSubRules) subRule;
                    SubRuleToken subRuleToken = iRuleWithSubRules.evaluateSubRules(scanner,
                            generateSubRuleTokens);
                    if (subRuleToken != null && subRuleToken.len > 0) { // Don't consider 0-len rules
                        matchedSubRule = true;
                        if (wholeMatch == null) {
                            wholeMatch = new SubRuleToken(getSuccessToken(), mark, -1); // len is set afterwards
                        }

                        if (generateSubRuleTokens) {
                            wholeMatch.addChild(subRuleToken);
                        }
                        break;
                    }
                } else {
                    IToken token = subRule.evaluate(scanner);
                    if (!token.isUndefined()) {
                        matchedSubRule = true;
                        if (generateSubRuleTokens) {
                            int finalOffset = scanner.getMark();
                            int foundLen = scanner.getMark() - mark;
                            if (foundLen > 0) {
                                //ignore zero len partitions here.
                                if (wholeMatch == null) {
                                    wholeMatch = new SubRuleToken(getSuccessToken(), mark, -1); // len is set afterwards
                                }

                                if (generateSubRuleTokens) {
                                    SubRuleToken sub = new SubRuleToken(token, mark, finalOffset - mark);
                                    wholeMatch.addChild(sub);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (wholeMatch != null) {
            wholeMatch.len = scanner.getMark() - wholeMatch.offset;
            if (wholeMatch.len <= 0) {
                wholeMatch = null;
            }
        }
        return wholeMatch;
    }

    public ILiClipsePredicateRule[] getSubRules() {
        return subRules;
    }

    @Override
    public String toTmYaml() {
        return toTmYaml(0);
    }

    @Override
    public String toTmYaml(int level) {
        String baseIndent = new FastStringBuffer().appendN("    ", level).toString();
        String indent = new FastStringBuffer().appendN("    ", level + 1).toString();

        FastStringBuffer buf = new FastStringBuffer();
        buf.append("{");

        if (subRules != null && subRules.length > 0) {
            buf.append("\n" + indent + "patterns: ");
            buf.appendObject(TmMatchRule.getPatternsYaml(subRules, level + 1));
            buf.append("\n" + baseIndent);
        }

        buf.append("}");

        return buf.toString();
    }

}
