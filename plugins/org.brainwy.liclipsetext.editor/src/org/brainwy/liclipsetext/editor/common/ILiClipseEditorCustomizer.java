package org.brainwy.liclipsetext.editor.common;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public interface ILiClipseEditorCustomizer {

	IOverviewRuler createOverviewRuler(BaseLiClipseEditor baseLiClipseEditor, ISharedTextColors sharedColors);

	StyledText createTextWidget(LiClipseSourceViewer liClipseSourceViewer, Composite parent, int styles);

	IPreferenceStore getPreferenceStore();

}
