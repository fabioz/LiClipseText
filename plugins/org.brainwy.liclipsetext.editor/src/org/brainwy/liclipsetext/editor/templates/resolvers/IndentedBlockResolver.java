/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.templates.resolvers;

import java.util.List;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.indent.LanguageIndent;
import org.brainwy.liclipsetext.editor.templates.LiClipseDocumentTemplateContext;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateContext;

public class IndentedBlockResolver extends SimpleTemplateVariableResolver {

    public IndentedBlockResolver() {
        super("indented_block", "Full line");
    }

    @Override
    protected String resolve(TemplateContext context) {
        LiClipseDocumentTemplateContext ctx = (LiClipseDocumentTemplateContext) context;
        LiClipseLanguage language = ctx.getLanguage();
        LanguageIndent indent = language.getIndent();
        String indentString = indent.getIndentString();

        TextSelectionUtils ts = ctx.selectBlock();
        String selectedText = ts.getSelectedText();
        if (indentString != null && indentString.length() > 0) {
            List<String> splitInLines = StringUtils.splitInLines(selectedText, true);
            int len = splitInLines.size();
            if (len > 0) {
                FastStringBuffer buf = new FastStringBuffer(selectedText.length() + (len * indentString.length()) + 1);
                for (int i = 0; i < len; i++) {
                    buf.append(indentString).append(splitInLines.get(i));
                }
                selectedText = buf.toString();
            }
        }
        return selectedText;
    }
}
