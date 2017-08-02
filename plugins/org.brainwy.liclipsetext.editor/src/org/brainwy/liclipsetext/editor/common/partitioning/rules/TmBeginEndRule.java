/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.brainwy.liclipsetext.editor.common.partitioning.IRuleWithSubRules2;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ITokenWithReplaceOperation;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange.EndRuleMatchFromStack;
import org.brainwy.liclipsetext.editor.regexp.CharsRegion;
import org.brainwy.liclipsetext.editor.regexp.RegexpHelper;
import org.brainwy.liclipsetext.editor.regexp.RegexpHelper.ReplaceInfo;
import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TmBeginEndRule implements ILiClipsePredicateRule, IRuleWithSubRules, IRuleWithSubRules2, IChangeTokenRule,
        ITextMateRule, IPrintableRule {

    private IToken fToken;
    public final TmMatchRule begin;
    private IToken fContentScope;
    private final String end;
    private IToken endScope;
    private final SortedMap<Integer, IEvalCaptures> endCaptures;
    private final boolean applyEndPatternLast;
    private List<ReplaceInfo> replacesMap;
    private MatchWhileAnySubRuleMatches anySubRule;

    public TmBeginEndRule(String begin, String end, Map beginCaptures, Map endCaptures,
            List<ILiClipsePredicateRule> subRules,
            IToken scope, IToken contentScope, int applyEndPatternLast) {
        this.begin = new TmMatchRule(begin, scope, beginCaptures);
        // There's a catch, in end we reference the groups from the begin, so
        // we'll only create it later on (when we have already matched
        // the begin so that we can fill the end parameters properly).
        // this.end = new TmMatchRule(end, scope, endCaptures);
        this.end = end;
        this.endScope = scope;
        this.applyEndPatternLast = applyEndPatternLast > 0;
        this.endCaptures = TmMatchRule.createCapturesMap(endCaptures);

        replacesMap = RegexpHelper.createReplaces(end);

        this.fToken = scope;
        this.fContentScope = TmMatchRule.isValidToken(contentScope) ? contentScope : null;

        if (subRules != null && subRules.size() > 0) {
            setSubRules(subRules);
        }
    }

    public void setSubRules(List<ILiClipsePredicateRule> subRules) {
        anySubRule = new MatchWhileAnySubRuleMatches(subRules,
                TmMatchRule.isValidToken(fContentScope) ? fContentScope : fToken);
    }

    public ILiClipsePredicateRule[] getSubRules() {
        return anySubRule == null ? null : anySubRule.getSubRules();
    }

    /**
     * Either will return null if it did not match or a list with the sub tokens matched.
     *
     * @param tokenScope: if passed we'll generate a single token with the full range (for partitioning).
     * Otherwise we'll generate tokens for each partition (for scanning a partition).
     * @throws DocumentTimeStampChangedException
     */
    @Override
    public SubRuleToken evaluateSubRules(final ScannerRange scanner, boolean generateSubRuleTokens)
            throws DocumentTimeStampChangedException {
        if (Debug.DEBUG_RULES) {
            //System.out.println(this.begin);
        }
        final int initialOffset = scanner.getMark();

        //Note: sets the mark to the new offset.
        CharsRegion beginRegion = this.begin.evaluateRegexp(scanner);
        if (beginRegion == null) {
            return null;
        }

        SubRuleToken wholeMatchSubRuleToken = new SubRuleToken(fToken, initialOffset, -1); //The len is set afterwards

        IToken beginToken = this.begin.getSuccessToken();
        if (beginToken instanceof ITokenWithReplaceOperation) {
            ITokenWithReplaceOperation iTokenWithReplaceOperation = (ITokenWithReplaceOperation) beginToken;
            beginToken = iTokenWithReplaceOperation.replaceToken(beginRegion);
        }
        SubRuleToken beginSubRule = new SubRuleToken(beginToken, initialOffset,
                scanner.getMark() - initialOffset);
        if (generateSubRuleTokens) {
            beginSubRule.addChildren((beginRegion.createSubTokens(initialOffset - beginRegion.getCurrentColumnInStr(),
                    this.begin.fCaptures, scanner)));
        }
        wholeMatchSubRuleToken.addChild(beginSubRule);

        SubRuleToken contentSubRuleToken = null;
        if (TmMatchRule.isValidToken(fContentScope)) {
            contentSubRuleToken = new SubRuleToken(fContentScope, scanner.getMark(), -1); //The len is set afterwards
            wholeMatchSubRuleToken.addChild(contentSubRuleToken);
        }

        // Note: if we found begin, return a match even if the end is not found.
        // Reference: http://www.apeth.com/nonblog/stories/textmatebundle.html
        // With begin/end, if the end pattern is not found, the overall match does
        // not fail: rather, once the begin pattern is matched, the overall match
        // runs to the end pattern or to the end of the document, whichever comes first.

        SubRuleToken endSubRuleToken = matchEndRegion(scanner, beginRegion, generateSubRuleTokens,
                contentSubRuleToken != null ? contentSubRuleToken : wholeMatchSubRuleToken);

        wholeMatchSubRuleToken.len = scanner.getMark() - initialOffset;
        if (contentSubRuleToken != null) {
            contentSubRuleToken.len = endSubRuleToken.offset - contentSubRuleToken.offset;
        }

        // Add the end if not 0-len match
        if (endSubRuleToken.len > 0) {
            wholeMatchSubRuleToken.addChild(endSubRuleToken);
        }
        return wholeMatchSubRuleToken;
    }

    /**
     * Returns the end region.
     * @throws DocumentTimeStampChangedException
     */
    private SubRuleToken matchEndRegion(final ScannerRange scanner, CharsRegion beginRegion,
            boolean generateSubRuleTokens, SubRuleToken subRuleToAddContentTokens)
            throws DocumentTimeStampChangedException {
        TmMatchRule endRule;
        if (this.replacesMap.size() == 0) {
            endRule = new TmMatchRule(end, endScope, endCaptures);
        } else {
            endRule = new TmMatchRule(RegexpHelper.replaceWithMap(end, beginRegion, replacesMap), endScope,
                    endCaptures);
        }
        if (!this.applyEndPatternLast) {
            scanner.pushBeginEndRule(endRule);
        }
        try {

            while (true) {
                //Ok, we were able to evaluate the start, now, we have to first evaluate the
                //internal patterns (and only when that's finished we'll evaluate the
                //end pattern -- if no internal patter was matched).

                final int mark = scanner.getMark();

                EndRuleMatchFromStack matchFromStack = scanner.getEndRuleMatchFromStack();
                if (matchFromStack != null) {
                    if (matchFromStack.endRule == endRule) {
                        scanner.clearEndRuleMatchFromStack();
                        scanner.setMark(matchFromStack.finalMark);
                        return matchFromStack.endRuleRegion;
                    } else {
                        // This should not be possible given that we only match the topmost one.
                        Log.log("Something is wrong: we have a begin as the topmost thing in the stack but another begin/end rule had an end before our own?");
                        return new SubRuleToken(endRule.getSuccessToken(), scanner.getMark(), 0);
                    }
                }

                if (!this.applyEndPatternLast) {
                    SubRuleToken endRuleRegion = endRule.evaluateSubRules(scanner, generateSubRuleTokens);
                    if (endRuleRegion != null) {
                        return endRuleRegion;
                    }
                }
                if (anySubRule != null) {
                    SubRuleToken evaluated = anySubRule.evaluateSubRules(scanner, generateSubRuleTokens);
                    if (evaluated != null) {
                        if (generateSubRuleTokens) {
                            subRuleToAddContentTokens.addChild(evaluated);
                        }

                        //Let's verify that we did indeed go forward.
                        int mark2 = scanner.getMark();
                        if (mark2 - mark <= 0) {
                            Log.log("Error: rule matching without going forward: forcing going forward to avoid recursion.");
                            int c = scanner.read();
                            if (c == ICharacterScanner.EOF) {
                                scanner.unread();
                                //End not matched: return 0-len rule
                                return new SubRuleToken(endRule.getSuccessToken(), scanner.getMark(), 0);
                            }
                        }

                        // Keep on going for the next loop (we already matched the current char).
                        continue;
                    }
                }

                if (this.applyEndPatternLast) {
                    SubRuleToken endRuleRegion = endRule.evaluateSubRules(scanner, generateSubRuleTokens);
                    if (endRuleRegion != null) {
                        return endRuleRegion;
                    }
                }

                //We didn't have any match if we got here... let's walk forward one char!
                int c = scanner.read();
                if (c == ICharacterScanner.EOF) {
                    scanner.unread();
                    //End not matched: return 0-len rule
                    return new SubRuleToken(endRule.getSuccessToken(), scanner.getMark(), 0);
                }
            }
        } finally {
            if (!this.applyEndPatternLast) {
                scanner.popBeginEndRule();
                EndRuleMatchFromStack matchFromStack = scanner.getEndRuleMatchFromStack();
                if (matchFromStack != null) {
                    if (matchFromStack.endRule == endRule) {
                        scanner.clearEndRuleMatchFromStack();
                    }
                }
            }
        }

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

    private static boolean reportedIssue = false;
    public static boolean expectToUseTmBeginEndRule = false;

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {
        if (!expectToUseTmBeginEndRule) {
            if (!reportedIssue) {
                reportedIssue = true;
                Log.log("Not expecting to use TmBeginEndRule (should use tm4e as the backend for parsing textmate now).");
            }
        }
        if (resume) {
            //This rule is not resumable because it depends on things such as the begin to
            //set the end (\1) and whether we're in the end of another match (\G)
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
        FastStringBuffer buf;
        if (this.anySubRule != null) {
            ILiClipsePredicateRule[] subRules = this.anySubRule.getSubRules();
            buf = new FastStringBuffer("TmBeginEndRule[", subRules.length * 20);
            for (ILiClipsePredicateRule rule : subRules) {
                buf.append("  ").append(rule.toString()).append('\n');
            }
            buf.append("]");

        } else {
            buf = new FastStringBuffer("TmBeginEndRule[]", 0);
        }
        return buf.toString();
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

        buf.append("\n" + indent + "begin: ");
        buf.appendObject(this.begin.getRegexp());

        if (this.begin.fCaptures != null && this.begin.fCaptures.size() > 0) {
            buf.append("\n" + indent + "beginCaptures: ");
            buf.appendObject(this.begin.createCapturesStr(level + 1));
        }

        buf.append("\n" + indent + "end: ");
        buf.appendObject(this.end);

        if (this.endCaptures != null && this.endCaptures.size() > 0) {
            buf.append("\n" + indent + "endCaptures: ");
            buf.appendObject(TmMatchRule.createCapturesStr(this.endCaptures, level + 1));
        }

        if (TmMatchRule.isValidToken(this.fContentScope)) {
            buf.append("\n" + indent + "contentName: ");
            buf.appendObject(this.fContentScope.getData());
        }

        if (TmMatchRule.isValidToken(this.fToken)) {
            buf.append("\n" + indent + "name: ");
            buf.appendObject(this.fToken.getData());
        }

        if (anySubRule != null) {
            ILiClipsePredicateRule[] subRules = anySubRule.getSubRules();
            if (subRules != null && subRules.length > 0) {
                buf.append("\n" + indent + "patterns: ");
                buf.appendObject(TmMatchRule.getPatternsYaml(subRules, level + 1));
            }
        }

        buf.append("\n" + baseIndent + "}");

        return buf.toString();
    }

}
