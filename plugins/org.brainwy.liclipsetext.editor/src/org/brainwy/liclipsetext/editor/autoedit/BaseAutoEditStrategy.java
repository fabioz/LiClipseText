/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.autoedit;

import org.brainwy.liclipsetext.editor.common.LiClipseSourceViewerConfiguration;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.auto_edit.LanguageAutoEdit;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyHelper;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyNewLineHelper;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

public final class BaseAutoEditStrategy extends DefaultIndentLineAutoEditStrategy implements IAutoEditStrategy {

    private boolean blockSelection;
    private LiClipseSourceViewerConfiguration configuration;

    /**
     * Use only for tests
     */
    /*default*/BaseAutoEditStrategy() {
        this(null);
    }

    public BaseAutoEditStrategy(LiClipseSourceViewerConfiguration liClipseSourceViewerConfiguration) {
        this.configuration = liClipseSourceViewerConfiguration;
    }

    public void setBlockSelection(boolean blockSelection) {
        this.blockSelection = blockSelection;
    }

    /*
     * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
     */
    @Override
    public void customizeDocumentCommand(IDocument document, DocumentCommand command) {

        AutoEditStrategyHelper helper = new AutoEditStrategyHelper(document, command);
        String regularIndent = "    ";
        if (configuration != null) {
            regularIndent = configuration.getIndentString();
        }

        LiClipseLanguage language = ((LiClipseDocumentPartitioner) document.getDocumentPartitioner()).language;
        LanguageAutoEdit autoEdit = language.getAutoEdit();
        if (autoEdit.customizeDocumentCommand(document, command, helper, regularIndent)) {
            return;
        }

        char c = helper.c;
        //
        //        switch (c) {
        //case '\0':
        //Note: this doesn't work well because we can't differentiate between a delete and a backspace at
        //this point!
        //boolean isDel = command.caretOffset == -1 && command.length == 1 && command.shiftsCaret == true
        //        && command.text.length() == 0;
        //if (isDel) {
        //    helper.handleDelete(document, command, c, regularIndent);
        //    return;
        //}
        //break;

        //            case '"':
        //            case '\'':
        //                boolean isStringContext = helper.isStringContext(document, command);
        //                helper.handleLiteral(document, command, isStringContext, c);
        //                return;
        //
        //            case '[':
        //            case '{':
        //            case '(':
        //            case '<':
        //                helper.handleOpenParens(document, command, c);
        //                return;
        //
        //            case ')':
        //            case ']':
        //            case '}':
        //            case '>':
        //                helper.handleCloseParens(document, command, c);
        //                return;
        //        }

        final boolean isNewLine = AutoEditStrategyNewLineHelper.isNewLineText(document, command.length, command.text);

        if (isNewLine) {
            helper.handleNewLine(document, command, c, regularIndent);
            return;
        }

    }

}