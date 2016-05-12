/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers;

import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.brainwy.liclipsetext.editor.outline.LiClipseOutlineCreator;
import org.brainwy.liclipsetext.editor.quick_outline.LiClipseQuickOutlineDialog;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.texteditor.ITextEditor;

public class QuickOutline extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITextEditor editor = EditorUtils.getActiveEditor();
        if (!(editor instanceof BaseLiClipseEditor)) {
            return null;
        }
        BaseLiClipseEditor liClipseEditor = (BaseLiClipseEditor) editor;

        LiClipseOutlineCreator outlineCreator = new LiClipseOutlineCreator(liClipseEditor);
        LiClipseNode outline = liClipseEditor.getOutline();
        if (outline == null) {
            outline = outlineCreator.createOutline();
        }
        if (outline != null) {
            new LiClipseQuickOutlineDialog(EditorUtils.getShell(), liClipseEditor, outline).open();
        }
        return null;
    }
}
