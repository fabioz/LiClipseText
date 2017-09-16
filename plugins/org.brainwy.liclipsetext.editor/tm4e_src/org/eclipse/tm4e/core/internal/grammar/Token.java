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
package org.eclipse.tm4e.core.internal.grammar;

import java.util.List;

import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.internal.oniguruma.OnigString;

class Token implements IToken {

	private int startIndex;

	private int endIndex;

	private List<String> scopes;

	public Token(int startIndex, int endIndex, List<String> scopes) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.scopes = scopes;
	}

	@Override
	public int getStartIndex() {
		return startIndex;
	}

	@Override
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	@Override
	public int getEndIndex() {
		return endIndex;
	}

	@Override
	public List<String> getScopes() {
		return scopes;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("{startIndex: ");
		s.append(startIndex);
		s.append(", endIndex: ");
		s.append(endIndex);
		s.append(", scopes: ");
		s.append(scopes);
		s.append("}");
		return s.toString();
	}

	@Override
	public IToken toUtf16(OnigString onigLineText) {
		return new Token(onigLineText.convertUtf8OffsetToUtf16(this.startIndex),
				onigLineText.convertUtf8OffsetToUtf16(this.endIndex), scopes);
	}
}
