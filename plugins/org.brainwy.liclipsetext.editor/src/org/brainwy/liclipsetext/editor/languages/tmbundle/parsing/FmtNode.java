/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.LowMemoryArrayList;

public class FmtNode extends Node {

    @Override
    public void toTemplateStr(SnippetToTemplateCtx ctx) {
        LowMemoryArrayList<Node> children = this.children;
        for (Node node2 : children) {
            node2.toTemplateStr(ctx);
        }
    }

    @Override
    public String toString() {
        return StringUtils.join("", "FmtNode[", this.children, "]");
    }

    @Override
    public void applyReplace(ReplaceContext ctx) throws Exception {
        for (Node child : this.children) {
            child.applyReplace(ctx);
        }
    }

}
