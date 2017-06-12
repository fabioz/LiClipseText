/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.partitioning;

import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public abstract class AbstractLiClipseRuleBasedScanner implements ICustomPartitionTokenScanner {

    protected IRule[] fRules;
    protected IToken fDefaultReturnToken = new Token(null);

    public AbstractLiClipseRuleBasedScanner() {
    }

    public void setRules(IRule[] rules) {
        fRules = rules;
    }

    public void setDefaultReturnToken(IToken defaultReturnToken) {
        Assert.isNotNull(defaultReturnToken.getData());
        fDefaultReturnToken = defaultReturnToken;
        if (IDocument.DEFAULT_CONTENT_TYPE.equals(fDefaultReturnToken.getData())) {
            fDefaultReturnToken = new Token(null);
            Log.log("Not sure why setting the default is not good... we should not set anything in this case and return a Token with null data.");
        }
    }

    public void nextToken(ScannerRange range) {
        //Treat case where we have no rules (read to the end).
        range.startNextToken();

        if (fRules == null) {
            int c;
            if ((c = range.read()) == ICharacterScanner.EOF) {
                range.setToken(Token.EOF);
                return;
            } else {
                while (true) {
                    c = range.read();
                    if (c == ICharacterScanner.EOF) {
                        range.unread();
                        range.setToken(fDefaultReturnToken);
                        return;
                    }
                }
            }
        }

        int length = fRules.length;
        for (int i = 0; i < length; i++) {
            IRule rule = fRules[i];
            if (rule instanceof IRuleWithSubRules) {
                IRuleWithSubRules iRuleWithSubRules = (IRuleWithSubRules) rule;
                SubRuleToken subRuleToken = iRuleWithSubRules.evaluateSubRules(range, true);
                if (subRuleToken == null) {
                    //undefined, just continue.
                    continue;
                } else {
                    range.setSubRuleToken(subRuleToken);
                    range.setToken(((IPredicateRule) rule).getSuccessToken());
                    return;
                }
            } else {
                IToken token = (rule.evaluate(range));
                if (token == null) {
                    Log.log("Error: rule " + rule + " returned a null token.");
                    continue;
                }
                if (!token.isUndefined()) {
                    range.setToken(token);
                    return;
                }
            }
        }

        int c = range.read();
        if (c == ICharacterScanner.EOF) {
            range.setToken(Token.EOF);
            return;
        }

        range.setToken(fDefaultReturnToken);
    }

    @Override
    public ScannerRange createPartialScannerRange(IDocument document, int offset, int length, String contentType,
            int partitionOffset) {
        return new ScannerRange(document, offset, length, contentType, partitionOffset,
                new PartitionCodeReaderInScannerHelper());
    }

    @Override
    public ScannerRange createScannerRange(IDocument document, int offset, int length) {
        return new ScannerRange(document, offset, length, new PartitionCodeReaderInScannerHelper());
    }

    @Override
    public void clearCache(IDocument document, int startAtOffset) {
    	// No internal cache.
    }
}
