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

public class PartitioningNimTest extends TestCase {

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

    public void testPartitioning() throws Exception {
        String txt = ""
                + "proc A()\n"
                + "template B()\n"
                + "macro C()\n"
                + "method C()\n"
                + "var y = 10_000.0'f32  # y is of type ``float32``\n"
                + "a = 'test'\n"
                + "b = \"\"\"\n"
                + "  test\n"
                + "  \"\"\"\n"
                + "\n"
                + "type\n"
                + "  Node = ref NodeObj # a traced reference to a NodeObj\n"
                + "  NodeObj = object\n"
                + "    le, ri: Node # left and right subtrees\n"
                + "proc `==`(x, y: ptr int): bool {.error.}\n"
                + "";

        IDocument document = new Document(txt);
        LiClipseLanguage language = TestUtils.loadLanguageFile("nim.liclipse");
        language.connect(document);
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:65",
                "singleLineComment:65:92",
                "__dftl_partition_content_type:92:96",
                "singleQuotedString:96:102",
                "__dftl_partition_content_type:102:107",
                "doubleQuotedMultiLineString:107:123",
                "__dftl_partition_content_type:123:151",
                "singleLineComment:151:185",
                "__dftl_partition_content_type:185:221",
                "singleLineComment:221:247",
                "__dftl_partition_content_type:247:278",
                "pragma:278:287",
                "__dftl_partition_content_type:287:");

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                IDocument.DEFAULT_CONTENT_TYPE, contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, 65);
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("keyword:0:4",
                "foreground:4:1",
                "method:5:1",
                "bracket:6:1",
                "bracket:7:1",
                "null:8:1",
                "keyword:9:8",
                "foreground:17:1",
                "method:18:1",
                "bracket:19:1",
                "bracket:20:1",
                "null:21:1",
                "keyword:22:5",
                "foreground:27:1",
                "method:28:1",
                "bracket:29:1",
                "bracket:30:1",
                "null:31:1",
                "keyword:32:6",
                "foreground:38:1",
                "method:39:1",
                "bracket:40:1",
                "bracket:41:1",
                "null:42:1",
                "keyword:43:3",
                "null:46:1",
                "foreground:47:1",
                "null:48:1",
                "operator:49:1",
                "null:50:1",
                "number:51:12",
                "null:63:2"), scan);

    }

}
