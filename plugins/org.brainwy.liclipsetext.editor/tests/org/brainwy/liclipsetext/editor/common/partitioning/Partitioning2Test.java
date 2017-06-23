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
        LiClipseDamagerRepairer.MERGE_TOKENS = false;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtils.clearLanguagesManager();
        LiClipseDamagerRepairer.MERGE_TOKENS = true;
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
        assertEquals(TestUtils.listToExpected("null:0:1",
                "operator:1:1",
                "null:2:1",
                "null:3:1",
                "singleQuotedString:4:6",
                "foreground:10:1",
                "null:11:1",
                "operator:12:1",
                "null:13:1",
                "null:14:1",
                "singleQuotedString:15:9",
                "null:24:1"), TestUtils.textPresentationToExpected(dummyTextViewer.appliedPresentations.remove(0)));

        // Our changes always damage whole lines (so, the whole line is damaged/repaired).
        document.replace(7, 0, "_foo_");
        assertEquals("a = 'te_foo_st'\nb = 'another'\n", document.get());
        assertEquals(1, dummyTextViewer.appliedPresentations.size());
        assertEquals(TestUtils.listToExpected("null:0:1",
                "operator:1:1",
                "null:2:1",
                "null:3:1",
                "singleQuotedString:4:11"),
                TestUtils.textPresentationToExpected(dummyTextViewer.appliedPresentations.remove(0)));
    }

    public void testTmPartitioningChanges() throws Exception {
        final IDocument document = new Document("p {color:red}");

        LiClipseLanguage language = TestUtils.loadLanguageFile("css.tmbundle",
                "css.tmbundle-master/Syntaxes/CSS.plist");
        language.connect(document);

        DummyTextViewer dummyTextViewer = new DummyTextViewer(document);

        // Upon being connected it does: processDamage(new Region(0, newDocument.getLength()), newDocument);
        TestUtils.connectPresentationReconciler(dummyTextViewer);
        assertEquals(1, dummyTextViewer.appliedPresentations.size());
        assertEquals(TestUtils.listToExpected("meta.selector.css:0:1",
                "punctuation.section.property-list.begin.css:1:1",
                "support.type.property-name.css:2:1",
                "punctuation.separator.key-value.css:3:5",
                "support.constant.color.w3c-standard-color-name.css:8:1",
                "punctuation.section.property-list.end.css:9:3",
                "punctuation.section.property-list.end.css:12:1"),
                TestUtils.textPresentationToExpected(dummyTextViewer.appliedPresentations.remove(0)));

        // Our changes always damage whole lines (so, the whole line is damaged/repaired).
        document.replace(9, 3, "black");
        assertEquals("p {color:black}", document.get());
        assertEquals(1, dummyTextViewer.appliedPresentations.size());
        assertEquals(TestUtils.listToExpected("meta.selector.css:0:1",
                "punctuation.section.property-list.begin.css:1:1",
                "support.type.property-name.css:2:1",
                "punctuation.separator.key-value.css:3:5",
                "support.constant.color.w3c-standard-color-name.css:8:1",
                "punctuation.section.property-list.end.css:9:5",
                "punctuation.section.property-list.end.css:14:1"),
                TestUtils.textPresentationToExpected(dummyTextViewer.appliedPresentations.remove(0)));
    }

}
