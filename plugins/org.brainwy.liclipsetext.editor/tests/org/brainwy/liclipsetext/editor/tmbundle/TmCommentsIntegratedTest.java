package org.brainwy.liclipsetext.editor.tmbundle;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.comment.LanguageComment;
import org.brainwy.liclipsetext.editor.languages.comment.LanguageComment.CommentType;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class TmCommentsIntegratedTest extends TestCase {

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

    public void testRubySourceIntegrated() throws Exception {
        String txt = "";
        final IDocument document = new Document(txt);

        LiClipseLanguage language = LiClipseTextEditorPlugin.getLanguagesManager()
                .getLanguageFromName("source.ruby");
        LanguageComment comment = language.getComment();
        assertEquals("#", comment.commentString);
        assertEquals(CommentType.COMMENT_TYPE_SINGLE_LINE, comment.commentType);
    }
}
