/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import java.util.List;

import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.LowMemoryArrayList;

/**
 * Base node class.
 */
public abstract class Node {

    /**
     * Note: children of a class don't necessarily have the same type as the parent.
     */
    protected LowMemoryArrayList<Node> children = new LowMemoryArrayList<Node>();

    public void addChild(Node treeNode) {
        if (treeNode instanceof TextNode) {
            int size = this.children.size();
            if (size > 0) {
                Node last = this.children.get(size - 1);
                if (last instanceof TextNode) {
                    ((TextNode) last).extend((TextNode) treeNode);
                    return;
                }
            }
        }
        this.children.add(treeNode);
    }

    public boolean hasChildren() {
        return this.children.size() > 0;
    }

    public void toTemplateStr(SnippetToTemplateCtx ctx) {
        for (Node c : children) {
            c.toTemplateStr(ctx);
        }
    }

    @Override
    public String toString() {
        String ch = "";
        if (children.size() > 0) {
            ch = " children:" + children.toString();
        }

        return StringUtils.join("", this.getClass().getSimpleName(), "[", ch, "]");
    }

    public void addChildren(List<Node> lst) {
        for (Node n : lst) {
            this.addChild(n);
        }
    }

    public List<Node> getAndClearChildren() {
        LowMemoryArrayList<Node> existing = children;
        children = new LowMemoryArrayList<Node>();
        return existing;
    }

    public abstract void applyReplace(ReplaceContext ctx) throws Exception;

    public boolean evaluateBool(ReplaceContext ctx) {
        return false;
    }

}