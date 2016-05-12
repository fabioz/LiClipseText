/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseTextAttribute;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubTokensTokensProvider;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionCodeReader;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionMerger;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.LowMemoryArrayList;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.IToken;

/**
 * Note: not thread-safe (synchronize outside).
 */
public class ScopeNavigationMatch implements INavigationMatch {

    /**
     * Can be something as 'default.class' or 'default.method' or just 'class', 'tag'.
     */
    private final String[] scopeParts;

    private final String icon;

    private final Map<String, ICustomPartitionTokenScanner> contentTypeToScannerCache = new HashMap<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ScopeNavigationMatch(Map m) {
        List<String> split = (List<String>) m.remove("scope");
        scopeParts = split.toArray(new String[split.size()]);
        this.icon = (String) m.remove("define");
    }

    public String getIcon() {
        return this.icon;
    }

    public IRegion find(final boolean forward, IDocument document, final int initialOffset) {
        Tuple<ICustomPartitionTokenScanner, List<TypedPosition>> scannerAndAcceptedPositions;
        try {
            scannerAndAcceptedPositions = calculateScannerAndAcceptedPositions(
                    forward, document, initialOffset);
            List<IRegion> ret = find(scannerAndAcceptedPositions, forward, document, initialOffset, true);
            if (ret.size() > 0) {
                return ret.get(0);
            }
        } catch (Exception e) {
            Log.log(e);
        }
        return null;
    }

    public List<IRegion> findAll(IDocument document) {
        Tuple<ICustomPartitionTokenScanner, List<TypedPosition>> scannerAndAcceptedPositions;
        try {
            boolean forward = true;
            int initialOffset = -1; //Has to be -1 (if we started at 0, we wouldn't get a match that is in position 0).
            scannerAndAcceptedPositions = calculateScannerAndAcceptedPositions(
                    forward, document, initialOffset);
            return find(scannerAndAcceptedPositions, forward, document, initialOffset, false);
        } catch (Exception e) {
            Log.log(e);
        }
        return new LowMemoryArrayList<IRegion>();
    }

