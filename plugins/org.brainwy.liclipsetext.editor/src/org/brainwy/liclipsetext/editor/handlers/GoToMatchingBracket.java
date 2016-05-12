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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.ui.texteditor.ITextEditor;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;

public class GoToMatchingBracket extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITextEditor editor = EditorUtils.getActiveEditor();
        if (!(editor instanceof BaseLiClipseEditor)) {
            return null;
        }
        BaseLiClipseEditor liClipseEditor = (BaseLiClipseEditor) editor;
        TextSelectionUtils ts = liClipseEditor.createTextSelectionUtils();
        if (ts.getSelLength() != 0) {
            return null;
        }
        ICharacterPairMatcher matcher = liClipseEditor.getCharacterPairMatcher();

        IRegion match = matcher.match(ts.getDoc(), ts.getAbsoluteCursorOffset());
        if (match != null) {
            int anchor = matcher.getAnchor();
            if (anchor == ICharacterPairMatcher.RIGHT) {
                liClipseEditor.selectAndReveal(match.getOffset() + 1, 0);
            } else {
                liClipseEditor.selectAndReveal(match.getOffset() + match.getLength(), 0);
            }
        }
        return null;
    }
}
