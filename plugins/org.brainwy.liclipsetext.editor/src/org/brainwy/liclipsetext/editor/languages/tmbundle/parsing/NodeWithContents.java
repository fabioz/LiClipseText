/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import java.util.ArrayList;
import java.util.Iterator;

import org.brainwy.liclipsetext.editor.languages.LanguageTemplates.LiClipseVariableResolver;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.LowMemoryArrayList;

public abstract class NodeWithContents extends Node {

    protected String string;
    protected int begin;
    protected int end;

    public NodeWithContents(String string, int begin, int end) {
        this.begin = begin;
        this.end = end;
        this.string = string;
    }

    public String getContentsSub() {
        return this.string.substring(begin, end);
    }

    @Override
    public void toTemplateStr(SnippetToTemplateCtx ctx) {
        String sub = string.substring(begin, end);
        boolean addCursor = "0".equals(sub);

        ArrayList<TextNode> textNodes = this.getTextNodes();
        LiClipseVariableResolver resolver = null;
        if (textNodes.size() > 0) {

            FastStringBuffer buf = new FastStringBuffer();
            ctx.pushBuffer(buf);
            for (Iterator<TextNode> it = textNodes.iterator(); it.hasNext();) {
                it.next().toTemplateStr(ctx);
            }
            ctx.popBuffer();

            resolver = ctx.obtainValidTemplateVariableName(buf.toString());
        } else {
            if (!addCursor) {
                resolver = ctx.obtainValidTemplateVariableName("");//request template for empty string
            }
        }
        if (resolver != null) {
            ctx.append(resolver);
        }
        if (addCursor) {
            ctx.append("${cursor}");
        }
    }

    private ArrayList<TextNode> getTextNodes() {
        LowMemoryArrayList<Node> children2 = this.children;
        ArrayList<TextNode> lst = new ArrayList<>(this.children.size());

        for (Node node : children2) {
            if (node instanceof TextNode) {
                lst.add((TextNode) node);
            }
        }
        return lst;
    }

    @Override
    public String toString() {
        String ch = "";
        if (children.size() > 0) {
            ch = " children:" + children.toString();
        }
        return StringUtils.join("", this.getClass().getSimpleName(), "[", string.substring(begin, end), ch, "]");
    }

}
