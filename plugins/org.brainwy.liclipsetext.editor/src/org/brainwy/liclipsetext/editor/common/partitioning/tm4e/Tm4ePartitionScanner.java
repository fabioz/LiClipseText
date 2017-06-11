package org.brainwy.liclipsetext.editor.common.partitioning.tm4e;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.ScannerHelper;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.languages.IStreamProvider;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.PartitionCodeReaderInScannerHelper;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.oniguruma.OnigString;
import org.eclipse.tm4e.core.registry.Registry;

public class Tm4ePartitionScanner implements ICustomPartitionTokenScanner {

	private IGrammar fGrammar;
	private final ScopeColorScanning scopeColoringScanning;
    private final ScannerHelper helper = new ScannerHelper();


	public Tm4ePartitionScanner(ScopeColorScanning scopeColoringScanning, LiClipseLanguage language) throws Exception {
		LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
		IGrammar grammar = languagesManager.getTm4EGrammar(language);
		this.fGrammar = grammar;

        helper.setLanguage(language);
        if (scopeColoringScanning != null) {
            this.scopeColoringScanning = scopeColoringScanning;
            this.scopeColoringScanning.freeze(language);
        } else {
            //This means that 'scope_definition_rules' was defined for the scope but
            //in the 'scope' which defines how to scan that scope there's nothing
            //defined (thus, the whole scope will not have sub-partitions and will
            //only have a single color).
            this.scopeColoringScanning = null;
        }
	}

    protected IToken fDefaultReturnToken = new Token(null);

    public void setDefaultReturnToken(IToken defaultReturnToken) {
        Assert.isNotNull(defaultReturnToken.getData());
        fDefaultReturnToken = defaultReturnToken;
    }

    public void nextToken(ScannerRange range) {
        if (range.nextOfferedToken()) {
            return;
        }
        range.startNextToken();

        //Check 0: EOF: just bail out
        int c = range.read();
        if (c == ICharacterScanner.EOF) {
            range.setToken(Token.EOF);
            return;
        }

        if (scopeColoringScanning == null) {
            range.setToken(fDefaultReturnToken);
            return;
        }

        //Note: check for whitespaces only after a rule.
        //As we may want to match a rule at the start of a line, we shouldn't handle all whitespaces
        //when one is found, only to the beginning of the next line.
        if (c == '\r' || c == '\n') {
            do {
                c = range.read();
            } while (c == '\r' || c == '\n'); //keep reading new lines until we get them all.
            range.unread();
            range.setToken(Token.WHITESPACE);
            return;

        }
        final int currOffset = range.getMark();
        Tuple<OnigString, Integer> lineFromOffsetAsBytes = range.getLineFromOffsetAsOnigString(currOffset);
        fGrammar.tokenizeLine(lineText, prevState)

        range.offerSubToken(sub2);

    }

    @Override
    public ScannerRange createPartialScannerRange(IDocument document, int offset, int length, String contentType,
            int partitionOffset) {
        return new ScannerRange(document, offset, length, contentType, partitionOffset,
                new PartitionCodeReaderInScannerHelper());
    }

    @Override
    public ScannerRange createScannerRange(IDocument document, int offset, int length) {
        return new ScannerRange(document, offset, length, new PartitionCodeReaderInScannerHelper());
    }

}
