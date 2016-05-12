package org.brainwy.liclipsetext.editor.scopes;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.scopes.ScopesParser;
import org.brainwy.liclipsetext.shared_core.parsing.Scopes;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class ScopesParserTest extends TestCase {

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

    public void testScopesParser() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("python.liclipse");
        ScopesParser parser = new ScopesParser(language);

        IDocument doc = new Document(""
                + "rara = 'string'\n"
                + "bbbb = 'st2'\n"
                + "");
        language.connect(doc);

        Scopes scopes = parser.createScopes(doc);
        assertEquals(""
                + "[1 rara = [2 'string' 2]\n"
                + "bbbb = [3 'st2' 3]\n"
                + " 1]"
                + "", scopes.debugString(doc).toString());

    }

    public void testScopesParser2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("python.liclipse");
        ScopesParser parser = new ScopesParser(language);

        IDocument doc = new Document(""
                + "rara = 'string'\n"
                + "bbbb = {bar}\n"
                + "");
        language.connect(doc);

        Scopes scopes = parser.createScopes(doc);
        assertEquals(""
                + "[1 rara = [2 'string' 2]\n"
                + "bbbb = [3 {bar} 3]\n"
                + " 1]"
                + "", scopes.debugString(doc).toString());

    }

    public void testScopesParserXml() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("xml.liclipse");
        ScopesParser parser = new ScopesParser(language);

        IDocument doc = new Document(""
                + "<p>\n"
                + "  <p>\n"
                + "  </p>\n"
                + "</p>\n"
                + "");
        language.connect(doc);

        Scopes scopes = parser.createScopes(doc);
        assertEquals(""
                + "[1 [2 [8 <p> 2][9 \n"
                + "  [3 [6 <p> 3][7 \n"
                + "   7][4 </p> 4] 6]\n"
                + " 9][5 </p> 5] 8]\n"
                + " 1]"
                + "", scopes.debugString(doc).toString());

    }
}
