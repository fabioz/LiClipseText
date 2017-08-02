/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.List;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SequencesRule implements ILiClipsePredicateRule, IChangeTokenRule {

    protected IToken fToken;
    private SequenceRule[] sequenceRules;

    public SequencesRule(List<String> sequences, IToken token) {
        sequenceRules = new SequenceRule[sequences.size()];

        for (int i = 0; i < sequences.size(); i++) {
            sequenceRules[i] = new SequenceRule(sequences.get(i), token);
        }
        this.fToken = token;
    }

    public void setToken(IToken token) {
        this.fToken = token;
        for (int i = 0; i < sequenceRules.length; i++) {
            SequenceRule rule = sequenceRules[i];
            rule.setToken(token);
        }
    }

    @Override
    public String toString() {
        return new FastStringBuffer("SequencesRule(", sequenceRules.length + 40)
                .append("sequences: ")
                .append(StringUtils.join("\n", (Object[]) sequenceRules))
                .append(")")
                .toString();
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
        for (int i = 0; i < sequenceRules.length; i++) {
            SequenceRule rule = sequenceRules[i];
            IToken evaluate = rule.evaluate(scanner, resume);
            if (!evaluate.isUndefined()) {
                return evaluate;
            }
        }

        return Token.UNDEFINED;
    }

}
