package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PartitioningTmHtmlTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtils.clearLanguagesManager();
    }

    public void testTmHtmlPartitioning() throws Exception {
        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "html.tmbundle"), "html.tmbundle-master/Syntaxes/HTML.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);

        Document document = new Document(""
                + "<html>test <a href=\"foo\">rara</a></html>");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("meta.tag.structure.any.html:0:6",
                "__dftl_partition_content_type:6:11",
                "meta.tag.inline.any.html:11:25",
                "__dftl_partition_content_type:25:29",
                "meta.tag.inline.any.html:29:33",
                "meta.tag.structure.any.html:33:"), partition);

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<String, ICustomPartitionTokenScanner>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                "meta.tag.inline.any.html", contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 11, 25);
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("punctuation.definition.tag.begin.html:11:1",
                "entity.name.tag.inline.any.html:12:1",
                "meta.tag.inline.any.html:13:1",
                "entity.other.attribute-name.html:14:4",
                "meta.tag.inline.any.html:18:1",
                "punctuation.definition.string.begin.html:19:1",
                "string.quoted.double.html:20:3",
                "punctuation.definition.string.end.html:23:1",
                "punctuation.definition.tag.end.html:24:1",
                "meta.tag.inline.any.html:25:4",
                "punctuation.definition.tag.begin.html:29:2",
                "entity.name.tag.inline.any.html:31:1",
                "punctuation.definition.tag.end.html:32:1",
                "punctuation.definition.tag.begin.html:33:2",
                "entity.name.tag.inline.any.html:35:4",
                "punctuation.definition.tag.end.html:39:1"), scan);
    }

    //    public void testTmHtmlPartitioning3() throws Exception {
    //        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
    //                new File(TestUtils.getLanguagesDir(), "htmml.tmbundle"), "html.tmbundle-master/Syntaxes/HTML.plist");
    //
    //        LiClipseLanguage language = metadata.loadLanguage(true);
    //
    //        Document document = new Document(""
    //                + "<html>test <a href=\"foo\">rara</a></html>");
    //        language.connect(document);
    //        String partition = TestUtils.partition(document);
    //        assertEquals(TestUtils.listToExpected(), partition);
    //
    //        Map<String, IFullScanner2> contentTypeToScanner = new HashMap<String, IFullScanner2>();
    //        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
    //                .getDocumentPartitioner();
    //        IFullScanner2 scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
    //                "PARTITION_NAME", contentTypeToScanner, language);
    //        ScannerRange range = scannerForContentType.createScannerRange(document, 49, document.getLength() - 49);
    //        String scan = TestUtils.scan(scannerForContentType, false);
    //        assertEquals(TestUtils.listToExpected(""), scan);
    //    }
}
