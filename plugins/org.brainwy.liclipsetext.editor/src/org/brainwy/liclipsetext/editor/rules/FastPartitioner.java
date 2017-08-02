///*******************************************************************************
// * Copyright (c) 2000, 2006 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
package org.brainwy.liclipsetext.editor.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallback;
import org.brainwy.liclipsetext.shared_core.document.DocumentSync;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.partitioner.TypedPositionWithSubTokens;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IDocumentPartitionerExtension2;
import org.eclipse.jface.text.IDocumentPartitionerExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.IToken;

/**
 * Copied most code from the original org.eclipse.jface.text.rules.FastPartitioner,
 *
 * extensions include:
 * - a way to get the fScanner
 */
public class FastPartitioner implements IDocumentPartitioner, IDocumentPartitionerExtension,
        IDocumentPartitionerExtension2, IDocumentPartitionerExtension3 {

    /**
     * The position category this partitioner uses to store the document's partitioning information.
     */
    private static final String CONTENT_TYPES_CATEGORY = "__content_types_category"; //$NON-NLS-1$
    /** The partitioner's scanner */
    protected final ICustomPartitionTokenScanner fScanner;
    /** The legal content types of this partitioner */
    protected final String[] fLegalContentTypes;
    protected final Set<String> fLegalContentTypesSet;
    /** The partitioner's document */
    protected IDocument fDocument;
    /** The document length before a document change occurred */
    protected int fPreviousDocumentLength;
    /** The position updater used to for the default updating of partitions */
    protected final DefaultPositionUpdater fPositionUpdater;
    /** The offset at which the first changed partition starts */
    protected int fStartOffset;
    /** The offset at which the last changed partition ends */
    protected int fEndOffset;
    /**The offset at which a partition has been deleted */
    protected int fDeleteOffset;
    /**
     * The position category this partitioner uses to store the document's partitioning information.
     */
    private final String fPositionCategory;
    /**
     * The active document rewrite session.
     */
    private DocumentRewriteSession fActiveRewriteSession;
    /**
     * Flag indicating whether this partitioner has been initialized.
     */
    private boolean fIsInitialized = false;
    /**
     * The cached positions from our document, so we don't create a new array every time
     * someone requests partition information.
     */
    private Position[] fCachedPositions = null;
    /** Debug option for cache consistency checking. */
    private static final boolean CHECK_CACHE_CONSISTENCY = "true" //$NON-NLS-1$
            .equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface.text/debug/FastPartitioner/PositionCache")); //$NON-NLS-1$;

    /**
     * fabioz: provide a way to get the scanner.
     */
    public ICustomPartitionTokenScanner getScanner() {
        return fScanner;
    }

    /**
     * Creates a new partitioner that uses the given scanner and may return
     * partitions of the given legal content types.
     *
     * @param scanner the scanner this partitioner is supposed to use
     * @param legalContentTypes the legal content types of this partitioner
     */
    public FastPartitioner(ICustomPartitionTokenScanner scanner, String[] legalContentTypes) {
        fScanner = scanner;
        fLegalContentTypes = TextUtilities.copy(legalContentTypes);
        fLegalContentTypesSet = new HashSet<String>();
        fLegalContentTypesSet.addAll(Arrays.asList(fLegalContentTypes));
        fPositionCategory = CONTENT_TYPES_CATEGORY + hashCode();
        fPositionUpdater = new DefaultPositionUpdater(fPositionCategory);
    }

    /*
     * @see org.eclipse.jface.text.IDocumentPartitionerExtension2#getManagingPositionCategories()
     */
    @Override
    public String[] getManagingPositionCategories() {
        return new String[] { fPositionCategory };
    }

    /*
     * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
     */
    @Override
    public final void connect(IDocument document) {
        connect(document, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be extended by subclasses.
     * </p>
     */
    @Override
    public void connect(IDocument document, boolean delayInitialization) {
        Assert.isNotNull(document);
        Assert.isTrue(!document.containsPositionCategory(fPositionCategory));

        fDocument = document;
        fDocument.addPositionCategory(fPositionCategory);

        fIsInitialized = false;
        if (!delayInitialization) {
            checkInitialization();
        }
    }

    /**
     * Calls {@link #initialize()} if the receiver is not yet initialized.
     */
    protected final void checkInitialization() {
        if (!fIsInitialized) {
            initialize();
        }
    }

    /**
     * Performs the initial partitioning of the partitioner's document.
     * <p>
     * May be extended by subclasses.
     * </p>
     */
    protected void initialize() {
        fIsInitialized = true;
        clearPositionCache();
        ICallback<Object, IDocument> iCallback = new ICallback<Object, IDocument>() {

            @Override
            public Object call(IDocument doc) {
                Assert.isTrue(doc == fDocument); // Make sure no copy is done here.
                ScannerRange scannerRange = fScanner.createScannerRange(doc);

                try {
                    scannerRange.nextToken(fScanner);
                    while (!scannerRange.getToken().isEOF()) {
                        //                System.out.println("tok:" + token.getData() + " " + fScanner.getTokenOffset() + " "
                        //                        + fScanner.getTokenLength());

                        if (scannerRange.getToken() instanceof SwitchLanguageToken) {
                            //fabioz: we have to deal with having sub-partitions

                            SwitchLanguageToken switchLanguageToken = (SwitchLanguageToken) scannerRange.getToken();
                            SubLanguageToken[] subTokens = switchLanguageToken.subTokens;
                            int len = subTokens.length;
                            for (int i = 0; i < len; i++) {
                                SubLanguageToken subToken = subTokens[i];
                                String contentType = subToken.getFullContentType();
                                if (isSupportedContentType(contentType)) {
                                    TypedPosition p = new TypedPosition(subToken.offset, subToken.len,
                                            contentType);
                                    doc.addPosition(fPositionCategory, p);
                                } else {
                                    Log.log("Skipping content type not registered: " + contentType);
                                }
                            }
                            //fabioz: end

                        } else {
                            String contentType = getTokenContentType(scannerRange.getToken());

                            if (isSupportedContentType(contentType)) {
                                TypedPosition p = new TypedPositionWithSubTokens(scannerRange.getTokenOffset(),
                                        scannerRange.getTokenLength(),
                                        contentType, scannerRange.getSubRuleToken());
                                doc.addPosition(fPositionCategory, p);
                            }
                        }

                        scannerRange.nextToken(fScanner);
                    }
                } catch (BadLocationException x) {
                    // cannot happen as offsets come from scanner
                    Log.log(x);
                } catch (BadPositionCategoryException x) {
                    // cannot happen if document has been connected before
                    Log.log(x);
                } catch (DocumentTimeStampChangedException e) {
                    // This one *should* be done in the main thread, so, this *shouldn't* happen.
                    Log.log(e);
                }
                return null;
            }
        };
        DocumentSync.runWithDocumentSynched(fDocument, iCallback, false);

    }

    /**
     * {@inheritDoc}
     * <p>
     * May be extended by subclasses.
     * </p>
     */
    @Override
    public void disconnect() {

        Assert.isTrue(fDocument.containsPositionCategory(fPositionCategory));

        try {
            fDocument.removePositionCategory(fPositionCategory);
        } catch (BadPositionCategoryException x) {
            // can not happen because of Assert
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be extended by subclasses.
     * </p>
     */
    @Override
    public void documentAboutToBeChanged(DocumentEvent e) {
        if (fIsInitialized) {

            Assert.isTrue(e.getDocument() == fDocument);

            fPreviousDocumentLength = e.getDocument().getLength();
            fStartOffset = -1;
            fEndOffset = -1;
            fDeleteOffset = -1;
        }
    }

    /*
     * @see IDocumentPartitioner#documentChanged(DocumentEvent)
     */
    @Override
    public final boolean documentChanged(DocumentEvent e) {
        if (fIsInitialized) {
            IRegion region = documentChanged2(e);
            return (region != null);
        }
        return false;
    }

    /**
     * Helper method for tracking the minimal region containing all partition changes.
     * If <code>offset</code> is smaller than the remembered offset, <code>offset</code>
     * will from now on be remembered. If <code>offset  + length</code> is greater than
     * the remembered end offset, it will be remembered from now on.
     *
     * @param offset the offset
     * @param length the length
     */
    private void rememberRegion(int offset, int length) {
        // remember start offset
        if (fStartOffset == -1) {
            fStartOffset = offset;
        } else if (offset < fStartOffset) {
            fStartOffset = offset;
        }

        // remember end offset
        int endOffset = offset + length;
        if (fEndOffset == -1) {
            fEndOffset = endOffset;
        } else if (endOffset > fEndOffset) {
            fEndOffset = endOffset;
        }
    }

    /**
     * Remembers the given offset as the deletion offset.
     *
     * @param offset the offset
     */
    private void rememberDeletedOffset(int offset) {
        fDeleteOffset = offset;
    }

    /**
     * Creates the minimal region containing all partition changes using the
     * remembered offset, end offset, and deletion offset.
     *
     * @return the minimal region containing all the partition changes
     */
    private IRegion createRegion() {
        if (fDeleteOffset == -1) {
            if (fStartOffset == -1 || fEndOffset == -1) {
                return null;
            }
            return new Region(fStartOffset, fEndOffset - fStartOffset);
        } else if (fStartOffset == -1 || fEndOffset == -1) {
            return new Region(fDeleteOffset, 0);
        } else {
            int offset = Math.min(fDeleteOffset, fStartOffset);
            int endOffset = Math.max(fDeleteOffset, fEndOffset);
            return new Region(offset, endOffset - offset);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be extended by subclasses.
     * </p>
     */
    @Override
    public IRegion documentChanged2(final DocumentEvent e) {

        if (!fIsInitialized) {
            return null;
        }
        Assert.isTrue(e.getDocument() == fDocument);

        ICallback<Object, IDocument> iCallback = new ICallback<Object, IDocument>() {

            @Override
            public Object call(IDocument docArg) {
                Assert.isTrue(docArg == fDocument);
                try {
                    Position[] positionsInCategory = getPositions();
                    IRegion line = fDocument.getLineInformationOfOffset(e.getOffset());
                    int reparseStart = line.getOffset();
                    int partitionStart = -1;
                    String contentType = null;
                    int newLength = e.getText() == null ? 0 : e.getText().length();

                    int first = fDocument.computeIndexInCategory(fPositionCategory, reparseStart);
                    //fabioz: We have to get the place where the sub-partition starts
                    while (first > 0
                            && SwitchLanguageToken
                                    .isSubLanguagePartition((TypedPosition) positionsInCategory[first - 1])) {
                        first--;
                    }
                    //fabioz: end

                    //fabioz: we're in the first one (case where there are none too), so, start at 0.
                    //This is especially troublesome when we have some rule which when applied will change
                    //the previous line state (which is the markdown title use-case).
                    if (first < 0) {
                        // i.e.: just in case...
                        first = 0;
                    }
                    if (first == 0) {
                        reparseStart = 0;

                    }
                    //fabioz: end

                    else { // first > 0
                        TypedPosition partition = (TypedPosition) positionsInCategory[first - 1];
                        if (partition.includes(reparseStart)) {
                            partitionStart = partition.getOffset();
                            contentType = partition.getType();
                            if (e.getOffset() == partition.getOffset() + partition.getLength()) {
                                // if editing at end of partition, reparse from the partition start
                                reparseStart = partitionStart;
                            }
                            --first;
                        } else if (reparseStart == e.getOffset()
                                && reparseStart == partition.getOffset() + partition.getLength()) {
                            partitionStart = partition.getOffset();
                            contentType = partition.getType();
                            reparseStart = partitionStart;
                            --first;
                        } else {
                            partitionStart = partition.getOffset() + partition.getLength();
                            contentType = IDocument.DEFAULT_CONTENT_TYPE;
                        }
                    }

                    fPositionUpdater.update(e);
                    for (int i = first; i < positionsInCategory.length; i++) {
                        Position p = positionsInCategory[i];
                        if (p.isDeleted) {
                            rememberDeletedOffset(e.getOffset());
                            break;
                        }
                    }
                    clearPositionCache();

                    positionsInCategory = getPositions();

                    ScannerRange scannerRange = fScanner.createResumableScannerRange(fDocument, reparseStart,
                            fDocument.getLength() - reparseStart, contentType, partitionStart);

                    int behindLastScannedPosition = reparseStart;
                    scannerRange.nextToken(fScanner);
                    List<SwitchLanguageToken> switchLanguageTokens = new ArrayList<>();

                    while (!scannerRange.getToken().isEOF()) {

                        if (scannerRange.getToken() instanceof SwitchLanguageToken) {
                            //fabioz: we have to deal with a top-partition that has multiple sub-partitions inside.
                            SwitchLanguageToken switchLanguageToken = (SwitchLanguageToken) scannerRange.getToken();
                            switchLanguageTokens.add(switchLanguageToken);
                            SubLanguageToken[] subTokens = switchLanguageToken.subTokens;
                            int len = subTokens.length;
                            for (int i = 0; i < len; i++) {
                                SubLanguageToken subToken = subTokens[i];
                                contentType = subToken.getFullContentType();
                                if (isSupportedContentType(contentType)) {
                                    int start = subToken.offset;
                                    int length = subToken.len;

                                    behindLastScannedPosition = start + length;
                                    int lastScannedPosition = behindLastScannedPosition - 1;

                                    // remove all affected positions
                                    while (first < positionsInCategory.length) {
                                        TypedPosition p = (TypedPosition) positionsInCategory[first];
                                        if (lastScannedPosition >= p.offset + p.length ||
                                                (p.overlapsWith(start, length) &&
                                                        (!fDocument.containsPosition(fPositionCategory, start, length)
                                                                ||
                                                                !contentType.equals(p.getType())))) {

                                            rememberRegion(p.offset, p.length);
                                            clearPositionCache();
                                            fDocument.removePosition(fPositionCategory, p);
                                            ++first;

                                        } else {
                                            break;
                                        }
                                    }

                                    // if position already exists and we have scanned at least the
                                    // area covered by the event, we are done
                                    Position position = getExactPosition(start, length);
                                    if (position != null) {
                                        if (position instanceof TypedPositionWithSubTokens) {
                                            TypedPositionWithSubTokens typedPositionWithSubTokens = (TypedPositionWithSubTokens) position;
                                            typedPositionWithSubTokens.clearSubRuleToken();
                                        }
                                        if (lastScannedPosition >= e.getOffset() + newLength) {
                                            onParsingFoundSwitchLanguageTokensAndPartitioningChanged(e,
                                                    switchLanguageTokens);
                                            return createRegion();
                                        }
                                        ++first;
                                    } else {
                                        // insert the new type position
                                        try {
                                            fDocument.addPosition(fPositionCategory,
                                                    new TypedPositionWithSubTokens(start, length,
                                                            contentType, scannerRange.getSubRuleToken()));
                                            clearPositionCache();
                                            rememberRegion(start, length);
                                        } catch (BadPositionCategoryException x) {
                                        } catch (BadLocationException x) {
                                        }
                                    }
                                }
                            }
                            //fabioz: end

                        } else {
                            contentType = getTokenContentType(scannerRange.getToken());

                            if (isSupportedContentType(contentType) || contentType == null) {
                                int start = scannerRange.getTokenOffset();
                                int length = scannerRange.getTokenLength();

                                behindLastScannedPosition = start + length;
                                int lastScannedPosition = behindLastScannedPosition - 1;

                                // remove all affected positions (must deal with contentType == null)
                                while (first < positionsInCategory.length) {
                                    TypedPosition p = (TypedPosition) positionsInCategory[first];
                                    if (lastScannedPosition >= p.offset + p.length
                                            || (p.overlapsWith(start, length) && (contentType == null
                                                    || !fDocument.containsPosition(
                                                            fPositionCategory, start, length)
                                                    || !contentType.equals(p
                                                            .getType())))) {

                                        rememberRegion(p.offset, p.length);
                                        clearPositionCache();
                                        fDocument.removePosition(fPositionCategory, p);
                                        ++first;

                                    } else {
                                        break;
                                    }
                                }

                                //if contentType == null, just remove the positions, don't add it back.
                                if (contentType != null) {
                                    // if position already exists and we have scanned at least the
                                    // area covered by the event, we are done
                                    Position position = getExactPosition(start, length);
                                    if (position != null) {
                                        if (position instanceof TypedPositionWithSubTokens) {
                                            TypedPositionWithSubTokens typedPositionWithSubTokens = (TypedPositionWithSubTokens) position;
                                            typedPositionWithSubTokens.setSubRuleToken(scannerRange.getSubRuleToken());
                                        }
                                        if (lastScannedPosition >= e.getOffset() + newLength) {
                                            onParsingFoundSwitchLanguageTokensAndPartitioningChanged(e,
                                                    switchLanguageTokens);
                                            return createRegion();
                                        }
                                        ++first;
                                    } else {
                                        // insert the new type position
                                        try {
                                            fDocument.addPosition(fPositionCategory,
                                                    new TypedPositionWithSubTokens(start, length,
                                                            contentType, scannerRange.getSubRuleToken()));
                                            clearPositionCache();
                                            rememberRegion(start, length);
                                        } catch (BadPositionCategoryException x) {
                                        } catch (BadLocationException x) {
                                        }
                                    }
                                }
                            }

                        }
                        scannerRange.nextToken(fScanner);

                    }

                    // If we got here, partitioning didn't change (so, don't call onParsingFoundSwitchLanguageTokensAndPartitioningChanged).
                    int first2 = fDocument.computeIndexInCategory(fPositionCategory, behindLastScannedPosition);

                    clearPositionCache();
                    positionsInCategory = getPositions();
                    TypedPosition p;
                    while (first2 < positionsInCategory.length) {
                        p = (TypedPosition) positionsInCategory[first2++];
                        fDocument.removePosition(fPositionCategory, p);
                        rememberRegion(p.offset, p.length);
                    }

                } catch (BadPositionCategoryException x) {
                    // should never happen on connected documents
                    Log.log(x);
                } catch (BadLocationException x) {
                    Log.log(x);
                } catch (DocumentTimeStampChangedException e1) {
                    Log.log(e1); // Shouldn't happen as this should *not* be in a thread.
                } finally {
                    clearPositionCache();
                }
                return null;
            }

        };
        DocumentSync.runWithDocumentSynched(fDocument, iCallback, false);

        return createRegion();
    }

    /**
     * Made for subclasses to override.
     */
    protected void onParsingFoundSwitchLanguageTokensAndPartitioningChanged(DocumentEvent e,
            List<SwitchLanguageToken> switchLanguageTokens) {

    }

    private Position getExactPosition(int start, int length)
            throws BadLocationException, BadPositionCategoryException {
        Position position = null;

        if (fDocument.containsPosition(fPositionCategory, start, length)) {
            position = getExactPosition(getPositions(), start, length);
            if (position == null) {
                Log.log("Error: document contains position and our cache didn't have it... (clearing cache and retrying).");
                clearPositionCache();
                position = getExactPosition(getPositions(), start, length);
                Assert.isNotNull(position);
            }
        }
        return position;
    }

    private Position getExactPosition(Position[] positionsInCategory, int offset, int length)
            throws BadLocationException, BadPositionCategoryException {
        int size = positionsInCategory.length;
        if (size == 0) {
            return null;
        }

        int index = fDocument.computeIndexInCategory(fPositionCategory, offset);
        if (index < size) {
            Position p = positionsInCategory[index];
            while (p != null && p.offset == offset) {
                if (p.length == length) {
                    return p;
                }
                ++index;
                p = (index < size) ? (Position) positionsInCategory[index] : null;
            }
        }

        return null;
    }

    /**
     * Returns the position in the partitoner's position category which is
     * close to the given offset. This is, the position has either an offset which
     * is the same as the given offset or an offset which is smaller than the given
     * offset. This method profits from the knowledge that a partitioning is
     * a ordered set of disjoint position.
     * <p>
     * May be extended or replaced by subclasses.
     * </p>
     * @param offset the offset for which to search the closest position
     * @return the closest position in the partitioner's category
     */
    protected TypedPosition findClosestPosition(int offset) {

        try {

            int index = fDocument.computeIndexInCategory(fPositionCategory, offset);
            Position[] category = getPositions();

            if (category.length == 0) {
                return null;
            }

            if (index < category.length) {
                if (offset == category[index].offset) {
                    return (TypedPosition) category[index];
                }
            }

            if (index > 0) {
                index--;
            }

            return (TypedPosition) category[index];

        } catch (BadPositionCategoryException x) {
        } catch (BadLocationException x) {
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be replaced or extended by subclasses.
     * </p>
     */
    @Override
    public String getContentType(int offset) {
        checkInitialization();

        TypedPosition p = findClosestPosition(offset);
        if (p != null && p.includes(offset)) {
            return p.getType();
        }

        return IDocument.DEFAULT_CONTENT_TYPE;
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be replaced or extended by subclasses.
     * </p>
     */
    @Override
    public ITypedRegion getPartition(int offset) {
        checkInitialization();

        try {

            Position[] category = getPositions();

            if (category == null || category.length == 0) {
                return new TypedRegionWithSubTokens(0, fDocument.getLength(), IDocument.DEFAULT_CONTENT_TYPE, null);
            }

            int index = fDocument.computeIndexInCategory(fPositionCategory, offset);

            if (index < category.length) {

                TypedPosition next = (TypedPosition) category[index];

                if (offset == next.offset) {
                    return new TypedRegionWithSubTokens(next.getOffset(), next.getLength(), next.getType(),
                            getSubTokenRelativeTo(next.getOffset(), next));
                }

                if (index == 0) {
                    return new TypedRegionWithSubTokens(0, next.offset, IDocument.DEFAULT_CONTENT_TYPE, null);
                }

                TypedPosition previous = (TypedPosition) category[index - 1];
                if (previous.includes(offset)) {
                    return new TypedRegionWithSubTokens(previous.getOffset(), previous.getLength(), previous.getType(),
                            getSubTokenRelativeTo(previous.getOffset(), previous));
                }

                int endOffset = previous.getOffset() + previous.getLength();
                return new TypedRegionWithSubTokens(endOffset, next.getOffset() - endOffset,
                        IDocument.DEFAULT_CONTENT_TYPE, null);
            }

            TypedPosition previous = (TypedPosition) category[category.length - 1];
            if (previous.includes(offset)) {
                return new TypedRegionWithSubTokens(previous.getOffset(), previous.getLength(), previous.getType(),
                        getSubTokenRelativeTo(previous.getOffset(), previous));
            }

            int endOffset = previous.getOffset() + previous.getLength();
            return new TypedRegionWithSubTokens(endOffset, fDocument.getLength() - endOffset,
                    IDocument.DEFAULT_CONTENT_TYPE, null);

        } catch (BadPositionCategoryException x) {
        } catch (BadLocationException x) {
        }

        return new TypedRegionWithSubTokens(0, fDocument.getLength(), IDocument.DEFAULT_CONTENT_TYPE, null);
    }

    /*
     * @see IDocumentPartitioner#computePartitioning(int, int)
     */
    @Override
    public final ITypedRegion[] computePartitioning(int offset, int length) {
        return computePartitioning(offset, length, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be replaced or extended by subclasses.
     * </p>
     */
    @Override
    public String[] getLegalContentTypes() {
        return TextUtilities.copy(fLegalContentTypes);
    }

    /**
     * Returns whether the given type is one of the legal content types.
     * <p>
     * May be extended by subclasses.
     * </p>
     *
     * @param contentType the content type to check
     * @return <code>true</code> if the content type is a legal content type
     */
    protected boolean isSupportedContentType(String contentType) {
        if (contentType != null) {
            // for (int i = 0; i < fLegalContentTypes.length; i++) {
            //     System.out.println(fLegalContentTypes[i]);
            // }
            return fLegalContentTypesSet.contains(contentType);
        }

        return false;
    }

    /**
     * Returns a content type encoded in the given token. If the token's
     * data is not <code>null</code> and a string it is assumed that
     * it is the encoded content type.
     * <p>
     * May be replaced or extended by subclasses.
     * </p>
     *
     * @param token the token whose content type is to be determined
     * @return the token's content type
     */
    protected String getTokenContentType(IToken token) {
        Object data = token.getData();
        if (data instanceof String) {
            return (String) data;
        }
        return null;
    }

    /* zero-length partition support */

    /**
     * {@inheritDoc}
     * <p>
     * May be replaced or extended by subclasses.
     * </p>
     */
    @Override
    public String getContentType(int offset, boolean preferOpenPartitions) {
        return getPartition(offset, preferOpenPartitions).getType();
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be replaced or extended by subclasses.
     * </p>
     */
    @Override
    public ITypedRegion getPartition(int offset, boolean preferOpenPartitions) {
        ITypedRegion region = getPartition(offset);
        if (preferOpenPartitions) {
            if (region.getOffset() == offset && !region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                if (offset > 0) {
                    region = getPartition(offset - 1);
                    if (region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                        return region;
                    }
                }
                return new TypedRegionWithSubTokens(offset, 0, IDocument.DEFAULT_CONTENT_TYPE, null);
            }
        }
        return region;
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be replaced or extended by subclasses.
     * </p>
     */
    @Override
    public ITypedRegion[] computePartitioning(int offset, int length, boolean includeZeroLengthPartitions) {
        checkInitialization();
        List<ITypedRegion> list = new ArrayList<>();

        try {

            int endOffset = offset + length;

            Position[] category = getPositions();

            TypedPosition previous = null, current = null;
            int start, end, gapOffset;
            Position gap = new Position(0);

            int startIndex = getFirstIndexEndingAfterOffset(category, offset);
            int endIndex = getFirstIndexStartingAfterOffset(category, endOffset);
            for (int i = startIndex; i < endIndex; i++) {

                current = (TypedPosition) category[i];
                TypedPositionWithSubTokens typedPositionWithSubTokens = (TypedPositionWithSubTokens) (current instanceof TypedPositionWithSubTokens
                        ? current
                        : null);

                gapOffset = (previous != null) ? previous.getOffset() + previous.getLength() : 0;
                gap.setOffset(gapOffset);
                gap.setLength(current.getOffset() - gapOffset);
                if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length)) ||
                        (gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
                    start = Math.max(offset, gapOffset);
                    end = Math.min(endOffset, gap.getOffset() + gap.getLength());
                    // Always null sub tokens here (creating gap with default content type).
                    list.add(new TypedRegionWithSubTokens(start, end - start, IDocument.DEFAULT_CONTENT_TYPE, null));
                }

                if (current.overlapsWith(offset, length)) {
                    start = Math.max(offset, current.getOffset());
                    end = Math.min(endOffset, current.getOffset() + current.getLength());

                    list.add(new TypedRegionWithSubTokens(start, end - start, current.getType(),
                            getSubTokenRelativeTo(start, typedPositionWithSubTokens)));
                }

                previous = current;
            }

            if (previous != null) {
                gapOffset = previous.getOffset() + previous.getLength();
                gap.setOffset(gapOffset);
                gap.setLength(fDocument.getLength() - gapOffset);
                if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length)) ||
                        (gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
                    start = Math.max(offset, gapOffset);
                    end = Math.min(endOffset, fDocument.getLength());
                    // Always null sub tokens here (creating gap with default content type).
                    list.add(new TypedRegionWithSubTokens(start, end - start, IDocument.DEFAULT_CONTENT_TYPE, null));
                }
            }

            if (list.isEmpty()) {
                list.add(new TypedRegionWithSubTokens(offset, length, IDocument.DEFAULT_CONTENT_TYPE, null));
            }

        } catch (BadPositionCategoryException ex) {
            // Make sure we clear the cache
            clearPositionCache();
        } catch (RuntimeException ex) {
            // Make sure we clear the cache
            clearPositionCache();
            throw ex;
        }

        TypedRegionWithSubTokens[] result = new TypedRegionWithSubTokens[list.size()];
        list.toArray(result);
        return result;
    }

    private SubRuleToken getSubTokenRelativeTo(int offset, TypedPosition typedPositionWithSubTokens) {
        SubRuleToken subRuleToken = null;
        if (typedPositionWithSubTokens instanceof TypedPositionWithSubTokens) {
            subRuleToken = ((TypedPositionWithSubTokens) typedPositionWithSubTokens).getSubRuleToken();
            if (subRuleToken != null) {
                SubRuleToken copy = subRuleToken.createCopy();
                if (offset != typedPositionWithSubTokens.offset) {
                    copy.addOffset(typedPositionWithSubTokens.offset);
                    copy.makeRelativeToOffset(offset);
                }
                subRuleToken = copy;
            }
        }
        return subRuleToken;
    }

    /**
     * Returns <code>true</code> if the given ranges overlap with or touch each other.
     *
     * @param gap the first range
     * @param offset the offset of the second range
     * @param length the length of the second range
     * @return <code>true</code> if the given ranges overlap with or touch each other
     */
    private boolean overlapsOrTouches(Position gap, int offset, int length) {
        return gap.getOffset() <= offset + length && offset <= gap.getOffset() + gap.getLength();
    }

    /**
     * Returns the index of the first position which ends after the given offset.
     *
     * @param positions the positions in linear order
     * @param offset the offset
     * @return the index of the first position which ends after the offset
     */
    private int getFirstIndexEndingAfterOffset(Position[] positions, int offset) {
        int i = -1, j = positions.length;
        while (j - i > 1) {
            int k = (i + j) >> 1;
            Position p = positions[k];
            if (p.getOffset() + p.getLength() > offset) {
                j = k;
            } else {
                i = k;
            }
        }
        return j;
    }

    /**
     * Returns the index of the first position which starts at or after the given offset.
     *
     * @param positions the positions in linear order
     * @param offset the offset
     * @return the index of the first position which starts after the offset
     */
    private int getFirstIndexStartingAfterOffset(Position[] positions, int offset) {
        int i = -1, j = positions.length;
        while (j - i > 1) {
            int k = (i + j) >> 1;
            Position p = positions[k];
            if (p.getOffset() >= offset) {
                j = k;
            } else {
                i = k;
            }
        }
        return j;
    }

    /*
     * @see org.eclipse.jface.text.IDocumentPartitionerExtension3#startRewriteSession(org.eclipse.jface.text.DocumentRewriteSession)
     */
    @Override
    public void startRewriteSession(DocumentRewriteSession session) throws IllegalStateException {
        if (fActiveRewriteSession != null) {
            throw new IllegalStateException();
        }
        fActiveRewriteSession = session;
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be extended by subclasses.
     * </p>
     */
    @Override
    public void stopRewriteSession(DocumentRewriteSession session) {
        if (fActiveRewriteSession == session) {
            flushRewriteSession();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * May be extended by subclasses.
     * </p>
     */
    @Override
    public DocumentRewriteSession getActiveRewriteSession() {
        return fActiveRewriteSession;
    }

    /**
     * Flushes the active rewrite session.
     */
    protected final void flushRewriteSession() {
        fActiveRewriteSession = null;

        // remove all position belonging to the partitioner position category
        try {
            fDocument.removePositionCategory(fPositionCategory);
        } catch (BadPositionCategoryException x) {
        }
        fDocument.addPositionCategory(fPositionCategory);

        fIsInitialized = false;
    }

    /**
     * Clears the position cache. Needs to be called whenever the positions have
     * been updated.
     */
    protected final void clearPositionCache() {
        if (fCachedPositions != null) {
            fCachedPositions = null;
        }
    }

    /**
     * Returns the partitioners positions.
     *
     * @return the partitioners positions
     * @throws BadPositionCategoryException if getting the positions from the
     *         document fails
     */
    protected final Position[] getPositions() throws BadPositionCategoryException {
        if (fCachedPositions == null) {
            fCachedPositions = fDocument.getPositions(fPositionCategory);
        } else if (CHECK_CACHE_CONSISTENCY) {
            Position[] positions = fDocument.getPositions(fPositionCategory);
            int len = Math.min(positions.length, fCachedPositions.length);
            for (int i = 0; i < len; i++) {
                if (!positions[i].equals(fCachedPositions[i])) {
                    System.err
                            .println(
                                    "FastPartitioner.getPositions(): cached position is not up to date: from document: " //$NON-NLS-1$
                                            + toString(positions[i]) + " in cache: " + toString(fCachedPositions[i])); //$NON-NLS-1$
                }
            }
            for (int i = len; i < positions.length; i++) {
                System.err
                        .println("FastPartitioner.getPositions(): new position in document: " + toString(positions[i])); //$NON-NLS-1$
            }
            for (int i = len; i < fCachedPositions.length; i++) {
                System.err
                        .println("FastPartitioner.getPositions(): stale position in cache: " //$NON-NLS-1$
                                + toString(fCachedPositions[i]));
            }
        }
        return fCachedPositions;
    }

    /**
     * Pretty print a <code>Position</code>.
     *
     * @param position the position to format
     * @return a formatted string
     */
    private String toString(Position position) {
        return "P[" + position.getOffset() + "+" + position.getLength() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
