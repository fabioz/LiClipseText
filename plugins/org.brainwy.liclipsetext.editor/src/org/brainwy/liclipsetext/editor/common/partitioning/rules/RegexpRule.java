/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.Map;

import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;

/**
 * Close to a tmMatchRule, but with less features (i.e.: no sub-rules nor captures).
 */
public class RegexpRule implements ILiClipsePredicateRule, IChangeTokenRule {

    private TmMatchRule tmMatchRule;

    public RegexpRule(String regexp, IToken token) {
        tmMatchRule = new TmMatchRule(regexp, token, (Map<Object, Object>) null);
    }

    public IToken evaluate(ICharacterScanner scanner) {
        return tmMatchRule.evaluate(scanner);
    }

    public IToken getSuccessToken() {
        return tmMatchRule.getSuccessToken();
    }

    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        return tmMatchRule.evaluate(scanner, resume);
    }

    public void setToken(IToken token) {
        tmMatchRule.setToken(token);
    }

    @Override
    public String toString() {
        return "RegexpRule";
    }
}
