package org.eclipse.tm4e.core.internal.theme;

import java.util.Map;

import org.eclipse.tm4e.core.internal.parser.PListObject;

public class PListTheme extends PListObject {

	public PListTheme(PListObject parent, boolean valueAsArray) {
		super(parent, valueAsArray);
	}

	@Override
	protected Map<String, Object> createRaw() {
		return new ThemeRaw();
	}

}
