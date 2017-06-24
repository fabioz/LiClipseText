/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.rules;

import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;

public interface IRuleWithSubRules {

    /**
     * Creates SubRuleTokens based on the sub-rules from this rule.
     *
     * @param generateSubRuleTokens if false, null means that we have not matched anything and
     * not-null means we matched something (but the len is not relied upon).
     *
     * This is used when we're only interested on making a partition and getting its size (and
     * we're not really interested in knowing which tokens were actually scanned).
     *
     * @return null if we did not have a match -- or a SubRuleToken with the match (note that
     * it may have children if generateSubRuleTokens is true, so, it may have to be post-processed).
     * @throws DocumentTimeStampChangedException
     */
    public SubRuleToken evaluateSubRules(ScannerRange scanner, boolean generateSubRuleTokens)
            throws DocumentTimeStampChangedException;

}
