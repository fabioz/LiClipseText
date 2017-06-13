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

/**
 * XML constants used with the "org.eclipse.tm4e.registry.grammars" extension
 * point.
 *
 */
public class XMLConstants {

	// grammar definition
	public static final String GRAMMAR_ELT = "grammar";
	public static final String NAME_ATTR = "name";
	public static final String SCOPE_NAME_ATTR = "scopeName";
	public static final String PATH_ATTR = "path";

	// scopeNameContentTypeBinding definition
	public static final String SCOPE_NAME_CONTENT_TYPE_BINDING_ELT = "scopeNameContentTypeBinding";
	public static final String CONTENT_TYPE_ID_ATTR = "contentTypeId";

	// injection definition
	public static final String INJECTION_ELT = "injection";
	public static final String INJECT_TO_ATTR = "injectTo";

	public static final String ID_ATTR = "id";

}
