package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

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

}
