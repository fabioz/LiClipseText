/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.auto_edit;

import org.brainwy.liclipsetext.editor.languages.tmbundle.TmIndentPart;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.editor.regexp.RegexpHelper;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyHelper;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyNewLineHelper;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;

public class IndentAutoEditRule implements ILanguageAutoEditRule {

    private String indentPattern;
    private String dedentPattern;
    private String scope;
    private Regex indentRegexp;
    private Regex dedentRegexp;

    public IndentAutoEditRule(TmIndentPart tmIndentPart) {
        indentPattern = tmIndentPart.indentPattern;
        dedentPattern = tmIndentPart.dedentPattern;
        scope = tmIndentPart.scope;
        if (indentPattern != null) {
            indentRegexp = RegexpHelper.createRegexp(indentPattern);
        }
        if (dedentPattern != null) {
            dedentRegexp = RegexpHelper.createRegexp(dedentPattern);
        }
    }

    @Override
    public boolean customizeDocumentCommand(IDocument document, DocumentCommand command, AutoEditStrategyHelper helper,
            String indentString, String contentType) {

        final boolean isNewLine = AutoEditStrategyNewLineHelper.isNewLineText(document, command.length, command.text);
        if (!isNewLine) {
            return false;
        }
        TextSelectionUtils ts = new TextSelectionUtils(document, command.offset);

        String cursorLineContents = ts.getCursorLineContents();
        Utf8WithCharLen bytes = RegexpHelper.getBytes(cursorLineContents);
        if (indentRegexp != null && matchesRegexp(bytes, indentRegexp)) {
            String prevLineIndent = new AutoEditStrategyNewLineHelper().getPreviousLineIndent(command, document);
            command.text = new FastStringBuffer(command.text, prevLineIndent.length() + indentString.length())
                    .append(prevLineIndent).append(indentString)
                    .toString();
            return true;
        }
        if (dedentRegexp != null && matchesRegexp(bytes, dedentRegexp)) {
            String prevLineIndent = new AutoEditStrategyNewLineHelper().getPreviousLineIndent(command, document);
            command.text = new FastStringBuffer(command.text, prevLineIndent.length())
                    .append(new FastStringBuffer(prevLineIndent, 0).deleteLastChars(indentString.length()))
                    .toString();
            return true;
        }

        return false;
    }

    private boolean matchesRegexp(Utf8WithCharLen bytes, Regex indentR) {
        byte[] b = bytes.getBytes();
        Matcher matcher = indentR.matcher(b);
        int i = matcher.search(0, b.length, Option.NONE);
        if (i >= 0) {
            return true;
        }
        return false;
    }

}
