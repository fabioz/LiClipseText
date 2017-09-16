/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial code from https://github.com/Microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 *  - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 *  - Fabio Zadrozny <fabiofz@gmail.com> - Not adding '\n' on tokenize if it already finished with '\n'
 */
package org.eclipse.tm4e.core.internal.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.tm4e.core.grammar.GrammarHelper;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IGrammarRepository;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult2;
import org.eclipse.tm4e.core.grammar.Injection;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.internal.grammar.parser.Raw;
import org.eclipse.tm4e.core.internal.matcher.Matcher;
import org.eclipse.tm4e.core.internal.oniguruma.OnigString;
import org.eclipse.tm4e.core.internal.rule.IRuleFactory;
import org.eclipse.tm4e.core.internal.rule.IRuleFactoryHelper;
import org.eclipse.tm4e.core.internal.rule.Rule;
import org.eclipse.tm4e.core.internal.rule.RuleFactory;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;
import org.eclipse.tm4e.core.theme.IThemeProvider;
import org.eclipse.tm4e.core.theme.ThemeTrieElementRule;

/**
 * TextMate grammar implementation.
 *
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts
 *
 */
public class Grammar implements IGrammar, IRuleFactoryHelper {

	private int _rootId;
	private int _lastRuleId;
	private final Map<Integer, Rule> _ruleId2desc;
	private final Map<String, IRawGrammar> _includedGrammars;
	private final IGrammarRepository _grammarRepository;
	private final IRawGrammar _grammar;
	private List<Injection> _injections;
	private final ScopeMetadataProvider _scopeMetadataProvider;

	public Grammar(IRawGrammar grammar, int initialLanguage, Map<String, Integer> embeddedLanguages,
			IGrammarRepository grammarRepository, IThemeProvider themeProvider) {
		this._scopeMetadataProvider = new ScopeMetadataProvider(initialLanguage, themeProvider, embeddedLanguages);
		this._rootId = -1;
		this._lastRuleId = 0;
		this._includedGrammars = new HashMap<String, IRawGrammar>();
		this._grammarRepository = grammarRepository;
		this._grammar = initGrammar(grammar, null);
		this._ruleId2desc = new HashMap<Integer, Rule>();
		this._injections = null;
	}

	public void onDidChangeTheme() {
		this._scopeMetadataProvider.onDidChangeTheme();
	}

	public ScopeMetadata getMetadataForScope(String scope) {
		return this._scopeMetadataProvider.getMetadataForScope(scope);
	}

	public List<Injection> getInjections(StackElement states) {
		if (this._injections == null) {
			this._injections = new ArrayList<Injection>();
			// add injections from the current grammar
			Map<String, IRawRule> rawInjections = this._grammar.getInjections();
			if (rawInjections != null) {
				for (Entry<String, IRawRule> injection : rawInjections.entrySet()) {
					String expression = injection.getKey();
					IRawRule rule = injection.getValue();
					collectInjections(this._injections, expression, rule, this, this._grammar);
				}
			}

			// add injection grammars contributed for the current scope
			if (this._grammarRepository != null) {
				Collection<String> injectionScopeNames = this._grammarRepository
						.injections(this._grammar.getScopeName());
				if (injectionScopeNames != null) {
					injectionScopeNames.forEach(injectionScopeName -> {
						IRawGrammar injectionGrammar = this.getExternalGrammar(injectionScopeName);
						if (injectionGrammar != null) {
							String selector = injectionGrammar.getInjectionSelector();
							if (selector != null) {
								collectInjections(this._injections, selector, (IRawRule) injectionGrammar, this,
										injectionGrammar);
							}
						}
					});
				}
			}
		}
		if (this._injections.size() == 0) {
			return this._injections;
		}
		return this._injections.stream().filter(injection -> injection.match(states)).collect(Collectors.toList());
	}

	private void collectInjections(List<Injection> result, String selector, IRawRule rule,
			IRuleFactoryHelper ruleFactoryHelper, IRawGrammar grammar) {
		String[] subExpressions = selector.split(",");
		Arrays.stream(subExpressions).forEach(subExpression -> {
			String expressionString = subExpression.replaceAll("L:", "");
			result.add(new Injection(Matcher.createMatcher(expressionString),
					RuleFactory.getCompiledRuleId(rule, ruleFactoryHelper, grammar.getRepository()), grammar,
					expressionString.length() < subExpression.length()));
		});
	}

	public Rule registerRule(IRuleFactory factory) {
		int id = (++this._lastRuleId);
		Rule result = factory.create(id);
		this._ruleId2desc.put(id, result);
		return result;
	}

	public Rule getRule(int patternId) {
		return this._ruleId2desc.get(patternId);
	}

	public IRawGrammar getExternalGrammar(String scopeName) {
		return getExternalGrammar(scopeName, null);
	}

