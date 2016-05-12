package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PartitioningGoTest extends TestCase {

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

    public void testPartitionerAndColoringGo() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("go.liclipse");
        String txt = "func (tv /*comment*/ T) Mv";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("funcWithReceiver:0:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("go.liclipse", "funcWithReceiver");

        //scan only the regular partition.
        TestUtils.checkScan(new Document(txt), scanner, "keyword:0:4",
                "null:4:1",
                "bracket:5:1",
                "null:6:3",
                "multiLineComment:9:11",
                "null:20:2",
                "bracket:22:1",
                "foreground:23:1",
                "method:24:2");

    }

    public void testPartitionerAndColoringGo2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("go.liclipse");
        String txt = "func Mv";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("go.liclipse");

        //scan only the regular partition.
        TestUtils.checkScan(new Document(txt), scanner, "keyword:0:4",
                "foreground:4:1",
                "method:5:2");

    }

}
