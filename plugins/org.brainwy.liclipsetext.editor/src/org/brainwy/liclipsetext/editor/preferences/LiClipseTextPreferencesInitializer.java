/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.preferences;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.shared_ui.word_boundaries.SubWordPreferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.service.prefs.Preferences;

public class LiClipseTextPreferencesInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        Preferences node = new DefaultScope().getNode(LiClipseTextEditorPlugin.PLUGIN_ID);

        for (Object[] o : LiClipseTextPreferences.NAME_COLOR_AND_STYLE) {
            node.put((String) o[0] + "_COLOR", (String) o[1]); //color
            if (o.length > 2) {
                node.putInt((String) o[0] + "_STYLE", (Integer) o[2]); //style
            }
        }
        node.putBoolean(LiClipseTextPreferences.USE_MATCHING_BRACKETS,
                LiClipseTextPreferences.DEFAULT_USE_MATCHING_BRACKETS);

        node.putBoolean(LiClipseTextPreferences.USE_MARK_OCCURRENCES,
                LiClipseTextPreferences.DEFAULT_USE_MARK_OCCURRENCES);

        node.put(SubWordPreferences.WORD_NAVIGATION_STYLE, SubWordPreferences.DEFAULT_WORD_NAVIGATION_STYLE);
    }

}
