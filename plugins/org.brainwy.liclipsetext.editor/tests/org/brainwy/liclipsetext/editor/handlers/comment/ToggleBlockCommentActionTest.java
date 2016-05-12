package org.brainwy.liclipsetext.editor.handlers.comment;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.handlers.ToggleComment;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;

public class ToggleBlockCommentActionTest extends TestCase {

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

    public void testToggleBlockComment() throws Exception {
        String s = "ccc";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "html.liclipse");
        ToggleComment.toggleBlockComment(null, language, new TextSelectionUtils(doc, 5));
        assertEquals("<!-- ccc -->", doc.get());
    }

    public void testToggleBlockComment2() throws Exception {
        String s = "   ccc";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "html.liclipse");
        ToggleComment.toggleBlockComment(null, language, new TextSelectionUtils(doc, 5));
        assertEquals("   <!-- ccc -->", doc.get());
    }

    public void testToggleBlockUncomment2() throws Exception {
        String s = "<!-- ccc -->";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "html.liclipse");
        ToggleComment.toggleBlockComment(null, language, new TextSelectionUtils(doc, 5));
        assertEquals("ccc", doc.get());
    }

    public void testToggleBlockUncomment3() throws Exception {
        String s = "<!-- ccc -->\n<!-- ccc -->\n<!-- ccc -->\n";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "html.liclipse");
        ToggleComment.toggleBlockComment(null, language, new TextSelectionUtils(doc, new TextSelection(
                5, 16)));
        assertEquals("ccc\nccc\n<!-- ccc -->\n", doc.get());
    }

    public void testToggleComment() throws Exception {
        String s = "  //ccc";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "javascript.liclipse");
        ToggleComment.toggleSingleLineComment(null, language, new TextSelectionUtils(doc, 5));
        assertEquals("  ccc", doc.get());
    }

    public void testToggleComment2() throws Exception {
        String s = "// ccc";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "javascript.liclipse");
        ToggleComment.toggleSingleLineComment(null, language, new TextSelectionUtils(doc, 5));
        assertEquals("ccc", doc.get());
    }

    public void testToggleComment3() throws Exception {
        String s = "//a\n  //b\n";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "javascript.liclipse");
        ToggleComment.toggleSingleLineComment(null, language,
                new TextSelectionUtils(doc, new TextSelection(doc, 0, s.length() - 1)));
        assertEquals("a\n  b\n", doc.get());
    }

    public void testCommentInsideSwitch() throws Exception {
        String s = "<script language=\"JavaScript\">\n"
                + "var a = 10;\n"
                + "</script>";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "html.liclipse");
        ToggleComment.execute(null, doc, new TextSelectionUtils(doc, 40), language);
        assertEquals("<script language=\"JavaScript\">\n// var a = 10;\n</script>",
                StringUtils.replaceNewLines(doc.get(), "\n"));
    }

    public void testCommentInCss() throws Exception {
        String s = ""
                + "body {\n"
                + "/* font*/\n"
                + "}\n"
                + "";
        Document doc = new Document(s);
        LiClipseLanguage language = TestUtils.connectDocumentToLanguage(doc, "css.tmbundle", "css.tmbundle-master/Syntaxes/CSS.plist");
        ToggleComment.execute(null, doc, new TextSelectionUtils(doc, 11), language);
        assertEquals("body {\nfont\n}\n",
                StringUtils.replaceNewLines(doc.get(), "\n"));
    }
}
