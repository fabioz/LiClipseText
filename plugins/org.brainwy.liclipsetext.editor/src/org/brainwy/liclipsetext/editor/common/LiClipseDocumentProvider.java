/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

/**
 * Note: subclass TextFileDocumentProvider is better because of sharing of the document!
 */
public class LiClipseDocumentProvider extends TextFileDocumentProvider {

    private final LiClipseLanguage defaultLanguage;
    private String initialLanguage;

    public LiClipseDocumentProvider(Class<? extends BaseLiClipseEditor> editorClass) {
        if (editorClass != LiClipseEditor.class) {
            //If it's not the default editor, we should check if we should force the language.
            defaultLanguage = LiClipseTextEditorPlugin.getLanguagesManager().getLanguageFromEditorId(editorClass.getName());
        } else {
            defaultLanguage = null;
        }
    }

    @Override
    public void connect(Object element) throws CoreException {
        super.connect(element);
        try {
            IDocument document = super.getDocument(element);
            if (document != null) {
                LanguagesManager manager = LiClipseTextEditorPlugin.getLanguagesManager();
                if (manager != null) {
                    if (element instanceof IEditorInput) {
                        IEditorInput iEditorInput = (IEditorInput) element;

                        LiClipseLanguage language = forcedAssociation.remove(iEditorInput);
                        if (language == null) {
                            if (initialLanguage != null) {
                                language = manager.getLanguageFromName(initialLanguage);
                            }
                            if (language == null) {
                                if (defaultLanguage != null) {
                                    language = defaultLanguage;
                                } else {
                                    String name = iEditorInput.getName();
                                    language = manager.getLanguage(name, document);
                                }
                            }
                        }
                        language.connect(document);
                    }
                }
            }
        } catch (Exception e) {
            Log.log(e);
        }
    }

    private final static Map<IEditorInput, LiClipseLanguage> forcedAssociation = new HashMap<IEditorInput, LiClipseLanguage>();

    public static void pushForceLanguageOnce(IEditorInput editorInput, LiClipseLanguage language) {
        forcedAssociation.put(editorInput, language);
    }

    public void setInitialLanguage(String initialLanguage) {
        this.initialLanguage = initialLanguage;
    }

}