/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.partitioning;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;

public interface ICustomPartitionTokenScanner {

    String DEFAULT_CONTENT_TYPE = IDocument.DEFAULT_CONTENT_TYPE;

    void nextToken(ScannerRange scannerRange);

    /**
     * Akin to IPartitionTokenScanner.setPartialRange
     *
     * Configures the scanner by providing access to the document range that should be scanned. The
     * range may not only contain complete partitions but starts at the beginning of a line in the
     * middle of a partition of the given content type. This requires that a partition delimiter can
     * not contain a line delimiter.
     *
     * @param document the document to scan
     * @param offset the offset of the document range to scan
     * @param length the length of the document range to scan
     * @param contentType the content type at the given offset
     * @param partitionOffset the offset at which the partition of the given offset starts
     */
    ScannerRange createPartialScannerRange(IDocument document, int offset, int length, String contentType,
            int partitionOffset);

    /**
     * Akin to ITokenScanner.setRange
     *
     * Configures the scanner by providing access to the document range that should
     * be scanned.
     *
     * @param document the document to scan
     * @param offset the offset of the document range to scan
     * @param length the length of the document range to scan
     */
    ScannerRange createScannerRange(IDocument document, int offset, int length);

	void setDefaultReturnToken(IToken defaultReturnToken);

	/**
	 * Scanner should clear the cache (starting at the passed offset).
	 */
	void clearCache(IDocument document, int startAtOffset);

}
