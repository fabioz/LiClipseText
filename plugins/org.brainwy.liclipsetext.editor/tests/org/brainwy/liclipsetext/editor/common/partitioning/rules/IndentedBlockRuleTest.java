package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.AnyWordRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.IndentedBlockRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.OneOrMoreSpacesRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.SequenceRule;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class IndentedBlockRuleTest extends TestCase {

    public void testIndentedBlockRule() throws Exception {
        IndentedBlockRule rule = new IndentedBlockRule("::", new Token("str"), null, 0);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = "::\n"
                + "  aa";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertEquals("str", evaluate.getData());
        assertEquals(-1, range.read());

    }

    public void testIndentedBlockRuleFinishes() throws Exception {
        IndentedBlockRule rule = new IndentedBlockRule("::", new Token("str"), null, 0);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = "::\n"
                + "  aa\n"
                + "\n"
                + "bar";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertEquals("str", evaluate.getData());
        assertEquals('b', (char) range.read());
    }

    public void testIndentedBlockRuleFinishesWindowsNewLine() throws Exception {
        IndentedBlockRule rule = new IndentedBlockRule("::", new Token("str"), null, 0);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = "::\r\n"
                + "  aa\r\n"
                + "\r\n"
                + "bar";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertEquals("str", evaluate.getData());
        assertEquals('b', (char) range.read());
    }

    public void testIndentedBlockRuleDontConsiderLinesWithWhitespaces() throws Exception {
        IndentedBlockRule rule = new IndentedBlockRule("::", new Token("str"), null, 0);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = "::\n"
                + "\n"
                + "  aa";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertEquals("str", evaluate.getData());
        assertEquals(-1, range.read());
    }

    public void testIndentedBlockRuleWithNoColRestriction() throws Exception {
        IndentedBlockRule rule = new IndentedBlockRule("::", new Token("str"), null, -1);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = "tt::\n"
                + "  aa";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        assertTrue(rule.evaluate(range).isUndefined());

        range.read();
        assertTrue(rule.evaluate(range).isUndefined());

        range.read();
        IToken evaluate = rule.evaluate(range);
        assertEquals("str", evaluate.getData());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testIndentedBlockRule2() throws Exception {
        List additionalRules = new ArrayList();
        additionalRules.add(new OneOrMoreSpacesRule(new Token("a")));
        additionalRules.add(new SequenceRule("[", new Token("a")));
        additionalRules.add(new AnyWordRule(new Token("b")));
        additionalRules.add(new SequenceRule("]", new Token("c")));

        IndentedBlockRule rule = new IndentedBlockRule("..", new Token("str"), additionalRules, 0);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = ".. [test]\n"
                + "  aa";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertEquals("str", evaluate.getData());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testIndentedBlockRuleNoMatch() throws Exception {
        List additionalRules = new ArrayList();
        additionalRules.add(new OneOrMoreSpacesRule(new Token("a")));
        additionalRules.add(new SequenceRule("[", new Token("a")));
        additionalRules.add(new AnyWordRule(new Token("b")));
        additionalRules.add(new SequenceRule("]", new Token("c")));

        IndentedBlockRule rule = new IndentedBlockRule("..", new Token("str"), additionalRules, 0);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = ".. [test\n"
                + "  aa";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertTrue(evaluate.isUndefined());
    }

}
