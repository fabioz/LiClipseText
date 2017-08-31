/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda, and others.
 * All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.templates;

import org.brainwy.liclipsetext.editor.common.ILiClipseEditor;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_ui.templates.AbstractDocumentTemplateContextWithIndent;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.templates.TemplateContextType;

public class LiClipseDocumentTemplateContext extends AbstractDocumentTemplateContextWithIndent {

    private LiClipseLanguage language;
    public String prefix;
    public String prefixWithSeparators;

    public LiClipseDocumentTemplateContext(TemplateContextType type, IDocument document, int offset, int length,
            String indentTo, ILiClipseEditor editor) {
        super(type, document, offset, length, indentTo);
        this.language = editor.getLiClipseLanguage();
    }

    public LiClipseDocumentTemplateContext(TemplateContextType type, IDocument document,
            int offset, int length, String indentTo, LiClipseLanguage language) {
        super(type, document, offset, length, indentTo);
        this.language = language;
    }

    @Override
    protected int getTabWidth() {
        return language.getIndent().getTabWidth();
    }

    @Override
    protected boolean getUseSpaces() {
        return language.getIndent().getTabsToSpaceEnabled();
    }

    public TextSelectionUtils createTextSelectionUtils() {
        return new TextSelectionUtils(getDocument(),
                new TextSelection(getDocument(), getCompletionOffset(), getCompletionLength()));
    }

    public TextSelectionUtils markSelection() {
        TextSelectionUtils ts = this.createTextSelectionUtils();
        this.setCompletionOffset(ts.getAbsoluteCursorOffset());
        this.setCompletionLength(ts.getSelLength());
        return ts;
    }

    public TextSelectionUtils selectBlock() {
        TextSelectionUtils ts = this.createTextSelectionUtils();

        try {
            int startPos = TextSelectionUtils.getFirstCharPosition(ts.getDoc(), ts.getAbsoluteCursorOffset());
            ts.selectCompleteLine();
            int absoluteCursorOffset = ts.getAbsoluteCursorOffset();
            int diff = startPos - absoluteCursorOffset;
            if (diff > 0) {
                int len = ts.getSelLength();
                ts.setSelection(startPos, startPos + (len - diff));
            }
        } catch (BadLocationException e) {
            Log.log(e);
        }

        this.setCompletionOffset(ts.getAbsoluteCursorOffset());
        this.setCompletionLength(ts.getSelLength());
        return ts;
    }

    public LiClipseLanguage getLanguage() {
        return this.language;
    }

    /**
     * The first is the prefix with the non-separator chars and then the separator chars before the given offset.
     *
     * The first element (non-separator chars) is always required, whereas the second may be null.
     */
    public void setPrefixes(String prefix, String prefixWithSeparators) {
        this.prefix = prefix;
        this.prefixWithSeparators = prefixWithSeparators;
    }

}
