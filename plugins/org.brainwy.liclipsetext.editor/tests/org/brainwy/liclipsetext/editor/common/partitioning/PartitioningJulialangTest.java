package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

public class PartitioningJulialangTest extends TestCase {

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

    public void testPartitionerAndColoringJulialang() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("julia.liclipse");
        String txt = "(2, 2).bar";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("julia.liclipse");

        //scan only the regular partition.
        TestUtils.checkScan(new Document(txt), scanner, "bracket:0:1",
                "number:1:1",
                "operator:2:1",
                "null:3:1",
                "number:4:1",
                "bracket:5:1",
                "null:6:4");

    }

}
