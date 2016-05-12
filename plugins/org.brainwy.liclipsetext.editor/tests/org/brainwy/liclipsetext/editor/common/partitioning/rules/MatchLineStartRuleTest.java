package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.MatchLineStartRule;
import org.brainwy.liclipsetext.shared_core.partitioner.CustomRuleBasedPartitionScanner;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.Token;

public class MatchLineStartRuleTest extends TestCase {

    public void testMatchLineStartRule() throws Exception {
        MatchLineStartRule rule = new MatchLineStartRule(new Token("ok"));
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "st\nr";
        scanner.setRange(new Document(str), 0, str.length());
        assertFalse(rule.evaluate(scanner).isUndefined());

        scanner.read(); //s
        assertTrue(rule.evaluate(scanner).isUndefined());

        scanner.read(); //t
        assertTrue(rule.evaluate(scanner).isUndefined());

        scanner.read(); //\n
        assertFalse(rule.evaluate(scanner).isUndefined());

        scanner.read(); //r
        assertTrue(rule.evaluate(scanner).isUndefined());
    }
}
