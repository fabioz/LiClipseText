package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitionerTmCache.Tm4eDocCache;
import org.brainwy.liclipsetext.editor.common.partitioning.tm4e.Tm4ePartitionScanner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.grammar.IGrammar;

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
        final IDocument document = createHtmlWith2CssPartitions();

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

    public void testPartitionerHtmlTmCache2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("html.liclipse");
        final IDocument document = createHtmlWith2CssPartitions();

        language.connect(document);
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        Tm4eDocCache docCache = documentPartitioner.getDocCache();
        DummyTextViewer dummyTextViewer = new DummyTextViewer(document);
        TestUtils.connectPresentationReconciler(dummyTextViewer);
        assertEquals(2, docCache.getCaches().size()); // caches for the respective partitions should be created

        document.replace(("<html>").length(), 0, "foo"); // just add more to the first line
        assertEquals(2, docCache.getCaches().size()); // should've kept both caches

        assertEquals(TestUtils.listToExpected("tag:0:6",
                "__dftl_partition_content_type:6:10",
                "this&open_tag:10:11",
                "this&class:11:16",
                "this&__dftl_partition_content_type:16:17",
                "this&keyword:17:21",
                "this&__dftl_partition_content_type:21:23",
                "this&doubleQuotedString:23:31",
                "this&__dftl_partition_content_type:31:32",
                "this&bracket:32:33",
                "source.css&__dftl_partition_content_type:33:55",
                "this&close_tag:55:57",
                "this&close_class:57:62",
                "this&bracket:62:63",
                "this&open_tag:63:64",
                "this&class:64:69",
                "this&__dftl_partition_content_type:69:70",
                "this&keyword:70:74",
                "this&__dftl_partition_content_type:74:76",
                "this&doubleQuotedString:76:84",
                "this&__dftl_partition_content_type:84:85",
                "this&bracket:85:86",
                "source.css&__dftl_partition_content_type:86:108",
                "this&close_tag:108:110",
                "this&close_class:110:115",
                "this&bracket:115:116",
                "tag:116:"), TestUtils.partition(document));

        Tm4ePartitionScanner scannerForContentType = (Tm4ePartitionScanner) documentPartitioner
                .getTokenScannerForContentType("source.css&__dftl_partition_content_type");
        ScannerRange scannerRange = scannerForContentType.createScannerRange(document, 33, 55 - 33);

        // Not the right API, but let's tokenize it to see if the cache is gotten.
        int lineOfOffset = document.getLineOfOffset(33);
        String lineContentsOfOffset = TextSelectionUtils.getLineContentsOfOffset(document, 33);
        IGrammar grammar = scannerForContentType.getGrammar();
        documentPartitioner.tokenizeLine(33, lineOfOffset, lineContentsOfOffset, grammar, scannerRange);
    }

    private IDocument createHtmlWith2CssPartitions() {
        final IDocument document = new Document("<html>\n"
                + "<style type=\"text/css\"><p { \n" +
                "/*\ncomment\n*/\n" +
                "}\n</style>" + "<style type=\"text/css\"><p { \n" +
                "/*\ncomment\n*/\n" +
                "}\n</style>"
                + "</html>");
        return document;
    }

}
