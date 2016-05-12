/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.mark_occurrences;

import java.lang.ref.WeakReference;
import java.util.ListResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_ui.editor.BaseEditor;
import org.brainwy.liclipsetext.shared_ui.editor.IEditListener;
import org.brainwy.liclipsetext.shared_ui.editor.IEditListener2;

public class LiclipseMarkOccurrencesDispatcher implements IEditListener, IEditListener2 {

    public void onSave(BaseEditor baseEditor, IProgressMonitor monitor) {
    }

    public void onCreateActions(ListResourceBundle resources, BaseEditor baseEditor, IProgressMonitor monitor) {
    }

    public void onDispose(BaseEditor baseEditor, IProgressMonitor monitor) {
    }

    public void onSetDocument(IDocument document, BaseEditor baseEditor, IProgressMonitor monitor) {
    }

    public void handleCursorPositionChanged(BaseEditor baseEditor, TextSelectionUtils ps) {
        LiClipseMarkOccurrencesJob.scheduleRequest(new WeakReference<BaseEditor>(baseEditor), ps);
    }
}