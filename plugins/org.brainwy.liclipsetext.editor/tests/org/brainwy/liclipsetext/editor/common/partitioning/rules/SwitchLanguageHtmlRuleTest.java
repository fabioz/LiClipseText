package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.SwitchLanguageHtmlRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.SwitchLanguageRule;
import org.brainwy.liclipsetext.editor.partitioning.LiClipseRuleBasedPartitionScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.SubLanguageToken;
import org.brainwy.liclipsetext.editor.rules.SwitchLanguageToken;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class SwitchLanguageHtmlRuleTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.startEditorPlugin();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtils.stopEditorPlugin();
    }

    public void testSwitchLanguageHtmlRule() throws Exception {
        Map<String, String> languageAttr = new HashMap<String, String>();
        Map<String, String> typeAttr = new HashMap<String, String>();
        typeAttr.put("text/javascript", "javascript");
        SwitchLanguageHtmlRule rule = new SwitchLanguageHtmlRule(typeAttr, languageAttr, "script", new Token(
                "javascript"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();

        String str = "" +
                "<SCRIPT tYpe='text/javascript'>a=\"\"</script>";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        checkToken(evaluate, "this&open_tag",
                "0:1",
                "this&class",
                "1:6",
                "this&__dftl_partition_content_type",
                "7:1",
                "this&keyword",
                "8:4",
                "this&__dftl_partition_content_type",
                "12:2",
                "this&singleQuotedString",
                "14:15",
                "this&__dftl_partition_content_type",
                "29:1",
                "this&bracket",
                "30:1",
                "javascript&__dftl_partition_content_type",
                "31:2",
                "javascript&doubleQuotedString",
                "33:2",
                "this&close_tag",
                "35:2",
                "this&close_class",
                "37:6",
                "this&bracket",
                "43:1");
    }

    public void testSwitchLanguageHtmlRuleEmpty() throws Exception {
        Map<String, String> languageAttr = new HashMap<String, String>();
        Map<String, String> typeAttr = new HashMap<String, String>();
        typeAttr.put("text/javascript", "javascript");
        SwitchLanguageHtmlRule rule = new SwitchLanguageHtmlRule(typeAttr, languageAttr, "script", new Token(
                "javascript"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();

        String str = "" +
                "<script src=\"lib/vendor/json2.js\"></script>";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        checkToken(evaluate, "this&open_tag",
                "0:1",
                "this&class",
                "1:6",
                "this&__dftl_partition_content_type",
                "7:1",
                "this&keyword",
                "8:3",
                "this&__dftl_partition_content_type",
                "11:2",
                "this&doubleQuotedString",
                "13:19",
                "this&__dftl_partition_content_type",
                "32:1",
                "this&bracket",
                "33:1",
                "this&close_tag",
                "34:2",
                "this&close_class",
                "36:6",
                "this&bracket",
                "42:1");
    }

    public void testSwitchLanguageHtmlRuleUnfinished() throws Exception {
        Map<String, String> languageAttr = new HashMap<String, String>();
        Map<String, String> typeAttr = new HashMap<String, String>();
        typeAttr.put("text/javascript", "javascript");
        SwitchLanguageHtmlRule rule = new SwitchLanguageHtmlRule(typeAttr, languageAttr, "script", new Token(
                "this"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();

        String str = "" +
                "<script type='text/javascript'>a=\"\"";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        checkToken(evaluate, "this&open_tag",
                "0:1",
                "this&class",
                "1:6",
                "this&__dftl_partition_content_type",
                "7:1",
                "this&keyword",
                "8:4",
                "this&__dftl_partition_content_type",
                "12:2",
                "this&singleQuotedString",
                "14:15",
                "this&__dftl_partition_content_type",
                "29:1",
                "this&bracket",
                "30:1",
                "javascript&__dftl_partition_content_type",
                "31:2",
                "javascript&doubleQuotedString",
                "33:2");
    }

    public void testSwitchLanguageHtmlRuleNoMatch() throws Exception {
        Map<String, String> languageAttr = new HashMap<String, String>();
        Map<String, String> typeAttr = new HashMap<String, String>();
        SwitchLanguageHtmlRule rule = new SwitchLanguageHtmlRule(typeAttr, languageAttr, "script", new Token(
                "this"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();

        String str = "" +
                "<script type='text/javascript'>a=\"\"";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        checkToken(evaluate, "this&open_tag",
                "0:1",
                "this&class",
                "1:6",
                "this&__dftl_partition_content_type",
                "7:1",
                "this&keyword",
                "8:4",
                "this&__dftl_partition_content_type",
                "12:2",
                "this&singleQuotedString",
                "14:15",
                "this&__dftl_partition_content_type",
                "29:1",
                "this&bracket",
                "30:1",
                "this&__dftl_partition_content_type",
                "31:4");

    }

    public void checkToken(IToken evaluate, String... expected) {
        assertTrue(evaluate instanceof SwitchLanguageToken);
        SwitchLanguageToken token = (SwitchLanguageToken) evaluate;
        SubLanguageToken[] subTokens = token.subTokens;
        ArrayList<String> found = new ArrayList<String>();
        for (SubLanguageToken subRuleToken : subTokens) {
            found.add((subRuleToken.getFullContentType()));
            found.add((subRuleToken.offset + ":" + subRuleToken.len));
        }
        assertEquals(TestUtils.listToExpected(expected), TestUtils.listToExpected(found));
    }

    public void testSwitchLanguageRuleStartEnd() throws Exception {
        SwitchLanguageRule rule = new SwitchLanguageRule("<%", "%%>", new Token("open_tag"), "javascript");
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();

        String str = "" +
                "<% a=''  %%>";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        checkToken(evaluate, "this&open_tag",
                "0:2",
                "javascript&__dftl_partition_content_type",
                "2:3",
                "javascript&singleQuotedString",
                "5:2",
                "javascript&__dftl_partition_content_type",
                "7:2",
                "this&open_tag",
                "9:3");

    }

    public void testNoSwitchLanguageRuleStartEnd() throws Exception {
        SwitchLanguageRule rule = new SwitchLanguageRule("<%", "%>", new Token("open_tag"), "javascript");
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();

        String str = "" +
                "<% a1 %>\n" +
                "\n" +
                "<% a2 %>\n" +
                "";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        IToken evaluate = rule.evaluate(range);
        checkToken(evaluate, "this&open_tag",
                "0:2",
                "javascript&__dftl_partition_content_type",
                "2:4",
                "this&open_tag",
                "6:2");
    }

}
