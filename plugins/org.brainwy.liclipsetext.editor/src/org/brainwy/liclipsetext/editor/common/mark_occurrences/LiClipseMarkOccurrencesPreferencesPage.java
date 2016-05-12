/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.mark_occurrences;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.brainwy.liclipsetext.shared_ui.field_editors.LinkFieldEditor;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public class LiClipseMarkOccurrencesPreferencesPage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public static final String USE_MARK_OCCURRENCES = LiClipseTextPreferences.USE_MARK_OCCURRENCES;

    public LiClipseMarkOccurrencesPreferencesPage() {
        super(FLAT);
        IPreferenceStore prefs = LiClipseTextEditorPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(prefs);
    }

    @Override
    protected void createFieldEditors() {
        Composite p = getFieldEditorParent();

        addField(new BooleanFieldEditor(USE_MARK_OCCURRENCES, "Mark Occurrences?", p));

        LinkFieldEditor colorsAndFontsLinkFieldEditor = new LinkFieldEditor("UNUSED",
                "Color of the occurences may be changed at\n" + "<a>Annotations</a>: Occurrences (LiClipse)", p,
                new SelectionListener() {

                    public void widgetSelected(SelectionEvent e) {
                        String id = "org.eclipse.ui.editors.preferencePages.Annotations";
                        IWorkbenchPreferenceContainer workbenchPreferenceContainer = ((IWorkbenchPreferenceContainer) getContainer());
                        workbenchPreferenceContainer.openPage(id, null);
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {
                    }
                });
        colorsAndFontsLinkFieldEditor.getLinkControl(p);
    }

    public void init(IWorkbench workbench) {
    }

    public static boolean useMarkOccurrences() {
        return LiClipseTextEditorPlugin.getDefault().getPreferenceStore().getBoolean(USE_MARK_OCCURRENCES);
    }

}
