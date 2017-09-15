package org.brainwy.liclipsetext.editor.preferences;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.shared_ui.word_boundaries.SubWordPreferences;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LiClipseWordNavigationPreferencesPage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public LiClipseWordNavigationPreferencesPage() {
        super(GRID);
        setPreferenceStore(LiClipseTextEditorPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        Composite p = getFieldEditorParent();

        addField(new RadioGroupFieldEditor(SubWordPreferences.WORD_NAVIGATION_STYLE, "Word Navigation Style", 2,
                new String[][] {
                        { "&SubWord", SubWordPreferences.WORD_NAVIGATION_STYLE_SUBWORD },
                        { "&Native", SubWordPreferences.WORD_NAVIGATION_STYLE_NATIVE },
                }, p));

    }

    @Override
    public void init(IWorkbench workbench) {
    }

}
