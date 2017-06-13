/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial code from https://github.com/Microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 *  - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.grammar;

/**
 * Result of the line tokenization2 API.
 *
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/main.ts
 */
public interface ITokenizeLineResult2 {

	/**
	 * The tokens in binary format. Each token occupies two array indices. For
	 * token i: - at offset 2*i => startIndex - at offset 2*i + 1 => metadata
	 *
	 */
	int[] getTokens();

	/**
	 * Returns the `prevState` to be passed on to the next line tokenization.
	 * 
	 * @return the `prevState` to be passed on to the next line tokenization.
	 */
	StackElement getRuleStack();

}
