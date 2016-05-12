package org.brainwy.liclipsetext.editor.common.partitioning;

import org.eclipse.jface.preference.IPreferenceStore;

public interface IColorCacheProvider {

	IColorCache createColorCache(IPreferenceStore prefs);

}
