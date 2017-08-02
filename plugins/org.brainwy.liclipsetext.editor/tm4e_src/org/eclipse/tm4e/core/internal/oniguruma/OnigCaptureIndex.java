/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial code from https://github.com/atom/node-oniguruma
 * Initial copyright Copyright (c) 2013 GitHub Inc.
 * Initial license: MIT
 *
 * Contributors:
 *  - GitHub Inc.: Initial code, written in JavaScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
 
package org.eclipse.tm4e.core.internal.oniguruma;

public class OnigCaptureIndex implements IOnigCaptureIndex {

	private final int index;
	private final int start;
	private final int end;

	public OnigCaptureIndex(int index, int start, int end) {
		this.index = index;
		this.start = start >= 0 ? start : 0;
		this.end = end >= 0 ? end : 0;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public int getLength() {
		return end - start;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{\"index\": ");
		result.append(getIndex());
		result.append(", \"start\": ");
		result.append(getStart());
		result.append(", \"end\": ");
		result.append(getEnd());
		result.append(", \"length\": ");
		result.append(getLength());
		result.append("}");
		return result.toString();
	}
}
