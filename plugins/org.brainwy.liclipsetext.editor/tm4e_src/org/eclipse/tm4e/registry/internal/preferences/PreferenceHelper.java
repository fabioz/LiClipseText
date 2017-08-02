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
package org.eclipse.tm4e.registry.internal.preferences;

import java.lang.reflect.Type;
import java.util.Collection;

import org.eclipse.tm4e.registry.GrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarDefinition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

/**
 * Helper class load, save preferences with Json format.
 *
 */
public class PreferenceHelper {

	private static final Gson DEFAULT_GSON;

	static {
		DEFAULT_GSON = new GsonBuilder()
				.registerTypeAdapter(IGrammarDefinition.class, new InstanceCreator<GrammarDefinition>() {
					@Override
					public GrammarDefinition createInstance(Type type) {
						return new GrammarDefinition();
					}
				}).create();
	}

	public static IGrammarDefinition[] loadGrammars(String json) {
		return DEFAULT_GSON.fromJson(json, GrammarDefinition[].class);
	}

	public static String toJson(Collection<IGrammarDefinition> definitions) {
		return DEFAULT_GSON.toJson(definitions);
	}

	/*
	 * public static void main(String[] args) { Collection<IGrammarDefinition>
	 * definitions = new ArrayList<>(); definitions.add(new
	 * GrammarDefinition("source.ts", "ts.json", null, null));
	 * 
	 * String json = toJson(definitions); System.err.println(json);
	 * 
	 * IGrammarDefinition[] defs = loadGrammars(json); System.err.println(defs);
	 * }
	 */
}
