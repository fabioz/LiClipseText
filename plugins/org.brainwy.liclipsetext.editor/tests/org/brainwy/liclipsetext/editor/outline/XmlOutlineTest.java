package org.brainwy.liclipsetext.editor.outline;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class XmlOutlineTest extends TestCase {

    public void testOutline() throws Exception {

        IDocument document = new Document(""
                + "<test><test>"
                + "</test></test>");
        TestUtils.connectDocumentToLanguage(document, "xml.liclipse");
        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        LiClipseLanguage language = partitioner.language;

        LiClipseNode outline = language.getOutline().createOutline(document);
        assertEquals(outline.toStringRepr(), ""
                + "TreeNode:null\n"
                + "    TreeNode:test offset:1 len:4 beginLine:1 icon:class\n"
                + "        TreeNode:test offset:7 len:4 beginLine:1 icon:class\n"
                + "");
    }

}
