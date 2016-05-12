/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers.extensions;

import java.io.File;

import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;

public class LiClipseActionContext {

    public final TextSelectionUtils ts;
    public final IDocument doc;
    public final String encoding;
    public final IEditorInput iEditorInput;
    public final File file;

    public LiClipseActionContext(TextSelectionUtils ts, IDocument doc, String encoding, IEditorInput iEditorInput,
            File file) {
        this.ts = ts;
        this.doc = doc;
        this.encoding = encoding;
        this.iEditorInput = iEditorInput;
        this.file = file;
    }

}
