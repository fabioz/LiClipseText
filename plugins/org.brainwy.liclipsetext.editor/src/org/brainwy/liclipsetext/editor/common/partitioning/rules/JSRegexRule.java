/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.io.IOException;

import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionCodeReader;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class JSRegexRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private IToken fToken;
    private SingleLineRule rule;

    public JSRegexRule(IToken token) {
        this.fToken = token;
        rule = new SingleLineRule("/", fToken, '\\', true);
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }

    @Override
    public void setToken(IToken token) {
        this.fToken = token;
        rule.setToken(token);
    }

    @Override
    public IToken getSuccessToken() {
        return fToken;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        IMarkScanner markScanner = (IMarkScanner) scanner;
        final int initialMark = markScanner.getMark();

        IToken ret = rule.evaluate(scanner, resume);

        if (!ret.isUndefined()) {
            int diff = markScanner.getMark() - initialMark;
            if (diff == 2) {
                // // (2 slashes) is a comment, not a regexp
                markScanner.setMark(initialMark);
                return Token.UNDEFINED;
            }

            //We found something which could be a JS regexp, let's see its context to know if it really matched.
            try {
                if (contextIsJSRegex(initialMark, (ScannerRange) scanner)) {
                    int c = scanner.read();
                    while (c == 'g' || c == 'i' || c == 'm') {
                        c = scanner.read();
                    }
                    scanner.unread();
                    return ret;
                }
            } catch (IOException | BadPositionCategoryException e) {
                Log.log(e);
                markScanner.setMark(initialMark);
                return Token.UNDEFINED;
            }
            markScanner.setMark(initialMark);
            return Token.UNDEFINED;
        }

        return ret;
    }

    private boolean contextIsJSRegex(int mark, ScannerRange scanner)
            throws IOException, BadPositionCategoryException {
        mark--; //I.e.: the mark passed is the position of the '/' and we want to check before that.
        if (mark < 0) {
            //skip
            return true; //at pos 0 it's a regexp (can't be a division).
        }

        final int initialOffset = mark;
        final IDocument doc = scanner.getDocument();
        ICharacterScanner backwardScanner = new ICharacterScanner() {

            private int fOffset = initialOffset;

            @Override
            public void unread() {
                throw new RuntimeException("Not implemented");
            }

            @Override
            public int read() {
                if (fOffset >= doc.getLength() || fOffset <= 0) {
                    return PartitionCodeReader.EOF;
                }
                int c;
                try {
                    c = doc.getChar(fOffset);
                } catch (BadLocationException e) {
                    Log.log(e);
                    return PartitionCodeReader.EOF;
                }
                fOffset--;
                return c;
            }

            @Override
            public char[][] getLegalLineDelimiters() {
                return null;
            }

            @Override
            public int getColumn() {
                return 0;
            }
        };

        int c = backwardScanner.read();
        while (Character.isWhitespace(c)) {
            if (c == '\r' || c == '\n' || c == PartitionCodeReader.EOF) {
                //only whitespaces before means regex
                return true;
            }

            c = backwardScanner.read();
        }
        if (PartitionCodeReader.EOF == c) {
            //only whitespaces before means regex
            return true;
        }

        //one of these means division
        if (c == '}' || c == ']' || c == ')' || Character.isLetterOrDigit(c)) {
            return false;
        }
        //++/ or --/ means division
        if (c == '-' || c == '+') {
            int c2 = backwardScanner.read();
            if (c2 == c) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "JSRegexRule";
    }
}
