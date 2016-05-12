/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import org.brainwy.liclipsetext.editor.regexp.CharsRegion;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.structure.FastStack;

public class ReplaceContext {

    private FastStack<CharsRegion> charsRegion = new FastStack<>(3);
    private FastStack<FastStringBuffer> buf = new FastStack<>(3);

    public ReplaceContext(CharsRegion charsRegion) {
        this.charsRegion.push(charsRegion);
    }

    public byte[] getBytesPosContents(int groupId) {
        return this.charsRegion.peek().getBytesPosContents(groupId);
    }

    public void append(FastStringBuffer buffer) {
        buf.peek().append(buffer);
    }

    public void append(String contents) {
        buf.peek().append(contents);
    }

    public void pushBuffer(FastStringBuffer buf2) {
        buf.push(buf2);
    }

    public void pushCharsRegion(CharsRegion charsRegion2) {
        this.charsRegion.push(charsRegion2);
    }

    public void popCharsRegion() {
        this.charsRegion.pop();
    }

    public CharsRegion peekCharsRegion() {
        return this.charsRegion.peek();
    }

    public boolean hasGroup(int groupId) {
        return charsRegion.peek().hasGroup(groupId);
    }

    private FastStack<NodeWithContents> nodesCtx = new FastStack<>(3);

    public void pushNodeWithContents(NodeWithContents variableNode) {
        nodesCtx.push(variableNode);
    }

    public void popNodeWithContents() {
        nodesCtx.pop();
    }

    public NodeWithContents peekNodeWithContents() {
        return nodesCtx.peek();
    }

}
