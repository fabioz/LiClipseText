package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.Arrays;

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
        LiClipseLanguage language = TestUtils.loadLanguageFile("css.tmbundle",
                "css.tmbundle-master/Syntaxes/CSS.plist");
        Document document = new Document(""
                + "a {test:test;}");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("source.css:0:"), partition);

        String last = TestUtils.scanAll(language, document, Arrays.asList("source.css:0:"));

        assertEquals(TestUtils.listToExpected("entity.name.tag.css:0:1",
                "meta.selector.css:1:1",
                "punctuation.section.property-list.begin.css:2:1",
                "meta.property-name.css:3:4",
                "punctuation.separator.key-value.css:7:1",
                "meta.property-value.css:8:4",
                "punctuation.terminator.rule.css:12:1",
                "punctuation.section.property-list.end.css:13:1"), last);
    }

}
