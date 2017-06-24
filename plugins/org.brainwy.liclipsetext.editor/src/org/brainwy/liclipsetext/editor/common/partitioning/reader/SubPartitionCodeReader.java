/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseTextAttribute;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionCodeReader;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionMerger;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.IToken;

public class SubPartitionCodeReader {

    /**
     * Cache for the sub-scanners we use.
     */
    private final Map<String, ICustomPartitionTokenScanner> contentTypeToScannerCache = new HashMap<>();

    /**
     * These are the top-level positions we must iterate on.
     */
    private ArrayList<TypedPosition> acceptedPositions;

    /**
     * Whether we're iterating forward or backward.
     */
    private boolean forward;

    /**
     * The current position we should return in acceptedPositions.
     */
    private int currentPos;

    /**
     * The top partitions we should return fully.
     */
    private Set<String> topPartitions;

    /**
     * The top-level for which we have sub-partitions to iterate on.
     */
    private Map<String, List<SubPartitionInfo>> subTopPartitions;

    /**
     * Initial offset for the reading (this works toghether with the 'forward' flag).
     */
    private int initialOffset;

    private IDocument document;

    /**
     * These are the sub-matches in a top-level partition.
     */
    private List<TypedPart> subMatchesInTypedPosition;

    /**
     * The current sub-position we're in subMatchesInTypedPosition.
     */
    private int currentSubPos;

    private class SubPartitionInfo {

        private final ICustomPartitionTokenScanner tokenScannerForContentType;
        private final String subLevel;
        private final String fullLevel;

        public SubPartitionInfo(String topLevel, String subLevel,
                LiClipseDocumentPartitioner liClipseDocumentPartitioner) {
            tokenScannerForContentType = liClipseDocumentPartitioner.obtainTokenScannerForContentType(topLevel,
                    contentTypeToScannerCache, liClipseDocumentPartitioner.language);
            this.subLevel = subLevel;
            this.fullLevel = topLevel + "." + subLevel;
        }

    }

    private static final String[] ALL_TOP_PARTITIONS = new String[0];
    private boolean readAllTop = false;

    private IAcceptPartition filter;

    private LiClipseLanguage language;

    private LiClipseDocumentPartitioner liClipseDocumentPartitioner;

    public static interface IAcceptPartition {

        /**
         * Will be called to filter top-level positions initially.
         */
        boolean accept(TypedPosition typedPosition);

        /**
         * Called afterwards for top-level and sub-level typed parts.
         */
        boolean accept(TypedPart typedPart);

        boolean getRequireOnlyTop();

    }

    public void configureReadAllTopPartition(boolean forward, IDocument document, final int initialOffset) {
        configurePartitions(forward, document, initialOffset, ALL_TOP_PARTITIONS);
    }

    public void configurePartitions(boolean forward, IDocument document, final int initialOffset,
            String... partitionsToRead) {
        configurePartitions(forward, document, initialOffset, (IAcceptPartition) null, partitionsToRead);
    }

    public void configurePartitions(boolean forward, IDocument document, final int initialOffset,
            IAcceptPartition filter) {
        configurePartitions(forward, document, initialOffset, filter, (String[]) null);
    }

