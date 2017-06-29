package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitionerTmCache.Tm4eDocCache;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class PartitioningHtmlTest extends TestCase {

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

    public void testPartitionerHtml() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("html.liclipse");
        String txt = "<a b=\"<p>no</p>\"></a>";
        txt = txt.toUpperCase();

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("tag:0:"), partition);

    }

    public void testPartitionerHtml2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("html.liclipse");
        String txt = "<a b='/a>u'></a>";
        txt = txt.toUpperCase();

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("tag:0:"), partition);

    }

    public void testPartitionerHtml3() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("html.liclipse");
        String txt = "<pre style=\"bar\">word</pre>";
        txt = txt.toUpperCase();

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("tag:0:17",
                "__dftl_partition_content_type:17:21",
                "tag:21:"), partition);

    }

    public void testPartitionerHtmlTmCache() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("html.liclipse");
        final IDocument document = new Document("<html>\n"
                + "<style type=\"text/css\"><p { \n" +
                "/*\ncomment\n*/\n" +
                "}\n</style>" + "<style type=\"text/css\"><p { \n" +
                "/*\ncomment\n*/\n" +
                "}\n</style>"
                + "</html>");

        language.connect(document);
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("tag:0:6",
                "__dftl_partition_content_type:6:7",
                "this&open_tag:7:8",
                "this&class:8:13",
                "this&__dftl_partition_content_type:13:14",
                "this&keyword:14:18",
                "this&__dftl_partition_content_type:18:20",
                "this&doubleQuotedString:20:28",
                "this&__dftl_partition_content_type:28:29",
                "this&bracket:29:30",
                "source.css&__dftl_partition_content_type:30:52",
                "this&close_tag:52:54",
                "this&close_class:54:59",
                "this&bracket:59:60",
                "this&open_tag:60:61",
                "this&class:61:66",
                "this&__dftl_partition_content_type:66:67",
                "this&keyword:67:71",
                "this&__dftl_partition_content_type:71:73",
                "this&doubleQuotedString:73:81",
                "this&__dftl_partition_content_type:81:82",
                "this&bracket:82:83",
                "source.css&__dftl_partition_content_type:83:105",
                "this&close_tag:105:107",
                "this&close_class:107:112",
                "this&bracket:112:113",
                "tag:113:"), partition);
        Tm4eDocCache docCache = documentPartitioner.getDocCache();
        assertEquals(0, docCache.getCaches().size()); // upon partitioning, no caching is done (only done when scanning)

        DummyTextViewer dummyTextViewer = new DummyTextViewer(document);

        // Upon being connected it does: processDamage(new Region(0, newDocument.getLength()), newDocument);
        TestUtils.connectPresentationReconciler(dummyTextViewer);
        assertEquals(1, dummyTextViewer.appliedPresentations.size());

        assertEquals(2, docCache.getCaches().size()); // caches for the respective partitions should be created

        document.replace(("<html>\n"
                + "<style type=\"text/css\"><p { \n" +
                "/*\ncomment\n*/\n" +
                "}\n</style>").length() - 1, 1, ""); // remove the last > so that it'll stop only on the second now

        partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("tag:0:6",
                "__dftl_partition_content_type:6:7",
                "this&open_tag:7:8",
                "this&class:8:13",
                "this&__dftl_partition_content_type:13:14",
                "this&keyword:14:18",
                "this&__dftl_partition_content_type:18:20",
                "this&doubleQuotedString:20:28",
                "this&__dftl_partition_content_type:28:29",
                "this&bracket:29:30",
                "source.css&__dftl_partition_content_type:30:104",
                "this&close_tag:104:106",
                "this&close_class:106:111",
                "this&bracket:111:112",
                "tag:112:"), partition);

        assertEquals(1, docCache.getCaches().size()); // we should've kept the first and deleted the 2nd
    }

}