	@Override
	public IRawGrammar getExternalGrammar(String scopeName, IRawRepository repository) {
		if (this._includedGrammars.containsKey(scopeName)) {
			return this._includedGrammars.get(scopeName);
		} else if (this._grammarRepository != null) {
			IRawGrammar rawIncludedGrammar = this._grammarRepository.lookup(scopeName);
			if (rawIncludedGrammar != null) {
				this._includedGrammars.put(scopeName,
						initGrammar(rawIncludedGrammar, repository != null ? repository.getBase() : null));
				return this._includedGrammars.get(scopeName);
			}
		}
		return null;
	}

	private IRawGrammar initGrammar(IRawGrammar grammar, IRawRule base) {
		grammar = clone(grammar);
		if (grammar.getRepository() == null) {
			((Raw) grammar).setRepository(new Raw());
		}
		Raw self = new Raw();
		self.setPatterns(grammar.getPatterns());
		self.setName(grammar.getScopeName());
		grammar.getRepository().setSelf(self);
		if (base != null) {
			grammar.getRepository().setBase(base);
		} else {
			grammar.getRepository().setBase(grammar.getRepository().getSelf());
		}
		return grammar;
	}

	private IRawGrammar clone(IRawGrammar grammar) {
		return (IRawGrammar) ((Raw) grammar).clone();
	}

	@Override
	public ITokenizeLineResult tokenizeLine(String lineText) {
		return tokenizeLine(lineText, null);
	}

	@Override
	public ITokenizeLineResult tokenizeLine(String lineText, StackElement prevState) {
		return _tokenize(lineText, prevState, false);
	}

	@Override
	public ITokenizeLineResult2 tokenizeLine2(String lineText) {
		return tokenizeLine2(lineText, null);
	}

	@Override
	public ITokenizeLineResult2 tokenizeLine2(String lineText, StackElement prevState) {
		return _tokenize(lineText, prevState, true);
	}

	@SuppressWarnings("unchecked")
	private <T> T _tokenize(String lineText, StackElement prevState, boolean emitBinaryTokens) {
		if (this._rootId == -1) {
			this._rootId = RuleFactory.getCompiledRuleId(this._grammar.getRepository().getSelf(), this,
					this._grammar.getRepository());
		}

		boolean isFirstLine;
		if (prevState == null || prevState.equals(StackElement.NULL)) {
			isFirstLine = true;
			ScopeMetadata rawDefaultMetadata = this._scopeMetadataProvider.getDefaultMetadata();
			ThemeTrieElementRule defaultTheme = rawDefaultMetadata.themeData.get(0);
			int defaultMetadata = StackElementMetadata.set(0, rawDefaultMetadata.languageId, rawDefaultMetadata.tokenType, defaultTheme.fontStyle, defaultTheme.foreground, defaultTheme.background);

			String rootScopeName = this.getRule(this._rootId).getName(null, null);
			ScopeMetadata rawRootMetadata = this._scopeMetadataProvider.getMetadataForScope(rootScopeName);
			int rootMetadata = ScopeListElement.mergeMetadata(defaultMetadata, null, rawRootMetadata);

			ScopeListElement scopeList = new ScopeListElement(null, rootScopeName, rootMetadata);

			prevState = new StackElement(null, this._rootId, -1, null, scopeList, scopeList);
		} else {
			isFirstLine = false;
			prevState.reset();
		}

		if (lineText.isEmpty() || lineText.charAt(lineText.length() - 1) != '\n') {
			// Only add \n if the passed lineText didn't have it.
			lineText += '\n';
		}
		OnigString onigLineText = GrammarHelper.createOnigString(lineText);
		int lineLength = lineText.length();
		LineTokens lineTokens = new LineTokens(emitBinaryTokens, lineText, this._grammarRepository.getLogger());
		StackElement nextState = LineTokenizer._tokenizeString(this, onigLineText, isFirstLine, 0, prevState,
				lineTokens);

		if (emitBinaryTokens) {
			return (T) new TokenizeLineResult2(lineTokens.getBinaryResult(nextState, lineLength), nextState);
		}
		IToken[] result = lineTokens.getResult(nextState, lineLength);
		// return (T) new TokenizeLineResult(result, nextState);
		IToken[] convertedFromUtf8ToUtf16 = new IToken[result.length];
		for (int i = 0; i < result.length; i++) {
			IToken iToken = result[i];
			convertedFromUtf8ToUtf16[i] = iToken.toUtf16(onigLineText);
		}
		return (T) new TokenizeLineResult(convertedFromUtf8ToUtf16, nextState);
	}

	@Override
	public String getName() {
		return _grammar.getName();
	}

	@Override
	public String getScopeName() {
		return _grammar.getScopeName();
	}

	@Override
	public Collection<String> getFileTypes() {
		return _grammar.getFileTypes();
	}

}
