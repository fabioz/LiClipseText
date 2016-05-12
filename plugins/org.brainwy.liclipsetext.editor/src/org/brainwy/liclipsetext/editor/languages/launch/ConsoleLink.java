/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.launch;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.brainwy.liclipsetext.shared_core.locator.BaseItemPointer;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.utils.UIUtils;

/**
 * Opens up a file with a given line
 */
public class ConsoleLink implements IHyperlink {

    public final BaseItemPointer pointer;

    public ConsoleLink(BaseItemPointer pointer) {
        this.pointer = pointer;
    }

    public void linkEntered() {

    }

    public void linkExited() {

    }

    public void linkActivated() {
        BaseItemPointer p = pointer;
        Object file = pointer.file;
        IEditorPart editor = null;
        if (file instanceof IFile) {
            try {
                editor = IDE.openEditor(UIUtils.getActivePage(), (IFile) file, true);
            } catch (PartInitException e) {
                Log.log(e);
            }

        } else if (file instanceof IPath) {
            IPath path = (IPath) file;
            editor = EditorUtils.openFile(path.toFile());

        } else if (file instanceof File) {
            editor = EditorUtils.openFile((File) file);
        }

        if (editor instanceof ITextEditor && p.start.line >= 0) {
            EditorUtils.showInEditor((ITextEditor) editor, p.start, p.end);
        }
    }
}