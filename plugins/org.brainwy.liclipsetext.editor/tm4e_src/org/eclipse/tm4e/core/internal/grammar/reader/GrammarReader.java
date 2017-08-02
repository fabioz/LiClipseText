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
package org.eclipse.tm4e.core.internal.grammar.reader;

import java.io.InputStream;

import org.eclipse.tm4e.core.internal.parser.json.JSONPListParser;
import org.eclipse.tm4e.core.internal.parser.xml.XMLPListParser;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;

/**
 * TextMate Grammar reader utilities.
 *
 */
public class GrammarReader {

	public final static IGrammarParser XML_PARSER = new IGrammarParser() {

		private XMLPListParser<IRawGrammar> parser = new XMLPListParser<IRawGrammar>(false);

		@Override
		public IRawGrammar parse(InputStream contents) throws Exception {
			return parser.parse(contents);
		}
	};

	public final static IGrammarParser JSON_PARSER = new IGrammarParser() {

		private JSONPListParser<IRawGrammar> parser = new JSONPListParser<IRawGrammar>(false);

		@Override
		public IRawGrammar parse(InputStream contents) throws Exception {
			return parser.parse(contents);
		}
	};
	
	public static IRawGrammar readGrammarSync(String filePath, InputStream in) throws Exception {
		SyncGrammarReader reader = new SyncGrammarReader(in, getGrammarParser(filePath));
		return reader.load();
	}

	private static IGrammarParser getGrammarParser(String filePath) {
		if (filePath.endsWith(".json")) {
			return JSON_PARSER;
		}
		return XML_PARSER;
	}
}
