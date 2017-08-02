/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class OptionalSequenceRule implements ILiClipsePredicateRule, IChangeTokenRule {

    protected IToken fToken;
    private char[] sequence;

    public void setToken(IToken token) {
        this.fToken = token;
    }

    public OptionalSequenceRule(String sequence, IToken token) {
        this.fToken = token;
        this.sequence = sequence.toCharArray();
    }

    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }

    public IToken getSuccessToken() {
        return fToken;
    }

    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        if (resume) {
            return Token.UNDEFINED;
        }
        for (int i = 0; i < sequence.length; i++) {
            int c = scanner.read();
            if (c != sequence[i]) {
                //Backup to where we started
                for (int j = 0; j <= i; j++) {
                    scanner.unread();
                }
                return fToken;
            }
        }

        return fToken;
    }

    @Override
    public String toString() {
        return new FastStringBuffer("OptionalSequenceRule(", sequence.length + 40)
                .append("sequence: ")
                .append(sequence)
                .append(")")
                .toString();
    }
}
