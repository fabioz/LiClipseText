/**
 * Copyright: Fabio Zadrozny
 * License: EPL
 */
package org.brainwy.liclipsetext.editor.autoedit;

import junit.framework.TestCase;

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

public class DartAutoEditTest extends TestCase {

    private BaseAutoEditStrategy strategy;
    private Document document;
    private LiClipseLanguage language;
    private DefaultScopeCreatingCharsProvider defaultScopeCreatingCharsProvider;

    public void testCloseParens() throws Exception {
        String doc = "//comment";
        DocCmd docCmd = new DocCmd(doc.length() - 1, 0, "'");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("'", docCmd.text);

        doc = "[]";
        docCmd = new DocCmd(doc.length() - 1, 0, "(");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("()", docCmd.text);
    }

    public void testCloseScope() throws Exception {
        String doc = "";
        DocCmd docCmd = new DocCmd(doc.length() - 1, 0, "'");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("''", docCmd.text);
    }

    public void testSkipClose() throws Exception {
        String str = "()";
        DocCmd docCmd = new DocCmd(str.length() - 1, 0, ")");
        customizeDocumentCommand(str, docCmd);
        assertEquals("", docCmd.text);
    }

    public void testAutoCreateScope() throws Exception {
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });
        connectLanguageToDoc("aaa");
        AutoEditStrategyScopeCreationHelper helper = new AutoEditStrategyScopeCreationHelper();
        TextSelectionUtils ps = new TextSelectionUtils(document, new TextSelection(document, 0, 3));
        helper.perform(ps, '\'', null, defaultScopeCreatingCharsProvider);
        assertEquals("'aaa'", document.get());
    }

    public void testIndentInsiderBracesScope() throws Exception {
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });
        DocCmd docCmd = new DocCmd("func(){}".length() - 1, 0, "\n");
        customizeDocumentCommand("func(){}", docCmd);
        assertEquals("\n    \n", docCmd.text);
    }

    private void customizeDocumentCommand(String doc, DocCmd docCmd) throws Exception {
        connectLanguageToDoc(doc);
        //System.out.println(TestUtils.partition(document));
        strategy.customizeDocumentCommand(document, docCmd);
    }

    private void connectLanguageToDoc(String doc) throws Exception {
        strategy = new BaseAutoEditStrategy();
        document = new Document(doc);
        language = TestUtils.loadLanguageFile("dart.liclipse");
        language.connect(document);
    }
}
