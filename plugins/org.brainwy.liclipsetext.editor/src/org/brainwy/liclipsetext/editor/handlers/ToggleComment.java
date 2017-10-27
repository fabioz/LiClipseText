/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.editor.handlers.comment.ToggleBlockCommentAction;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.comment.LanguageComment;
import org.brainwy.liclipsetext.editor.rules.SwitchLanguageToken;
import org.brainwy.liclipsetext.shared_core.actions.ToggleLineCommentAction;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.ITextEditor;

public class ToggleComment extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITextEditor editor = EditorUtils.getActiveEditor();
        if (!(editor instanceof BaseLiClipseEditor)) {
            return null;
        }
        BaseLiClipseEditor liClipseEditor = (BaseLiClipseEditor) editor;
        IDocument document = liClipseEditor.getDocument();
        TextSelectionUtils ts = liClipseEditor.createTextSelectionUtils();

        LiClipseLanguage language = liClipseEditor.getLiClipseLanguage();
        execute(liClipseEditor, document, ts, language);

        return null;
    }

    public static void execute(BaseLiClipseEditor liClipseEditor, IDocument document, TextSelectionUtils ts,
            LiClipseLanguage language) throws ExecutionException {
        //if we have no selection, we have to get the offset of the start of the line.

        TypedPart read = DocumentTimeStampChangedException.retryUntilNoDocChanges(() -> {
            SubPartitionCodeReader subPartitionCodeReader = new SubPartitionCodeReader();
            subPartitionCodeReader.configureReadAllTopPartition(true, document, ts.getStartLineOffset());
            return subPartitionCodeReader.read();
        });

        if (read != null) {
            if (SwitchLanguageToken.isSubLanguagePartition(read.type)) {
                Tuple<String, String> topAndSub = SwitchLanguageToken.getSubLanguageAndContentType(read.type);
                if (!topAndSub.o1.equals("this") && !topAndSub.o1.equals(language.name.toLowerCase())) {
                    LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                    LiClipseLanguage temp = languagesManager.getLanguageFromName(topAndSub.o1);
                    if (temp != null) {
                        language = temp;
                    }
                }
            }
        }

        if (language != null) {
            LanguageComment languageComment = language.getComment();
            switch (languageComment.commentType) {
                case COMMENT_TYPE_SINGLE_LINE:
                    toggleSingleLineComment(liClipseEditor, language, ts);
                    break;

                case COMMENT_TYPE_MULTI_LINE:
                    toggleBlockComment(liClipseEditor, language, ts);
                    break;

                default:
                    Log.log("Comment type not recognized: " + languageComment.commentType);
                    break;
            }
        }
    }

    public static void toggleBlockComment(BaseLiClipseEditor liClipseEditor, LiClipseLanguage language,
            TextSelectionUtils ts)
            throws ExecutionException {
        LanguageComment languageComment = language.getComment();
        String commentStart = languageComment.commentStart;
        String commentEnd = languageComment.commentEnd;

        ToggleBlockCommentAction action = new ToggleBlockCommentAction(
                ts,
                commentStart,
                commentEnd,
                language,
                1);

        try {
            Tuple<Integer, Integer> region = action.execute();
            if (liClipseEditor != null) {
                liClipseEditor.selectAndReveal(region.o1, region.o2);
            }
        } catch (BadLocationException e) {
            throw new ExecutionException("Unable to complete toggle comment.", e);
        }
    }

    public static void toggleSingleLineComment(BaseLiClipseEditor liClipseEditor, LiClipseLanguage language,
            TextSelectionUtils ts)
            throws ExecutionException {
        String commentPattern = "//";
        String commentString = language.getComment().commentString;
        if (commentString != null) {
            commentPattern = commentString;
        }
        ToggleLineCommentAction action = new ToggleLineCommentAction(
                ts,
                commentPattern,
                1);

        try {
            Tuple<Integer, Integer> region = action.execute();
            if (liClipseEditor != null) {
                liClipseEditor.selectAndReveal(region.o1, region.o2);
            }
        } catch (BadLocationException e) {
            throw new ExecutionException("Unable to complete toggle comment.", e);
        }
    }
}
