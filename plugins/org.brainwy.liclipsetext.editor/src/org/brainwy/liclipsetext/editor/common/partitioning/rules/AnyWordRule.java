/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class AnyWordRule implements ILiClipsePredicateRule, IChangeTokenRule {

    protected IToken fToken;
    protected final Set<String> fExcept;
    private final HashSet<Character> fAdditional;
    private final boolean fMustStartUppercase;

    public AnyWordRule(IToken token) {
        this.fToken = token;
        this.fExcept = null;
        this.fAdditional = null;
        this.fMustStartUppercase = false;
    }

    public AnyWordRule(IToken token, List<String> except, String additionalChars, boolean mustStartUppercase) {
        this.fToken = token;
        this.fExcept = new HashSet<>(except);
        this.fAdditional = new HashSet<>();
        if (additionalChars != null) {
            for (int i = 0; i < additionalChars.length(); i++) {
                this.fAdditional.add(additionalChars.charAt(i));
            }
        }
        this.fMustStartUppercase = mustStartUppercase;
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
        if (this.fExcept != null || this.fAdditional != null) {
            int c = scanner.read();
            FastStringBuffer buf = new FastStringBuffer();

            if ((!fMustStartUppercase ? true : Character.isUpperCase(c)) && Character.isJavaIdentifierStart(c)) {
                IMarkScanner markScanner = (IMarkScanner) scanner;
                int mark = markScanner.getMark();
                buf.append((char) c);

                while (true) {
                    c = scanner.read();
                    if (!Character.isJavaIdentifierPart(c)
                            && (this.fAdditional == null || !this.fAdditional.contains((char) c))) {
                        break;
                    }
                    buf.append((char) c);
                }
                if (this.fExcept != null && this.fExcept.contains(buf.toString())) {
                    markScanner.setMark(mark);
                    return Token.UNDEFINED;
                }

                scanner.unread();
                return fToken;
            }
            scanner.unread();

        } else {
            int c = scanner.read();
            if (Character.isJavaIdentifierStart(c)) {
                do {
                    c = scanner.read();
                } while (Character.isJavaIdentifierPart(c));
                scanner.unread();
                return fToken;
            }
            scanner.unread();
        }
        return Token.UNDEFINED;
    }

    public void setToken(IToken token) {
        this.fToken = token;
    }

    @Override
    public String toString() {
        return "AnyWordRule";
    }
}
