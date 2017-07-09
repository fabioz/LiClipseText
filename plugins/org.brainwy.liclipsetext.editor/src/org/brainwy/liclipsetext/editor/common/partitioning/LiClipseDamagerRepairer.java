/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubTokensTokensProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.tm4e.Tm4ePartitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ITextAttributeProviderToken;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.DummyToken;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

public final class LiClipseDamagerRepairer implements IPresentationRepairer, IPresentationDamager {

    public static boolean MERGE_TOKENS = true;
    private static final boolean DEBUG = false;
    private final CustomTextAttributeTokenCreator defaultTokenCreator;
    private ICustomPartitionTokenScanner fScanner;
    private TextAttribute fDefaultTextAttribute = new TextAttribute(null);
    private IDocument fDocument;

    public LiClipseDamagerRepairer(ICustomPartitionTokenScanner scanner,
            CustomTextAttributeTokenCreator defaultTokenCreator) {
        this.fScanner = scanner;
        this.defaultTokenCreator = defaultTokenCreator;
    }

    private StyleRange addRange(TextPresentation presentation, int offset, int length, TextAttribute attr, IToken token,
            StyleRange lastRange) {
        try {
            if (attr != null) {
                if (lastRange != null) {
                    if (lastRange.start >= offset) {
                        Log.log("Error. Trying to add range (" + offset + ") < last range (" + lastRange.start + ")");
                        return lastRange;
                    }
                }
                int style = attr.getStyle();
                int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
                StyleRange styleRange = new StyleRange(offset, length, attr.getForeground(), attr.getBackground(),
                        fontStyle);
                styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
                styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
                styleRange.font = attr.getFont();
                styleRange.data = token;
                presentation.addStyleRange(styleRange);
                return styleRange;
            }
        } catch (Exception e) {
            Log.log(e);
        }
        return null;
    }

    @Override
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
        IDocument document = e.getDocument();
        IRegion ret = partition;
        if (!documentPartitioningChanged) {
            try {
                IRegion info = document.getLineInformationOfOffset(e.getOffset());
                if (fScanner instanceof Tm4ePartitionScanner) {
                    // Dealing with textmate grammar: damage must be done from line start or start of partition to end of partition.
                    int offset = Math.max(info.getOffset(), partition.getOffset());
                    int partitionEndOffset = partition.getOffset() + partition.getLength();
                    return new Region(offset, partitionEndOffset - offset);
                }
                IRegion infoEnd = document.getLineInformationOfOffset(e.getOffset() + e.getText().length());
                if (info.getOffset() == infoEnd.getOffset()) {
                    return info;
                }
                if (info.getOffset() > infoEnd.getOffset()) {
                    IRegion temp = info;
                    info = infoEnd;
                    infoEnd = temp;
                }
                ret = new Region(info.getOffset(),
                        (infoEnd.getOffset() + infoEnd.getLength()) - info.getOffset());
                if (DEBUG) {
                    System.out.println("Damage:");
                    System.out.println(document.get(ret.getOffset(), ret.getLength()));
                }
                return ret;
            } catch (BadLocationException x) {
                Log.log(x);
            }
        } else {
            ret = partition;
        }
        if (DEBUG) {
            System.out.println("Damage partition:");
            try {
                System.out.println(document.get(ret.getOffset(), ret.getLength()));
            } catch (BadLocationException e1) {
                Log.log(e1);
            }
        }

