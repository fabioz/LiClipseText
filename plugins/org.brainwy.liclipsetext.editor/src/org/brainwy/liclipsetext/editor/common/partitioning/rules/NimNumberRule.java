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

public class NimNumberRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private IToken fToken;

    public NimNumberRule(IToken token) {
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
                            || c == 'd' || c == 'D' || c == 'e' || c == 'E' || c == 'f' || c == 'F' || c == '_') {
                        //still hexa
                    } else {
                        //Non hexa: unread char and return the proper token.
                        if (c == '\'') {
                            return consumeNumberPostfix(scanner);
                        } else {
                            scanner.unread();
                        }
                        return fToken;
                    }
                }
            } else if ((char) c == '0' && c1 == 'o') {
                //Match octal
                while (true) {
                    c = scanner.read();
                    if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7'
                            || c == '_') {
                        //still octal
                    } else {
                        //Non octal: unread char and return the proper token.
                        if (c == '\'') {
                            return consumeNumberPostfix(scanner);
                        } else {
                            scanner.unread();
                        }
                        return fToken;
                    }
                }

            } else if ((char) c == '0' && (c1 == 'b' || c1 == 'B')) {
                //Match binary
                while (true) {
                    c = scanner.read();
                    if (c == '0' || c == '1' || c == '_') {
                        //still binary
                    } else {
                        //Non binary: unread char and return the proper token.
                        if (c == '\'') {
                            return consumeNumberPostfix(scanner);
                        } else {
                            scanner.unread();
                        }
                        return fToken;
                    }
                }

            } else {
                //Match decimal/floats
                scanner.unread(); //unread the one we just read to match the hexa/octal/binary
                while (true) {
                    c = scanner.read();
                    if (Character.isDigit(c) || c == 'e' || c == '.' || c == '_') {
                        //still number
                    } else {
                        //Non number: unread char and return the proper token.
                        if (c == '\'') {
                            return consumeNumberPostfix(scanner);
                        } else {
                            scanner.unread();
                        }
                        return fToken;
                    }
                }
            }

        }

        scanner.unread();
        return Token.UNDEFINED;
    }

    private IToken consumeNumberPostfix(ICharacterScanner scanner) {
        //Called right after finding a ' char.
        int c = scanner.read();
        while (Character.isLetterOrDigit(c)) {
            c = scanner.read();
        }
        scanner.unread();
        return this.fToken;
    }

    public void setToken(IToken token) {
        this.fToken = token;
    }

    public IToken getSuccessToken() {
        return fToken;
    }

    @Override
    public String toString() {
        return "NimNumberRule";
    }
}
