/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import java.util.HashMap;
import java.util.Map;

class CodeNode extends NodeWithContents {

    public CodeNode(String snippet, int begin, int end) {
        super(snippet, begin, end);
    }

    @Override
    public void applyReplace(ReplaceContext ctx) throws Exception {
        //TODO: FINISH
    }

    private static Map<String, String> replacements = new HashMap<String, String>();

    static {
        replacements.put("\"$TM_BUNDLE_SUPPORT/bin/snippet_paren.rb\"", "(");
        replacements.put("\"$TM_BUNDLE_SUPPORT/bin/snippet_paren.rb\" end", ")");
    }

    @Override
    public void toTemplateStr(SnippetToTemplateCtx ctx) {
        String sub = this.getContentsSub();
        String newStr = CodeNode.replacements.get(sub);
        if (newStr == null) {
            System.err.println("Unable to provide a replacement for code in snippet: " + sub + " " + this.string);
        } else {
            ctx.append(newStr);
        }
    }
}