/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import org.brainwy.liclipsetext.shared_core.string.StringUtils;

class IfNode extends Node {

    private ElseNode elseNode;
    private boolean notIfNode = false;

    public IfNode() {
    }

    public void setElseNode(ElseNode elseNode) {
        this.elseNode = elseNode;
    }

    @Override
    public String toString() {
        String ch = "";
        if (children.size() > 0) {
            ch = " children:" + children.toString();
        }
        String en = "";
        if (elseNode != null) {
            ch = " " + elseNode.toString();
        }
        String not = "";
        if (notIfNode) {
            not = "NOT";
        }
        return StringUtils.join("", this.getClass().getSimpleName(), "[", not, en, ch,
                "]");
    }

    //Used for Else nodes when not inside an If-else (to make a not-if).
    public void setNotIfNode(boolean b) {
        notIfNode = b;
    }

    @Override
    public void applyReplace(ReplaceContext ctx) throws Exception {
        Boolean eval = null;
        for (Node c : children) {
            boolean temp = c.evaluateBool(ctx);
            if (this.notIfNode) {
                temp = !temp;
            }
            if (eval == null) {
                eval = temp;
            } else {
                eval = eval && temp;
            }
        }

        if (eval != null && eval) {
            //match
            NodeWithContents node = ctx.peekNodeWithContents();
            String contentsSub = node.getContentsSub();
            ctx.append(contentsSub);

        } else {
            //match else (if there's an else)
            if (this.elseNode != null) {
                this.elseNode.applyReplace(ctx);
            }
        }
    }

}