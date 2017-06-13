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
 * Logger API.
 *
 */
public interface ILogger {

	/**
	 * Log lever.
	 *
	 */
	public enum LogLevel {

		INFO, WARN, ERROR;
	}

	ILogger DEFAULT_LOGGER = new AbstractLogger() {

		@Override
		protected void logWarn(String message, Throwable exception) {

		}

		@Override
		protected void logInfo(String message) {

		}

		@Override
		protected void logError(String message, Throwable exception) {

		}
	};

	/**
	 * Returns true if logger is enabled and false otherwise.
	 * 
	 * @return true if logger is enabled and false otherwise.
	 */
	boolean isEnabled();

	/**
	 * Log information message.
	 * 
	 * @param message
	 *            the message of the log.
	 */
	void log(String message);

	/**
	 * Log error message.
	 * 
	 * @param message
	 *            the message of the log.
	 * @param exception
	 */
	void log(String message, Throwable exception);

	/**
	 * Log message.
	 * 
	 * @param message
	 *            the message of the log.
	 * @param exception
	 *            error or null.
	 * @param level
	 *            the level of the log.
	 */
	void log(String message, Throwable exception, LogLevel level);

}
