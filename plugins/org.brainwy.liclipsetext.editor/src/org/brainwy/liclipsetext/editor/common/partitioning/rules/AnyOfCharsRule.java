/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.HashSet;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class AnyOfCharsRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private IToken fToken;
    private HashSet<Character> chars;

    public AnyOfCharsRule(IToken token, String string) {
        this.fToken = token;
        chars = new HashSet<>();
        int length = string.length();
        for (int i = 0; i < length; i++) {
            chars.add(string.charAt(i));
        }
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }

    @Override
    public void setToken(IToken token) {
        this.fToken = token;
    }

    @Override
    public IToken getSuccessToken() {
        return this.fToken;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        int c = scanner.read();
        if (chars.contains((char) c)) {
            do {
                c = scanner.read();
            } while (chars.contains((char) c));
            scanner.unread();
            return fToken;
        } else {
            scanner.unread();
        }
        return Token.UNDEFINED;
    }

}
