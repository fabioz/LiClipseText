/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.partitioning;

import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.rules.IToken;

public interface ICustomPartitionTokenScanner {

    String DEFAULT_CONTENT_TYPE = IDocument.DEFAULT_CONTENT_TYPE;

    void nextToken(ScannerRange scannerRange) throws DocumentTimeStampChangedException;

    IToken getDefaultReturnToken();

    /**
     * Akin to IPartitionTokenScanner.setPartialRange
     *
     * This method should be used to create a partitioner (which can resume at a given point).
     *
     * @param document the document to scan
     * @param offset the offset of the document range to scan
     * @param length the length of the document range to scan
     * @param contentType the content type at the given offset
     * @param partitionOffset the offset at which the partition of the given offset starts
     */
    default public ScannerRange createResumableScannerRange(IDocument document, int offset, int length,
            String contentType,
            int partitionOffset) {
        return new ScannerRange(document, offset, length, contentType, partitionOffset,
                new PartitionCodeReaderInScannerHelper(), this);
    }

    /**
     * This method should be used to create a partitioner for the whole document (without resuming).
     */
    default public ScannerRange createScannerRange(IDocument document) {
        return createScannerRange(document, 0, document.getLength());
    }

    default public ScannerRange createScannerRange(IDocument document, int offset, int length) {
        return createScannerRange(document, offset, length, ((IDocumentExtension4) document).getModificationStamp());
    }

    /**
     * This method should be used to create a scanner (which will provide coloring for a given partition).
     */
    default public ScannerRange createScannerRange(IDocument document, int offset, int length, long docTime) {
        return new ScannerRange(document, offset, length, new PartitionCodeReaderInScannerHelper(), this, docTime);
    }

    void setDefaultReturnToken(IToken defaultReturnToken);

}
