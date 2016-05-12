/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.actions.ScopeSelectionAction;

public class SelectEnclosingScope extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITextEditor activeEditor = EditorUtils.getActiveEditor();
        if (!(activeEditor instanceof BaseLiClipseEditor)) {
            return null;
        }

        try {
            BaseLiClipseEditor editor = (BaseLiClipseEditor) activeEditor;
            IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
            ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();

            new ScopeSelectionAction().perform(doc, selection, editor);
        } catch (Exception e) {
            Log.log(e);
        }

        return null;
    }

}
