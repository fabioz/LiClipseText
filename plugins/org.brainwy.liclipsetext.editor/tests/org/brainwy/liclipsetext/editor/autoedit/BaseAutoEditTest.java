/**
 * Copyright: Fabio Zadrozny
 * License: EPL
 */
package org.brainwy.liclipsetext.editor.autoedit;

import org.brainwy.liclipsetext.editor.autoedit.BaseAutoEditStrategy;
import org.brainwy.liclipsetext.editor.common.DefaultScopeCreatingCharsProvider;
import org.brainwy.liclipsetext.editor.common.ILiClipseLanguageProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyScopeCreationHelper;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.utils.DocCmd;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;

import junit.framework.TestCase;

public class BaseAutoEditTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.clearLanguagesManager();
    }

    private BaseAutoEditStrategy strategy;
    private Document document;
    private LiClipseLanguage language;
    private DefaultScopeCreatingCharsProvider defaultScopeCreatingCharsProvider;

    public void testCloseParens() throws Exception {
        String doc = "()";
        DocCmd docCmd = new DocCmd(doc.length() - 1, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("()", docCmd.text);

        doc = "[]";
        docCmd = new DocCmd(doc.length() - 1, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("()", docCmd.text);

        doc = "((()))";
        docCmd = new DocCmd(3, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("()", docCmd.text);

        doc = "((()))";
        docCmd = new DocCmd(4, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("()", docCmd.text);

        doc = ",";
        docCmd = new DocCmd(0, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("()", docCmd.text);
    }

    public void testNoCloseParens() throws Exception {
        String doc = "test";
        DocCmd docCmd = new DocCmd(0, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        String expected = "(";
        assertEquals(expected, docCmd.text);

        doc = "[][]";
        docCmd = new DocCmd(2, 0, "[");
        customizeDocumentCommand(doc, docCmd);
        expected = "[";
        assertEquals(expected, docCmd.text);

        doc = "(test";
        docCmd = new DocCmd(0, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        expected = "(";
        assertEquals(expected, docCmd.text);

        doc = ")test";
        docCmd = new DocCmd(0, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        expected = "(";
        assertEquals(expected, docCmd.text);

        doc = "()test";
        docCmd = new DocCmd(0, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        expected = "(";
        assertEquals(expected, docCmd.text);

        doc = "())";
        docCmd = new DocCmd(0, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        expected = "(";
        assertEquals(expected, docCmd.text);

        doc = "())";
        docCmd = new DocCmd(1, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        expected = "(";
        assertEquals(expected, docCmd.text);

        doc = "(()))";
        docCmd = new DocCmd(2, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        expected = "(";
        assertEquals(expected, docCmd.text);

        doc = "[]][]";
        docCmd = new DocCmd(2, 0, "[");
        customizeDocumentCommand(doc, docCmd);
        expected = "[";
        assertEquals(expected, docCmd.text);

        doc = "\n\n)";
        docCmd = new DocCmd(0, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        expected = "(";
        assertEquals(expected, docCmd.text);
    }

    public void testNewLineAfterBracket() throws Exception {
        String str = "" +
                "function(){}" +
                "";
        DocCmd docCmd = new DocCmd(str.length() - 1, 0, "\n");
        customizeDocumentCommand(str, docCmd);
        assertEquals("\n    \n", docCmd.text);
    }

    public void testNewLineBetweenTags() throws Exception {
        String str = "" +
                "function()><" +
                "";
        DocCmd docCmd = new DocCmd(str.length() - 1, 0, "\n");
        customizeDocumentCommand(str, docCmd);
        assertEquals("\n    \n", docCmd.text);
    }

    public void testIndentAfterColon() throws Exception {
        String str = "" +
                "def m1:" +
                "";
        DocCmd docCmd = new DocCmd(str.length(), 0, "\n");
        customizeDocumentCommand(str, docCmd);
        assertEquals("\n    ", docCmd.text);
    }

    public void testColonReplacement() throws Exception {
        String str = "" +
                "def m1:" +
                "";
        DocCmd docCmd = new DocCmd(str.length() - 1, 0, ":");
        customizeDocumentCommand(str, docCmd);
        assertEquals("", docCmd.text);
    }

    public void testAutoCreateScope() throws Exception {
        connectLanguageToDoc("a\na");
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });

        AutoEditStrategyScopeCreationHelper helper = new AutoEditStrategyScopeCreationHelper();
        TextSelectionUtils ps = new TextSelectionUtils(document, new TextSelection(document, 0, 3));
        helper.perform(ps, '\'', null, defaultScopeCreatingCharsProvider);
        assertEquals("'''a\na'''", document.get());
    }

    private void customizeDocumentCommand(String doc, DocCmd docCmd) throws Exception {
        connectLanguageToDoc(doc);
        //System.out.println(TestUtils.partition(document));
        strategy.customizeDocumentCommand(document, docCmd);
    }

    private void connectLanguageToDoc(String doc) throws Exception {
        strategy = new BaseAutoEditStrategy();
        document = new Document(doc);
        language = TestUtils.loadLanguageFile("python.liclipse");
        language.connect(document);
    }
}
