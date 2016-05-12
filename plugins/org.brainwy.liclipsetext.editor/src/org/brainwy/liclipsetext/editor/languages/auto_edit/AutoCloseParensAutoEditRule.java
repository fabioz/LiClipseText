/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.auto_edit;

import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguageIO;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyHelper;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

public class AutoCloseParensAutoEditRule extends AbstractScopedAutoEditRule {

    public static final String AUTO_CLOSE_PARENS = "auto_close_parens";

    private final char[] autoCloseChars;

    @SuppressWarnings("rawtypes")
    public AutoCloseParensAutoEditRule(Map<String, Object> map, List<IStatus> errorList) {
        super(map);
        List list = (List) LiClipseLanguageIO.getRequired(map, AUTO_CLOSE_PARENS, List.class);
        autoCloseChars = LiClipseLanguageIO.convertListOfStringsToArrayOfChars(list, AUTO_CLOSE_PARENS, errorList);
    }

    public boolean customizeDocumentCommand(IDocument document, DocumentCommand command, AutoEditStrategyHelper helper,
            String indentString, String contentType) {
        if (!this.scope.equals(contentType)) {
            return false;
        }

        int length = autoCloseChars.length;
        char c = helper.c;
        for (int i = 0; i < length; i++) {
            if (c == autoCloseChars[i]) {
                helper.handleOpenParens(document, command, c);
                return true;
            }
        }

        return false;
    }

}
