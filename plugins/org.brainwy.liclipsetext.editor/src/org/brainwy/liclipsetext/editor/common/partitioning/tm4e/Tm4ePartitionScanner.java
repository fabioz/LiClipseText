package org.brainwy.liclipsetext.editor.common.partitioning.tm4e;

import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.IPartitionCodeReaderInScannerHelper.LineInfo;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;

public class Tm4ePartitionScanner implements ICustomPartitionTokenScanner {

    private static final boolean DEBUG = false;

    private IGrammar fGrammar;

    @SuppressWarnings("unused")
    private LiClipseLanguage language;

    public Tm4ePartitionScanner(LiClipseLanguage language) throws Exception {
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        this.language = language;
        IGrammar grammar = languagesManager.getTm4EGrammar(language);
        this.fGrammar = grammar;
    }

    public IGrammar getGrammar() {
        return fGrammar;
    }

    protected IToken fDefaultReturnToken = new Token(null);

    @Override
    public void setDefaultReturnToken(IToken defaultReturnToken) {
        Assert.isNotNull(defaultReturnToken.getData());
        fDefaultReturnToken = defaultReturnToken;
    }

    @Override
    public IToken getDefaultReturnToken() {
        return fDefaultReturnToken;
    }

    @Override
    public void nextToken(ScannerRange range) throws DocumentTimeStampChangedException {
        int lastEndOffset = range.getTokenOffset() + range.getTokenLength();

        if (range.nextOfferedToken()) {
            return;
        }

        range.startNextToken();

        //Check 0: EOF: just bail out
        final int currOffset = range.getMark();

        if (currOffset < lastEndOffset) {
            range.checkDocumentTimeStampChanged();
            Log.log("Error computing tokens. Start offset " + currOffset + " < Last offset " + lastEndOffset);
        }

        int c = range.read();
        if (c == ICharacterScanner.EOF) {
            range.finishTm4ePartition(fGrammar);
            range.setToken(Token.EOF);
            return;
        }

        int lineFromOffset;
        try {
            lineFromOffset = range.getLineFromOffset(currOffset);
        } catch (BadLocationException e) {
            range.checkDocumentTimeStampChanged();
            Log.log(e);
            range.setToken(fDefaultReturnToken);
            return;
        }

        LineInfo lineInfo = range.getLineAsString(lineFromOffset);
        String s = lineInfo.str;

        if (currOffset > lineInfo.lineOffset) {
            // Starting in the middle of a line (can't use prev state and must get line substring).
            s = s.substring(currOffset - lineInfo.lineOffset);
            if (DEBUG) {
                System.out.println("Starting at the middle of line!");
            }
        }

        int lineEndOffset = currOffset + s.length() - 1;
        if (lineEndOffset > range.getRangeEndOffset()) {
            // If the line is > than the current range
            s = s.substring(lineEndOffset - range.getRangeEndOffset()) + '\n';
            lineEndOffset = range.getRangeEndOffset();
        }

        range.setMark(lineEndOffset); // -1 because we get the line contents without new lines and always add a \n.

        // Now, skip the real lines.
        c = range.read();
        if (c == '\r') {
            c = range.read();
            if (c == '\n') {
            } else {
                range.unread();
            }
        } else if (c == '\n') {
        } else {
            range.unread();
        }

        if (DEBUG) {
            System.out.println("Tokenizing line: " + lineFromOffset + " -- " + s);
        }
        ITokenizeLineResult tokenizeLine = range.tokenizeLine(currOffset, lineFromOffset, s, fGrammar);
        org.eclipse.tm4e.core.grammar.IToken[] tokens = tokenizeLine.getTokens();

        if (tokens == null || tokens.length == 0) {
            SubRuleToken wholeMatchSubRuleToken = new SubRuleToken(fDefaultReturnToken, currOffset,
                    range.getMark() - currOffset);
            range.setCurrentSubToken(wholeMatchSubRuleToken);

        } else {
            org.eclipse.tm4e.core.grammar.IToken iToken = tokens[0];
            SubRuleToken subRuleToken = new SubRuleToken(getToken(iToken), currOffset + iToken.getStartIndex(),
                    iToken.getEndIndex() - iToken.getStartIndex());
            if (subRuleToken.offset < lastEndOffset) {
                range.checkDocumentTimeStampChanged();
                Log.log("Error computing first token. Start offset " + subRuleToken.offset + " < Last offset "
                        + lastEndOffset);
            }
            lastEndOffset = subRuleToken.offset + subRuleToken.len;

            range.setCurrentSubToken(subRuleToken);

            for (int i = 1; i < tokens.length; i++) {
                iToken = tokens[i];
                subRuleToken = new SubRuleToken(getToken(iToken), currOffset + iToken.getStartIndex(),
                        iToken.getEndIndex() - iToken.getStartIndex());

                if (subRuleToken.offset < lastEndOffset) {
                    range.checkDocumentTimeStampChanged();
                    Log.log("Error computing token. Start offset " + subRuleToken.offset
                            + " < Last offset" + lastEndOffset);
                }
                lastEndOffset = subRuleToken.offset + subRuleToken.len;

                range.offerSubToken(subRuleToken);
            }

            // Check last one to cover full range
            int endOffset = subRuleToken.offset + subRuleToken.len;
            int diff = range.getMark() - endOffset;
            if (diff != 0) {
                if (diff > 0) {
                    subRuleToken.len += diff;
                } else {
                    // diff < 0
                    if (Math.abs(diff) >= subRuleToken.len) {
                        subRuleToken.len = 0;
                    } else {
                        subRuleToken.len += diff;
                    }
                }
            }
        }
    }

    private IToken getToken(org.eclipse.tm4e.core.grammar.IToken iToken) {
        List<String> scopes = iToken.getScopes();
        return new ContentTypeToken(scopes.get(scopes.size() - 1));
    }

}
