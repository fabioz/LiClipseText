/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.HashSet;
import java.util.Set;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class PrevCharNotIn implements ILiClipsePredicateRule, IChangeTokenRule, IEmptyMatchRule {

    private IToken fToken;
    private final Set<Character> chars = new HashSet<>();

    public PrevCharNotIn(IToken token, String chars) {
        this.fToken = token;
        int len = chars.length();
        for (int i = 0; i < len; i++) {
            this.chars.add(chars.charAt(i));
        }
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        if (resume) {
            return Token.UNDEFINED;
        }
        IMarkScanner markScanner = (IMarkScanner) scanner;
        int mark = markScanner.getMark();
        if (mark == 0) {
            return fToken;
        }
        scanner.unread();
        int c = scanner.read();
        if (!chars.contains((char) c)) {
            return fToken;
        }
        return Token.UNDEFINED;
    }

    public void setToken(IToken token) {
        this.fToken = token;
    }

    public IToken getSuccessToken() {
        return fToken;
    }

    @Override
    public String toString() {
        return "PrevCharNotIn";
    }

}
