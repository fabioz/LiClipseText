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

public class NumberRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private IToken fToken;

    public NumberRule(IToken token) {
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

            int c1 = scanner.read();
            if ((char) c == '0' && (c1 == 'x' || c1 == 'X')) {
                //Match hexa
                while (true) {
                    c = scanner.read();
                    if (Character.isDigit(c) || c == 'a' || c == 'A' || c == 'b' || c == 'B' || c == 'c' || c == 'C'
                            || c == 'd' || c == 'D' || c == 'e' || c == 'E' || c == 'f' || c == 'F') {
                        //still hexa
                    } else {
                        //Non hexa: unread char and return the proper token.
                        scanner.unread();
                        return fToken;
                    }
                }
            } else {
                //Match non-hexa
                scanner.unread(); //unread the one we just read to match the hexa
                while (true) {
                    c = scanner.read();
                    if (Character.isDigit(c) || c == 'e' || c == '.') {
                        //still number
                    } else {
                        //Non number: unread char and return the proper token.
                        scanner.unread();
                        return fToken;
                    }
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
        return "NumberRule";
    }
}
