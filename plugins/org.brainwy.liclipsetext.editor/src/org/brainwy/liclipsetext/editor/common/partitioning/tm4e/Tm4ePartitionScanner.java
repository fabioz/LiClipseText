package org.brainwy.liclipsetext.editor.common.partitioning.tm4e;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.ScannerHelper;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.IStreamProvider;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.IPartitionCodeReaderInScannerHelper.LineInfo;
import org.brainwy.liclipsetext.editor.partitioning.PartitionCodeReaderInScannerHelper;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.DummyToken;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.registry.Registry;

public class Tm4ePartitionScanner implements ICustomPartitionTokenScanner {

	private IGrammar fGrammar;
	private final ScopeColorScanning scopeColoringScanning;
    private StackElement[] fLines;
	private LiClipseLanguage language;

	public Tm4ePartitionScanner(ScopeColorScanning scopeColoringScanning, LiClipseLanguage language) throws Exception {
		LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
		this.language = language;
		IGrammar grammar = languagesManager.getTm4EGrammar(language);
		this.fGrammar = grammar;

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
        final int currOffset = range.getMark();
        int c = range.read();
        if (c == ICharacterScanner.EOF) {
            range.setToken(Token.EOF);
            return;
        }

        if (scopeColoringScanning == null) {
            range.setToken(fDefaultReturnToken);
            return;
        }

        int lineFromOffset;
		try {
			lineFromOffset = range.getLineFromOffset(currOffset);
		} catch (BadLocationException e) {
			Log.log(e);
			range.setToken(fDefaultReturnToken);
			return;
		}

		LineInfo lineInfo = range.getLineAsString(lineFromOffset);
		String s = lineInfo.str;
        StackElement prevState = null;
    	if(lineFromOffset > 0) {
    		prevState = fLines[lineFromOffset-1];
    	}

		if(currOffset > lineInfo.lineOffset) {
			// Starting in the middle of a line (can't use prev state and must get line substring).
			prevState = null;
			s = s.substring(currOffset - lineInfo.lineOffset);
		}

		int lineEndOffset = currOffset + s.length()-1;
		if(lineEndOffset > range.getRangeEndOffset()) {
			// If the line is > than the current range
			s = s.substring(lineEndOffset - range.getRangeEndOffset()) + '\n';
			lineEndOffset = range.getRangeEndOffset();
		}

        range.setMark(lineEndOffset); // -1 because we get the line contents without new lines and always add a \n.

        // Now, skip the real lines.
        c = range.read();
        if(c == '\r') {
        	c = range.read();
        	if(c == '\n') {
        	}else {
        		range.unread();
        	}
        }else if(c == '\n'){
        }else {
        	range.unread();
        }

    	ITokenizeLineResult tokenizeLine = fGrammar.tokenizeLine(s, prevState);
    	fLines[lineFromOffset] = tokenizeLine.getRuleStack();

    	org.eclipse.tm4e.core.grammar.IToken[] tokens = tokenizeLine.getTokens();
    	if(tokens == null || tokens.length == 0) {
    		SubRuleToken wholeMatchSubRuleToken = new SubRuleToken(fDefaultReturnToken, currOffset, range.getMark()-currOffset);
    		range.setCurrentSubToken(wholeMatchSubRuleToken);

    	}else {
			org.eclipse.tm4e.core.grammar.IToken iToken = tokens[0];
			SubRuleToken subRuleToken = new SubRuleToken(getToken(iToken), currOffset+iToken.getStartIndex(), iToken.getEndIndex()-iToken.getStartIndex());
			range.setCurrentSubToken(subRuleToken);

    		for (int i = 1; i < tokens.length; i++) {
    			iToken = tokens[i];
    			subRuleToken = new SubRuleToken(getToken(iToken), currOffset+iToken.getStartIndex(), iToken.getEndIndex()-iToken.getStartIndex());
				range.offerSubToken(subRuleToken);
    		}

    		// Check last one to cover full range
    		int endOffset = subRuleToken.offset + subRuleToken.len;
    		subRuleToken.len += range.getMark() - endOffset;
    	}
    	return;
    }

    private IToken getToken(org.eclipse.tm4e.core.grammar.IToken iToken) {
		List<String> scopes = iToken.getScopes();
		return new ContentTypeToken(scopes.get(scopes.size()-1));
	}

	@Override
    public ScannerRange createPartialScannerRange(IDocument document, int offset, int length, String contentType,
            int partitionOffset) {
		checkCache(document, offset);
        return new ScannerRange(document, offset, length, contentType, partitionOffset,
                new PartitionCodeReaderInScannerHelper());
    }

	private void checkCache(IDocument document, int offset) {
		if(this.fLines == null || document.getNumberOfLines() != this.fLines.length) {
			this.clearCache(document, offset);
		}
	}

    @Override
    public ScannerRange createScannerRange(IDocument document, int offset, int length) {
    	if(this.fLines == null || document.getNumberOfLines() != this.fLines.length) {
    		this.fLines = new StackElement[document.getNumberOfLines()];
    	}
        return new ScannerRange(document, offset, length, new PartitionCodeReaderInScannerHelper());
    }

    @Override
    public void clearCache(IDocument document, int startAtOffset) {
    	if(fLines == null) {
    		fLines = new StackElement[document.getNumberOfLines()];
    		return;
    	}
    	try {
			int lineOfOffset = document.getLineOfOffset(startAtOffset);
			int numberOfLines = document.getNumberOfLines();

			if(numberOfLines != fLines.length) {
				StackElement[] newStack = new StackElement[numberOfLines];
				System.arraycopy(fLines, 0, newStack, 0, Math.min(fLines.length,lineOfOffset));
				fLines = newStack;
			}else {
				for(int i=lineOfOffset;i<numberOfLines;i++) {
					fLines[i] = null;
				}
			}

		} catch (BadLocationException e) {
			Log.log(e);
		}
    }
}
