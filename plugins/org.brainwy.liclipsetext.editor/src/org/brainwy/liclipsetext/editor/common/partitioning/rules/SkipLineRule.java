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

public class SkipLineRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private IToken fToken;

    public SkipLineRule(IToken token) {
        this.fToken = token;
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
        int c = scanner.read();
        if (c == ICharacterScanner.EOF) {
            scanner.unread();
            return fToken; //EOF: match as if it was EOL.
        }

        while (true) {
            switch (c) {
                case ICharacterScanner.EOF:
                    scanner.unread();
                    return fToken;

                case '\r':
                    c = scanner.read();
                    if (c != '\n') {
                        scanner.unread();
                    }
                    return fToken;

                case '\n':
                    return fToken;
            }
            c = scanner.read();

        }

    }

    public void setToken(IToken token) {
        this.fToken = token;
    }

}
