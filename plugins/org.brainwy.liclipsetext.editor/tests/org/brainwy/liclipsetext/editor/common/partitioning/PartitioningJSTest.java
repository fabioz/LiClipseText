package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class PartitioningJSTest extends TestCase {

    public void testPartitioning() throws Exception {
        String txt = ""
                + "var ret = functionstff();"
                + "";

        IDocument document = new Document(txt);
        LiClipseLanguage language = TestUtils.loadLanguageFile("javascript.liclipse");
        language.connect(document);
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:");

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                IDocument.DEFAULT_CONTENT_TYPE, contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("keyword:0:3",
                "null:3:1",
                "foreground:4:3",
                "null:7:1",
                "operator:8:1",
                "null:9:1",
                "foreground:10:12",
                "bracket:22:1",
                "bracket:23:1",
                "operator:24:1"), scan);

    }

    public void testPartitioning2() throws Exception {
        String txt = ""
                + "var ret = function()"
                + "";

        IDocument document = new Document(txt);
        LiClipseLanguage language = TestUtils.loadLanguageFile("javascript.liclipse");
        language.connect(document);
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:");

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                IDocument.DEFAULT_CONTENT_TYPE, contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("keyword:0:3",
                "null:3:1",
                "method:4:3",
                "foreground:7:1",
                "operator:8:1",
                "foreground:9:1",
                "keyword:10:8",
                "bracket:18:1",
                "bracket:19:1"), scan);

    }

}
