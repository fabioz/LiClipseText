/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.theme;

/**
 * A single theme setting.
 * 
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/main.ts
 */
public interface IRawThemeSetting {

	String getName();

	Object getScope();

	IThemeSetting getSetting();

}
