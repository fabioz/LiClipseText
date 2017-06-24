/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SequenceRule implements ILiClipsePredicateRule, IChangeTokenRule {

    protected IToken fToken;
    private final char[] sequence;
    private final int length;

    public void setToken(IToken token) {
        this.fToken = token;
    }

    public SequenceRule(String sequence, IToken token) {
        this.fToken = token;
        this.sequence = sequence.toCharArray();
        this.length = this.sequence.length;
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

        IMarkScanner markScanner = (IMarkScanner) scanner;
        int mark = markScanner.getMark();

        for (int i = 0; i < length; i++) {
            int c = scanner.read();
            if (c != sequence[i]) {
                //Backup to where we started
                markScanner.setMark(mark);
                return Token.UNDEFINED;
            }
        }

        return fToken;
    }

    @Override
    public String toString() {
        return new FastStringBuffer("SequenceRule(", sequence.length + 40)
                .append("sequence: ")
                .append(sequence)
                .append(")")
                .toString();
    }
}
