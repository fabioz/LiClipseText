package org.brainwy.liclipsetext.editor.common.partitioning.reader;

import java.io.File;
import java.util.ArrayList;

import org.brainwy.liclipsetext.editor.common.partitioning.DummyColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.IAcceptPartition;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataTmBundleZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.core.internal.filebuffers.SynchronizableDocument;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;

import junit.framework.TestCase;

public class SubPartitionCodeReaderTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.clearLanguagesManager();
    }

    public void testSubPartitionCodeReaderStartingOnMiddle() throws Exception {
        String txt = ""
                + "abcde = 'string'"
                + "";
        IDocument doc = configureDocument(txt, "python.liclipse");

        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        reader.configurePartitions(true, doc, 11, "singleQuotedString", "singleLineComment");
        TypedPart read = reader.read();
        assertEquals(8, read.offset); //Note: if we start in the middle of a partition, we'll still return that partition (fully).

        read = reader.read();
        assertEquals(null, read);
    }

    public void testSubPartitionCodeReaderWithTopAndSub() throws Exception {
        String txt = ""
                + "class AAA:"
                + "    abcde = 'string'"
                + "";
        IDocument doc = configureDocument(txt, "python.liclipse");

        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        reader.configurePartitions(true, doc, 7, "default.class", "singleQuotedString");
        TypedPart read = reader.read();
        assertEquals(6, read.offset); //Note: if we start in the middle of a partition, we'll still return that partition (fully).

        read = reader.read();
        assertEquals(22, read.offset);

        read = reader.read();
        assertEquals(null, read);
    }

    public void testSubPartitionCodeReader1() throws Exception {
        String txt = ""
                + "a = 10\n"
                + "b = 'str'\n"
                + "#comment\n"
                + "";
        IDocument doc = configureDocument(txt, "python.liclipse");

        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        reader.configurePartitions(true, doc, 0, "singleQuotedString", "singleLineComment");
        TypedPart read = reader.read();
        assertEquals(11, read.offset);

        read = reader.read();
        assertEquals(17, read.offset);

        read = reader.read();
        assertEquals(null, read);
    }

    public void testSubPartitionCodeReader() throws Exception {
        String txt = ""
                + "class A:\n"
                + "    pass\n"
                + "def m1:\n"
                + "    pass\n"
                + "\n"
                + "";
        IDocument doc = configureDocument(txt, "python.liclipse");

        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        reader.configurePartitions(true, doc, 0, "default.class", "default.method");

        TypedPart read = reader.read();
        assertEquals(6, read.offset);

        read = reader.read();
        assertEquals(22, read.offset);

        read = reader.read();
        assertEquals(null, read);
    }

    public void testSubPartitionCodeReader2() throws Exception {
        String txt = ""
                + "class A:\n"
                + "    pass\n"
                + "def m1:\n"
                + "    'str'\n"
                + "    pass\n"
                + "\n"
                + "";
        IDocument doc = configureDocument(txt, "python.liclipse");

        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        IAcceptPartition filter = new IAcceptPartition() {

            public boolean accept(TypedPosition typedPosition) {
                top += 1;
                return true;
            }

            public boolean getRequireOnlyTop() {
                return false;
            }

            public boolean accept(TypedPart typedPart) {
                sub += 1;
                return true;
            }
        };
        reader.configurePartitions(true, doc, 0, filter);

        TypedPart part;
        ArrayList<TypedPart> lst = new ArrayList<TypedPart>();
        while ((part = reader.read()) != null) {
            lst.add(part);
        }
        assertEquals(3, top);
        assertEquals(""
                + "type:__dftl_partition_content_type.keyword offset:0 len:5\n"
                + "type:__dftl_partition_content_type.foreground offset:5 len:1\n"
                + "type:__dftl_partition_content_type.class offset:6 len:1\n"
                + "type:__dftl_partition_content_type.foreground offset:7 len:1\n"
                + "type:__dftl_partition_content_type.keyword offset:13 len:4\n"
                + "type:__dftl_partition_content_type.keyword offset:18 len:3\n"
                + "type:__dftl_partition_content_type.foreground offset:21 len:1\n"
                + "type:__dftl_partition_content_type.method offset:22 len:2\n"
                + "type:__dftl_partition_content_type.foreground offset:24 len:1\n"
                + "type:singleQuotedString.singleQuotedString offset:30 len:5\n"
                + "type:__dftl_partition_content_type.keyword offset:40 len:4"
                + "", StringUtils.join("\n", lst));
    }

    public void testSubPartitionCodeReader3() throws Exception {
        String txt = ""
                + "<!-- ccc -->\n<!-- ccc -->\n<!-- ccc -->\n"
                + "";
        IDocument doc = configureDocument(txt, "html.liclipse");

        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        IAcceptPartition filter = new IAcceptPartition() {

            public boolean accept(TypedPosition typedPosition) {
                top += 1;
                return true;
            }

            public boolean getRequireOnlyTop() {
                return false;
            }

            public boolean accept(TypedPart typedPart) {
                sub += 1;
                return true;
            }
        };
        reader.configurePartitions(true, doc, 0, filter);

        TypedPart part;
        ArrayList<TypedPart> lst = new ArrayList<TypedPart>();
        while ((part = reader.read()) != null) {
            lst.add(part);
        }
        assertEquals(""
                + "type:multiLineComment.multiLineComment offset:0 len:12\n"
                + "type:__dftl_partition_content_type.foreground offset:12 len:1\n"
                + "type:multiLineComment.multiLineComment offset:13 len:12\n"
                + "type:__dftl_partition_content_type.foreground offset:25 len:1\n"
                + "type:multiLineComment.multiLineComment offset:26 len:12\n"
                + "type:__dftl_partition_content_type.foreground offset:38 len:1", StringUtils.join("\n", lst));
    }

    public void testSubPartitionCodeReaderBackward() throws Exception {
        String txt = ""
                + "class A:\n"
                + "    pass\n"
                + "def m1:\n"
                + "    pass\n"
                + "\n"
                + "";
        IDocument doc = configureDocument(txt, "python.liclipse");

        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        reader.configurePartitions(false, doc, doc.getLength(), "default.class", "default.method");

        TypedPart read = reader.read();
        assertEquals(22, read.offset);

        read = reader.read();
        assertEquals(6, read.offset);

        read = reader.read();
        assertEquals(null, read);
    }

    public void testSubPartitionCodeReader5() throws Exception {
        String txt = "<?php\nclass Cart {\n"
                + "    var $items;\n"
                + "}\n?>";
        IDocument doc = new Document(txt);

        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "php.tmbundle"), "php.tmbundle-master/Syntaxes/PHP.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);

        language.connect(doc);

        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        reader.configurePartitions(false, doc, doc.getLength(), new IAcceptPartition() {

            @Override
            public boolean accept(TypedPosition typedPosition) {
                return true;
            }

            @Override
            public boolean accept(TypedPart typedPart) {
                return true;
            }

            @Override
            public boolean getRequireOnlyTop() {
                return false;
            }

        });

        ArrayList<String> lst = new ArrayList<>();
        while (true) {
            TypedPart read;
            read = reader.read();
            if (read == null) {
                break;
            }
            lst.add(read.subType);
        }
        assertEquals(TestUtils.listToExpected("punctuation.section.embedded.end.php",
                "source.php",
                "punctuation.section.scope.end.php",
                "punctuation.terminator.expression.php",
                "variable.other.php",
                "punctuation.definition.variable.php",
                "source.php",
                "storage.type.php",
                "source.php",
                "punctuation.section.scope.begin.php",
                "meta.class.php",
                "entity.name.type.class.php",
                "meta.class.php",
                "storage.type.class.php",
                "punctuation.section.embedded.begin.php"), TestUtils.listToExpected(lst));
    }

    int top;
    int sub;

    private IDocument configureDocument(String txt, String language) throws Exception {
        final IDocument document = new SynchronizableDocument();
        document.set(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile(language);
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        IColorCache colorManager = new DummyColorCache();
        partitioner.createTokenScanners(colorManager);
        return document;
    }

}
