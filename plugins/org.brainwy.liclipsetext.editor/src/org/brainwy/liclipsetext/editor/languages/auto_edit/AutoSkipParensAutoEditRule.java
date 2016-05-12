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

public class AutoSkipParensAutoEditRule extends AbstractScopedAutoEditRule {

    public static final String AUTO_SKIP_PARENS = "auto_skip_parens";
    private final char[] autoSkipChars;

    @SuppressWarnings("rawtypes")
    public AutoSkipParensAutoEditRule(Map<String, Object> map, List<IStatus> errorList) {
        super(map);
        List list = (List) LiClipseLanguageIO.getRequired(map, AUTO_SKIP_PARENS, List.class);
        autoSkipChars = LiClipseLanguageIO.convertListOfStringsToArrayOfChars(list, AUTO_SKIP_PARENS, errorList);
    }

    public boolean customizeDocumentCommand(IDocument document, DocumentCommand command, AutoEditStrategyHelper helper,
            String indentString, String contentType) {
        if (!this.scope.equals(contentType)) {
            return false;
        }

        int length = autoSkipChars.length;
        char c = helper.c;
        for (int i = 0; i < length; i++) {
            if (c == autoSkipChars[i]) {
                helper.handleCloseParens(document, command, c);
                return true;
            }
        }

        return false;
    }

}
