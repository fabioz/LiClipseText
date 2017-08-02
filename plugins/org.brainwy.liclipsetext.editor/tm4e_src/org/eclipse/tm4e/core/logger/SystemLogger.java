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
package org.eclipse.tm4e.core.logger;

/**
 * System out/err logger.
 *
 */
public class SystemLogger extends AbstractLogger {

	public static final ILogger INSTANCE = new SystemLogger();

	protected SystemLogger() {

	}

	@Override
	protected void logInfo(String message) {
		System.out.println(message);
	}

	@Override
	protected void logWarn(String message, Throwable exception) {
		System.out.println(message);
		if (exception != null) {
			exception.printStackTrace();
		}
	}

	@Override
	protected void logError(String message, Throwable exception) {
		System.err.println(message);
		if (exception != null) {
			exception.printStackTrace();
		}
	}

}
