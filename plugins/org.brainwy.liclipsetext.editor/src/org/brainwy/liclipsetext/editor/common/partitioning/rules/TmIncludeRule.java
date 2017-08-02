/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.Arrays;
import java.util.Map;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.IFullScanner2;
import org.brainwy.liclipsetext.editor.common.partitioning.IRuleWithSubRules2;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.DummyToken;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class TmIncludeRule implements ILiClipsePredicateRule, IRuleWithSubRules, IRuleWithSubRules2, IChangeTokenRule,
        ILanguageDependentRule, ITextMateRule, IPrintableRule {

    private String include;
    private LiClipseLanguage language;
    private ILiClipsePredicateRule wrappedRule;
    private boolean resolved = false;
    private IToken fToken = new DummyToken("TmIncludeRule: " + this.include + " " + this.hashCode());

    public TmIncludeRule(String include) {
        Assert.isNotNull(include);
        this.include = include;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) throws DocumentTimeStampChangedException {
        resolve();
        if (wrappedRule != null) {
            IToken ret = wrappedRule.evaluate(scanner);
            if (!ret.isUndefined()) {
                return fToken;
            }
        }
        return Token.UNDEFINED;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {
        if (resume) { //non-resumable
            return Token.UNDEFINED;
        }
        return evaluate(scanner);
    }

    private void resolve() {
        if (!resolved) {
            resolved = true;
            if (include.startsWith("#")) {
                Map<String, ILiClipsePredicateRule> ruleAliases = language.ruleAliases;
                ILiClipsePredicateRule rule = ruleAliases.get(include.substring(1));
                if (rule == null) {
                    Log.log("Unable to resolve repository include: " + include + " in language: " + this.language.name);
                } else {
                    rule = new MatchWhileAnySubRuleMatches(Arrays.asList(rule), new DummyToken(
                            language.name));
                    this.wrappedRule = rule;
                }

            } else if (include.equals("$self")) {
                this.wrappedRule = new MatchWhileAnySubRuleMatches(language.rules, new DummyToken(
                        language.name));

            } else {
                LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                LiClipseLanguage languageFromName = languagesManager.getLanguageFromName(include);
                if (languageFromName == null) {
                    Log.log("Unable to resolve language include: " + include + " in language: " + this.language.name);
                } else {
                    this.wrappedRule = new MatchWhileAnySubRuleMatches(languageFromName.rules, new DummyToken(
                            languageFromName.name));
                }

            }
            if (wrappedRule instanceof ILanguageDependentRule) {
                ((ILanguageDependentRule) wrappedRule).setLanguage(language);
            }
        }
    }

    @Override
    public void setToken(IToken token) {
        this.fToken = token;
    }

    @Override
    public SubRuleToken evaluateSubRules(ScannerRange scanner, boolean generateSubRuleTokens)
            throws DocumentTimeStampChangedException {
        resolve();
        if (wrappedRule instanceof IRuleWithSubRules) {
            return ((IRuleWithSubRules) wrappedRule).evaluateSubRules(scanner, generateSubRuleTokens);
        }
        if (wrappedRule != null) {
            //I.e.: wrap for the protocol to keep working.
            IFullScanner2 scanner2 = (IFullScanner2) scanner;
            int mark = scanner2.getMark();

            IToken evaluate = wrappedRule.evaluate(scanner);
            if (!evaluate.isUndefined()) {
                return new SubRuleToken(evaluate, mark, scanner2.getMark() - mark);
            }

        }
        return null;
    }

    @Override
    public IToken getSuccessToken() {
        return fToken;
    }

    @Override
    public void setLanguage(LiClipseLanguage liClipseLanguage) {
        this.language = liClipseLanguage;
        if (liClipseLanguage != null) {
            this.fToken = new ContentTypeToken(liClipseLanguage.name + ".include");
        }
    }

    @Override
    public String toString() {
        return "TmIncludeRule: " + this.include;
    }

    @Override
    public String toTmYaml() {
        return "{ include: " + this.include + " }";
    }

    @Override
    public String toTmYaml(int level) {
        return toTmYaml();
    }
}