    private void configurePartitions(boolean forward, IDocument document, final int initialOffset,
            IAcceptPartition filter, String... partitionsToRead) {
        LiClipseDocumentPartitioner liClipseDocumentPartitioner;
        try {
            readAllTop = partitionsToRead == ALL_TOP_PARTITIONS;
            this.filter = filter;
            this.forward = forward;
            this.currentPos = 0;
            this.initialOffset = initialOffset;
            this.document = document;
            IDocumentPartitioner documentPartitioner = document.getDocumentPartitioner();
            if (documentPartitioner instanceof LiClipseDocumentPartitioner) {
                liClipseDocumentPartitioner = (LiClipseDocumentPartitioner) documentPartitioner;
            } else {
                throw new RuntimeException("Expected LiClipseDocumentPartitioner.");
            }

            this.liClipseDocumentPartitioner = liClipseDocumentPartitioner;
            this.language = liClipseDocumentPartitioner.language;

            //this are the top-level positions. If all we have are top-level partitions to read, our
            //work is just sorting out the positions here, otherwise, we also need to get into
            //sub-partitions.
            List<TypedPosition> positions = calculatePositions(document, liClipseDocumentPartitioner);
            this.acceptedPositions = new ArrayList<TypedPosition>(positions.size());

            this.topPartitions = new HashSet<String>();
            this.subTopPartitions = new HashMap<String, List<SubPartitionInfo>>();
            if (!readAllTop && filter == null) {
                int len = partitionsToRead.length;
                for (int i = 0; i < len; i++) {
                    String p = partitionsToRead[i];
                    int indexOf = p.indexOf('.');
                    if (indexOf == -1) {
                        if (p.equals("default")) {
                            p = IDocument.DEFAULT_CONTENT_TYPE;
                        }
                        topPartitions.add(p);
                    } else {
                        String top = p.substring(0, indexOf);
                        if (top.equals("default")) {
                            top = IDocument.DEFAULT_CONTENT_TYPE;
                        }
                        List<SubPartitionInfo> list = subTopPartitions.get(top);
                        if (list == null) {
                            list = new ArrayList<SubPartitionCodeReader.SubPartitionInfo>();
                            subTopPartitions.put(top, list);
                        }
                        list.add(new SubPartitionInfo(top, p.substring(indexOf + 1, p.length()),
                                liClipseDocumentPartitioner));
                    }
                }
            }

            if (filter != null) {
                for (TypedPosition typedPosition : positions) {
                    if (filter.accept(typedPosition)) {
                        acceptedPositions.add(typedPosition);
                    }
                }
                if (!forward) {

                    Collections.reverse(acceptedPositions);
                }
            } else {
                if (forward) {
                    for (TypedPosition typedPosition : positions) {
                        if (typedPosition.getOffset() + typedPosition.getLength() > initialOffset
                                && (readAllTop || topPartitions.contains(typedPosition.getType()) || subTopPartitions
                                        .containsKey(typedPosition.getType()))) {
                            acceptedPositions.add(typedPosition);
                        }
                    }
                } else {
                    //backward
                    for (TypedPosition typedPosition : positions) {
                        if (typedPosition.getOffset() < initialOffset
                                && (readAllTop || topPartitions.contains(typedPosition.getType()) || subTopPartitions
                                        .containsKey(typedPosition.getType()))) {
                            acceptedPositions.add(typedPosition);
                        }
                    }
                    Collections.reverse(acceptedPositions);
                }
            }
        } catch (Exception e) {
            Log.log(e);
            return;
        }

    }

    /**
     * Return a list with the calculated positions (properly filled with default values to cover for the whole
     * document).
     *
     * Use Arrays.binarySearch later on to find a position.
     * @return
     */
    private List<TypedPosition> calculatePositions(IDocument document,
            LiClipseDocumentPartitioner liClipseDocumentPartitioner)
            throws BadPositionCategoryException {
        //Ok, we have all the types and position (and whatever is not there is 'default').
        Position[] positions = PartitionCodeReader.getDocumentTypedPositions(document, IDocument.DEFAULT_CONTENT_TYPE);

        return PartitionMerger.sortAndMergePositions(positions, document.getLength());
    }

    public static final class TypedPart {

        public final String type;
        public final String subType;
        public final int offset;
        public final int length;

        public TypedPart(String type, String subType, int offset, int length) {
            this.type = type;
            this.offset = offset;
            this.length = length;
            this.subType = subType;
        }

        @Override
        public String toString() {
            return new FastStringBuffer()
                    .append("type:")
                    .append(this.type)
                    .append(" offset:")
                    .append(this.offset)
                    .append(" len:")
                    .append(this.length)
                    .toString();
        }
    }

    public TypedPart read() throws DocumentTimeStampChangedException {
        boolean filterRequireOnlyTop = false;
        if (filter != null) {
            filterRequireOnlyTop = filter.getRequireOnlyTop();
        }
        while (true) {
            if (subMatchesInTypedPosition != null && currentSubPos < subMatchesInTypedPosition.size()) {
                currentSubPos++;
                return subMatchesInTypedPosition.get(currentSubPos - 1);
            }

            if (currentPos < acceptedPositions.size()) {
                TypedPosition typedPosition = acceptedPositions.get(currentPos);
                currentPos++; //Walk for the next time
                String type = typedPosition.getType();
                if (readAllTop || topPartitions.contains(type) || filterRequireOnlyTop) {
                    //All good: matched a full partition (so, even backwards/forwards is Ok at this point).
                    int offset = typedPosition.getOffset();
                    int length = typedPosition.getLength();
                    if (checkMatch(offset, length)) {
                        return new TypedPart(type, "", offset, length);
                    }
                } else {

                    //Ok, we need to find a sub-scope.
                    List<TypedPart> matchesInTypedPosition = new ArrayList<SubPartitionCodeReader.TypedPart>();
                    if (filter != null) {
                        ICustomPartitionTokenScanner tokenScanner = liClipseDocumentPartitioner
                                .obtainTokenScannerForContentType(
                                        type, contentTypeToScannerCache, language);

                        matchesInTypedPosition.addAll(findMatchesInTypedPosition(tokenScanner, typedPosition, null));

                    } else {

                        List<SubPartitionInfo> list = subTopPartitions.get(type);
                        int size = list.size();
                        for (int i = 0; i < size; i++) {
                            SubPartitionInfo subPartitionInfo = list.get(i);
                            ICustomPartitionTokenScanner tokenScanner = subPartitionInfo.tokenScannerForContentType;
                            matchesInTypedPosition.addAll(findMatchesInTypedPosition(tokenScanner, typedPosition,
                                    subPartitionInfo));
                        }
                    }
                    Collections.sort(matchesInTypedPosition, forward ? forwardComparator : backwardComparator);

                    currentSubPos = 0;
                    this.subMatchesInTypedPosition = matchesInTypedPosition;
                }
            } else {
                return null;
            }
        }
    }

