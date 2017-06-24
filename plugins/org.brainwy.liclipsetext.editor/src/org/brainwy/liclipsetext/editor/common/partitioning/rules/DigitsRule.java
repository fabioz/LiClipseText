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

/**
 * A bit different from the number rule because it only accepts digits (not hexa or octal or floats).
 */
public class DigitsRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private IToken fToken;

    public DigitsRule(IToken token) {
        this.fToken = token;
    }

    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }

    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        if (resume) {
            return Token.UNDEFINED;
        }

        int c = scanner.read();
        //It has to start with a digit.
        if (Character.isDigit(c)) {
            while (true) {
                c = scanner.read();
                if (!Character.isDigit(c)) {
                    //Non number: unread char and return the proper token.
                    scanner.unread();
                    return fToken;
                }
            }
        }

        scanner.unread();
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
        return "DigitsRule";
    }
}
