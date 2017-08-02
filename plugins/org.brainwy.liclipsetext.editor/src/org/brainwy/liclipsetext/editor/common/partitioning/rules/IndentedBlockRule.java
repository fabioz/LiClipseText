/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.List;

import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class IndentedBlockRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private IToken fToken;
    private final String start;
    private final SequenceRule sequenceRule;
    private final CompositeRule additionalStartCompositeRule;
    private final int column;

    /**
     * If start = '..'
     *
     * We'd match
     *
     * ..
     *    aaa
     *    aaa
     *    aaa
     *    aaa
     * stop matching on dedent.
     * @param additionalStartRules
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public IndentedBlockRule(String start, IToken token, List additionalStartRules, int column) {
        this.fToken = token;
        this.start = start;
        sequenceRule = new SequenceRule(start, token);
        if (additionalStartRules != null && additionalStartRules.size() > 0) {
            this.additionalStartCompositeRule = new CompositeRule(additionalStartRules);
        } else {
            this.additionalStartCompositeRule = null;
        }
        this.column = column;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) throws DocumentTimeStampChangedException {
        return evaluate(scanner, false);
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {

        IMarkScanner markScanner = (IMarkScanner) scanner;
        int mark = markScanner.getMark();

        if (!sequenceRule.evaluate(scanner, resume).isUndefined()) {
            //must match exactly sequence at the start of the line
            if (this.column == -1 || Math.abs(scanner.getColumn() - this.start.length()) == this.column) {
                boolean goOn = true;
                if (additionalStartCompositeRule != null) {
                    //LiClipseContentTypeDefinitionScanner
                    SubRuleToken evaluate = additionalStartCompositeRule
                            .evaluateSubRules((ScannerRange) scanner, false);
                    if (evaluate == null) {
                        goOn = false;
                    }
                }
                if (goOn) {
                    int c = scanner.read();
                    if (c == '\r') {
                        //eat the \r\n.
                        c = scanner.read();
                        if (c != '\n') {
                            scanner.unread();
                        }
                    } else if (c == '\n') {

                    } else {
                        readToEndOfLine(scanner);
                    }
                    //Ok, read all lines that are indented.
                    while (readIndentedLine(scanner)) {
                    }
                    return this.fToken;
                }
            }

        }

        markScanner.setMark(mark);
        return Token.UNDEFINED;
    }

    private boolean readIndentedLine(ICharacterScanner scanner) {
        int c = scanner.read();
        if (c == '\r') {
            c = scanner.read();
            if (c != '\n') {
                scanner.unread();
            }
            return true; //found empty new-line
        }
        if (c == '\n') {
            return true; //found empty new-line
        }
        if (c != ICharacterScanner.EOF) {
            if (Character.isWhitespace(c)) {
                //Ok, read to the end of line (it's an indented block)
                readToEndOfLine(scanner);
                return true;
            } else {
                scanner.unread();
                return false;
            }
        }
        scanner.unread();
        return false;

    }

    private void readToEndOfLine(ICharacterScanner scanner) {
        while (true) {
            int c = scanner.read();
            if (c == '\n' || c == '\r') {
                if (c == '\r') {
                    c = scanner.read();
                    if (c != '\n') {
                        scanner.unread();
                    }
                }
                return;
            } else if (c == ICharacterScanner.EOF) {
                scanner.unread();
                return;
            }
            //Not EOL nor EOF: keep on going
        }
    }

    @Override
    public void setToken(IToken token) {
        this.fToken = token;
    }

    @Override
    public IToken getSuccessToken() {
        return fToken;
    }

    @Override
    public String toString() {
        return "IndentedBlockRule";
    }
}
