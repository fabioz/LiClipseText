/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.Set;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class WordSeparatorRule implements ILiClipsePredicateRule, IChangeTokenRule, ILanguageDependentRule {

    private IToken fToken;
    private LiClipseLanguage liClipseLanguage;
    private Set<Character> separatorChars;

    public WordSeparatorRule(IToken token) {
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

        if (separatorChars == null) {
            separatorChars = liClipseLanguage.getSeparatorChars();
        }

        int c = scanner.read();
        try {
            if (c == ICharacterScanner.EOF || Character.isWhitespace(c) || separatorChars.contains((char) c)) {
                return fToken;
            }
        } finally {
            //Always unread (this is always a 0-sized rule).
            scanner.unread();
        }
        return Token.UNDEFINED;
    }

    public void setToken(IToken token) {
        this.fToken = token;
    }

    @Override
    public void setLanguage(LiClipseLanguage liClipseLanguage) {
        this.liClipseLanguage = liClipseLanguage;
    }

}
