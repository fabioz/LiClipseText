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
package org.eclipse.tm4e.core.internal.types;

import java.util.Collection;

public interface IRawRule {

	Integer getId();

	void setId(Integer id);

	String getInclude();

	void setInclude(String include);

	String getName();

	void setName(String name);

	String getContentName();

	void setContentName(String name);

	String getMatch();

	void setMatch(String match);

	IRawCaptures getCaptures();

	void setCaptures(IRawCaptures captures);

	String getBegin();

	void setBegin(String begin);

	IRawCaptures getBeginCaptures();

	void setBeginCaptures(IRawCaptures beginCaptures);

	String getEnd();

	void setEnd(String end);

	String getWhile();
	
	IRawCaptures getEndCaptures();

	void setEndCaptures(IRawCaptures endCaptures);

	IRawCaptures getWhileCaptures();

	Collection<IRawRule> getPatterns();

	void setPatterns(Collection<IRawRule> patterns);

	IRawRepository getRepository();

	void setRepository(IRawRepository repository);

	boolean isApplyEndPatternLast();

	void setApplyEndPatternLast(boolean applyEndPatternLast);
}
