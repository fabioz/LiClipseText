package org.brainwy.liclipsetext.editor.tmbundle;

import java.util.Arrays;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class TmBundlesLanguageTest extends TestCase {

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

    public void testTmBundleLanguage() throws Exception {
        String txt = "class A:\n"
                + "    '''docstring'''"
                + "    pass";
        final IDocument document = new Document(txt);

        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test.liclipse");
        language.connect(document);

        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("source.python:0:"), partition);

        assertEquals(TestUtils.listToExpected("storage.type.class.python:0:5",
                "meta.class.old-style.python:5:1",
                "entity.name.type.class.python:6:1",
                "punctuation.section.class.begin.python:7:2",
                "comment.block.python:9:4",
                "punctuation.definition.string.begin.python:13:3",
                "string.quoted.single.block.python:16:9",
                "punctuation.definition.string.end.python:25:3",
                "source.python:28:4",
                "keyword.control.flow.python:32:4"),
                TestUtils.scanAll(language, document, Arrays.asList("source.python:0:")));
    }
}
