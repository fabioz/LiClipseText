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
 * Placeholder node -- only for snippets.

## Placeholders
    $int
    ${int}
    ${int:snippet}
    ${int/regexp/format/options} --> from super
    ${int|choice 1,...,choice n|} --> transform this into a template variable!
 */
class PlaceholderNode extends NodeWithRegexp {

    private int targetI;
    private List<ChoiceNode> choiceNodes = new LowMemoryArrayList<>();

    public PlaceholderNode(String string, int begin, int end) {
        super(string, begin, end);
        String sub = super.getContentsSub();
        this.targetI = Integer.parseInt(sub);
    }

    @Override
    public void applyReplace(ReplaceContext ctx) {
        //TODO: FINISH
    }

    public void addChoiceNode(ChoiceNode choiceNode) {
        choiceNodes.add(choiceNode);
    }

    @Override
    public String toString() {
        String re = "";
        if (regexp != null) {
            re = " regexp: " + regexp;
        }

        String opts = "";
        if (regexpOptions != null) {
            opts = " opts: " + regexpOptions;
        }

        String ch = "";
        if (children.size() > 0) {
            ch = " children:" + children.toString();
        }

        String choice = "";
        if (choiceNodes.size() > 0) {
            choice = " choiceNodes:" + choiceNodes.toString();
        }

        String fmt = "";
        if (this.formatNode != null) {
            fmt = " fmt: " + formatNode;
        }
        return StringUtils.join("", this.getClass().getSimpleName(), "[", string.substring(begin, end), re, fmt, opts,
                ch, choice,
                "]");
    }
}