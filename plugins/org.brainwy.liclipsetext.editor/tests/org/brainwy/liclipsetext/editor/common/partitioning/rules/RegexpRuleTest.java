package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseContentTypeDefinitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.RegexpRule;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.LiClipseRuleBasedPartitionScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class RegexpRuleTest extends TestCase {

    public void testRegexpRule() throws Exception {
        RegexpRule rule = new RegexpRule("ab(c)?", new Token("str"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseContentTypeDefinitionScanner();
        String str = "abc";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertTrue(!evaluate.isUndefined());
        assertEquals(-1, range.read());
    }

    public void testRegexpRule2() throws Exception {
        RegexpRule rule = new RegexpRule("ab(c)?", new Token("str"));
        ICustomPartitionTokenScanner scanner = new LiClipseContentTypeDefinitionScanner();
        String str = "a\nb\nc\nabf";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertTrue(evaluate.isUndefined());
        assertEquals('a', range.read());

        evaluate = rule.evaluate(range);
        assertTrue(evaluate.isUndefined());
        assertEquals('\n', range.read());

        evaluate = rule.evaluate(range);
        assertTrue(evaluate.isUndefined());
        assertEquals('b', range.read());

        evaluate = rule.evaluate(range);
        assertTrue(evaluate.isUndefined());
        assertEquals('\n', range.read());

        evaluate = rule.evaluate(range);
        assertTrue(evaluate.isUndefined());
        assertEquals('c', range.read());

        evaluate = rule.evaluate(range);
        assertTrue(evaluate.isUndefined());
        assertEquals('\n', range.read());

        evaluate = rule.evaluate(range);
        assertTrue(!evaluate.isUndefined());

        assertEquals('f', range.read());
    }
}
