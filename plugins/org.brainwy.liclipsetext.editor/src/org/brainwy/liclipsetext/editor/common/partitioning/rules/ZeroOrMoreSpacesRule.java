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

public class ZeroOrMoreSpacesRule implements ILiClipsePredicateRule, IChangeTokenRule {

    protected IToken fToken;

    public void setToken(IToken token) {
        this.fToken = token;
    }

    public ZeroOrMoreSpacesRule(IToken token) {
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
            //Note that even if we don't have a match, we return something (even if 0 length)
            scanner.unread();
            return fToken;
        }

        return fToken;
    }

    @Override
    public String toString() {
        return "ZeroOrMoreSpacesRule";
    }
}
