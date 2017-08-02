/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.AbstractLiClipseRuleBasedScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.LinkedListWarningOnSlowOperations;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * StringScanner for a partition: its configuration is defined by the language definition
 * (where we define a scope).
 */
public class LiClipsePartitionScanner extends AbstractLiClipseRuleBasedScanner {

    private final ScopeColorScanning scopeColoringScanning;
    private ILiClipsePredicateRule[] subRules;
    private final ScannerHelper helper = new ScannerHelper();

    /**
     * Only for tests!
     */
    public LiClipsePartitionScanner() {
        scopeColoringScanning = null;
    }

    public LiClipsePartitionScanner(ScopeColorScanning scopeColoringScanning,
            LiClipseLanguage language) {
        helper.setLanguage(language);
        if (scopeColoringScanning != null) {
            this.scopeColoringScanning = scopeColoringScanning;
            this.scopeColoringScanning.freeze(language);
            this.subRules = this.scopeColoringScanning.getSubRules();
        } else {
            //This means that 'scope_definition_rules' was defined for the scope but
            //in the 'scope' which defines how to scan that scope there's nothing
            //defined (thus, the whole scope will not have sub-partitions and will
            //only have a single color).
            this.scopeColoringScanning = null;
        }
    }

    @Override
    public void nextToken(ScannerRange range) throws DocumentTimeStampChangedException {
        int currOffset = range.getTokenOffset();
        int currLen = range.getTokenLength();

        this.internalNextToken(range);

        int newOffset = range.getTokenOffset();
        if (newOffset < currOffset + currLen) {
            throw new AssertionFailedException(
                    StringUtils.format(
                            "Error: tokens overlapping (curr offset: %s, curr len: %s, newOffset: %s, newLen: %s)",
                            currOffset, currLen, newOffset, range.getTokenLength()));
        }
        // System.out.println("Gotten token: " + this + ": " + ret + " offset: " + newOffset + " len: " + getTokenLength());
    }

    public void internalNextToken(ScannerRange range) throws DocumentTimeStampChangedException {
        if (range.nextOfferedToken()) {
            return;
        }
        range.startNextToken();

        //Check 0: EOF: just bail out
        int c = range.read();
        if (c == ICharacterScanner.EOF) {
            range.setToken(Token.EOF);
            return;
        }

        if (scopeColoringScanning == null) {
            range.setToken(fDefaultReturnToken);
            return;
        }

        if (subRules != null) {
            range.unread();
            Assert.isTrue(range.getTokenLength() == 0);
            final int mark = range.getMark();
            for (int i = 0; i < subRules.length; i++) {
                ILiClipsePredicateRule rule = subRules[i];
                if (rule instanceof IRuleWithSubRules) {
                    IRuleWithSubRules iRuleWithSubRules = (IRuleWithSubRules) rule;

                    //If it returns anything, it matched the sub rules.
                    SubRuleToken subRuleTokens = iRuleWithSubRules.evaluateSubRules(range, true);
                    if (subRuleTokens != null) {
                        if (subRuleTokens.len > 0) {
                            LinkedList<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
                            //Add the root as the one from this scanner!
                            lst.add(new SubRuleToken(fDefaultReturnToken, mark, range.getMark() - mark));
                            subRuleTokens.flatten(lst);
                            //Important: use iterator instead of using indexes
                            //(as it could be a LinkedList).
                            Iterator<SubRuleToken> it = lst.iterator();
                            SubRuleToken subToken = it.next();
                            range.setCurrentSubToken(subToken);
                            SubRuleToken lastToken = subToken;

                            int endBoundary = subToken.offset + subToken.len;
                            while (it.hasNext()) {
                                SubRuleToken sub2 = it.next();
                                if (sub2.offset < endBoundary) {
                                    Log.log(StringUtils.format(
                                            "Error: ignoring token which overlapped (%s and %s overlapped)", lastToken,
                                            sub2));
                                    continue;
                                }
                                endBoundary = sub2.offset + sub2.len;
                                lastToken = sub2;
                                range.offerSubToken(sub2);
                            }
                            return;
                        }
                    }

                } else {
                    IToken token = rule.evaluate(range);
                    if (!token.isUndefined()) {
                        range.setToken(token);
                        return;
                    }
                }
                range.setMark(mark);
            }
            range.read();
        }

        //Note: check for whitespaces only after a rule.
        //As we may want to match a rule at the start of a line, we shouldn't handle all whitespaces
        //when one is found, only to the beginning of the next line.
        if (c == '\r' || c == '\n') {
            do {
                c = range.read();
            } while (c == '\r' || c == '\n'); //keep reading new lines until we get them all.
            range.unread();
            range.setToken(Token.WHITESPACE);
            return;

        } else if (Character.isWhitespace(c)) {
            do {
                c = range.read();
            } while (c != '\r' && c != '\n' && Character.isWhitespace(c));
            range.unread();
            range.setToken(Token.WHITESPACE);
            return;
        }

        boolean caseInsensitive = scopeColoringScanning.caseInsensitive;
        if (caseInsensitive) {
            c = Character.toLowerCase(c);
        }

        Set<Character> tokenChars = scopeColoringScanning.getTokenChars();

        //Not a whitespace: let's check for coloring parts
        if (Character.isJavaIdentifierStart(c) || c == '@' || tokenChars.contains((char) c)) {

            //TODO: This must be improved: basically, we want to get anything that forms a word for the
            //language. We can have a default, but the language must be able to override it.

            //We want to load the full word for the language and after it's loaded, check if we have
            //a color for it.

            FastStringBuffer buf = new FastStringBuffer();
            buf.append((char) c);
            while (true) {
                c = range.read();
                if (caseInsensitive) {
                    c = Character.toLowerCase(c);
                }

                if (Character.isJavaIdentifierPart(c) || c == '@' || tokenChars.contains((char) c)) {
                    buf.append((char) c);
                } else {
                    range.unread();
                    break;
                }
            }
            IToken token = scopeColoringScanning.getToken(buf.toString());
            if (token != null) {
                range.setToken(token);
                return;
            }
            range.setToken(fDefaultReturnToken);
            return;

        } else {
            //TODO: This must be improved: basically, we want to get anything that does NOT form a word for the
            //language. We can have a default, but the language must be able to override it.

            //We want to load everything that may make up an operator, bracket, etc (for which we have a color).

            //Check operators, brackets, etc.
            String single = String.valueOf((char) c); //i.e.: may be a bracket, operator, etc.
            IToken token = scopeColoringScanning.getToken(single);
            if (token != null) {
                FastStringBuffer buf = new FastStringBuffer();
                buf.append((char) c);
                while (true) {
                    c = range.read();
                    if (caseInsensitive) {
                        c = Character.toLowerCase(c);
                    }

                    if (Character.isWhitespace(c) || Character.isJavaIdentifierPart(c)
                            || Character.isJavaIdentifierStart(c)) {
                        range.unread();
                        break;
                    }

                    buf.append((char) c);
                    if (scopeColoringScanning.getToken(buf.toString()) == token) {
                        continue;
                    } else {
                        range.unread();
                        break;
                    }
                }

                range.setToken(token);
                return;
            }
        }

        range.setToken(fDefaultReturnToken);
        return;
    }

}
