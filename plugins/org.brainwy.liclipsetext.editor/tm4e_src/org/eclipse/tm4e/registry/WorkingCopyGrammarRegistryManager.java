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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tm4e.registry.internal.AbstractGrammarRegistryManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Working copy of grammar registry manager.
 *
 */
public class WorkingCopyGrammarRegistryManager extends AbstractGrammarRegistryManager {

	private final IGrammarRegistryManager manager;

	private List<IGrammarDefinition> added;

	private List<IGrammarDefinition> removed;

	public WorkingCopyGrammarRegistryManager(IGrammarRegistryManager manager) {
		this.manager = manager;
		load();
	}

	@Override
	public void load() {
		// Copy grammar definitions
		IGrammarDefinition[] definitions = manager.getDefinitions();
		for (IGrammarDefinition definition : definitions) {
			super.registerGrammarDefinition(definition);
			// Copy binding scope/content types
			String scopeName = definition.getScopeName();
			String[] contentTypes = manager.getContentTypesForScope(scopeName);
			if (contentTypes != null) {
				for (String contentTypeId : contentTypes) {
					super.registerContentTypeBinding(contentTypeId, scopeName);
				}
			}
			// Copy injection
			Collection<String> injections = manager.getInjections(scopeName);
			if (injections != null) {
				for (String injectFrom : injections) {
					super.registerInjection(injectFrom, scopeName);
				}
			}
		}
	}

	@Override
	public void addGrammarDefinition(IGrammarDefinition definition) {
		super.addGrammarDefinition(definition);
		if (added == null) {
			added = new ArrayList<>();
		}
		added.add(definition);
	}

	@Override
	public void removeGrammarDefinition(IGrammarDefinition definition) {
		super.removeGrammarDefinition(definition);
		if (added != null && added.contains(definition)) {
			added.remove(definition);
		} else {
			if (removed == null) {
				removed = new ArrayList<>();
			}
			removed.add(definition);
		}
	}

	@Override
	public void save() throws BackingStoreException {
		if (added != null) {
			for (IGrammarDefinition definition : added) {
				manager.addGrammarDefinition(definition);
			}
		}
		if (removed != null) {
			for (IGrammarDefinition definition : removed) {
				manager.removeGrammarDefinition(definition);
			}
		}
		if (added != null || removed != null) {
			manager.save();
		}
	}

}
