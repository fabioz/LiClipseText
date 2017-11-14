package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PartitioningRstTest extends TestCase {

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

    public void testLiClipseRstPartitioning() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("rst.liclipse");
        Document document = new Document(""
                + "reStructuredText Demonstration\r\n"
                + "================================\r\n"
                + "\r\n"
                + "");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("title:0:64",
                "__dftl_partition_content_type:64:"), partition);
    }

    public void testLiClipseRstPartitioning2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("rst.liclipse");
        Document document = new Document("aaa\n" +
                "-----\n" +
                "\n" +
                "bbbb\n" +
                "-----"
                + "");
        language.connect(document);

        DummyTextViewer dummyTextViewer = new DummyTextViewer(document);
        TestUtils.connectPresentationReconciler(dummyTextViewer);
        assertEquals(1, dummyTextViewer.appliedPresentations.size());

        document.replace(("aaa\n" +
                "-----\n" +
                "\n" +
                "bbbb").length(), 0, "xxx");
        assertEquals(2, dummyTextViewer.appliedPresentations.size());
    }

    public void testLiClipseRstPartitioning4() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("rst.liclipse");
        Document document = new Document("aaa\n" +
                "-----\n" +
                "");
        language.connect(document);

        DummyTextViewer dummyTextViewer = new DummyTextViewer(document);
        TestUtils.connectPresentationReconciler(dummyTextViewer);
        assertEquals(1, dummyTextViewer.appliedPresentations.size());

        document.replace(document.getLength(), 0, "\nff");
        document.replace(("aaa\n" +
                "-----").length() - 2, 2, "");

        document.replace(document.getLength(), 0, "\nff");
        document.replace(("aaa\n" +
                "-----").length() - 2, 0, "--");

    }

}
