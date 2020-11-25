package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.brainwy.liclipsetext.editor.languages.LanguageMetadataTmBundleZipFileInfo;
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
        assertEquals(TestUtils.listToExpected("source.swift:0:"), partition);

        List<String> asList = TestUtils.partitionAsList(document);
        asList = Arrays.asList(asList.get(asList.size() - 1));
        String last = TestUtils.scanAll(language, document, asList);
        assertEquals(TestUtils.listToExpected("storage.type.swift:0:3",
                "source.swift:3:5",
                "keyword.operator.assignment.swift:8:1",
                "source.swift:9:1",
                "punctuation.definition.string.begin.swift:10:1",
                "string.quoted.double.swift:11:1",
                "punctuation.definition.string.end.swift:12:1",
                "source.swift:13:10",
                "constant.numeric.integer.decimal.swift:23:2",
                "source.swift:25:3",
                "punctuation.definition.comment.swift:28:2",
                "comment.line.double-slash.swift:30:11",
                "comment.line.double-slash.swift:41:2"), last);

    }

    private LiClipseLanguage setupLanguage() throws Exception {
        LanguageMetadataTmBundleZipFileInfo metadata = setup();

        LiClipseLanguage language = metadata.loadLanguage(true);
        return language;
    }

    private String connectAndPartition(LiClipseLanguage language, Document document) throws Exception {
        language.connect(document);

        List<String> asList = TestUtils.partitionAsList(document);
        String partition = TestUtils.listToExpected(asList);
        return partition;
    }

    private LanguageMetadataTmBundleZipFileInfo setup() {
        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "swift.tmbundle"),
                "swift.tmbundle-master/Syntaxes/Swift.tmLanguage");
        return metadata;
    }

}
