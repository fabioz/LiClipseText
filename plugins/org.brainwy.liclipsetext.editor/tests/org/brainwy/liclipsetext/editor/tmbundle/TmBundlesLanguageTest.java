package org.brainwy.liclipsetext.editor.tmbundle;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class TmBundlesLanguageTest extends TestCase {

    public void testTmBundleLanguage() throws Exception {
        String txt = "class A:\n"
                + "    '''docstring'''"
                + "    pass";
        final IDocument document = new Document(txt);

        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test.liclipse");
        language.connect(document);

        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("meta.class.old-style.python:0:8",
                "__dftl_partition_content_type:8:9",
                "source.python.include.8:9:28",
                "__dftl_partition_content_type:28:32",
                "keyword.control.flow.python:32:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test.liclipse",
                "source.python.include.8");

        TestUtils.checkScan(new Document(txt.substring(9, 28)), scanner, "comment.block.python:0:4",
                "punctuation.definition.string.begin.python:4:3",
                "string.quoted.single.block.python:7:9",
                "punctuation.definition.string.end.python:16:3");
    }
}
