/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;

/**
 * When it comes to TextMate rules, we can actually mix them with regular rules, but
 * parsing TM rules, there are some peculiarities because it has to consume sub-rules
 * even to parse top-level rules (so, it's slower -- and more flexible -- when compared
 * to the regular parsing), so, to overcome that, we may end up parsing things differently
 * when dealing with grammars that only have TM rules (i.e.: a single parse should create
 * the inner tokens too and keep it to use later on).
 */
public interface ITextMateRule extends ILiClipsePredicateRule, IRuleWithSubRules {

}
