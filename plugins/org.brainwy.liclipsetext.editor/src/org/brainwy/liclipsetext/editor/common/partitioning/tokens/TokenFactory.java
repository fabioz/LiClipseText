/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.tokens;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.DummyToken;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class TokenFactory {

    public static IToken createTokenCopy(IToken successToken, String s) {
        if (successToken instanceof DummyToken) {
            return new DummyToken(s);
        } else if (successToken instanceof TargetLanguageToken) { // Must be before ContentTypeToken.
        	return new TargetLanguageToken(s);
        } else if (successToken instanceof ContentTypeToken) {
            return new ContentTypeToken(s);
        } else if (successToken instanceof Token) { // Must be after all the others
            return new Token(s);
        } else {
            Log.log("Did not expect token: " + successToken.getClass());
            return successToken;
        }
    }

}
