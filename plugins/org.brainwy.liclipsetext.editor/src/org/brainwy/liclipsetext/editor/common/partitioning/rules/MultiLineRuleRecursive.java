/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.List;

import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * A multi-line rule defines the start/end sequence (say '[' and ']'), but if
 * the rule should be recursive (i.e.: each new '[' inside the initial '[' would open
 * a new context to match the same one), then we can use this rule.
 */
public class MultiLineRuleRecursive extends MultiLineRuleWithSkip {

    public MultiLineRuleRecursive(String startSequence, String endSequence, IToken token, char escapeCharacter,
            List<ILiClipsePredicateRule> loadedSubRules) {
        super(startSequence, endSequence, token, escapeCharacter, loadedSubRules);
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {
        if (resume) {
            return Token.UNDEFINED;
        }

        IMarkScanner markScanner = (IMarkScanner) scanner;
        int mark = markScanner.getMark();

        int c;
        for (int i = 0; i < fStartSequence.length; i++) {
            c = scanner.read();
            if (c != fStartSequence[i]) {
                //Backup to where we started
                markScanner.setMark(mark);
                return Token.UNDEFINED;
            }
        }

        int level = 0;

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

            // Try to match the end sequence
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
                if (level == 0) {
                    return fToken;
                } else {
                    level--;
                }
            }

            // Try to match the start sequence again to see if a level needs to be raised.
            matched = true;
            for (int i = 0;; i++) {
                if (c != fStartSequence[i]) {
                    markScanner.setMark(mark);
                    matched = false;
                    break;
                }
                if (i + 1 < fStartSequence.length) {
                    c = scanner.read();
                } else {
                    break;
                }
            }
            if (matched) {
                level++;
            }

        }
    }

    @Override
    public String toString() {
        return new FastStringBuffer("MultiLineRuleRecursive(", fStartSequence.length + fEndSequence.length + 30)
                .append("start: ")
                .append(fStartSequence)
                .append(" end: ")
                .append(fEndSequence)
                .append(" escapeCharacter: ")
                .append(fEscapeCharacter)
                .append(")")
                .toString();
    }

}