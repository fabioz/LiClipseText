package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

public class PartitioningJinja2Test extends TestCase {

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

    public void testPartitionerAndColoringJinja2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("jinja2.liclipse");
        String txt = ""
                + "<ul>\n"
                + "# for href, caption in [('index.html', 'Index'),\n"
                + "                        ('about.html', 'About')]:\n"
                + "    <li><a href=\"{{ href }}\">{{ caption }}</a></li>\n"
                + "# endfor\n"
                + "</ul>";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("tag:0:4",
                "__dftl_partition_content_type:4:5",
                "line_statement:5:104",
                "__dftl_partition_content_type:104:108",
                "tag:108:133",
                "jinja2_filter:133:146",
                "tag:146:155",
                "__dftl_partition_content_type:155:156",
                "line_statement:156:165",
                "tag:165:"), partition);
    }

    public void testPartitionerAndColoringJinja2a() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("jinja2.liclipse");
        String txt = ""
                + "# for item in seq:\n"
                + "    <li>{{ item|safe }}</li>     ## this comment is ignored\n"
                + "# endfor\n"
                + "";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("line_statement:0:19",
                "__dftl_partition_content_type:19:23",
                "tag:23:27",
                "jinja2_filter:27:42",
                "tag:42:47",
                "__dftl_partition_content_type:47:52",
                "jinja2_comment2:52:79",
                "line_statement:79:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("jinja2.liclipse", "jinja2_filter");

        TestUtils.checkScan(new Document("{{ item|safe }}"), scanner, "bracket:0:1",
                "bracket:1:1",
                "null:2:5",
                "constant:7:1",
                "filter:8:4",
                "null:12:1",
                "bracket:13:1",
                "bracket:14:1");

    }
}
