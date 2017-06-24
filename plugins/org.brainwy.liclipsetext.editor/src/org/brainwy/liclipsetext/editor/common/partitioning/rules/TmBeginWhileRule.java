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
public class TmBeginWhileRule
        implements ILiClipsePredicateRule, IRuleWithSubRules, IRuleWithSubRules2, IChangeTokenRule,
        ITextMateRule, IPrintableRule {

    private IToken fToken;
    public final TmMatchRule begin;
    private final String whileStr;
    private List<ReplaceInfo> replacesMap;
    private MatchWhileAnySubRuleMatches anySubRule;
    private IToken fContentScope;
    private SortedMap<Integer, IEvalCaptures> whileCaptures;

    public TmBeginWhileRule(String begin, String whileStr, Map beginCaptures, Map whileCaptures,
            List<ILiClipsePredicateRule> subRules,
            IToken scope, IToken contentScope) {
        this.begin = new TmMatchRule(begin, scope, beginCaptures);
        // There's a catch, in while we reference the groups from the begin, so
        // we'll only create it later on (when we have already matched
        // the begin so that we can fill the while parameters properly).
        this.whileStr = whileStr;
        this.whileCaptures = TmMatchRule.createCapturesMap(whileCaptures);

        replacesMap = RegexpHelper.createReplaces(whileStr);

        this.fToken = scope;
        this.fContentScope = TmMatchRule.isValidToken(contentScope) ? contentScope : null;

        if (subRules != null && subRules.size() > 0) {
            setSubRules(subRules);
        }
    }

    private void setSubRules(List<ILiClipsePredicateRule> subRules) {
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
        if (scanner.isInBeginWhile()) {
            return null;
        }

        final int initialOffset = scanner.getMark();

        //Note: sets the mark to the new offset.
        CharsRegion beginRegion = this.begin.evaluateRegexp(scanner);
        if (beginRegion == null) {
            return null;
        }

        try {
            scanner.setInBeginWhile(true);

            SubRuleToken wholeMatchSubRuleToken = new SubRuleToken(fToken, initialOffset, -1); //The len is set afterwards

            IToken beginToken = this.begin.getSuccessToken();
            if (beginToken instanceof ITokenWithReplaceOperation) {
                ITokenWithReplaceOperation iTokenWithReplaceOperation = (ITokenWithReplaceOperation) beginToken;
                beginToken = iTokenWithReplaceOperation.replaceToken(beginRegion);
            }
            SubRuleToken beginSubRule = new SubRuleToken(beginToken, initialOffset,
                    scanner.getMark() - initialOffset);
            if (generateSubRuleTokens) {
                beginSubRule
                        .addChildren((beginRegion.createSubTokens(initialOffset - beginRegion.getCurrentColumnInStr(),
                                this.begin.fCaptures, scanner)));
            }
            wholeMatchSubRuleToken.addChild(beginSubRule);

            SubRuleToken subRuleToAddContentTokens = wholeMatchSubRuleToken;
            SubRuleToken contentSubRuleToken = null;
            if (TmMatchRule.isValidToken(this.fContentScope)) {
                contentSubRuleToken = new SubRuleToken(fContentScope, scanner.getMark(), -1); //The len is set afterwards
                wholeMatchSubRuleToken.addChild(contentSubRuleToken);
                subRuleToAddContentTokens = contentSubRuleToken;
            }

            TmMatchRule whileRule;
            if (this.replacesMap.size() == 0) {
                whileRule = new TmMatchRule(whileStr, this.fContentScope, whileCaptures);
            } else {
                whileRule = new TmMatchRule(RegexpHelper.replaceWithMap(whileStr, beginRegion, replacesMap),
                        fContentScope,
                        whileCaptures);
            }

            FOUND_WHILE_PATTERN: while (true) {
                final int markAfterLastMatch = scanner.getMark();

                //Note: if we started matching, we'll always match up to EOF (or a sub-pattern)!
                REACHED_END_OF_LINE_AFTER_MATCH: while (true) {
                    int currMark = scanner.getMark();
                    int matchRulesOrEof = matchRulesOrEof(scanner, generateSubRuleTokens, subRuleToAddContentTokens,
                            markAfterLastMatch);
                    if (matchRulesOrEof != 0 && scanner.getMark() <= currMark) {
                        //Something went bad: we didn't walk a char forward as we expected!
                        scanner.read(); // just read anything to go forward!
                    }
                    switch (matchRulesOrEof) {
                        case 0: // EOF
                            if (contentSubRuleToken != null) {
                                contentSubRuleToken.len = scanner.getMark() - contentSubRuleToken.offset;
                            }
                            wholeMatchSubRuleToken.len = scanner.getMark() - wholeMatchSubRuleToken.offset;
                            if (wholeMatchSubRuleToken.len == 0) {
                                return null; // Don't match 0-len matches
                            }
                            return wholeMatchSubRuleToken;
                        case 1: // matched something (and walked at least one char
                        case 2: // matched nothing: go on for the next char
                            break; // break the switch (and continue the while(true) for the next char).

                        case 3: // matched \r or \n
                            break REACHED_END_OF_LINE_AFTER_MATCH;
                    }
                }
                final int startOfLineMark = scanner.getMark();
                scanner.setLastRegexpMatchOffset(startOfLineMark);

                while (true) {
                    // Ok, we just made the begin match, now, let's see in this line if we're able to find the
                    // while match. If we do find it, we'll consider the remainder of the line as a match
                    // (but not the start of the line until the match) and we'll consider the contents
                    // of the match as we did after the begin was found.
                    SubRuleToken subTokens2 = whileRule.evaluateSubRules(scanner, generateSubRuleTokens);
                    if (subTokens2 != null && scanner.getMark() > markAfterLastMatch) { // only consider it if we did indeed walk forward
                        if (generateSubRuleTokens) {
                            subRuleToAddContentTokens.addChild(subTokens2);
                        }
                        continue FOUND_WHILE_PATTERN;
                    } else {
                        int c = scanner.read();
                        switch (c) {
                            case ICharacterScanner.EOF:
                                //FALLTHROUGH (no need to unread as we'll go to the startOfLineMark).
                            case '\r':
                                //FALLTHROUGH (no need to read \n as we'll go to the startOfLineMark).
                            case '\n':
                                // In the whole line we didn't find the while rule, so, just consider
                                // that we stopped at the start of the line.
                                scanner.setMark(startOfLineMark);
                                if (contentSubRuleToken != null) {
                                    contentSubRuleToken.len = scanner.getMark() - contentSubRuleToken.offset;
                                }
                                wholeMatchSubRuleToken.len = scanner.getMark() - wholeMatchSubRuleToken.offset;
                                if (wholeMatchSubRuleToken.len == 0) {
                                    return null; // Don't match 0-len matches
                                }
                                return wholeMatchSubRuleToken;
                        }
                    }
                }
            }
        } finally {
            scanner.setInBeginWhile(false);
        }

    }

    /**
     * @return
     *    0 if EOF reached
     *    1 if some pattern matched
     *    2 if nothing matched
     *    3 if we reached EOL
     * @throws DocumentTimeStampChangedException
     */
    private int matchRulesOrEof(ScannerRange scanner, boolean generateSubRuleTokens,
            SubRuleToken subRuleToAddContentTokens, int markAfterLastMatch) throws DocumentTimeStampChangedException {
        if (anySubRule != null) {
            SubRuleToken evaluated = anySubRule.evaluateSubRules(scanner, generateSubRuleTokens);
            if (evaluated != null) {
                if (generateSubRuleTokens) {
                    subRuleToAddContentTokens.addChild(evaluated);
                }

                //Let's verify that we did indeed go forward.
                int mark2 = scanner.getMark();
                if (mark2 - markAfterLastMatch <= 0) {
                    Log.log("Error: rule matching without going forward: forcing going forward to avoid recursion.");
                    int c1 = scanner.read();
                    if (c1 == ICharacterScanner.EOF) {
                        scanner.unread();
                        //                        if (generateSubRuleTokens) {
                        //                            RegexpHelper.fillWithSubToken(fContentScope, markAfterLastMatch, mark2 - markAfterLastMatch,
                        //                                    lst);
                        //                        }
                        return 0;
                    }
                }
                // Keep on going for the next loop (we already matched the current char).
                return 1;
            }
        }

        int c = scanner.read();
        switch (c) {
            case ICharacterScanner.EOF:
                scanner.unread();
                //                if (generateSubRuleTokens) {
                //                    RegexpHelper.fillWithSubToken(fContentScope, markAfterLastMatch,
                //                            scanner.getMark() - markAfterLastMatch, lst);
                //                }
                return 0;

            case '\n':
                //                if (generateSubRuleTokens) {
                //                    RegexpHelper.fillWithSubToken(fContentScope, markAfterLastMatch,
                //                            scanner.getMark() - markAfterLastMatch, lst);
                //                }
                return 3;

            case '\r':
                c = scanner.read();
                if (c != '\n') {
                    scanner.unread();
                }
                //                if (generateSubRuleTokens) {
                //                    RegexpHelper.fillWithSubToken(fContentScope, markAfterLastMatch,
                //                            scanner.getMark() - markAfterLastMatch, lst);
                //                }
                return 3;
        }
        return 2; // walk char without matching anything
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
            buf = new FastStringBuffer("TmBeginWhileRule[", subRules.length * 20);
            for (ILiClipsePredicateRule rule : subRules) {
                buf.append("  ").append(rule.toString()).append('\n');
            }
            buf.append("]");

        } else {
            buf = new FastStringBuffer("TmBeginWhileRule[]", 0);
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

        if (this.begin.fCaptures.size() > 0) {
            buf.append("\n" + indent + "beginCaptures: ");
            buf.appendObject(this.begin.createCapturesStr(level + 1));
        }

        buf.append("\n" + indent + "while: ");
        buf.appendObject(this.whileStr);

        if (TmMatchRule.isValidToken(this.fContentScope)) {
            buf.append("\n" + indent + "name: ");
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
