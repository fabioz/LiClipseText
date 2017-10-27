/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.comment;

import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.LanguageConfig;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.core.runtime.IStatus;

public class LanguageComment extends LanguageConfig {

    public LanguageComment(LiClipseLanguage liClipseLanguage) {
        super(liClipseLanguage);
    }

    public enum CommentType {
        COMMENT_TYPE_SINGLE_LINE, COMMENT_TYPE_MULTI_LINE
    }

    //Default is single line
    public CommentType commentType = CommentType.COMMENT_TYPE_SINGLE_LINE;

    //Below is only for single-line

    //Default single-line is //
    public String commentString = "//";

    //Below is only for multi-line

    //Default multi-line comment is /* */
    public String commentStart = "/*";

    public String commentEnd = "*/";

    public String scope;

    @SuppressWarnings({ "rawtypes" })
    public void load(Map map, List<IStatus> errorList) {
        String type = (String) map.remove("type");
        type = type.toLowerCase();

        if ("multiline".equals(type)) {
            commentType = CommentType.COMMENT_TYPE_MULTI_LINE;

            Object start = map.remove("start");
            if (start != null) {
                commentStart = (String) start;
            }
            Object end = map.remove("end");
            if (end != null) {
                commentEnd = (String) end;
            }

            scope = (String) map.remove("scope");

        } else if ("singleline".equals(type)) {
            commentType = CommentType.COMMENT_TYPE_SINGLE_LINE;

            Object sequence = map.remove("sequence");
            if (sequence != null) {
                commentString = (String) sequence;
            }

        } else {
            LiClipseTextEditorPlugin.createWarning("Type not recognized: "
                    + type + " valid: multiLine, singleLine.", errorList);
        }

        if (!map.isEmpty()) {
            LiClipseTextEditorPlugin.createWarning("Fields not treated in comment: "
                    + StringUtils.join(", ", map.keySet()), errorList);
        }
    }

}
