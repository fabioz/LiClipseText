package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.ParseHtmlTagHelper;
import org.brainwy.liclipsetext.editor.rules.SubLanguageToken;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;

import junit.framework.TestCase;

public class ParseHtmlTagHelperTest extends TestCase {

    public void testParseHtmlTag() throws Exception {
        ParseHtmlTagHelper helper = new ParseHtmlTagHelper("<script text=\"foo\" attr=\"bar\">");
        assertEquals(""
                + "Tag{\n"
                + "startBracePos: 0\n"
                + "slashPos: -1\n"
                + "startTagName: 1\n"
                + "endTagName: 7\n"
                + "attributes:\n"
                + "HtmlAttribute[\n"
                + "  attr start: 8\n"
                + "  attr end: 12\n"
                + "  attr: text\n"
                + "  value start: 14\n"
                + "  value end: 17\n"
                + "  value: foo\n"
                + "] \n"
                + "HtmlAttribute[\n"
                + "  attr start: 19\n"
                + "  attr end: 23\n"
                + "  attr: attr\n"
                + "  value start: 25\n"
                + "  value end: 28\n"
                + "  value: bar\n"
                + "] \n"
                + "endBranceEnd: 29\n"
                + "}"
                + "", helper.toString());

        List<SubLanguageToken> tokens = helper.generateTokens("this", 5, 50);
        assertEquals(""
                + "SubLanguageToken[this&open_tag offset: 5 len: 1]\n"
                + "SubLanguageToken[this&class offset: 6 len: 6]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 12 len: 1]\n"
                + "SubLanguageToken[this&keyword offset: 13 len: 4]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 17 len: 2]\n"
                + "SubLanguageToken[this&doubleQuotedString offset: 19 len: 3]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 22 len: 2]\n"
                + "SubLanguageToken[this&keyword offset: 24 len: 4]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 28 len: 2]\n"
                + "SubLanguageToken[this&doubleQuotedString offset: 30 len: 3]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 33 len: 1]\n"
                + "SubLanguageToken[this&bracket offset: 34 len: 1]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 35 len: 15]"
                + "", StringUtils.join("\n", tokens));
    }

    public void testParseHtmlTag2() throws Exception {
        ParseHtmlTagHelper helper = new ParseHtmlTagHelper(" <script text=\"fo'<o\" attr='b\">ar'>");
        assertEquals(""
                + "Tag{\n"
                + "startBracePos: 1\n"
                + "slashPos: -1\n"
                + "startTagName: 2\n"
                + "endTagName: 8\n"
                + "attributes:\n"
                + "HtmlAttribute[\n"
                + "  attr start: 9\n"
                + "  attr end: 13\n"
                + "  attr: text\n"
                + "  value start: 15\n"
                + "  value end: 20\n"
                + "  value: fo'<o\n"
                + "] \n"
                + "HtmlAttribute[\n"
                + "  attr start: 22\n"
                + "  attr end: 26\n"
                + "  attr: attr\n"
                + "  value start: 28\n"
                + "  value end: 33\n"
                + "  value: b\">ar\n"
                + "] \n"
                + "endBranceEnd: 34\n"
                + "}"
                + "", helper.toString());
    }

    public void testParseHtmlTag3() throws Exception {
        ParseHtmlTagHelper helper = new ParseHtmlTagHelper(" <script a=\"b\"");
        assertEquals(""
                + "Tag{\n"
                + "startBracePos: 1\n"
                + "slashPos: -1\n"
                + "startTagName: 2\n"
                + "endTagName: 8\n"
                + "attributes:\n"
                + "HtmlAttribute[\n"
                + "  attr start: 9\n"
                + "  attr end: 10\n"
                + "  attr: a\n"
                + "  value start: 12\n"
                + "  value end: 13\n"
                + "  value: b\n"
                + "] \n"
                + "endBranceEnd: -1\n"
                + "}"
                + "", helper.toString());
    }

    public void testParseHtmlTag4() throws Exception {
        ParseHtmlTagHelper helper = new ParseHtmlTagHelper(" <a=\"b\"/>"); //wrong!
        assertEquals(""
                + "Tag{\n"
                + "startBracePos: 1\n"
                + "slashPos: 7\n"
                + "startTagName: 2\n"
                + "endTagName: 7\n"
                + "attributes:\n"
                + "\n"
                + "endBranceEnd: 8\n"
                + "}"
                + "", helper.toString());
    }

    public void testParseHtmlTag5() throws Exception {
        ParseHtmlTagHelper helper = new ParseHtmlTagHelper(" <>"); //wrong!
        assertEquals(""
                + "Tag{\n"
                + "startBracePos: 1\n"
                + "slashPos: -1\n"
                + "startTagName: -1\n"
                + "endTagName: -1\n"
                + "attributes:\n"
                + "\n"
                + "endBranceEnd: 2\n"
                + "}"
                + "", helper.toString());
    }

    public void testParseHtmlTag6() throws Exception {
        ParseHtmlTagHelper helper = new ParseHtmlTagHelper(" </a>");
        assertEquals(""
                + "Tag{\n"
                + "startBracePos: 1\n"
                + "slashPos: 2\n"
                + "startTagName: 3\n"
                + "endTagName: 4\n"
                + "attributes:\n"
                + "\n"
                + "endBranceEnd: 4\n"
                + "}"
                + "", helper.toString());
    }

    public void testParseHtmlTag7() throws Exception {
        ParseHtmlTagHelper helper = new ParseHtmlTagHelper(" a>");
        assertEquals(""
                + "Tag{\n"
                + "startBracePos: -1\n"
                + "slashPos: -1\n"
                + "startTagName: -1\n"
                + "endTagName: -1\n"
                + "attributes:\n"
                + "\n"
                + "endBranceEnd: -1\n"
                + "}"
                + "", helper.toString());
    }
}