    public List<IRegion> find(Tuple<ICustomPartitionTokenScanner, List<TypedPosition>> scannerAndAcceptedPositions,
            boolean forward,
            IDocument document, int initialOffset, boolean stopOnFirst) {
        List<IRegion> regions = new ArrayList<IRegion>();
        try {
            List<TypedPosition> acceptedPositions = scannerAndAcceptedPositions.o2;
            ICustomPartitionTokenScanner tokenScanner = scannerAndAcceptedPositions.o1;
            //Note: the iteration is already backwards if we're going backwards instead of forwards.
            if (tokenScanner == null) {
                for (TypedPosition typedPosition : acceptedPositions) {
                    IRegion[] region = checkMatch(document, typedPosition.getOffset(), typedPosition.getLength(),
                            initialOffset,
                            forward);
                    if (region != null) {
                        addRegions(regions, region);
                        if (stopOnFirst) {
                            return regions;
                        }
                    }
                }
            } else {
                //we also need to find the sub-scope.
                for (TypedPosition typedPosition : acceptedPositions) {
                    SubTokensTokensProvider subTokensTokensProvider = new SubTokensTokensProvider(document,
                            typedPosition, tokenScanner);

                    if (forward) {
                        //A bit more optimized since we can only look forwards and don't need a buffer.
                        while (true) {
                            IToken token = subTokensTokensProvider.nextToken();
                            if (token.isEOF()) {
                                break;
                            }
                            String contentTypeFromToken = LiClipseTextAttribute
                                    .getContentTypeFromToken(token);
                            if (contentTypeFromToken != null && scopeParts[1].equals(contentTypeFromToken)) {
                                IRegion[] region = checkMatch(document, subTokensTokensProvider.getTokenOffset(),
                                        subTokensTokensProvider.getTokenLength(), initialOffset, forward);
                                if (region != null) {
                                    addRegions(regions, region);
                                    if (stopOnFirst) {
                                        return regions;
                                    }
                                }
                            }

                        }

                    } else {
                        //checking backwards... we have to consume all tokens, reverse it and only then
                        //check each sub-part.
                        ArrayList<IRegion> possibleRegions = new ArrayList<IRegion>();
                        while (true) {
                            IToken token = subTokensTokensProvider.nextToken();
                            if (token.isEOF()) {
                                break;
                            }
                            String contentTypeFromToken = LiClipseTextAttribute
                                    .getContentTypeFromToken(token);
                            if (contentTypeFromToken != null && scopeParts[1].equals(contentTypeFromToken)) {
                                possibleRegions.add(new Region(subTokensTokensProvider.getTokenOffset(),
                                        subTokensTokensProvider.getTokenLength()));
                            }
                        }

                        Collections.reverse(possibleRegions);
                        for (IRegion iRegion : possibleRegions) {
                            IRegion region[] = checkMatch(document, iRegion.getOffset(), iRegion.getLength(),
                                    initialOffset,
                                    forward);
                            if (region != null) {
                                addRegions(regions, region);
                                if (stopOnFirst) {
                                    return regions;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.log(e);
            return regions;
        }

        return regions;
    }

    private void addRegions(List<IRegion> regions, IRegion[] region) {
        regions.addAll(Arrays.asList(region));
    }

    public Tuple<ICustomPartitionTokenScanner, List<TypedPosition>> calculateScannerAndAcceptedPositions(
            final boolean forward,
            IDocument document, final int initialOffset) throws BadPositionCategoryException {
        Tuple<ICustomPartitionTokenScanner, List<TypedPosition>> scannerAndAcceptedPositions = new Tuple<ICustomPartitionTokenScanner, List<TypedPosition>>(
                null, null);
        LiClipseDocumentPartitioner liClipseDocumentPartitioner;
        try {
            IDocumentPartitioner documentPartitioner = document.getDocumentPartitioner();
            if (documentPartitioner instanceof LiClipseDocumentPartitioner) {
                liClipseDocumentPartitioner = (LiClipseDocumentPartitioner) documentPartitioner;
            } else {
                throw new RuntimeException("Expected LiClipseDocumentPartitioner.");
            }
        } catch (Exception e) {
            Log.log(e);
            return null;
        }

        if (scopeParts.length == 0 || scopeParts[0].length() == 0) {
            throw new RuntimeException("No scope provided!");
        }
        String contentType = scopeParts[0];
        if (scopeParts.length == 1) {
            //Ok, no need for the scanner, only basic partitioning
        } else if (scopeParts.length == 2) {
            //We also need the scanner!
            scannerAndAcceptedPositions.o1 = liClipseDocumentPartitioner.obtainTokenScannerForContentType(contentType,
                    contentTypeToScannerCache, liClipseDocumentPartitioner.language);
            if (scannerAndAcceptedPositions.o1 == null) {
                throw new RuntimeException("No token scanner configured for content type: " + contentType);
            }
        } else {
            throw new RuntimeException("Only supporting scope with at most 2 levels. Received: "
                    + StringUtils.join(".", scopeParts));
        }

        List<TypedPosition> positions = calculatePositions(document, liClipseDocumentPartitioner);
        scannerAndAcceptedPositions.o2 = new ArrayList<TypedPosition>(positions.size());

        if (forward) {
            for (TypedPosition typedPosition : positions) {
                if (typedPosition.getOffset() + typedPosition.getLength() > initialOffset
                        && contentType.equals(typedPosition.getType())) {
                    scannerAndAcceptedPositions.o2.add(typedPosition);
                }
            }
        } else {
            //backward
            for (TypedPosition typedPosition : positions) {
                if (typedPosition.getOffset() < initialOffset && contentType.equals(typedPosition.getType())) {
                    scannerAndAcceptedPositions.o2.add(typedPosition);
                }
            }
            Collections.reverse(scannerAndAcceptedPositions.o2);
        }
        return scannerAndAcceptedPositions;
    }

    /**
     * Subclasses may reimplement to do other checks in the region and even return sub-areas.
     *
     * Returning null means that we did not have a match.
     * @param document
     * @param forward
     * @param initialOffset
     */
    protected IRegion[] checkMatch(IDocument document, int offset, int length, int initialOffset, boolean forward) {
        if (forward) {
            if (offset <= initialOffset) {
                return null;
            }
        } else {
            if (offset >= initialOffset) {
                return null;
            }
        }
        return new Region[] { new Region(offset, length) };
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

}
