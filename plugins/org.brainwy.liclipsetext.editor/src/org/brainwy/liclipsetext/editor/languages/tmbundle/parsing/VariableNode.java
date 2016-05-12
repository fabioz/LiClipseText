/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import java.util.List;

import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.editor.regexp.CharsRegion;
import org.brainwy.liclipsetext.editor.regexp.RegexpHelper;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;

/**
 * Variable node.

    ${var}
    ${var:?if:else}
    ${var:+if}
    ${var:-else}
    ${var:else}
    ${var/regexp/format/options}
    ${var:[/upcase][/downcase][/capitalize][/asciify]}

 */
class VariableNode extends NodeWithRegexp {

    private IfNode ifNode;

    public VariableNode(String snippet, int begin, int end) {
        super(snippet, begin, end);
    }

    @Override
    public void applyReplace(ReplaceContext ctx) throws Exception {

        if (this.ifNode != null) {
            ctx.pushNodeWithContents(this);
            this.ifNode.applyReplace(ctx);
            ctx.popNodeWithContents();

        } else if (this.regexp != null && this.regexp.length() > 0) {
            //Something as ${1/(#)(#)?(#)?/${3:?3:${2:?2:1}}/}
            Regex re = RegexpHelper.createRegexp(this.regexp);

            int targetI = Integer.parseInt(this.getContentsSub());
            if (!ctx.hasGroup(targetI)) {
                //System.out.println("Unable to find group in: " + this + " matching: "
                //        + ctx.peekCharsRegion().getStrPosContents(0));
            } else {
                byte[] bytesFoundInTargetGroup = ctx.getBytesPosContents(targetI);
                Matcher matcher = re.matcher(bytesFoundInTargetGroup);
                int match = matcher.match(0, bytesFoundInTargetGroup.length, Option.CAPTURE_GROUP);
                if (match >= 0) {
                    Utf8WithCharLen bytes = new Utf8WithCharLen(bytesFoundInTargetGroup);
                    ctx.pushCharsRegion(new CharsRegion(matcher, bytes));
                    //Ok, we have a match, let's see which groups can we apply to.
                    this.formatNode.applyReplace(ctx);
                    ctx.popCharsRegion();
                }
            }

        } else if (this.children.size() > 0) {
            //Converted
            //TODO: FINISH
        } else {
            //simple var
            //TODO: FINISH
        }
    }

    public void setIfNode(IfNode ifNode) {
        this.ifNode = ifNode;
    }

    @Override
    public void addChild(Node treeNode) {
        throw new RuntimeException("Invalid for this node.");
    }

    @Override
    public void addChildren(List<Node> lst) {
        throw new RuntimeException("Invalid for this node.");
    }

    public void addConvertNode(ConvertNode convertNode) {
        this.children.add(convertNode);
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
            ch = " convert:" + children.toString();
        }
        String fmt = "";
        if (this.formatNode != null) {
            fmt = " fmt: " + formatNode;
        }

        String ifN = "";
        if (this.ifNode != null) {
            ifN = " " + ifNode;
        }
        return StringUtils.join("", this.getClass().getSimpleName(), "[", string.substring(begin, end), ifN, re, fmt,
                opts,
                ch,
                "]");
    }
}