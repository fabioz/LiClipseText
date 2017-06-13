package org.eclipse.tm4e.core.internal.grammar.parser;

import java.util.Map;

import org.eclipse.tm4e.core.internal.parser.PListObject;

public class PListGrammar extends PListObject {

	public PListGrammar(PListObject parent, boolean valueAsArray) {
		super(parent, valueAsArray);
	}

	@Override
	protected Map<String, Object> createRaw() {
		return new Raw();
	}

}
