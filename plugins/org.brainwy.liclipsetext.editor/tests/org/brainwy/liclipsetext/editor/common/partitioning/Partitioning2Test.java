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

        //        TestUtils.updateDocumentPartitions(document);

        //        LiClipseOutlineCreator creator = new LiClipseOutlineCreator(new ILiClipseEditor() {
        //
        //            public IDocument getDocument() {
        //                return document;
        //            }
        //        });
        //        TreeNode<OutlineData> node = creator.createOutline();
        //        assertEquals(1820, node.getChildren().size());
    }
}
