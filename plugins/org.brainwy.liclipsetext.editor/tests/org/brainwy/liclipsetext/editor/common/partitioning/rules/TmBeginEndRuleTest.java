package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseContentTypeDefinitionScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.editor.regexp.CharsRegion;
import org.brainwy.liclipsetext.editor.regexp.RegexpHelper;
import org.brainwy.liclipsetext.editor.regexp.RegexpHelper.ReplaceInfo;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.Document;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;

import junit.framework.TestCase;

public class TmBeginEndRuleTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TmBeginEndRule.expectToUseTmBeginEndRule = true;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TmBeginEndRule.expectToUseTmBeginEndRule = false;
    }

    public void testTmBeginEndRule() throws Exception {
        TmBeginEndRule rule = new TmBeginEndRule("<<(\"|')(\\w+)\\1", "^\\2$", new HashMap<>(), new HashMap<>(), null,
                new Token("str"), null, 0);
        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "<<'ABS'\nfoo\nbar\nABS\n";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertTrue(!evaluate.isUndefined());
        assertEquals(0, range.getTokenOffset());
        assertEquals(str.length() - 1, range.getTokenLength());
        assertEquals('\n', range.read());
        assertEquals(-1, range.read());
    }

    public void testTmBeginEndRuleApplyLast0() throws Exception {
        List<ILiClipsePredicateRule> subRules = new ArrayList<>();
        subRules.add(new TmMatchRule("b", new Token("pattern"), (SortedMap<Integer, IEvalCaptures>) null));

        TmBeginEndRule rule = new TmBeginEndRule("a", "b", new HashMap<>(), new HashMap<>(), subRules,
                new Token("begin_end"), null, 1);

        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "abbbb";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertTrue(!evaluate.isUndefined());
        assertEquals(0, range.getTokenOffset());
        assertEquals(str.length(), range.getTokenLength());
        assertEquals(-1, range.read());
    }

    public void testTmBeginEndRuleApplyLast1() throws Exception {
        List<ILiClipsePredicateRule> subRules = new ArrayList<>();
        subRules.add(new TmMatchRule("b", new Token("pattern"), (SortedMap<Integer, IEvalCaptures>) null));

        TmBeginEndRule rule = new TmBeginEndRule("a", "b", new HashMap<>(), new HashMap<>(), subRules,
                new Token("begin_end"), null, 0);

        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "abbbb";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        assertTrue(!evaluate.isUndefined());
        assertEquals(0, range.getTokenOffset());
        assertEquals(2, range.getTokenLength());
        assertEquals('b', range.read());
    }

    public void testTmBeginEndRuleContent() throws Exception {
        TmBeginEndRule rule = new TmBeginEndRule("<<(\"|')(\\w+)\\1", "^\\2$", new HashMap<>(), new HashMap<>(), null,
                new Token("str"), new Token("internal"), 0);
        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "<<'ABS'\nfoo\nbar\nABS\n";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        List<SubRuleToken> subRules = rule.evaluateSubRules(range, true).flatten();
        assertEquals(
                "[SubRuleToken[str offset: 0 len: 7], SubRuleToken[internal offset: 7 len: 9], SubRuleToken[str offset: 16 len: 3]]",
                subRules.toString());
    }

    public void testLineAsBytes() throws Exception {
        // Note that we convert \r, \n and \r\n to \n in the matching.
        // (and when we do that bytes.getLastCharEquals2LenLineDelimiter() becomes true).
        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner();
        String str = "a\nb\r\nc\rd";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        assertEquals("a\n", RegexpHelper.getUtf8AsString(range.getLineFromLineAsBytes(0).o1.getBytes()));
        Utf8WithCharLen bytes = range.getLineFromLineAsBytes(1).o1;
        String utf8AsString = RegexpHelper.getUtf8AsString(bytes.getBytes());
        assertEquals("b\n", utf8AsString);
        assertTrue(bytes.getLastCharEquals2LenLineDelimiter());

        assertEquals("c\n", RegexpHelper.getUtf8AsString(range.getLineFromLineAsBytes(2).o1.getBytes()));
        assertEquals("d", RegexpHelper.getUtf8AsString(range.getLineFromLineAsBytes(3).o1.getBytes()));
        assertSame(null, range.getLineFromLineAsBytes(4));

        assertEquals("a\n", RegexpHelper.getUtf8AsString(range.getLineFromOffsetAsBytes(0).o1.getBytes()));
        assertEquals("a\n", RegexpHelper.getUtf8AsString(range.getLineFromOffsetAsBytes(1).o1.getBytes()));

        assertEquals("b\n", RegexpHelper.getUtf8AsString(range.getLineFromOffsetAsBytes(2).o1.getBytes()));
        assertEquals("b\n", RegexpHelper.getUtf8AsString(range.getLineFromOffsetAsBytes(3).o1.getBytes()));
        assertEquals("b\n", RegexpHelper.getUtf8AsString(range.getLineFromOffsetAsBytes(4).o1.getBytes()));

        assertEquals("c\n", RegexpHelper.getUtf8AsString(range.getLineFromOffsetAsBytes(5).o1.getBytes()));
        assertEquals("c\n", RegexpHelper.getUtf8AsString(range.getLineFromOffsetAsBytes(6).o1.getBytes()));

        assertEquals("d", RegexpHelper.getUtf8AsString(range.getLineFromOffsetAsBytes(7).o1.getBytes()));
        assertSame(null, range.getLineFromOffsetAsBytes(8));

    }

    public void testTmBeginEndRuleWithPatterns() throws Exception {
        Map<Object, Object> captures = new HashMap<>();
        captures.put(1, new Token("capture"));

        List<ILiClipsePredicateRule> subRules = new ArrayList<ILiClipsePredicateRule>();
        // stop right before a new line
        subRules.add(new TmBeginEndRule("//", "(?=\n)", null, null, subRules, new Token("comment1"), null, 0));

        //?= (lookahead)
        //?! (negative lookahead)
        TmBeginEndRule rule = new TmBeginEndRule("(?=//)", "(?!\\G)", new HashMap<>(), new HashMap<>(), subRules,
                new Token("comment0"), null, 0);
        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "//test\na";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        Tuple<Utf8WithCharLen, Integer> lineFromLineAsBytes = range.getLineFromLineAsBytes(0);
        assertEquals("//test\n", RegexpHelper.getUtf8AsString(lineFromLineAsBytes.o1.getBytes()));

        assertTrue(!evaluate.isUndefined());
        assertEquals(0, range.getTokenOffset());
        assertEquals(str.length() - 1, range.getTokenLength());
        assertEquals('a', range.read());
        assertEquals(-1, range.read());
    }

    public void testReplacingOfPatterns() throws Exception {
        List<ReplaceInfo> replacesMap = RegexpHelper.createReplaces("my\\1\\3foo\\4");
        assertEquals(
                "[Replace[groupId: 1 begin: 2 end:4], Replace[groupId: 3 begin: 4 end:6], Replace[groupId: 4 begin: 9 end:11]]",
                replacesMap.toString());

        Regex regex = RegexpHelper.createRegexp("(a)(b)(c)(d)");
        Utf8WithCharLen bytes = RegexpHelper.getBytes("abcd");
        Matcher matcher = regex.matcher(bytes.getBytes());
        assertEquals(0, matcher.search(0, bytes.getBytesLen(), Option.CAPTURE_GROUP));
        assertEquals("myacfood",
                RegexpHelper.replaceWithMap("my\\1\\3foo\\4", new CharsRegion(matcher, bytes), replacesMap));

        replacesMap = RegexpHelper.createReplaces("my4");
        assertEquals("[]", replacesMap.toString());

        replacesMap = RegexpHelper.createReplaces("\\1");
        assertEquals("[Replace[groupId: 1 begin: 0 end:2]]", replacesMap.toString());
    }

    public void testReplacingOfPatterns2() throws Exception {

        //The last one should not be matched (we want to match \4 not \\4)
        List<ReplaceInfo> replacesMap = RegexpHelper.createReplaces("my\\1\\3foo\\\\4");
        assertEquals("[Replace[groupId: 1 begin: 2 end:4], Replace[groupId: 3 begin: 4 end:6]]",
                replacesMap.toString());

        Regex regex = RegexpHelper.createRegexp("(a)(b)(c)(d)");
        Utf8WithCharLen bytes = RegexpHelper.getBytes("abcd");
        Matcher matcher = regex.matcher(bytes.getBytes());
        assertEquals(0, matcher.search(0, bytes.getBytesLen(), Option.CAPTURE_GROUP));
        assertEquals("myacfoo\\4",
                RegexpHelper.replaceWithMap("my\\1\\3foo\\4", new CharsRegion(matcher, bytes), replacesMap));
    }

    public void testReplacingOfPatterns2a() throws Exception {

        //The last one should not be matched (we want to match \4 not \\4)
        List<ReplaceInfo> replacesMap = RegexpHelper.createReplaces("my\\1\\3f\\1oox");
        assertEquals(
                "[Replace[groupId: 1 begin: 2 end:4], Replace[groupId: 3 begin: 4 end:6], Replace[groupId: 1 begin: 7 end:9]]",
                replacesMap.toString());

        Regex regex = RegexpHelper.createRegexp("(a)(b)(c)(d)");
        Utf8WithCharLen bytes = RegexpHelper.getBytes("abcd");
        Matcher matcher = regex.matcher(bytes.getBytes());
        assertEquals(0, matcher.search(0, bytes.getBytesLen(), Option.CAPTURE_GROUP));
        assertEquals("myacfaoox",
                RegexpHelper.replaceWithMap("my\\1\\3f\\1oox", new CharsRegion(matcher, bytes), replacesMap));
    }

    public void testReplacingOfPatterns3() throws Exception {
        assertEquals("\0bar", RegexpHelper.regexpWithNoSlashGMatch("\\Gbar"));
    }

}
