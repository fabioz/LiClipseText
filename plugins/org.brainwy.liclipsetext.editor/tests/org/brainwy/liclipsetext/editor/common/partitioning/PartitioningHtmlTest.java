package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

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

}
