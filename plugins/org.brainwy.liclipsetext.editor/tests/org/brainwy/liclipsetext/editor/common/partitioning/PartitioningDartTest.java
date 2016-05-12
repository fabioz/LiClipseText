package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

public class PartitioningDartTest extends TestCase {

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

    public void testPartitionerAndColoringDart() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("dart.liclipse");
        String txt = "if(true){";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("dart.liclipse");

        //scan only the regular partition.
        TestUtils.checkScan(new Document(txt), scanner, "keyword:0:2",
                "bracket:2:1",
                "keyword:3:4",
                "bracket:7:1",
                "bracket:8:1");

    }

}