    private static final Comparator<TypedPart> forwardComparator = new Comparator<TypedPart>() {

        public int compare(TypedPart o1, TypedPart o2) {
            return o1.offset - o2.offset;
        }
    };
    private static final Comparator<TypedPart> backwardComparator = new Comparator<TypedPart>() {

        public int compare(TypedPart o1, TypedPart o2) {
            return o2.offset - o1.offset;
        }
    };

    private List<TypedPart> findMatchesInTypedPosition(ICustomPartitionTokenScanner tokenScanner,
            TypedPosition typedPosition,
            SubPartitionInfo subPartitionInfo) throws DocumentTimeStampChangedException {
        SubTokensTokensProvider subTokensProvider = new SubTokensTokensProvider(document,
                typedPosition, tokenScanner);
        List<TypedPart> ret = new ArrayList<TypedPart>();

        // if (forward) {
        //A bit more optimized since we can only look forwards and don't need a buffer.
        while (true) {
            IToken token;
            token = subTokensProvider.nextToken();
            if (token.isEOF()) {
                break;
            }
            String contentType = LiClipseTextAttribute.getContentTypeFromToken(token);
            if (filter != null && contentType != null) {
                int offset = subTokensProvider.getTokenOffset();
                int length = subTokensProvider.getTokenLength();
                if (checkMatch(offset, length)) {
                    TypedPart typedPart = new TypedPart(typedPosition.getType() + "." + contentType, contentType,
                            offset, length);
                    if (filter.accept(typedPart)) {
                        ret.add(typedPart);
                    }
                }
            } else if (contentType != null && subPartitionInfo.subLevel.equals(contentType)) {
                int offset = subTokensProvider.getTokenOffset();
                int length = subTokensProvider.getTokenLength();
                if (checkMatch(offset, length)) {
                    ret.add(new TypedPart(subPartitionInfo.fullLevel, contentType, offset, length));
                }
            }

        }

        // Note: not doing the distinction backward/forward here because we don't have an option
        // to stop on the first match (so, not optimizing for that)
        // } else {
        //     //checking backwards... we have to consume all tokens, reverse it and only then
        //     //check each sub-part.
        //     IToken token;
        //     ArrayList<IRegion> possibleRegions = new ArrayList<IRegion>();
        //     do {
        //         token = tokenScanner.nextToken();
        //         LiClipseTextAttribute data = (LiClipseTextAttribute) token.getData();
        //         if (data != null && subPartitionInfo.subLevel.equals(data.contentType)) {
        //             possibleRegions.add(new Region(tokenScanner.getTokenOffset(),
        //                     tokenScanner.getTokenLength()));
        //         }
        //
        //     } while (!token.isEOF());
        //
        //     Collections.reverse(possibleRegions);
        //     for (IRegion iRegion : possibleRegions) {
        //         int offset = iRegion.getOffset();
        //         int length = iRegion.getLength();
        //         if (checkMatch(offset, length)) {
        //             ret.add(new TypedPart(subPartitionInfo.fullLevel, offset, length));
        //         }
        //     }
        // }
        return ret;
    }

    /**
     * Check if it's within a valid range.
     */
    protected boolean checkMatch(int offset, int length) {
        if (forward) {
            if (offset + length < initialOffset) {
                return false;
            }
        } else {
            if (offset > initialOffset) {
                return false;
            }
        }
        return true;
    }

}
