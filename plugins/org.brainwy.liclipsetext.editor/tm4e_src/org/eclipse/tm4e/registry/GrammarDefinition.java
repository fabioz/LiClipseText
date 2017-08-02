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
package org.eclipse.tm4e.registry;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Grammar definition defined by the "org.eclipse.tm4e.registry.grammars"
 * extension point. Here a sample to register TypeScript TextMate grammar.
 * 
 * <pre>
 * <extension
         point="org.eclipse.tm4e.registry.grammars">
      <grammar
      		scopeName="source.ts"
            path="./syntaxes/TypeScript.tmLanguage.json" >
      </grammar>
   </extension>
 * </pre>
 *
 */
public class GrammarDefinition extends TMResource implements IGrammarDefinition {

	private String scopeName;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public GrammarDefinition() {
		super();
	}

	/**
	 * Constructor for extension point.
	 * 
	 * @param element
	 */
	public GrammarDefinition(String scopeName, String path) {
		super(path);
		this.scopeName = scopeName;
	}

	public GrammarDefinition(IConfigurationElement ce) {
		super(ce);
		this.scopeName = ce.getAttribute(XMLConstants.SCOPE_NAME_ATTR);
	}

	@Override
	public String getScopeName() {
		return scopeName;
	}
}