        return ret;
    }

    /**
     * Note: prefer the version which receives the document modification stamp.
     */
    @Override
    public void createPresentation(TextPresentation presentation, ITypedRegion region) {
        // Note: this may be called in a thread, so, if the document changes during the process, the
        // text presentation should be considered invalid (the caller process has to take care of that).
        long docTime = ((IDocumentExtension4) fDocument).getModificationStamp();
        createPresentation(presentation, region, docTime, true);
    }

    /**
     * This method should be preferred if the docTime is gotten at the start of some method.
     */
    public void createPresentation(TextPresentation presentation, ITypedRegion region, long docTime,
            boolean cacheFinalResult) {
        try {
            internalCreatePresentation(presentation, region, docTime, cacheFinalResult);
        } catch (Exception e) {
            if (e instanceof DocumentTimeStampChangedException) {
                return;
            }
            if (docTime != ((IDocumentExtension4) fDocument).getModificationStamp()) {
                // Callers are responsible for re-requesting in this case.
                return;
            }
            Log.log(e);
        }
    }

    @Override
    public void setDocument(IDocument document) {
        fDocument = document;
    }

    private void internalCreatePresentation(TextPresentation presentation, ITypedRegion region, long docTime,
            boolean cacheFinalResult)
            throws DocumentTimeStampChangedException {
        int lastStart = region.getOffset();
        int lastTokenEndOffset = lastStart;
        boolean firstToken = true;
        IToken lastToken = Token.UNDEFINED;
        TextAttribute lastAttribute = getTokenTextAttribute(lastToken);
        IDocument doc = fDocument;
        if (hasTimeChanged(docTime, doc)) {
            throw new DocumentTimeStampChangedException();
        }

        if (DEBUG) {
            System.err.println("\n\n\nStarting to compute tokens for region: start offset: " + region.getOffset()
                    + " end offset: " + (region.getOffset() + region.getLength()) + " thread: "
                    + Thread.currentThread().getName());
        }
        SubTokensTokensProvider subTokensProvider = new SubTokensTokensProvider(doc, region, fScanner, docTime,
                cacheFinalResult);

        int maxOffset = region.getOffset() + region.getLength();
        int minOffset = region.getOffset();
        //System.out.println("Computing sub tokens scanning");
        int i = 0;
        StyleRange lastRange = null;
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

                final TextAttribute attribute = getTokenTextAttribute(token);
                final int tokenOffset = subTokensProvider.getTokenOffset();
                final int tokenEndOffset = tokenOffset + subTokensProvider.getTokenLength();
                if (DEBUG) {
                    System.out.println("Token found: offset start: " + tokenOffset + " offset end: " + tokenEndOffset);
                }

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

                lastToken = token;
                if (MERGE_TOKENS && lastAttribute != null && lastAttribute.equals(attribute)) {
                    if (tokenEndOffset > lastTokenEndOffset) {
                        lastTokenEndOffset = tokenEndOffset;
                    }
                } else {
                    if (!firstToken) {
                        lastRange = addRange(presentation, lastStart, lastTokenEndOffset - lastStart, lastAttribute,
                                token, lastRange);
                    }
                    firstToken = false;
                    lastAttribute = attribute;
                    lastStart = tokenOffset;
                    lastTokenEndOffset = tokenEndOffset;
                }
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

        if (lastTokenEndOffset > lastStart) {
            lastRange = addRange(presentation, lastStart, lastTokenEndOffset - lastStart, lastAttribute, lastToken,
                    lastRange);
        }
    }

    private boolean hasTimeChanged(long docTime, IDocument doc) {
        return docTime != ((IDocumentExtension4) doc).getModificationStamp() || doc != fDocument;
    }

    private TextAttribute getTokenTextAttribute(IToken token) {
        Object data = token.getData();
        if (data instanceof TextAttribute) {
            Log.log("Not expecting data to be a TextAttribute.");
            return (TextAttribute) data;
        }
        if (token instanceof ITextAttributeProviderToken) {
            ITextAttributeProviderToken colorToken = (ITextAttributeProviderToken) token;
            return colorToken.getTokenTextAttribute(defaultTokenCreator);
        }

        if (token instanceof DummyToken) {
            return fDefaultTextAttribute;
        }

        if (data instanceof String) {
            Log.log("Expected token to be a ITextAttributeProviderToken. Found String: " + data);
        } else if (data != null) {
            Log.log("Data not TextAttribute nor String: " + data);
        }
        return fDefaultTextAttribute;
    }
}