/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers.extensions;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;

public class BaseLiClipseHandler implements ILiClipseHandler {

    private String languageName;

    public BaseLiClipseHandler(String languageName) {
        this.languageName = languageName.toLowerCase();
    }

    public boolean canHandle(LiClipseLanguage language) {
        if (language != null && language.name != null) {
            return language.name.toLowerCase().equals(languageName);
        }
        return false;
    }

}
