/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.HashSet;
import java.util.List;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class RepeatCharToEolRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private IToken fToken;
    private final HashSet<Integer> chars;

    public RepeatCharToEolRule(IToken token, List<String> chars) {
        this.fToken = token;
        this.chars = new HashSet<Integer>();
        for (String s : chars) {
            if (s.length() != 1) {
                throw new AssertionFailedException(
                        "RepeatCharToEolRule expects a list of Strings with a single char. Found: "
                                + s + " with " + s.length() + " chars.");
            }
            this.chars.add((int) s.charAt(0));
        }
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
        IMarkScanner markScanner = (IMarkScanner) scanner;
        int mark = markScanner.getMark();

        int foundC = scanner.read();
        if (foundC == ICharacterScanner.EOF || !this.chars.contains(foundC)) {
            scanner.unread();
            return Token.UNDEFINED;
        }

        //Read the first time out of the loop: we must have at least 2 chars in sequence to consider it.
        int c;
        c = scanner.read();
        switch (c) {
            case ICharacterScanner.EOF:
            case '\r':
            case '\n':
                markScanner.setMark(mark);
                return Token.UNDEFINED;
        }
        if (c != foundC) {
            markScanner.setMark(mark);
            return Token.UNDEFINED;
        }

        while (true) {
            c = scanner.read();
            switch (c) {
                case ICharacterScanner.EOF:
                case '\r':
                case '\n':
                    scanner.unread();
                    return fToken;
            }
            if (c != foundC) {
                markScanner.setMark(mark);
                return Token.UNDEFINED;
            }
        }

    }

    public void setToken(IToken token) {
        this.fToken = token;
    }

}
