package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class PartitioningMarkdownTest extends TestCase {

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

    public void testPartitioningMarkdown() throws Exception {
        String txt = ""
                + "aaa\n"
                + ""
                + "";

        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("markdown_test.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        String scan = TestUtils.scan(partitioner.getScanner(), document);
        assertEquals(TestUtils.listToExpected("null:0:4"), scan);

        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:");

        document.replace(txt.length(), 0, "---");
        TestUtils.checkPartitions(document, "title:0:");
        txt = document.get();

        document.replace(txt.length() - 3, 3, "");
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:");

        //
        //        document.replace(txt.length() - " ''' ".length() + 1, 0, "j");
        //        TestUtils.checkPartitions(document, "singleQuotedString:0:7",
        //                "__dftl_partition_content_type:7:10",
        //                "singleQuotedString:10:18",
        //                "__dftl_partition_content_type:18:20",
        //                "singleQuotedString:20:23",
        //                "__dftl_partition_content_type:23:24",
        //                "singleQuotedMultiLineString:24:35",
        //                "__dftl_partition_content_type:35:");

    }

    public void testPartitioningMarkdown2() throws Exception {
        String txt = ""
                + "aaa\n"
                + "---\n"
                + "\n"
                + "aaa\n"
                + "---\n"
                + "";

        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("markdown_test.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        String scan = TestUtils.scan(partitioner.getScanner(), document);
        String initial = document.get();
        assertEquals(TestUtils.listToExpected("title:0:7",
                "null:7:2",
                "title:9:7",
                "null:16:1"), scan);

        TestUtils.checkPartitions(document, "title:0:7",
                "__dftl_partition_content_type:7:9",
                "title:9:16",
                "__dftl_partition_content_type:16:");

        document.replace(5, 2, "");
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:7",
                "title:7:14",
                "__dftl_partition_content_type:14:");

        document.replace(5, 0, "--");
        assertEquals(initial, document.get());

        TestUtils.checkPartitions(document, "title:0:7",
                "__dftl_partition_content_type:7:9",
                "title:9:16",
                "__dftl_partition_content_type:16:");

    }

    public void testPartitionerAndColoringMarkdown() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("markdown_test.liclipse");
        String txt = "Title\n------------\n\nSomething else";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("title:0:18",
                "__dftl_partition_content_type:18:"), partition);

        //        LiClipsePartitionScanner scanner = TestUtils.createScanner("css.liclipse", "cssBlock");
        //
        //        //scan only the cssBlock partition.
        //        TestUtils.checkScan(new Document(txt.substring(1)), scanner, "bracket:0:1",
        //                "class:1:10",
        //                "null:11:1",
        //                "singleQuotedString:12:5",
        //                "null:17:1",
        //                "bracket:18:1");
    }
    //
    //    public void testPartitionerAndColoringCss2() throws Exception {
    //        LiClipseLanguage language = TestUtils.loadLanguageFile("css.liclipse");
    //        String txt = "a:hover{-moz-border-top:'str';}"; //TODO: Use regexp for the a:hover, check how to deal with prefixes for the tokens.
    //        txt = txt.toUpperCase();
    //
    //        Document document = new Document(txt);
    //        language.connect(document);
    //        String partition = TestUtils.partition(document);
    //        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:7",
    //                "cssBlock:7:"), partition);
    //
    //        LiClipsePartitionScanner scanner = TestUtils.createScanner("css.liclipse");
    //
    //        //scan only the regular partition.
    //        TestUtils.checkScan(new Document(txt.substring(0, 7)), scanner, "keyword:0:1",
    //                "class:1:6");
    //
    //        //scan only the cssBlock partition.
    //        scanner = TestUtils.createScanner("css.liclipse", "cssBlock");
    //        TestUtils.checkScan(new Document(txt.substring(7)), scanner, "bracket:0:1",
    //                "class:1:15",
    //                "null:16:1",
    //                "singleQuotedString:17:5",
    //                "null:22:1",
    //                "bracket:23:1");
    //    }
    //
    //    public void testLiClipseCssPartitioning() throws Exception {
    //        LiClipseLanguage language = TestUtils.loadLanguageFile("css.liclipse");
    //        Document document = new Document(""
    //                + "a {test:test;}");
    //        language.connect(document);
    //        String partition = TestUtils.partition(document);
    //        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:2",
    //                "cssBlock:2:"), partition);
    //    }

}
