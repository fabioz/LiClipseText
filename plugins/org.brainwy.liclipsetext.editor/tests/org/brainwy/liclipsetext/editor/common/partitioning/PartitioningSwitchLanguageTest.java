package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class PartitioningSwitchLanguageTest extends TestCase {

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

    public void testPartitioningSwitchingLanguage() throws Exception {
        String txt = "<p><script language=\"JavaScript\">\n"
                + "var a = 10;\n"
                + "</script></p>";
        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("html.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        String scan = TestUtils.scan(partitioner.getScanner(), document);
        assertEquals(TestUtils.listToExpected("tag:0:3",
                "javascript:3:52",
                "tag:55:4"), scan);

        checkPartitions(document, "tag:0:3",
                "this&open_tag:3:4",
                "this&class:4:10",
                "this&__dftl_partition_content_type:10:11",
                "this&keyword:11:19",
                "this&__dftl_partition_content_type:19:21",
                "this&doubleQuotedString:21:31",
                "this&__dftl_partition_content_type:31:32",
                "this&bracket:32:33",
                "javascript&__dftl_partition_content_type:33:46",
                "this&close_tag:46:48",
                "this&close_class:48:54",
                "this&bracket:54:55",
                "tag:55:");

        document.replace(txt.length() - "</script></p>".length(), 0, "r=20");
        checkPartitions(document, "tag:0:3",
                "this&open_tag:3:4",
                "this&class:4:10",
                "this&__dftl_partition_content_type:10:11",
                "this&keyword:11:19",
                "this&__dftl_partition_content_type:19:21",
                "this&doubleQuotedString:21:31",
                "this&__dftl_partition_content_type:31:32",
                "this&bracket:32:33",
                "javascript&__dftl_partition_content_type:33:50",
                "this&close_tag:50:52",
                "this&close_class:52:58",
                "this&bracket:58:59",
                "tag:59:");
    }

    public void testPartitioningSwitchingLanguageCss() throws Exception {
        String txt = "<p><script language=\"JavaScript\">\n"
                + "var a = 10;\n"
                + "</script>\n"
                + "<style type='text/css'>p {color:'blue';}</style></p>";
        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("html.liclipse");
        partitioningSetup.connect(document);

        checkPartitions(document, "tag:0:3",
                "this&open_tag:3:4",
                "this&class:4:10",
                "this&__dftl_partition_content_type:10:11",
                "this&keyword:11:19",
                "this&__dftl_partition_content_type:19:21",
                "this&doubleQuotedString:21:31",
                "this&__dftl_partition_content_type:31:32",
                "this&bracket:32:33",
                "javascript&__dftl_partition_content_type:33:46",
                "this&close_tag:46:48",
                "this&close_class:48:54",
                "this&bracket:54:55",
                "__dftl_partition_content_type:55:56",
                "this&open_tag:56:57",
                "this&class:57:62",
                "this&__dftl_partition_content_type:62:63",
                "this&keyword:63:67",
                "this&__dftl_partition_content_type:67:69",
                "this&singleQuotedString:69:77",
                "this&__dftl_partition_content_type:77:78",
                "this&bracket:78:79",
                "source.css&source.css.include.1:79:81",
                "source.css&source.css.3:81:96",
                "this&close_tag:96:98",
                "this&close_class:98:103",
                "this&bracket:103:104",
                "tag:104:");

    }

    private void checkPartitions(IDocument document, String... expected) throws Exception {
        String found = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected(expected), found);
    }

}
