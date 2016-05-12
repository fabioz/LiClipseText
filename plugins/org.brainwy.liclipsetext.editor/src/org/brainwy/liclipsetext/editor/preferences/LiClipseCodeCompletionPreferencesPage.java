/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.preferences;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LiClipseCodeCompletionPreferencesPage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public static final String AUTOCOMPLETE_ON_ALL_CHARS = "AUTOCOMPLETE_ON_ALL_CHARS";
    public static final boolean DEFAULT_AUTOCOMPLETE_ON_ALL_ASCII_CHARS = false;

    public LiClipseCodeCompletionPreferencesPage() {
        super(GRID);
        setPreferenceStore(LiClipseTextEditorPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        Composite p = getFieldEditorParent();

        addField(new BooleanFieldEditor(AUTOCOMPLETE_ON_ALL_CHARS,
                "Request completion on all letter chars and '_'?", p));

    }

    public void init(IWorkbench workbench) {
    }

    public static boolean useAutocompleteOnAllAsciiChars() {
        return LiClipseTextEditorPlugin.getDefault().getPreferenceStore().getBoolean(AUTOCOMPLETE_ON_ALL_CHARS);
    }

}