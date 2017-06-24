/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.List;

import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class MultiLineRuleWithSkipAndStartRules implements ILiClipsePredicateRule, IChangeTokenRule {

    protected IToken fToken;
    protected final char[] fEndSequence;
    protected final char fEscapeCharacter;
    private final ILiClipsePredicateRule[] subRules;
    private final ILiClipsePredicateRule[] startLoadedSubRules;

    @Override
    public void setToken(IToken token) {
        this.fToken = token;
    }

    public MultiLineRuleWithSkipAndStartRules(List<ILiClipsePredicateRule> startLoadedSubRules, String end,
            IToken token,
            Character escapeCharacter, List<ILiClipsePredicateRule> loadedSubRules) {
        this.startLoadedSubRules = startLoadedSubRules != null && startLoadedSubRules.size() > 0 ? startLoadedSubRules
                .toArray(new ILiClipsePredicateRule[startLoadedSubRules.size()]) : null;

        this.fEndSequence = end.toCharArray();
        this.fToken = token;
        this.fEscapeCharacter = escapeCharacter;
        this.subRules = loadedSubRules != null && loadedSubRules.size() > 0 ? loadedSubRules
                .toArray(new ILiClipsePredicateRule[loadedSubRules.size()]) : null;
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
    public IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {
        if (resume) {
            return Token.UNDEFINED;
        }

        IMarkScanner markScanner = (IMarkScanner) scanner;
        int mark = markScanner.getMark();

        //we don't actually care about the result, just that we skipped the needed chars.
        int startRulesLen = startLoadedSubRules.length;
        for (int i = 0; i < startRulesLen; i++) {
            IToken matched = startLoadedSubRules[i].evaluate(scanner, false);
            if (matched.isUndefined()) {
                markScanner.setMark(mark);
                return Token.UNDEFINED;
            }
        }

        int c;

        //Ok, found start sequence, now, find the end sequence.
        OUT: while (true) {
            if (subRules != null) {
                //we don't actually care about the result, just that we skipped the needed chars.
                int subRulesLen = subRules.length;
                for (int i = 0; i < subRulesLen; i++) {
                    IToken matched = subRules[i].evaluate(scanner, false);
                    if (!matched.isUndefined()) {
                        continue OUT;
                    }
                }
            }

            c = scanner.read();
            if (c == ICharacterScanner.EOF) {
                return fToken; //Always match open partitions that are unclosed on a multi line rule.
            }
            if (c == fEscapeCharacter) { //skip the next char if skip char is matched
                c = scanner.read();
                if (c == ICharacterScanner.EOF) {
                    return fToken; //Always match open partitions that are unclosed on a multi line rule.
                }
                continue;
            }
            mark = markScanner.getMark();
            boolean matched = true;

            for (int i = 0;; i++) {
                if (c != fEndSequence[i]) {
                    markScanner.setMark(mark);
                    matched = false;
                    break;
                }
                if (i + 1 < fEndSequence.length) {
                    c = scanner.read();
                } else {
                    break;
                }
            }
            if (matched) {
                return fToken;
            }
        }
    }

    @Override
    public String toString() {
        return new FastStringBuffer("MultiLineRuleWithSkipAndStartRules(", fEndSequence.length + 40)
                .append("start: RULES")
                .append("end: ")
                .append(fEndSequence)
                .append("subRules: ")
                .append(subRules.toString())
                .append(")")
                .toString();
    }

}
