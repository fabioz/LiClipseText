/**
 * Copyright: Fabio Zadrozny
 * License: EPL
 */
package org.brainwy.liclipsetext.editor.autoedit;

import org.brainwy.liclipsetext.editor.autoedit.BaseAutoEditStrategy;
import org.brainwy.liclipsetext.editor.common.DefaultScopeCreatingCharsProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.utils.DocCmd;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class Jinja2AutoEditTest extends TestCase {

    private BaseAutoEditStrategy strategy;

    @Override
    protected void setUp() throws Exception {
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.clearLanguagesManager();
    }

    private Document document;
    private LiClipseLanguage language;
    private DefaultScopeCreatingCharsProvider defaultScopeCreatingCharsProvider;

    public void testCloseScope1() throws Exception {
        String doc = "";
        DocCmd docCmd = new DocCmd(0, 0, "{");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("{}", docCmd.text);
    }

    public void testCloseScope2() throws Exception {
        String doc = "{}";
        DocCmd docCmd = new DocCmd(1, 0, "{");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("{}", docCmd.text);
    }

    public void testCloseScope3() throws Exception {
        String doc = "{}";
        DocCmd docCmd = new DocCmd(1, 0, "#");
        customizeDocumentCommand(doc, docCmd);
        assertEquals("##", docCmd.text);
    }

    private void customizeDocumentCommand(String doc, DocCmd docCmd) throws Exception {
        connectLanguageToDoc(doc);
        //System.out.println(TestUtils.partition(document));
        strategy.customizeDocumentCommand(document, docCmd);
    }

    private void connectLanguageToDoc(String doc) throws Exception {
        strategy = new BaseAutoEditStrategy();
        document = new Document(doc);
        language = TestUtils.loadLanguageFile("jinja2.liclipse");
        language.connect(document);
    }
}
