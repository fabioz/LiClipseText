/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.IToken;

public class MultiLineRule extends MultiLineRuleWithSkip {

    public MultiLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter) {
        super(startSequence, endSequence, token, escapeCharacter, null);
    }

    @Override
    public String toString() {
        return new FastStringBuffer("MultiLineRule(", fStartSequence.length + fEndSequence.length + 30)
                .append("start: ")
                .append(fStartSequence)
                .append(" end: ")
                .append(fEndSequence)
                .append(" escapeCharacter: ")
                .append(fEscapeCharacter)
                .append(")")
                .toString();
    }

}