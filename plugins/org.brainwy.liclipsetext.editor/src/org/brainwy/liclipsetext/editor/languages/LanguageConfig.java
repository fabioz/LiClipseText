/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.lang.ref.WeakReference;
import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.eclipse.core.runtime.IStatus;

public class LanguageConfig {
    protected WeakReference<LiClipseLanguage> liClipseLanguage;

    public LanguageConfig(LiClipseLanguage liClipseLanguage) {
        this.liClipseLanguage = new WeakReference<LiClipseLanguage>(liClipseLanguage);
    }

    protected Integer asInt(Object obj, Integer defaultInt, List<IStatus> errorList) {
        if (obj == null) {
            return defaultInt;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof String) {
            String string = (String) obj;
            return Integer.parseInt(string);
        }
        LiClipseTextEditorPlugin.createWarning("Expected: " + obj + " to be an int", errorList);
        return defaultInt;
    }

}
