/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.ui.texteditor.ITextEditor;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.editor.handlers.extensions.ILiClipseFormatHandler;
import org.brainwy.liclipsetext.editor.handlers.extensions.LiClipseActionContext;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.SelectionKeeper;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.utils.BaseExtensionHelper;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;

public class Formatter extends AbstractHandler {

    @SuppressWarnings("unchecked")
    public Object execute(ExecutionEvent event) throws ExecutionException {

        try {
            ITextEditor editor = EditorUtils.getActiveEditor();
            if (!(editor instanceof BaseLiClipseEditor)) {
                return null;
            }
            BaseLiClipseEditor liClipseEditor = (BaseLiClipseEditor) editor;
            LiClipseLanguage language = liClipseEditor.getLanguage();
            if (language == null) {
                return null;
            }

            ILiClipseFormatHandler handler = null;
            List<ILiClipseFormatHandler> participants = BaseExtensionHelper
                    .getParticipants("org.brainwy.liclipsetext.editor.liclipse_formatter");
            for (ILiClipseFormatHandler iLiClipseFormatHandler : participants) {
                if (iLiClipseFormatHandler.canHandle(language)) {
                    handler = iLiClipseFormatHandler;
                    break;
                }
            }
            if (handler == null) {
                return null;
            }

            TextSelectionUtils ts = liClipseEditor.createTextSelectionUtils();

            IDocument doc = ts.getDoc();
            DocumentRewriteSession session = null;
            final SelectionKeeper selectionKeeper = new SelectionKeeper(ts);

            try {
                String encoding = liClipseEditor.getEncoding();
                String contents = handler.format(new LiClipseActionContext(ts, doc, encoding, liClipseEditor
                        .getEditorInput(), liClipseEditor.getEditorFile()));

                if (doc instanceof IDocumentExtension4) {
                    IDocumentExtension4 ext = (IDocumentExtension4) doc;
                    session = ext.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
                }
                TextSelectionUtils.setOnlyDifferentCode(doc, doc.get(), contents);

            } catch (Exception e) {
                liClipseEditor.getStatusLineManager().setErrorMessage(e.getMessage());
            } finally {
                if (session != null) {
                    ((IDocumentExtension4) doc).stopRewriteSession(session);
                }
            }
            selectionKeeper.restoreSelection(liClipseEditor.getSelectionProvider(), doc);

            liClipseEditor.getStatusLineManager().setMessage(null);
        } catch (Exception e) {
            Log.log(e);
        }
        return null;
    }
}
