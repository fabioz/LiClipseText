package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PartitioningCssTest extends TestCase {

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


    public void testLiClipseCssPartitioning() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("css.tmbundle", "css.tmbundle-master/Syntaxes/CSS.plist");
        Document document = new Document(""
                + "a {test:test;}");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("source.css.include.1:0:2",
        		"source.css.3:2:"), partition);
    }

}
