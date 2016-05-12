package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class PartitioningCppTest extends TestCase {

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

    public void testPartitionerAndColoringCpp() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("cpp.liclipse");
        String txt = "this->something";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("cpp.liclipse", IDocument.DEFAULT_CONTENT_TYPE);

        //scan only inside brackets.
        TestUtils.checkScan(new Document(txt), scanner, "keyword:0:4",
                "operator:4:1",
                "operator:5:1",
                "null:6:9");
    }

}
