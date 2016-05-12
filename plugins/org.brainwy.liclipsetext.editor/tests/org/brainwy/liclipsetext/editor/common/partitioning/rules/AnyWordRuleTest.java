package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.AnyWordRule;
import org.brainwy.liclipsetext.shared_core.partitioner.CustomRuleBasedPartitionScanner;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.Token;

public class AnyWordRuleTest extends TestCase {

    public void testAnyWordRule() throws Exception {
        AnyWordRule rule = new AnyWordRule(new Token("ok"));
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "st\nrr";
        scanner.setRange(new Document(str), 0, str.length());
        assertEquals(0, scanner.getMark());

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(2, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined());
        scanner.read(); //skip the new line

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(5, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined()); //Don't Match EOF
    }

    public void testAnyWordRuleExcept() throws Exception {
        List<String> except = new ArrayList<>();
        String additional = "";
        boolean mustStartUppercase = false;
        AnyWordRule rule = new AnyWordRule(new Token("ok"), except, additional, mustStartUppercase);
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "st\nrr";
        scanner.setRange(new Document(str), 0, str.length());
        assertEquals(0, scanner.getMark());

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(2, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined());
        scanner.read(); //skip the new line

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(5, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined()); //Don't Match EOF
    }

    public void testAnyWordRuleExcept2() throws Exception {
        List<String> except = new ArrayList<>();
        except.add("st");
        String additional = "";
        boolean mustStartUppercase = false;
        AnyWordRule rule = new AnyWordRule(new Token("ok"), except, additional, mustStartUppercase);
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "st\nrr";
        scanner.setRange(new Document(str), 0, str.length());
        assertEquals(0, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined());
        assertEquals(1, scanner.getMark()); //fishy... why has it walked? (should remain fixed?)
        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(2, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined());
        scanner.read(); //skip the new line

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(5, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined()); //Don't Match EOF
    }

    public void testAnyWordRuleAdditional() throws Exception {
        List<String> except = new ArrayList<>();
        String additional = ".";
        boolean mustStartUppercase = false;
        AnyWordRule rule = new AnyWordRule(new Token("ok"), except, additional, mustStartUppercase);
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "a.\nrr";
        scanner.setRange(new Document(str), 0, str.length());
        assertEquals(0, scanner.getMark());

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(2, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined());
        scanner.read(); //skip the new line

        assertFalse(rule.evaluate(scanner).isUndefined());
        assertEquals(5, scanner.getMark());

        assertTrue(rule.evaluate(scanner).isUndefined()); //Don't Match EOF
    }
}
