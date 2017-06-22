package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.InputStream;

import org.brainwy.liclipsetext.editor.languages.LanguageLoaderTest;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class Partitioning2Test extends TestCase {

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

    public void testPythonPartitioningHugeFile() throws Exception {
        InputStream resourceAsStream = LanguageLoaderTest.class.getClassLoader().getResourceAsStream("huge.py");
        String txt = FileUtils.getStreamContents(resourceAsStream, "utf-8", null);

        final IDocument document = new Document(txt);
        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("python.liclipse");
        partitioningSetup.connect(document);

    }

    public void testPythonPartitioningChanges() throws Exception {
        final IDocument document = new Document("a = 'test'\nb = 'another'\n");

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("python.liclipse");
        partitioningSetup.connect(document);

        DummyTextViewer dummyTextViewer = new DummyTextViewer(document);

        // Upon being connected it does: processDamage(new Region(0, newDocument.getLength()), newDocument);
        TestUtils.connectPresentationReconciler(dummyTextViewer);
        assertEquals(1, dummyTextViewer.appliedPresentations.size());
        assertEquals(TestUtils.listToExpected("null:0:4",
                "singleQuotedString:4:6",
                "null:10:5",
                "singleQuotedString:15:9",
                "null:24:1"), TestUtils.textPresentationToExpected(dummyTextViewer.appliedPresentations.remove(0)));

        // Our changes always damage whole lines (so, the whole line is damaged/repaired).
        document.replace(7, 0, "_foo_");
        assertEquals("a = 'te_foo_st'\nb = 'another'\n", document.get());
        assertEquals(1, dummyTextViewer.appliedPresentations.size());
        assertEquals(TestUtils.listToExpected("null:0:4",
                "singleQuotedString:4:11"),
                TestUtils.textPresentationToExpected(dummyTextViewer.appliedPresentations.remove(0)));
    }

}
