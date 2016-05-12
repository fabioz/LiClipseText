package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.ILiClipseEditor;
import org.brainwy.liclipsetext.editor.common.partitioning.DummyColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.OutlineData;
import org.brainwy.liclipsetext.editor.outline.LiClipseOutlineCreator;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.TreeNode;
import org.eclipse.core.internal.filebuffers.SynchronizableDocument;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class PartitioningToTreeTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.clearLanguagesManager();
    }

    public void testPartitioningToTreePython() throws Exception {
        String txt = ""
                + "class A:\n"
                + "    pass\n"
                + "def m1:\n"
                + "    pass\n"
                + "\n"
                + "";

        LiClipseOutlineCreator creator = newOutlineCreator(txt);
        TreeNode<OutlineData> node = creator.createOutline();
        assertEquals(2, node.getChildren().size());
    }

    public void testPartitioningToTreePython2() throws Exception {
        String txt = ""
                + "class A:\n"
                + "    def m1:\n"
                + "        pass\n"
                + "\n"
                + "";

        LiClipseOutlineCreator creator = newOutlineCreator(txt);
        TreeNode<OutlineData> node = creator.createOutline();
        assertEquals(1, node.getChildren().size());
    }

    public void testPartitioningToTreePython3() throws Exception {
        String txt = ""
                + "class A:\n"
                + "    def m1:\n"
                + "        pass\n"
                + "def m2:\n"
                + "    pass\n"
                + "\n"
                + "";

        LiClipseOutlineCreator creator = newOutlineCreator(txt);
        TreeNode<OutlineData> node = creator.createOutline();
        assertEquals(2, node.getChildren().size());
    }

    public void testPartitioningToTreeJavascript() throws Exception {
        String txt = ""
                + "var a = function(){:\n"
                + "    var b = function(){}\n"
                + "}\n"
                + "var c = function(){\n"
                + "}\n"
                + "";

        LiClipseOutlineCreator creator = newOutlineCreator(txt, "javascript.liclipse");
        TreeNode<OutlineData> node = creator.createOutline();
        assertEquals(2, node.getChildren().size());
    }

    public void testPartitioningToTreeJavascript2() throws Exception {
        String txt = ""
                + "\n"
                + "var f = function(){\n"
                + "	function bar(){\n"
                + "	}\n"
                + "	//---commenting\n"
                + "}\n"
                + "";

        LiClipseOutlineCreator creator = newOutlineCreator(txt, "javascript.liclipse");
        TreeNode<OutlineData> node = creator.createOutline();
        assertEquals(1, node.getChildren().size());
    }

    public void testPartitioningToTreeJavascript3() throws Exception {
        String txt = ""
                + "\n"
                + "var a1 = function(){\n"
                + "	//---c1\n"
                + "}\n"
                + "var b2 = function(){\n"
                + "	//---c2\n"
                + "}\n"
                + "";

        LiClipseOutlineCreator creator = newOutlineCreator(txt, "javascript.liclipse");
        TreeNode<OutlineData> node = creator.createOutline();
        assertEquals(2, node.getChildren().size());
    }

    public void testPartitioningToTreeHtml() throws Exception {
        TestUtils.startEditorPlugin();
        try {
            String txt = ""
                    + "\n"
                    + "<html attr='2'>\n"
                    + "<p attr='1'>\n"
                    + "</p>\n"
                    + "</html>\n"
                    + "";

            LiClipseOutlineCreator creator = newOutlineCreator(txt, "html.liclipse");
            TreeNode<OutlineData> node = creator.createOutline();
            assertEquals(1, node.getChildren().size());
        } finally {
            TestUtils.stopEditorPlugin();
        }
    }

    private LiClipseOutlineCreator newOutlineCreator(String txt) throws Exception {
        return newOutlineCreator(txt, "python.liclipse");
    }

    private LiClipseOutlineCreator newOutlineCreator(String txt, String language) throws Exception {
        final IDocument document = new SynchronizableDocument();
        document.set(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile(language);
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        IColorCache colorManager = new DummyColorCache();
        partitioner.createTokenScanners(colorManager);

        LiClipseOutlineCreator creator = new LiClipseOutlineCreator(new ILiClipseEditor() {

            public IDocument getDocument() {
                return document;
            }

            public LiClipseLanguage getLiClipseLanguage() {
                throw new RuntimeException("not implemented");
            }

            public TextSelectionUtils createTextSelectionUtils() {
                throw new RuntimeException("not implemented");
            }
        });
        return creator;
    }

}
