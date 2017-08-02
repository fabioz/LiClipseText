package org.eclipse.tm4e.core.internal.theme;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.tm4e.core.theme.FontStyle;
import org.eclipse.tm4e.core.theme.IRawTheme;
import org.eclipse.tm4e.core.theme.IRawThemeSetting;
import org.eclipse.tm4e.core.theme.IThemeSetting;

public class ThemeRaw extends HashMap<String, Object> implements IRawTheme, IRawThemeSetting, IThemeSetting {

	@Override
	public String getName() {
		return (String) super.get("name");
	}

	@Override
	public Collection<IRawThemeSetting> getSettings() {
		return (Collection<IRawThemeSetting>) super.get("settings");
	}

	@Override
	public Object getScope() {
		return super.get("scope");
	}

	@Override
	public IThemeSetting getSetting() {
		return (IThemeSetting) super.get("settings");
	}

	@Override
	public Object getFontStyle() {
		return super.get("fontStyle");
	}

	@Override
	public String getBackground() {
		return (String) super.get("background");
	}

	@Override
	public String getForeground() {
		return (String) super.get("foreground");
	}

}
