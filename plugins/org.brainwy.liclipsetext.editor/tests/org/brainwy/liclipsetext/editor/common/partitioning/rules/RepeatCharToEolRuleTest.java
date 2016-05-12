package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.Arrays;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.RepeatCharToEolRule;
import org.brainwy.liclipsetext.shared_core.partitioner.CustomRuleBasedPartitionScanner;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.Token;

public class RepeatCharToEolRuleTest extends TestCase {

    public void testRepeatCharToEolRule() throws Exception {
        RepeatCharToEolRule rule = new RepeatCharToEolRule(new Token("ok"), Arrays.asList("-", "*"));
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "------\n**-\n*****";
        scanner.setRange(new Document(str), 0, str.length());
        assertEquals(0, scanner.getMark());

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(6, scanner.getMark());

        assertEquals('\n', scanner.read());
        assertTrue(rule.evaluate(scanner).isUndefined());
        assertEquals(7, scanner.getMark());

        assertEquals('*', scanner.read());
        assertTrue(rule.evaluate(scanner).isUndefined());
        assertEquals('*', scanner.read());
        assertEquals(9, scanner.getMark());
        assertTrue(rule.evaluate(scanner).isUndefined());
        assertEquals('-', scanner.read());
        assertEquals(10, scanner.getMark());
        assertEquals('\n', scanner.read());
        assertEquals(11, scanner.getMark());

        //Read to EOF
        assertFalse(rule.evaluate(scanner).isUndefined());
        assertTrue(rule.evaluate(scanner).isUndefined()); //Do not match EOF
    }
}
