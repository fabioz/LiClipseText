/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SingleTokenScanner implements ICustomPartitionTokenScanner {

    protected IToken fDefaultReturnToken = new Token(null);

    @Override
    public void setDefaultReturnToken(IToken defaultReturnToken) {
        Assert.isNotNull(defaultReturnToken.getData());
        fDefaultReturnToken = defaultReturnToken;
    }

    @Override
    public IToken getDefaultReturnToken() {
        return fDefaultReturnToken;
    }

    @Override
    public void nextToken(ScannerRange range) {
        range.startNextToken();

        if (range.read() == ICharacterScanner.EOF) {
            range.setToken(Token.EOF);
            return;
        } else {
            range.setMark(range.getRangeEndOffset());
            range.setToken(fDefaultReturnToken);
        }
    }
}
