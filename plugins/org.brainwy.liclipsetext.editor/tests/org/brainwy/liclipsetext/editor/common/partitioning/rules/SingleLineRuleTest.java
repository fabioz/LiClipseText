package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.SingleLineRule;
import org.brainwy.liclipsetext.editor.rules.PatternRule;
import org.brainwy.liclipsetext.shared_core.partitioner.CustomRuleBasedPartitionScanner;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SingleLineRuleTest extends TestCase {

    public void testSingleLineRule() throws Exception {
        SingleLineRule rule = new SingleLineRule("'", new Token("str"), '\\', true);
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "a'10'";
        scanner.setRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(scanner);
        assertTrue(evaluate.isUndefined());
        scanner.read(); //pass the a

        evaluate = rule.evaluate(scanner);
        assertEquals("str", evaluate.getData());
    }

    public void testSingleLineRule2() throws Exception {
        SingleLineRule rule = new SingleLineRule("'", new Token("str"), '\\', true);
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "'10'";
        scanner.setRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(scanner);
        assertEquals(evaluate.getData(), "str");
    }

    public void testSingleLineRule3() throws Exception {
        SingleLineRule rule = new SingleLineRule("'", new Token("str"), '\\', true);
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "'10";
        scanner.setRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(scanner);
        assertEquals(evaluate.getData(), "str");
    }

    public void testSingleLineRule4() throws Exception {
        SingleLineRule rule = new SingleLineRule("'", new Token("str"), '\\', true);
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "'10\\\nfoo'";
        scanner.setRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(scanner);
        assertEquals(evaluate.getData(), "str");
        assertEquals(9, scanner.getMark());
    }

    public void testPatternLineRule4() throws Exception {
        PatternRule rule = new PatternRule("'", "'", new Token("str"), '\\', true, true);
        CustomRuleBasedPartitionScanner scanner = new CustomRuleBasedPartitionScanner();
        String str = "'10\\\nfoo'";
        scanner.setRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(scanner);
        assertEquals(evaluate.getData(), "str");
        assertEquals(9, scanner.getMark());
    }
}
