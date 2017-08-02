/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.partitioning;

import org.brainwy.liclipsetext.shared_core.partitioner.PartitionCodeReader;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public interface IPartitionCodeReaderInScannerHelper {

    void setDocument(IDocument document);

    PartitionCodeReader getOffPartitionCodeReader(int currOffset);

    /**
     * Returns the Utf8WithCharLen and the number of columns for the current offset in the current line.
     * May return null.
     */
    Tuple<Utf8WithCharLen, Integer> getLineFromOffsetAsBytes(int offset);

    /**
     * Returns the Utf8WithCharLen and the line offset.
     * May return null.
     */
    Tuple<Utf8WithCharLen, Integer> getLineFromLineAsBytes(int currLine);

    int getNumberOfLines();

    int getLineFromOffset(int offset) throws BadLocationException;

    void setInInBeginWhile(boolean b);

    boolean isInBeginWhile();

    LineInfo getLineAsString(int currLine);

	public static class LineInfo{

		public final String str;
		public final int lineOffset;

		public LineInfo(String string, int lineOffset) {
			this.str = string;
			this.lineOffset = lineOffset;
		}

	}

}