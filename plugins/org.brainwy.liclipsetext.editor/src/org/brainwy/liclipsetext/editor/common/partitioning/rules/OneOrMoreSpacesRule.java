/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class OneOrMoreSpacesRule implements ILiClipsePredicateRule, IChangeTokenRule {

    protected IToken fToken;

    public void setToken(IToken token) {
        this.fToken = token;
    }

    public OneOrMoreSpacesRule(IToken token) {
        this.fToken = token;
    }

    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }

    public IToken getSuccessToken() {
        return fToken;
    }

    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        int c = scanner.read();
        if (Character.isWhitespace(c) && c != '\r' && c != '\n') {
            while (true) {
                c = scanner.read();
                if (c == ICharacterScanner.EOF || !Character.isWhitespace(c) || c == '\r' || c == '\n') {
                    scanner.unread();
                    break;
                }
            }
        } else {
            //If we don't read at least one space, it's not a match.
            scanner.unread();
            return Token.UNDEFINED;
        }

        return fToken;
    }

    @Override
    public String toString() {
        return "OneOrMoreSpacesRule";
    }
}
