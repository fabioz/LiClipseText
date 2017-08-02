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

import org.joni.Region;

public class OnigResult implements IOnigNextMatchResult {

	private int indexInScanner;
	private final Region region;
	private IOnigCaptureIndex[] captureIndices;

	public OnigResult(Region region, int indexInScanner) {
		this.region = region;
		this.indexInScanner = indexInScanner;
	}

	@Override
	public int getIndex() {
		return indexInScanner;
	}

	public void setIndex(int index) {
		this.indexInScanner = index;
	}

	@Override
	public IOnigCaptureIndex[] getCaptureIndices() {
		if (captureIndices == null) {
			captureIndices = new IOnigCaptureIndex[region.beg.length];
			int captureStart = -1, captureEnd = -1;
			for (int i = 0; i < region.beg.length; i++) {
				captureStart = region.beg[i];
				captureEnd = region.end[i];
				captureIndices[i] = new OnigCaptureIndex(i, captureStart, captureEnd);
			}
		}
		return captureIndices;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{\n");
		result.append("  \"index\": ");
		result.append(getIndex());
		result.append(",\n");
		result.append("  \"captureIndices\": [\n");
		int i = 0;
		for (IOnigCaptureIndex captureIndex : getCaptureIndices()) {
			if (i > 0) {
				result.append(",\n");
			}
			result.append("    ");
			result.append(captureIndex);
			i++;
		}
		result.append("\n");
		result.append("  ]\n");
		result.append("}");
		return result.toString();
	}

	public int LocationAt(int index) {
		if (region.beg.length > 0) {
			return region.beg[0] + index;
		}
		return 0;
	}

	public int count() {
		return region.beg.length;
	}

}
