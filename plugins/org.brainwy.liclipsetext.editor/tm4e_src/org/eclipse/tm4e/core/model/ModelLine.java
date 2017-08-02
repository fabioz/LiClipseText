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
package org.eclipse.tm4e.core.model;

import java.util.List;

public class ModelLine {

	//String text;
	boolean isInvalid;
	TMState state;
	List<TMToken> tokens;

	public ModelLine(/*String text*/) {
		//this.text = text;
	}

	public void resetTokenizationState() {
		this.state = null;
		this.tokens = null;
	}

	public TMState getState() {
		return state;
	}

	public void setState(TMState state) {
		this.state = state;
	}

	public void setTokens(List<TMToken> tokens) {
		this.tokens = tokens;
	}

	public List<TMToken> getTokens() {
		return tokens;
	}
}
