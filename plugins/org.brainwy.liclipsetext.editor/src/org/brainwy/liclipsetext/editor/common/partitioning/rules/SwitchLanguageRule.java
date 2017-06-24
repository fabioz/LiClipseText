/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseContentTypeDefinitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.TargetLanguageToken;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.PatternRule;
import org.brainwy.liclipsetext.editor.rules.SubLanguageToken;
import org.brainwy.liclipsetext.editor.rules.SwitchLanguageToken;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Rule for switching a language. Everything within this rule should actually take place as
 * the different language loaded (including outline, indent, completions, etc).
 *
 * This rule is not really Ok because it'll start on <script and not where the language actually begins.
 * See: SwitchLanguageHtmlRule
 */
public class SwitchLanguageRule extends PatternRule implements ISwitchLanguageRule {

    private final String switchToLanguageName;
    private final LiClipseLanguage language;
    private final ICustomPartitionTokenScanner fScanner;
    private IToken token;

    public SwitchLanguageRule(String startSequence, String endSequence, IToken token, String targetLanguage) {
        super(startSequence, endSequence, new TargetLanguageToken(targetLanguage), '\0', false, false, false);
        this.token = token;
        this.switchToLanguageName = targetLanguage;
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        this.language = languagesManager.getLanguageFromName(this.switchToLanguageName);
        if (this.language != null) {
            this.fScanner = new LiClipseContentTypeDefinitionScanner(language);
        } else {
            this.fScanner = null;
        }
    }

    @Override
    public String toString() {
        return new FastStringBuffer("SwitchLanguageRule(", fStartSequence.length + fEndSequence.length + 30)
                .append("start: ")
                .append(fStartSequence)
                .append(" end: ")
                .append(fEndSequence)
                .append(" token: ")
                .appendObject(fToken)
                .append(" language: ")
                .appendObject(switchToLanguageName)
                .append(")")
                .toString();
    }

    @Override
    protected IToken doEvaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {
        if (resume) {
            return Token.UNDEFINED;
        }
        if (this.language == null) {
            return super.doEvaluate(scanner, resume);
        }

        IToken ret = super.doEvaluate(scanner, resume);
        if (!ret.isUndefined()) {
            ScannerRange tokenScanner = (ScannerRange) scanner;
            int offset = tokenScanner.getTokenOffset() + fStartSequence.length;
            int len = tokenScanner.getTokenLength() - (fStartSequence.length + fEndSequence.length);
            ScannerRange range = (ScannerRange) scanner;
            range.pushRange(offset, len);
            try {
                List<SubLanguageToken> subTokens = new ArrayList<SubLanguageToken>();
                subTokens.add(new SubLanguageToken("this", (String) token.getData(), tokenScanner.getTokenOffset(),
                        fStartSequence.length));
                IToken tok = range.nextToken(this.fScanner);
                String baseLanguage = (String) ret.getData();
                while (!tok.isEOF()) {
                    subTokens.add(
                            new SubLanguageToken(baseLanguage, (String) range.getToken().getData(),
                                    range.getTokenOffset(),
                                    range.getTokenLength()));
                    tok = range.nextToken(this.fScanner);
                }
                int offset2 = offset + len;
                subTokens.add(new SubLanguageToken("this", (String) token.getData(), offset2, fEndSequence.length));
                ret = new SwitchLanguageToken(baseLanguage, subTokens);
            } finally {
                range.popRange();
            }
        }
        return ret;
    }

    public List<LiClipseLanguage> getLanguages() {
        return Arrays.asList(this.language);
    }

}
