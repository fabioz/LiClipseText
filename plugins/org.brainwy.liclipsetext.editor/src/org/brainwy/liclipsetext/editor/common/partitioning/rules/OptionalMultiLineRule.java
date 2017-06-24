/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;

public class OptionalMultiLineRule extends MultiLineRule {

    public OptionalMultiLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter) {
        super(startSequence, endSequence, token, escapeCharacter);
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {
        IToken ret = super.evaluate(scanner, resume);
        if (ret.isUndefined()) {
            return this.fToken; //never return undefined as it's optional!
        }
        return ret;
    }

    @Override
    public String toString() {
        return new FastStringBuffer("OptionalMultiLineRule(", fStartSequence.length + fEndSequence.length + 30)
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
