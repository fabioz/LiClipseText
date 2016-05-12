/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor;

import java.io.File;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IEditorAssociationOverride;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager.LanguageOrBinary;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;

public class LiClipseEditorOverride implements IEditorAssociationOverride {

    public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType,
            IEditorDescriptor[] editorDescriptors) {
        return editorDescriptors;
    }

    public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType,
            IEditorDescriptor[] editorDescriptors) {
        return editorDescriptors;
    }

    public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType,
            IEditorDescriptor editorDescriptor) {
        try {
            if (contentType == null && editorDescriptor == null) {
                IEditorRegistry reg = PlatformUI.getWorkbench().getEditorRegistry();

                String name = editorInput.getName();
                LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                if (name != null) {
                    LiClipseLanguage language = languagesManager.getLanguageForFilename(name);
                    if (language != null && language.name != null) {
                        editorDescriptor = reg.findEditor("com.brainwy.liclipse.editor.common.LiClipseEditor."
                                + language.name.toLowerCase());
                    }
                }
                LanguageOrBinary languageOrBinary = null;
                if (editorDescriptor == null) {
                    //Use shebang
                    File file = EditorUtils.getFileFromEditorInput(editorInput);
                    if (file != null) {
                        languageOrBinary = languagesManager.getLanguageFromFileContents(file);
                        LiClipseLanguage language = languageOrBinary.language;
                        if (language != null && language.name != null) {
                            editorDescriptor = reg.findEditor("com.brainwy.liclipse.editor.common.LiClipseEditor."
                                    + language.name.toLowerCase());
                        }
                    }
                }
                if (editorDescriptor == null && languageOrBinary != null && !languageOrBinary.isBinary) {
                    //if everything failed, use the 'base' liclipse editor (but only if not binary).
                    editorDescriptor = reg.findEditor("com.brainwy.liclipse.editor.common.LiClipseEditor");
                }
            }
        } catch (Exception e) {
            Log.log(e);
        }
        return editorDescriptor;
    }

    public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType,
            IEditorDescriptor editorDescriptor) {
        try {
            if (contentType == null && editorDescriptor == null) {
                IEditorRegistry reg = PlatformUI.getWorkbench().getEditorRegistry();

                LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                if (fileName != null) {
                    LiClipseLanguage language = languagesManager.getLanguageForFilename(fileName);
                    if (language != null && language.name != null) {
                        editorDescriptor = reg.findEditor("com.brainwy.liclipse.editor.common.LiClipseEditor."
                                + language.name.toLowerCase());
                    }
                }
                //If we only have the name, don't make the override by default.
                //if (editorDescriptor == null) {
                //if everything failed, use the 'base' liclipse editor.
                //editorDescriptor = reg.findEditor("com.brainwy.liclipse.editor.common.LiClipseEditor");
                //}
            }
        } catch (Exception e) {
            Log.log(e);

        }
        return editorDescriptor;
    }

}
