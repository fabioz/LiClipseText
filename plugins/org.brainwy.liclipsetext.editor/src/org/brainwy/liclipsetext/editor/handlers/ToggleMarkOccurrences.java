/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers;

import java.lang.ref.WeakReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;
import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.editor.common.mark_occurrences.LiClipseMarkOccurrencesJob;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.editor.BaseEditor;

public class ToggleMarkOccurrences extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITextEditor activeEditor = EditorUtils.getActiveEditor();
        if (!(activeEditor instanceof BaseLiClipseEditor)) {
            return null;
        }
        BaseLiClipseEditor baseLiClipseEditor = (BaseLiClipseEditor) activeEditor;

        try {
            IPreferenceStore store = LiClipseTextEditorPlugin.getDefault().getPreferenceStore();
            boolean prev = store.getBoolean(LiClipseTextPreferences.USE_MARK_OCCURRENCES);
            store.setValue(LiClipseTextPreferences.USE_MARK_OCCURRENCES, !prev);
            baseLiClipseEditor.getStatusLineManager().setMessage(
                    "Toggled mark occurrences. Currently: " + (prev ? "Off" : "On"));
            LiClipseMarkOccurrencesJob.scheduleRequest(new WeakReference<BaseEditor>(baseLiClipseEditor),
                    baseLiClipseEditor.createTextSelectionUtils(), 0); //On the action, ask it to happen now.
        } catch (Exception e) {
            Log.log(e);
        }

        return null;
    }

}
