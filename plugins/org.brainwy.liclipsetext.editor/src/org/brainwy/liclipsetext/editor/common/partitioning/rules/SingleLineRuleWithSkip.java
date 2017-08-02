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
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * A single line rule which usually ends in the end of the line but may have skips.
 */
public class SingleLineRuleWithSkip implements ILiClipsePredicateRule, IChangeTokenRule {

    protected IToken fToken;
    private final char[] sequence;
    private final char escapeCharacter;
    private final boolean escapeContinuesLine;
    protected final ILiClipsePredicateRule[] subRules;

    @Override
    public void setToken(IToken token) {
        this.fToken = token;
    }

    public char[] getSequence() {
        return sequence;
    }

    public SingleLineRuleWithSkip(String sequence, IToken token, char escapeCharacter,
            boolean escapeContinuesLine, List<ILiClipsePredicateRule> loadedSubRules) {
        Assert.isTrue(sequence.length() > 0);
        this.sequence = sequence.toCharArray();
        this.fToken = token;
        this.escapeCharacter = escapeCharacter;
        this.escapeContinuesLine = escapeContinuesLine;
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
        } else {
            int c = scanner.read();
            if (c == sequence[0]) {
                //matched first. Let's check the remainder
                for (int i = 1; i < sequence.length; i++) {
                    c = scanner.read();
                    if (c != sequence[i]) {
                        scanner.unread();
                        for (int j = 0; j < i; j++) {
                            scanner.unread();
                        }
                        return Token.UNDEFINED;
                    }
                }
                //if it got here, the start was detected
                if (detectEnd(scanner)) {
                    return fToken;
                }
            }
        }

        scanner.unread();
        return Token.UNDEFINED;
    }

    private boolean detectEnd(ICharacterScanner scanner) throws DocumentTimeStampChangedException {
        OUT: while (true) {
            int c = scanner.read();
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

            if (c == ICharacterScanner.EOF) {
                //match
                return true;

            } else if (c == escapeCharacter) {
                if (escapeContinuesLine) {
                    //Consume new line and keep on matching
                    c = scanner.read();
                    if (c == '\r') {
                        c = scanner.read();
                        if (c != '\n') {
                            scanner.unread();
                        }
                    }
                } else {
                    //Escape does not continue line: if it's a new line, match it (but don't consume it).
                    c = scanner.read();
                    if (c == '\r' || c == '\n') {
                        scanner.unread();
                    }

                    return true;
                }
            } else if (c == '\r' || c == '\n') {
                //If it's a new line, match it (but don't consume it).
                //                scanner.unread();
                return true;

            } else if (c == sequence[0]) {
                // Let's check if we had a match: if we did, return true, otherwise, keep on going.

                //matched first. Let's check the remainder
                boolean found = true;
                for (int i = 1; i < sequence.length; i++) {
                    c = scanner.read();
                    if (c != sequence[i]) {
                        found = false;
                        scanner.unread();
                        for (int j = 0; j < i; j++) {
                            scanner.unread();
                        }
                        break;
                    }
                }
                return found;

            }
        }
    }

    @Override
    public String toString() {
        return new FastStringBuffer("SingleLineRule(", sequence.length + 60)
                .append("sequence: ")
                .append(sequence)
                .append(" escapeCharacter:")
                .append(escapeCharacter)
                .append(" escapeContinuesLine:")
                .append(escapeContinuesLine)
                .append(")")
                .toString();
    }
}
