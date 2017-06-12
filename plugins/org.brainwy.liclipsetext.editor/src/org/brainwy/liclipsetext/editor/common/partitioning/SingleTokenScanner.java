/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.PartitionCodeReaderInScannerHelper;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SingleTokenScanner implements ICustomPartitionTokenScanner {

    protected IToken fDefaultReturnToken = new Token(null);

    public void setDefaultReturnToken(IToken defaultReturnToken) {
        Assert.isNotNull(defaultReturnToken.getData());
        fDefaultReturnToken = defaultReturnToken;
    }

    public void nextToken(ScannerRange range) {
        range.startNextToken();

        if (range.read() == ICharacterScanner.EOF) {
            range.setToken(Token.EOF);
            return;
        }
        while (range.read() != ICharacterScanner.EOF) {
            //keep on going until the end if the first was not an EOF.
        }
        range.unread(); //unread EOF
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
    	// No cache
    }

}
