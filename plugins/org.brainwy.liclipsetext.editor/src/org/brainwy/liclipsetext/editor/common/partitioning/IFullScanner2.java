/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.shared_core.partitioner.IFullScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.BadLocationException;

public interface IFullScanner2 extends IFullScanner, IMarkScanner, ICustomPartitionTokenScanner {

    String getContentFromOffsetToEndOfDoc(int currOffset);

    /**
     * Returns the bytes asked for and the position (in the bytes array) to be considered as the current offset.
     * Can be null!
     *
     * If a line has a trailing \r\n, \r or \n, that should be returned too.
     */
    Tuple<Utf8WithCharLen, Integer> getLineFromOffsetAsBytes(int currOffset);

    Tuple<Utf8WithCharLen, Integer> getLineFromLineAsBytes(int currLine);

    int getNumberOfLines();

    int getLineFromOffset(int offset) throws BadLocationException;

    /**
     * This is the offset where the last regexp was found.
     *
     * It's used for \G (if we are at the same position, \G is
     * kept is matching regexps -- otherwise it's set to '\0').
     */
    void setLastRegexpMatchOffset(int endOffset);

    int getLastRegexpMatchOffset();

    void setInBeginWhile(boolean b);

    boolean isInBeginWhile();

    void pushRange(int offset, int len);

    void popRange();
}
