/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.parsing;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.brainwy.liclipsetext.shared_core.editor.IBaseEditor;
import org.brainwy.liclipsetext.shared_core.parsing.BaseParser;
import org.brainwy.liclipsetext.shared_core.parsing.ChangedParserInfoForObservers;
import org.brainwy.liclipsetext.shared_core.parsing.ErrorParserInfoForObservers;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentPartitioner;

public class LiClipseParser extends BaseParser {

    public LiClipseParser(IBaseEditor edit) {
        super(LiClipseParserManager.getParserManager());
    }

    @Override
    public ParseOutput reparseDocument(Object... argsToReparse) {
        ParseOutput parseResult = reparse();
        if (disposed) {
            return new ParseOutput();
        }

        long documentTime = System.currentTimeMillis();

        IAdaptable adaptable = (IAdaptable) input;
        ErrorParserInfoForObservers errorInfo = null;
        if (parseResult.error != null) {
            errorInfo = new ErrorParserInfoForObservers(parseResult.error, adaptable, document, argsToReparse);
        }

        if (parseResult.ast != null) {
            ChangedParserInfoForObservers info = new ChangedParserInfoForObservers(
                    parseResult.ast,
                    parseResult.modificationStamp,
                    adaptable,
                    document,
                    documentTime,
                    errorInfo,
                    argsToReparse);
            fireParserChanged(info);
        }

        if (errorInfo != null) {
            fireParserError(errorInfo);

        }
        return parseResult;
    }

    public ParseOutput reparse() {
        IDocumentPartitioner partitioner = document.getDocumentPartitioner();
        if (partitioner instanceof LiClipseDocumentPartitioner) {
            LiClipseDocumentPartitioner liClipseDocumentPartitioner = (LiClipseDocumentPartitioner) partitioner;
            long modificationStamp = ((IDocumentExtension4) this.document).getModificationStamp();
            LiClipseNode outline = liClipseDocumentPartitioner.language.getOutline().createOutline(document);
            //System.out.println("Created outline: " + outline);
            return new ParseOutput(outline, null, modificationStamp);
        }
        return new ParseOutput();
    }

}
