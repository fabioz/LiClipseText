package org.eclipse.tm4e.core.theme;

import java.util.Collection;

public interface IRawTheme {

	String getName();

	Collection<IRawThemeSetting> getSettings();

}
