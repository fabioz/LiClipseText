/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christopher Lenz (cmlenz@gmx.de) - support for line continuation
 *******************************************************************************/
package org.brainwy.liclipsetext.editor.rules;

import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.IToken;

/**
 * A specific configuration of a single line rule
 * whereby the pattern begins with a specific sequence but
 * is only ended by a line delimiter.
 */
public class EndOfLineRule extends PatternRule {

    /**
     * Creates a rule for the given starting sequence
     * which, if detected, will return the specified token.
     *
     * @param start the pattern's start sequence
     * @param token the token to be returned on success
     */
    public EndOfLineRule(String start, IToken token) {
        super(start, null, token, (char) 0, true, true); //no escape char!
    }

    @Override
    public String toString() {
        return new FastStringBuffer("EndOfLineRule(", fStartSequence.length + 20)
                .append("start: ")
                .append(fStartSequence)
                .append(")")
                .toString();
    }

}
