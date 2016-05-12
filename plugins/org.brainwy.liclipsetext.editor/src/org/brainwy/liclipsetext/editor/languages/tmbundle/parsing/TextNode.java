/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import org.brainwy.liclipsetext.editor.regexp.CharsRegion;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;

class TextNode extends Node {

    private int level;
    private FastStringBuffer buffer;

    public TextNode(char c, int level) {
        this.buffer = new FastStringBuffer(2);
        this.buffer.append(c);
        this.level = level;
    }

    public TextNode(String substring, int level) {
        this.buffer = new FastStringBuffer(substring, 2);
        this.level = level;
    }

    public void extend(TextNode treeNode) {
        this.buffer.append(treeNode.buffer);
    }

    @Override
    public void addChild(Node treeNode) {
        throw new RuntimeException("TextNode should have no children");
    }

    @Override
    public void toTemplateStr(SnippetToTemplateCtx ctx) {
        int len = buffer.length();
        for (int i = 0; i < len; i++) {
            ctx.append(stringFromChar(buffer.charAt(i)));
        }
    }

    private String stringFromChar(char c) {
        if (level > 0) {
            //This will be inside a resolver...
            return String.valueOf(c);
        }
        if (c == '$') {
            return "$$";
        } else {
            return String.valueOf(c);
        }
    }

    @Override
    public String toString() {
        String cls = this.getClass().getSimpleName();
        return StringUtils.join("", cls, "[", buffer, "]");
    }

    @Override
    public void applyReplace(ReplaceContext ctx) {
        ctx.append(buffer);
    }

    @Override
    public boolean evaluateBool(ReplaceContext ctx) {
        String string = this.buffer.toString();
        int parseInt = Integer.parseInt(string);
        CharsRegion charsRegion = ctx.peekCharsRegion();
        if (charsRegion != null) {
            if (charsRegion.hasGroup(parseInt)) {
                return true;
            }
        }
        return false;
    }
}