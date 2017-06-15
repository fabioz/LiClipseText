/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubTokensTokensProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ITextAttributeProviderToken;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.DummyToken;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

public final class LiClipseDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

    private final CustomTextAttributeTokenCreator defaultTokenCreator;
    private ICustomPartitionTokenScanner fScanner;
    private TextAttribute fDefaultTextAttribute = new TextAttribute(null);
    private IDocument fDocument;

    public LiClipseDamagerRepairer(ICustomPartitionTokenScanner scanner,
            CustomTextAttributeTokenCreator defaultTokenCreator) {
        this.fScanner = scanner;
        this.defaultTokenCreator = defaultTokenCreator;
    }

    private void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
        try {
            if (attr != null) {
                int style = attr.getStyle();
                int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
                StyleRange styleRange = new StyleRange(offset, length, attr.getForeground(), attr.getBackground(),
                        fontStyle);
                styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
                styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
                styleRange.font = attr.getFont();
                presentation.addStyleRange(styleRange);
            }
        } catch (Exception e) {
            Log.log(e);
        }
    }

    @Override
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e,
            boolean documentPartitioningChanged) {
        // The DefaultDamagerRepairer will usually not update the whole partition
        // if the partitioning hasn't changed (i.e.: when some text is highlighted
        // for instance).
        // But this doesn't work well if the partitioner is more complex and has
        // to look for the whole partition in order to get the colors properly
        // (i.e.: textmate rules)
        // So, we override to always update the whole partition.
        //
        // Example failing:
        //
        // Code:
        // def in(a)
        // end
        // c=A.new(a, b)
        //
        // With textmate ruby grammar:
        // highlight 'end' -- by doing so the end color actually becomes
        // wrong because the scanner started at 'end' and not at the start
        // of the partition.
        return partition;
    }

    @Override
    public void createPresentation(TextPresentation presentation, ITypedRegion region) {
        try {
            internalCreatePresentation(presentation, region);
        } catch (Exception e) {
            Log.log(e);
        }
    }

    @Override
    public void setDocument(IDocument document) {
        fDocument = document;
    }

    private void internalCreatePresentation(TextPresentation presentation, ITypedRegion region) {
        int lastStart = region.getOffset();
        int length = 0;
        boolean firstToken = true;
        IToken lastToken = Token.UNDEFINED;
        TextAttribute lastAttribute = getTokenTextAttribute(lastToken);

        SubTokensTokensProvider subTokensProvider = new SubTokensTokensProvider(fDocument, region, fScanner);

        int maxOffset = region.getOffset() + region.getLength();
        int minOffset = region.getOffset();
        //System.out.println("Computing sub tokens scanning");
        int i = 0;
        long initialTime = System.currentTimeMillis();
        while (true) {
            IToken token = subTokensProvider.nextToken();
            if (token.isEOF()) {
                break;
            }
            i += 1;
            if (i % 500 == 0) {
                if (System.currentTimeMillis() - initialTime > 10000) {
                    Log.log("Skipping coloring for region: " + region + " (10 seconds elapsed). Iterations: "
                            + i);
                    break;
                }
            }

            final TextAttribute attribute = getTokenTextAttribute(token);
            final int tokenOffset = subTokensProvider.getTokenOffset();
            final int tokenLength = subTokensProvider.getTokenLength();

            if (tokenOffset < minOffset) {
                Log.log("Error in scanning partition: tokenOffset < minOffset.");
                continue;
            }
            if (tokenOffset + tokenLength > maxOffset) {
                Log.log("Error in scanning partition: tokenOffset + tokenLength > maxOffset.");
                continue;
            }

            if (lastAttribute != null && lastAttribute.equals(attribute)) {
                length += tokenLength;
                firstToken = false;
            } else {
                if (!firstToken) {
                    addRange(presentation, lastStart, length, lastAttribute);
                }
                firstToken = false;
                lastToken = token;
                lastAttribute = attribute;
                lastStart = tokenOffset;
                length = tokenLength;
            }
        }

        addRange(presentation, lastStart, length, lastAttribute);
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