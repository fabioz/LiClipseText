/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.navigation.LanguageNavigation;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;

public class NextPrevElement {

    public void navigate(boolean forward) {
        ITextEditor editor = EditorUtils.getActiveEditor();
        if (!(editor instanceof BaseLiClipseEditor)) {
            return;
        }
        BaseLiClipseEditor liClipseEditor = (BaseLiClipseEditor) editor;
        IDocument document = liClipseEditor.getDocument();
        ITextSelection iTextSelection = liClipseEditor.getTextSelection();
        if (document == null || iTextSelection == null) {
            Log.log("Invalid document (" + document + ") or selection (" + iTextSelection + ")");
            return;
        }

        LiClipseLanguage liClipseLanguage = liClipseEditor.getLiClipseLanguage();
        LanguageNavigation navigation = liClipseLanguage.getNavigation();

        IRegion i = navigation.find(forward, document, iTextSelection.getOffset());
        if (i != null) {
            editor.selectAndReveal(i.getOffset(), i.getLength());
        }
    }

    public IRegion find(LiClipseLanguage language, boolean forward, Document document, int offset) {
        LanguageNavigation navigation = language.getNavigation();
        return navigation.find(forward, document, offset);
    }

}
