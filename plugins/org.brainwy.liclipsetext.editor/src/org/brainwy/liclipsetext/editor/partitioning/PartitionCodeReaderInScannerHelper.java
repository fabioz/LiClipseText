/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.partitioning;

import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionCodeReader;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;

public class PartitionCodeReaderInScannerHelper implements IPartitionCodeReaderInScannerHelper {

    private PartitionCodeReader codeReader;

    private long oldTime = -1;

    private IDocument fDocument;

    private Map<Integer, Tuple<Utf8WithCharLen, Integer>> lineToBytes = new HashMap<>();
    private Map<Integer, LineInfo> lineToStr = new HashMap<>();

    private int fDocLen;

    private int fNumberOfLines;
    private final Map<Integer, Tuple<Utf8WithCharLen, Integer>> offsetToContentsAndColumn = new HashMap<>();

    @Override
    public void setDocument(IDocument document) {
        offsetToContentsAndColumn.clear();
        IDocumentExtension4 docExt = (IDocumentExtension4) document;
        long newTime = docExt.getModificationStamp();

        if (document != fDocument || newTime != oldTime) {
            codeReader = null;
            lineToBytes.clear();
            lineToStr.clear();
        }
        oldTime = newTime;
        fDocument = document;
        fDocLen = document.getLength();
        fNumberOfLines = document.getNumberOfLines();
    }

    @Override
    public PartitionCodeReader getOffPartitionCodeReader(int currOffset) {
        IDocument doc = this.fDocument;
        try {
            if (codeReader == null) {
                String contentType = IDocument.DEFAULT_CONTENT_TYPE;
                codeReader = new PartitionCodeReader(contentType);
                codeReader.configureForwardReader(doc, currOffset, doc.getLength(), true);
            } else {
                codeReader.configureForwardReaderKeepingPositions(currOffset, doc.getLength());
            }
        } catch (Exception e) {
            Log.log(e);
            return null;
        }
        return codeReader;
    }

    @Override
    public LineInfo getLineAsString(final int currLine) {
    	if (currLine >= fNumberOfLines) {
    		return null;
    	}
    	LineInfo s = lineToStr.get(currLine);
    	if (s != null) {
    		return s;
    	}
    	try {
    		IRegion lineInformation = fDocument.getLineInformation(currLine);

            int lineOffset = lineInformation.getOffset();
            int lineLen = lineInformation.getLength();

            s = new LineInfo(new FastStringBuffer(fDocument.get(lineOffset, lineLen), 1).append('\n').toString(), lineOffset);
            lineToStr.put(currLine, s);
            return s;
    	} catch (BadLocationException e) {
    		Log.log(e);
    		return null;
    	}
    }

    /**
     * Returns the Utf8WithCharLen and the number of columns for the current offset in the current line.
     * May return null.
     */
    @Override
    public Tuple<Utf8WithCharLen, Integer> getLineFromOffsetAsBytes(final int offset) {
        if (offset >= fDocLen) {
            return null;
        }
        Tuple<Utf8WithCharLen, Integer> tuple = offsetToContentsAndColumn.get(offset);
        if (tuple != null) {
            return tuple;
        }
        try {
            int currLine = this.fDocument.getLineOfOffset(offset);
            IRegion lineInformation = fDocument.getLineInformation(currLine);

            Tuple<Utf8WithCharLen, Integer> bytes = getLineFromLineAsBytes(currLine);
            if (bytes == null) {
                return null;
            }

            int startLineOffset = lineInformation.getOffset();
            int charsLen = bytes.o1.getCharsLen();
            if (bytes.o1.getLastCharEquals2LenLineDelimiter()) {
                // We have \r\n, but the charsLen only has the \n.
                charsLen += 1;
            }

            for (int col = 0; col < charsLen; col++) {
                offsetToContentsAndColumn.put(startLineOffset + col,
                        new Tuple<>(bytes.o1, bytes.o1.getBytesPosFromCharPos(col)));
            }

            //Now, we have to get the column in chars and convert it to the column in the bytes...
            return offsetToContentsAndColumn.get(offset);
        } catch (BadLocationException e) {
            Log.log(e);
            return null;
        }
    }

    /**
     * Returns the Utf8WithCharLen and the line offset.
     * May return null.
     */
    @Override
    public Tuple<Utf8WithCharLen, Integer> getLineFromLineAsBytes(int currLine) {

        Tuple<Utf8WithCharLen, Integer> bs = this.lineToBytes.get(currLine);
        if (bs != null) {
            return bs;
        }

        if (currLine >= fNumberOfLines) {
            return null;
        }

        String line;
        try {
            IRegion lineInformation = fDocument.getLineInformation(currLine);
            int lineOffset = lineInformation.getOffset();
            int lineLen = lineInformation.getLength();

            String lineDelimiter = fDocument.getLineDelimiter(currLine);

            int lineDelimiterLen = 0;
            if (lineDelimiter != null) {
                lineDelimiterLen = lineDelimiter.length();
                line = new FastStringBuffer(fDocument.get(lineOffset, lineLen), 1).append('\n').toString();
            } else {
                line = fDocument.get(lineOffset, lineLen);
            }

            Utf8WithCharLen bytes = new Utf8WithCharLen(line);
            if (lineDelimiterLen == 2) {
                bytes.setLastCharEquals2LenLineDelimiter(true);
            }

            Tuple<Utf8WithCharLen, Integer> ret = new Tuple<Utf8WithCharLen, Integer>(bytes,
                    lineOffset);
            this.lineToBytes.put(currLine, ret);
            return ret;
        } catch (BadLocationException e) {
            return null;
        }

    }

    @Override
    public int getNumberOfLines() {
        return fDocument.getNumberOfLines();
    }

    @Override
    public int getLineFromOffset(int offset) throws BadLocationException {
        return fDocument.getLineOfOffset(offset);
    }

    private boolean inBeginWhile;

    @Override
    public void setInInBeginWhile(boolean b) {
        inBeginWhile = b;
    }

    @Override
    public boolean isInBeginWhile() {
        return inBeginWhile;
    }

}
