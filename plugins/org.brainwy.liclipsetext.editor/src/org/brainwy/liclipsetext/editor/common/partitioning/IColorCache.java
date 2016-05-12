/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallbackListener;
import org.eclipse.swt.graphics.Color;

public interface IColorCache {

	boolean isValidScope(String colorName);

	/**
	 * Reload a color (may also be called to reload in a style change).
	 *
	 * The name is something as colorName+"_COLOR" or colorName+"_STYLE".
	 */
	void reloadProperty(String name);

	/**
	 * If it's a color property that changed, reload it.
	 */
	void checkReloadProperty(String property);

	void registerContentTypeToken(ContentTypeToken token, LiClipseLanguage language);

	void dispose();

	void unregisterOnReloadColorsListener(ICallbackListener<String> listener);

	void registerOnReloadColorsListener(ICallbackListener<String> listener);

	Color getColor(String colorName);

}