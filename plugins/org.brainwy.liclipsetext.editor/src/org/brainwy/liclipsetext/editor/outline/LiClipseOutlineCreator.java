/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.outline;

import org.brainwy.liclipsetext.editor.common.ILiClipseEditor;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.eclipse.jface.text.IDocument;

public class LiClipseOutlineCreator {

    private ILiClipseEditor editor;

    public LiClipseOutlineCreator(ILiClipseEditor editor) {
        this.editor = editor;
    }

    public LiClipseNode createOutline() {
        IDocument document = editor.getDocument();
        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        LiClipseLanguage language = partitioner.language;
        LanguageOutline outline = language.getOutline();
        return outline.createOutline(document);
    }

}
