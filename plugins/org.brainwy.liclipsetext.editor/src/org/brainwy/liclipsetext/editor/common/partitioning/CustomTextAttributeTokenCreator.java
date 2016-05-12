/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;

public class CustomTextAttributeTokenCreator implements ITokenCreator {

    private IColorCache colorManager;
    private LiClipseLanguage language;

    /**
     * Just for subclasses.
     */
    protected CustomTextAttributeTokenCreator() {
    }

    public CustomTextAttributeTokenCreator(IColorCache colorManager, LiClipseLanguage language) {
        this.colorManager = colorManager;
        this.language = language;
    }

    public void registerContentTypeToken(ContentTypeToken token) {
        colorManager.registerContentTypeToken(token, language);
    }

}