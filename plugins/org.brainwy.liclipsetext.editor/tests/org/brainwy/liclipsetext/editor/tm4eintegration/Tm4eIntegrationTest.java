package org.brainwy.liclipsetext.editor.tm4eintegration;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.IStreamProvider;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.registry.Registry;

import junit.framework.TestCase;

public class Tm4eIntegrationTest extends TestCase{

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

	public void testTM4eIntegration() throws Exception {
		Registry registry = new Registry();

        LiClipseLanguage liclipseLanguage = new LanguagesManager(TestUtils.getTestLanguagesDir())
                .getLanguageFromName("source.ts");
        IStreamProvider streamProvider = liclipseLanguage.file.getStreamProvider();

		IGrammar grammar = registry.loadGrammarFromPathSync(".tmLanguage",
				streamProvider.getStream());

		ITokenizeLineResult result = grammar.tokenizeLine("/**");
		for (int i = 0; i < result.getTokens().length; i++) {
			System.err.println(result.getTokens()[i]);
		}
		result = grammar.tokenizeLine("**/ ", result.getRuleStack());
		for (int i = 0; i < result.getTokens().length; i++) {
			System.err.println(result.getTokens()[i]);
		}
	}
}
