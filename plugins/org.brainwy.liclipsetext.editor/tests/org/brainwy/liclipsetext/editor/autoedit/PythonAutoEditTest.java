/**
 * Copyright: Fabio Zadrozny
 * License: EPL
 */
package org.brainwy.liclipsetext.editor.autoedit;

import org.brainwy.liclipsetext.editor.common.DefaultScopeCreatingCharsProvider;
import org.brainwy.liclipsetext.editor.common.ILiClipseLanguageProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyScopeCreationHelper;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;

import junit.framework.TestCase;

public class PythonAutoEditTest extends TestCase {

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

    public void testAutoCreateScope() throws Exception {
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });
        connectLanguageToDoc("aaa");
        AutoEditStrategyScopeCreationHelper helper = new AutoEditStrategyScopeCreationHelper();
        TextSelectionUtils ps = new TextSelectionUtils(document, new TextSelection(document, 0, 3));
        helper.perform(ps, '"', null, defaultScopeCreatingCharsProvider);
        assertEquals("\"aaa\"", document.get());
    }

    public void testAutoCreateScope2() throws Exception {
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });
        connectLanguageToDoc("aaa");
        AutoEditStrategyScopeCreationHelper helper = new AutoEditStrategyScopeCreationHelper();
        TextSelectionUtils ps = new TextSelectionUtils(document, new TextSelection(document, 3, 0));
        helper.perform(ps, '"', null, defaultScopeCreatingCharsProvider);
        assertEquals("aaa\"\"", document.get());
    }

    public void testAutoCreateScope3() throws Exception {
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });
        connectLanguageToDoc("a\na");
        AutoEditStrategyScopeCreationHelper helper = new AutoEditStrategyScopeCreationHelper();
        TextSelectionUtils ps = new TextSelectionUtils(document, new TextSelection(document, 0, 3));
        helper.perform(ps, '\'', null, defaultScopeCreatingCharsProvider);
        assertEquals("'''a\na'''", document.get());
    }

    public void testAutoCreateScope4() throws Exception {
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });
        connectLanguageToDoc("a = ");
        AutoEditStrategyScopeCreationHelper helper = new AutoEditStrategyScopeCreationHelper();
        TextSelectionUtils ps = new TextSelectionUtils(document, new TextSelection(document, 4, 0));
        helper.perform(ps, '\'', null, defaultScopeCreatingCharsProvider);
        assertEquals("a = ''", document.get());
    }

    public void testAutoCreateScope5() throws Exception {
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });
        connectLanguageToDoc("a = \na");
        AutoEditStrategyScopeCreationHelper helper = new AutoEditStrategyScopeCreationHelper();
        TextSelectionUtils ps = new TextSelectionUtils(document, new TextSelection(document, 4, 0));
        helper.perform(ps, '\'', null, defaultScopeCreatingCharsProvider);
        assertEquals("a = ''\na", document.get());
    }

    public void testAutoCreateScope6() throws Exception {
        defaultScopeCreatingCharsProvider = new DefaultScopeCreatingCharsProvider(new ILiClipseLanguageProvider() {

            public LiClipseLanguage getLanguage() {
                return language;
            }
        });
        connectLanguageToDoc("''");
        AutoEditStrategyScopeCreationHelper helper = new AutoEditStrategyScopeCreationHelper();
        TextSelectionUtils ps = new TextSelectionUtils(document, new TextSelection(document, 2, 0));
        helper.perform(ps, '\'', null, defaultScopeCreatingCharsProvider);
        assertEquals("''''''", document.get());
    }

    private void connectLanguageToDoc(String doc) throws Exception {
        document = new Document(doc);
        language = TestUtils.loadLanguageFile("python.liclipse");
        language.connect(document);
    }
}
