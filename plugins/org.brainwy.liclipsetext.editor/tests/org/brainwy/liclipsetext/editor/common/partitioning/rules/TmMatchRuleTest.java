package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseContentTypeDefinitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class TmMatchRuleTest extends TestCase {

    public void testTmMatchRule() throws Exception {
        Map<Object, Object> captures = new HashMap<>();
        captures.put(1, new Token("capture"));

        TmMatchRule rule = new TmMatchRule("a(b)(c)?", new Token("str"), captures);
        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "abc";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertTrue(!evaluate.isUndefined());
        assertEquals(-1, range.read());
    }

    public void testTmMatchRuleSubTokens() throws Exception {
        Map<Object, Object> captures = new HashMap<>();
        captures.put(1, new Token("capture"));

        TmMatchRule rule = new TmMatchRule("a(b)(c)?", new Token("str"), captures);
        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "abc";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        List<SubRuleToken> subTokens = rule.evaluateSubRules(range, true).flatten();
        assertEquals(3, subTokens.size());
        assertEquals(
                "["
                        + "SubRuleToken[str offset: 0 len: 1], "
                        + "SubRuleToken[capture offset: 1 len: 1], "
                        + "SubRuleToken[str offset: 2 len: 1]"
                        + "]",
                (subTokens.toString()));
    }

    public void testTmMatchRuleSubTokens2() throws Exception {
        Map<Object, Object> captures = new HashMap<>();
        captures.put(1, new Token("capture"));
        captures.put(3, new Token("capture2"));

        TmMatchRule rule = new TmMatchRule("a(b)(.)(.)", new Token("str"), captures);
        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "abáá";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        List<SubRuleToken> subTokens = rule.evaluateSubRules(range, true).flatten();
        assertEquals(4, subTokens.size());
        assertEquals(
                "[SubRuleToken[str offset: 0 len: 1], SubRuleToken[capture offset: 1 len: 1], SubRuleToken[str offset: 2 len: 1], SubRuleToken[capture2 offset: 3 len: 1]]",
                (subTokens.toString()));
    }

    public void testTmMatchRule3() throws Exception {
        Map<Object, Object> captures = new HashMap<>();
        IToken evaluate;
        TmMatchRule rule0 = new TmMatchRule("^\\s*\\b(anchor)\\b", new Token("c0"), captures);
        TmMatchRule rule1 = new TmMatchRule("\\G\\s*\\b(test)\\b", new Token("c1"), captures);

        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule0);
        String str = "anchor test end test";
        ScannerRange range = ((ICustomPartitionTokenScanner) scanner).createScannerRange(new Document(str), 0,
                str.length());
        evaluate = rule0.evaluate(range);
        assertEquals("c0", evaluate.getData());
        assertEquals(6, range.getMark());

        range.setMark(5);
        evaluate = rule1.evaluate(range);
        assertSame(null, evaluate.getData());

        range.setMark(6);
        evaluate = rule1.evaluate(range);
        assertEquals("c1", evaluate.getData());

    }

    public void testTmMatchRule2() throws Exception {
        Map<Object, Object> captures = new HashMap<>();
        captures.put(1, new Token("capture"));

        TmMatchRule rule = new TmMatchRule("ab(c)?", new Token("str"), captures);
        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
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

    public void testMatch() throws Exception {
        Map<Object, Object> captures = new HashMap<>();
        captures.put(1, "a");
        captures.put(2, "b");
        captures.put(3, "c");
        captures.put(4, "d");
        captures.put(5, "e");
        captures.put(6, "f");
        captures.put(7, "g");
        TmMatchRule tmMatchRule = new TmMatchRule(
                "^\\s*(class)\\s+(([.a-zA-Z0-9_:]+(\\s*(<)\\s*[.a-zA-Z0-9_:]+)?)|((<<)\\s*[.a-zA-Z0-9_:]+))",
                new Token("foo"), captures);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = "class ArticlesController < ApplicationController\n";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        List<SubRuleToken> evaluateSubRules = tmMatchRule.evaluateSubRules(range, true).flatten();
        int last = 0;
        FastStringBuffer buffer = new FastStringBuffer();
        for (SubRuleToken subRuleToken : evaluateSubRules) {
            if (subRuleToken.offset < last) {
                fail("The tokens should not overlap.");
            }
            buffer.append(subRuleToken.toString());
            buffer.append('\n');
            last = subRuleToken.offset + subRuleToken.len;
        }
        assertEquals("SubRuleToken[a offset: 0 len: 5]\n" +
                "SubRuleToken[foo offset: 5 len: 1]\n" +
                "SubRuleToken[c offset: 6 len: 18]\n" +
                "SubRuleToken[d offset: 24 len: 1]\n" +
                "SubRuleToken[e offset: 25 len: 1]\n" +
                "SubRuleToken[d offset: 26 len: 22]\n", buffer.toString());
    }

    public void testMatchCaptures() throws Exception {
        ////Test: {1={patterns=[{match=[a-zA-Z0-9_]+, name=entity.other.inherited-class.php}, {match=,, name=punctuation.separator.classes.php}]}, 2={name=entity.other.inherited-class.php}}
        Map<Object, Object> captures = new HashMap<>();
        List<ILiClipsePredicateRule> rules = Arrays.asList((ILiClipsePredicateRule) new AnyWordRule(new Token("any")));
        ILiClipsePredicateRule rule = new MatchWhileAnySubRuleMatches(rules, new Token("inner"));
        captures.put(1, rule);
        TmMatchRule tmMatchRule = new TmMatchRule(
                "a(.*)b",
                new Token("outer"), captures);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner();
        String str = "a cc dd bxxxn";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        List<SubRuleToken> evaluateSubRules = tmMatchRule.evaluateSubRules(range, true).flatten();
        int last = 0;
        FastStringBuffer buffer = new FastStringBuffer();
        for (SubRuleToken subRuleToken : evaluateSubRules) {
            if (subRuleToken.offset < last) {
                fail("The tokens should not overlap.");
            }
            buffer.append(subRuleToken.toString());
            buffer.append('\n');
            last = subRuleToken.offset + subRuleToken.len;
        }
        assertEquals("SubRuleToken[outer offset: 0 len: 2]\n" +
                "SubRuleToken[any offset: 2 len: 2]\n" +
                "SubRuleToken[outer offset: 4 len: 1]\n" +
                "SubRuleToken[any offset: 5 len: 2]\n" +
                "SubRuleToken[outer offset: 7 len: 2]\n", buffer.toString());
    }
}
