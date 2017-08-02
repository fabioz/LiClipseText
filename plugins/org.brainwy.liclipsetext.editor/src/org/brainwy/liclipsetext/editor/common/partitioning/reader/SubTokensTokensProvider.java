/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.reader;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.TypedRegionWithSubTokens;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.partitioner.TypedPositionWithSubTokens;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
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

    public SubTokensTokensProvider(IDocument document, ITypedRegion region, ICustomPartitionTokenScanner tokenScanner,
            long docTime, boolean cacheFinalResult) {
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
            this.scannerRange = tokenScanner.createScannerRange(document, region.getOffset(), region.getLength(),
                    docTime);
            this.scannerRange.setCacheFinalResult(cacheFinalResult);
        }
    }

    public IToken nextToken() throws DocumentTimeStampChangedException {
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

    public static List<SubRuleToken> getTokens(IDocument doc, ITypedRegion region,
            ICustomPartitionTokenScanner scanner,
            long docTime, boolean cacheFinalResult) throws DocumentTimeStampChangedException {

        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) doc.getDocumentPartitioner();
        List<SubRuleToken> tokens = documentPartitioner.getCachedTokens(doc, region, docTime);
        if (tokens != null) {
            return tokens;
        }

        tokens = new LinkedList<SubRuleToken>();
        int lastStart = region.getOffset();

        SubTokensTokensProvider subTokensProvider = new SubTokensTokensProvider(doc, region, scanner, docTime,
                cacheFinalResult);

        int maxOffset = region.getOffset() + region.getLength();
        int minOffset = region.getOffset();

        if (maxOffset > doc.getLength()) {
            Log.logInfo("maxOffset (" + maxOffset + ") > doc len (" + doc.getLength() + ").");
        }
        int i = 0;
        try {
            while (true) {
                IToken token = subTokensProvider.nextToken();
                if (token.isEOF()) {
                    break;
                }
                i += 1;
                if (i % 50 == 0) {
                    if (hasTimeChanged(docTime, doc)) {
                        throw new DocumentTimeStampChangedException();
                    }
                }

                final int tokenOffset = subTokensProvider.getTokenOffset();
                final int tokenEndOffset = tokenOffset + subTokensProvider.getTokenLength();

                if (tokenOffset < minOffset) {
                    if (hasTimeChanged(docTime, doc)) {
                        throw new DocumentTimeStampChangedException();
                    }

                    Log.log(StringUtils.format(
                            "Error in scanning partition: tokenOffset (%s) < minOffset (%s).", tokenOffset, minOffset));
                    continue;
                }
                if (tokenEndOffset > maxOffset) {
                    if (hasTimeChanged(docTime, doc)) {
                        throw new DocumentTimeStampChangedException();
                    }
                    Log.log(StringUtils.format(
                            "Error in scanning partition: tokenOffset (%s) -----  tokenEndOffset (%s) > maxOffset (%s). Doc len: %s",
                            tokenOffset, tokenEndOffset, maxOffset, doc.getLength()));
                    break;
                }

                if (tokenEndOffset < tokenOffset) {
                    if (hasTimeChanged(docTime, doc)) {
                        throw new DocumentTimeStampChangedException();
                    }

                    Log.log(StringUtils.format(
                            "Error in scanning partition: tokenEndOffsetOffset (%s) < tokenOffset (%s).",
                            tokenEndOffset, tokenOffset, maxOffset));
                    continue;
                }
                if (tokenEndOffset == tokenOffset) {
                    continue; // 0-len partition is Ok on textmate
                }

                if (tokenOffset < lastStart) {
                    if (hasTimeChanged(docTime, doc)) {
                        throw new DocumentTimeStampChangedException();
                    }

                    Log.log(StringUtils.format(
                            "Error in scanning partition: tokenOffset (%s) < lastStart (%s).",
                            tokenOffset, lastStart));
                    continue;
                }
                tokens.add(new SubRuleToken(token, tokenOffset, tokenEndOffset - tokenOffset));

                lastStart = tokenOffset;
            }
        } catch (RuntimeException e) {
            // If the doc changes in the meanwhile, index errors are ok.
            if (hasTimeChanged(docTime, doc)) {
                throw new DocumentTimeStampChangedException();
            }
            Log.log(e);
        }

        if (hasTimeChanged(docTime, doc)) {
            throw new DocumentTimeStampChangedException();
        }

        if (cacheFinalResult) {
            documentPartitioner.setCachedTokens(doc, region, docTime, tokens);
        }
        return tokens;

    }

    private static boolean hasTimeChanged(long docTime, IDocument doc) {
        return docTime != ((IDocumentExtension4) doc).getModificationStamp();
    }

}
