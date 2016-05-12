package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.SkipLineRule;
import org.brainwy.liclipsetext.shared_core.partitioner.CustomRuleBasedPartitionScanner;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.Token;

public class SkipLineRuleTest extends TestCase {

    public void testSkipLineRule() throws Exception {
        SkipLineRule rule = new SkipLineRule(new Token("ok"));
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "st\nrr";
        scanner.setRange(new Document(str), 0, str.length());
        assertEquals(0, scanner.getMark());

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(3, scanner.getMark());

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(5, scanner.getMark());

        assertTrue(!rule.evaluate(scanner).isUndefined()); //Match EOF (i.e.: if we get to the end of the file, consider it as a new line).
        assertTrue(!rule.evaluate(scanner).isUndefined()); //Match EOF (i.e.: if we get to the end of the file, consider it as a new line).
    }
}
