/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle;

import java.util.ArrayList;
import java.util.List;

public class TmGrammarPart implements ITmLanguagePart {

    private TmLanguageHandler handler;
    private String name;
    private String caption;
    private String shebang;
    private List<String> fileTypes;

    public TmGrammarPart(TmLanguageHandler handler) {
        this.handler = handler;
        this.name = this.handler.getValue(TmLanguageHandler.SCOPE_NAME).toString();
        this.caption = this.handler.getValue(TmLanguageHandler.NAME).toString();

        // Optional
        Object value = this.handler.getValue(TmLanguageHandler.SHEBANG);
        if (value != null) {
            this.shebang = value.toString();
        }

        value = this.handler.getValue(TmLanguageHandler.FILE_TYPES);
        if (value != null) {
            if (value instanceof List) {
                List list = (List) value;
                fileTypes = new ArrayList<String>(list.size());
                for (Object object : list) {
                    fileTypes.add(object.toString());
                }
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public String getCaption() {
        return caption;
    }

    public String getShebang() {
        return shebang;
    }

    public List<String> getFileTypes() {
        return fileTypes;
    }

}