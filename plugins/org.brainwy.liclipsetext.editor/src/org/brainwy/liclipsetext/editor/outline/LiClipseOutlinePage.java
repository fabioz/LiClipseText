/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.outline;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.shared_ui.SharedUiPlugin;
import org.brainwy.liclipsetext.shared_ui.outline.BaseOutlinePage;
import org.eclipse.jface.preference.IPreferenceStore;

public class LiClipseOutlinePage extends BaseOutlinePage {

    public LiClipseOutlinePage(BaseLiClipseEditor liClipseEditor) {
        super(liClipseEditor, SharedUiPlugin.getImageCache(), "org.brainwy.liclipsetext.editor");
    }

    @Override
    public IPreferenceStore getStore() {
        return LiClipseTextEditorPlugin.getDefault().getPreferenceStore();
    }

}
