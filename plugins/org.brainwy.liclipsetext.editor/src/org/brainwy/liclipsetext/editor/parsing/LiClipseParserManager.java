/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.parsing;

import org.brainwy.liclipsetext.shared_core.editor.IBaseEditor;
import org.brainwy.liclipsetext.shared_core.parsing.BaseParserManager;
import org.brainwy.liclipsetext.shared_core.parsing.IParser;

public class LiClipseParserManager extends BaseParserManager {

    private static LiClipseParserManager manager;

    public static synchronized LiClipseParserManager getParserManager() {
        if (manager == null) {
            manager = new LiClipseParserManager();
        }
        return manager;
    }

    public static synchronized void setParserManager(LiClipseParserManager LiClipseParserManager) {
        manager = LiClipseParserManager;
    }

    private LiClipseParserManager() {

    }

    @Override
    protected IParser createParser(IBaseEditor edit) {
        return new LiClipseParser(edit);
    }

}
