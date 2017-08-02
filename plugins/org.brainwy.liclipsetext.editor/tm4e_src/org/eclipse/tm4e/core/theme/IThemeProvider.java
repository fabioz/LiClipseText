package org.eclipse.tm4e.core.theme;

import java.util.List;

public interface IThemeProvider {

	List<ThemeTrieElementRule> themeMatch(String scopeName);

	ThemeTrieElementRule getDefaults();

}
