package org.eclipse.tm4e.core.internal.grammar;

import java.util.List;

import org.eclipse.tm4e.core.theme.ThemeTrieElementRule;

public class ScopeMetadata {

	public final String scopeName;
	public final int languageId;
	public final int tokenType;
	public final List<ThemeTrieElementRule> themeData;

	public ScopeMetadata(String scopeName, int languageId, int tokenType, List<ThemeTrieElementRule> themeData) {
		this.scopeName = scopeName;
		this.languageId = languageId;
		this.tokenType = tokenType;
		this.themeData = themeData;
	}
}
