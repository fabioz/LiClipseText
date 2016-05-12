package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.brainwy.liclipsetext.editor.languages.LanguageMetadataZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PartitioningSwiftTest extends TestCase {

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

    /**
     * http://www.oracle.com/technetwork/articles/javase/supplementary-142654.html
     * TODO: we have to deal better with character sets that have 2 java chars for one
     * unicode char (to convert to int properly).
     *
     * Deal with characters with Character.isHighSurrogate()
     */
    public void todoTestCharLen() throws Exception {
        File testSwiftUnicodeDir = TestUtils.getTestSwiftUnicodeDir();
        File f = new File(testSwiftUnicodeDir, "my.swift");
        String contents = FileUtils.getFileContentsCustom(f, "utf-8", String.class);
        byte[] bytes = FileUtils.getFileContentsBytes(f);

        assertEquals(27, contents.length());
        assertEquals(bytes.length, 29);

        Utf8WithCharLen utf8WithCharLen = new Utf8WithCharLen(contents);
        Utf8WithCharLen utf8WithCharLen2 = new Utf8WithCharLen(bytes);

        assertEquals(27, contents.length());
        assertEquals(27, contents.length());

        assertEquals(29, utf8WithCharLen2.getBytesLen());
        assertEquals(26, utf8WithCharLen2.getCharPosFromBytesPos(29));
    }

    public void testSwiftUnicode() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document(""
                + "let cat = \"🐱\"; println(10)\n" +
                "// prints \"🐱\"\n" +
                "");

        String partition = connectAndPartition(language, document);
        assertEquals(TestUtils.listToExpected("source.swift.include.6:0:3",
                "__dftl_partition_content_type:3:8",
                "source.swift.include.4:8:9",
                "__dftl_partition_content_type:9:10",
                "source.swift.include.3:10:14",
                "__dftl_partition_content_type:14:24",
                "source.swift.include.3:24:26",
                "__dftl_partition_content_type:26:28",
                "source.swift.include.1:28:"), partition);

        List<String> asList = TestUtils.partitionAsList(document);
        asList = Arrays.asList(asList.get(asList.size() - 1));
        String last = TestUtils.scanAll(language, document, asList);
        assertEquals(TestUtils.listToExpected("punctuation.definition.comment.swift:28:2",
                "comment.line.double-slash.swift:30:12",
                "comment.line.double-slash.swift:42:1"), last);

    }

    public void testSwiftNonUnicode() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document(""
                + "let cat = \"a\"; println(10)\n" +
                "// prints \"a\"\n" +
                "");
        String partition = connectAndPartition(language, document);
        assertEquals(TestUtils.listToExpected("source.swift.include.6:0:3",
                "__dftl_partition_content_type:3:8",
                "source.swift.include.4:8:9",
                "__dftl_partition_content_type:9:10",
                "source.swift.include.3:10:13",
                "__dftl_partition_content_type:13:23",
                "source.swift.include.3:23:25",
                "__dftl_partition_content_type:25:27",
                "source.swift.include.1:27:"), partition);

    }

    public void testSwiftSimple() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document(""
                + "a = \"10\"\n");
        String partition = connectAndPartition(language, document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:2",
                "source.swift.include.4:2:3",
                "__dftl_partition_content_type:3:4",
                "source.swift.include.3:4:8",
                "__dftl_partition_content_type:8:"), partition);
        //
        //        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
        //                .getDocumentPartitioner();
        //        Map<String, IFulICustomPartitionTokenScannerntentTypeToScanner = new HashMap<>();
        //        IFulICustomPartitionTokenScannernnerForContentType = documentPartitioner.obtainTokenScannerForContentType(
        //                "text.html.markdown.include", contentTypeToScanner, language);
        //        ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
        //        String scan = TestUtils.scan(scannerForContentType, false);
        //        assertEquals(TestUtils.listToExpected("meta.paragraph.markdown:0:5",
        //                "markup.heading.setext.2.markdown:5:5"), scan);
    }

    private LiClipseLanguage setupLanguage() throws Exception {
        LanguageMetadataZipFileInfo metadata = setup();

        LiClipseLanguage language = metadata.loadLanguage(true);
        return language;
    }

    private String connectAndPartition(LiClipseLanguage language, Document document) throws Exception {
        language.connect(document);

        List<String> asList = TestUtils.partitionAsList(document);
        String partition = TestUtils.listToExpected(asList);
        return partition;
    }

    private LanguageMetadataZipFileInfo setup() {
        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "swift.tmbundle"),
                "swift.tmbundle-master/Syntaxes/Swift.tmLanguage");
        return metadata;
    }

}
