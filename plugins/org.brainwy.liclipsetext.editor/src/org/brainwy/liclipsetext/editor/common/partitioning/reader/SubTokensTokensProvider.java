/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.reader;

import java.util.Iterator;
import java.util.List;

import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.TypedRegionWithSubTokens;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.partitioner.TypedPositionWithSubTokens;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SubTokensTokensProvider {

    private ScannerRange scannerRange;
    private List<SubRuleToken> flattened;
    private ICustomPartitionTokenScanner tokenScanner;
    private Iterator<SubRuleToken> subTokensIterator;
    private SubRuleToken currSubToken;
    private final int baseOffset;

    public SubTokensTokensProvider(IDocument document, TypedPosition typedPosition,
            ICustomPartitionTokenScanner tokenScanner) {
        this.tokenScanner = tokenScanner;
        this.baseOffset = typedPosition.offset;
        if (typedPosition instanceof TypedPositionWithSubTokens) {
            TypedPositionWithSubTokens typedPositionWithSubTokens = (TypedPositionWithSubTokens) typedPosition;
            SubRuleToken subRuleToken = typedPositionWithSubTokens.getSubRuleToken();
            if (subRuleToken != null) {
                flattened = subRuleToken.flatten();
                subTokensIterator = flattened.iterator();
            }
        }
        if (subTokensIterator == null) {
            this.scannerRange = tokenScanner.createScannerRange(document, typedPosition.getOffset(),
                    typedPosition.getLength());
        }

    }

    public SubTokensTokensProvider(IDocument document, ITypedRegion region, ICustomPartitionTokenScanner tokenScanner) {
        this.tokenScanner = tokenScanner;
        this.baseOffset = region.getOffset();
        if (region instanceof TypedRegionWithSubTokens) {
            TypedRegionWithSubTokens typedRegionWithSubTokens = (TypedRegionWithSubTokens) region;
            SubRuleToken subRuleToken = typedRegionWithSubTokens.getSubRuleToken();
            if (subRuleToken != null) {
                flattened = subRuleToken.flatten();
                subTokensIterator = flattened.iterator();
            }
        }
        if (subTokensIterator == null) {
            this.scannerRange = tokenScanner.createScannerRange(document, region.getOffset(), region.getLength());
        }
    }

    public IToken nextToken() {
        if (subTokensIterator != null) {
            if (subTokensIterator.hasNext()) {
                currSubToken = subTokensIterator.next();
                return currSubToken.token;
            } else {
                return Token.EOF;
            }
        } else {
            return scannerRange.nextToken(tokenScanner);
        }
    }

    public int getTokenOffset() {
        if (subTokensIterator != null) {
            if (currSubToken != null) {
                return baseOffset + currSubToken.offset;
            } else {
                return 0;
            }

        } else {
            return scannerRange.getTokenOffset();
        }

    }

    public int getTokenLength() {
        if (subTokensIterator != null) {
            if (currSubToken != null) {
                return currSubToken.len;
            } else {
                return 0;
            }
        } else {
            return scannerRange.getTokenLength();
        }
    }

}
