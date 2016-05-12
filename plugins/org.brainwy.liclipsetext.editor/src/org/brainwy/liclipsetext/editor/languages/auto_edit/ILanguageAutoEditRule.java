/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.auto_edit;

import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyHelper;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

public interface ILanguageAutoEditRule {

    public abstract boolean customizeDocumentCommand(IDocument document, DocumentCommand command,
            AutoEditStrategyHelper helper,
            String indentString, String contentType);
}
