package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.preferences.LiClipseColorsPreferencesPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

public interface IColorCacheProvider {

	IColorCache createColorCache(IPreferenceStore prefs);

	boolean createFieldEditors(LiClipseColorsPreferencesPage liClipseColorsPreferencesPage, Composite parent);

}
