package org.brainwy.liclipsetext.editor.tmbundle;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class TmIncludeTest extends TestCase {

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

    public void testInclude() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test_tm_rules4.liclipse");

        String string = "aa (bb ( cc )   dd ) ee";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:3",
                "recurse:3:20",
                "__dftl_partition_content_type:20:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test_tm_rules4.liclipse",
                "recurse");

        TestUtils.checkScan(new Document(string), scanner, "null:0:3",
                "recurse:3:1",
                "decorator:4:2",
                "recurse:6:1",
                "recurse:7:1",
                "recurse:8:1",
                "decorator:9:2",
                "recurse:11:1",
                "recurse:12:1",
                "recurse:13:3",
                "decorator:16:2",
                "recurse:18:1",
                "recurse:19:1",
                "null:20:3");
    }

    public void testIncludeSelf() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test_tm_rules5.liclipse");

        String string = "aa (bb ( cc )   dd ) ee";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:3",
                "recurse:3:20",
                "__dftl_partition_content_type:20:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test_tm_rules5.liclipse",
                "recurse");

        TestUtils.checkScan(new Document(string), scanner, "null:0:3",
                "recurse:3:1",
                "decorator:4:2",
                "recurse:6:1",
                "recurse:7:1",
                "recurse:8:1",
                "decorator:9:2",
                "recurse:11:1",
                "recurse:12:1",
                "recurse:13:3",
                "decorator:16:2",
                "recurse:18:1",
                "recurse:19:1",
                "null:20:3");
    }

}
