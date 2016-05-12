/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.indent.LanguageIndent;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.brainwy.liclipsetext.shared_core.auto_edit.IIndentationStringProvider;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.editor.BaseEditor;

public abstract class AbstractLiClipseEditor extends BaseEditor implements ILiClipseEditor, IIndentationStringProvider {

    /**
     * @return the language currently being used in this editor (can be null)
     */
    public LiClipseLanguage getLiClipseLanguage() {
        IDocumentProvider documentProvider = getDocumentProvider();
        if (documentProvider == null) {
            return null;
        }
        IDocument document = documentProvider.getDocument(
                this.getEditorInput());
        if (document == null) {
            return null;
        }
        IDocumentPartitioner partitioner = document.getDocumentPartitioner();
        if (!(partitioner instanceof LiClipseDocumentPartitioner)) {
            return null;
        }
        LiClipseDocumentPartitioner liClipseDocumentPartitioner = (LiClipseDocumentPartitioner) partitioner;
        return liClipseDocumentPartitioner.language;
    }

    /**
     * @return the document currently being edited in this document.
     */
    @Override
    public IDocument getDocument() {
        return EditorUtils.getDocument(this);
    }

    /**
     * @return the source viewer for the editor.
     */
    public LiClipseSourceViewer getEditorSourceViewer() {
        return (LiClipseSourceViewer) this.getSourceViewer();
    }

    public ITextSelection getTextSelection() {
        return EditorUtils.getTextSelection(this);
    }

    @Override
    public TextSelectionUtils createTextSelectionUtils() {
        return EditorUtils.createTextSelectionUtils(this);
    }

    @Override
    protected boolean isTabsToSpacesConversionEnabled() {
        LiClipseLanguage liClipseLanguage = getLiClipseLanguage();
        return isTabsToSpacesConversionEnabled(liClipseLanguage);
    }

    public static boolean isTabsToSpacesConversionEnabled(LiClipseLanguage liClipseLanguage) {
        if (liClipseLanguage != null) {
            LanguageIndent indent = liClipseLanguage.getIndent();
            if (indent != null) {
                Boolean tabsToSpaceEnabled = indent.getTabsToSpaceEnabled();
                if (tabsToSpaceEnabled != null) {
                    return tabsToSpaceEnabled;
                }
            }
        }
        IPreferenceStore preferenceStore = LiClipseTextPreferences.getChainedPreferenceStore();
        return preferenceStore != null
                && preferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);

    }

}
