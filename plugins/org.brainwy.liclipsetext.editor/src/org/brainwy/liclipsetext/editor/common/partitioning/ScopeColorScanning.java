/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.RulesFactory;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.OrderedMap;
import org.eclipse.core.runtime.AssertionFailedException;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;

/**
 * Defines the keywords, brackets, sub rules, etc.
 *
 * Used in the LiClipsePartitionScanner.
 */
public class ScopeColorScanning {

    public final Map<String, List<String>> tokenToWords = new HashMap<String, List<String>>();
    private final Map<String, IToken> wordToToken = new HashMap<String, IToken>();
    private RulesFactory rulesFactory; //may be null if we have no rules in the scanning
    private ILiClipsePredicateRule[] subRules; //may be null if we have no rules in the scanning
    private Set<Character> allValidCharsInWords = null;
    public final boolean caseInsensitive;
    private LiClipseLanguage language;

    public ScopeColorScanning(boolean caseInsensitive, LiClipseLanguage liClipseLanguage) {
        this.caseInsensitive = caseInsensitive;
        this.language = liClipseLanguage;
    }

    public ILiClipsePredicateRule[] getSubRules() {
        return subRules;
    }

    public void setKeywords(List<String> keywords) {
        tokenToWords.put("keyword", keywords);
    }

    public List<String> getKeywords() {
        List<String> list = tokenToWords.get("keyword");
        return list;
    }

    public void setBrackets(List<String> brackets) {
        tokenToWords.put("bracket", brackets);
    }

    public void setOperators(List<String> operators) {
        tokenToWords.put("operator", operators);
    }

    /**
     * @return true if we have more than one way to color things in the partition.
     */
    public boolean empty() {
        Set<Entry<String, List<String>>> entrySet = tokenToWords.entrySet();
        for (Entry<String, List<String>> entry : entrySet) {
            if (entry.getValue().size() > 0) {
                return false;
            }
        }
        if (subRules != null && subRules.length > 0) {
            return false;
        }
        return true;
    }

    public IToken getToken(String word) {
        return wordToToken.get(word);
    }

    /**
     * Updates the color for the words and sub-rules based on the colors in the preferences.
     */
    public void freeze(LiClipseLanguage language) {
        wordToToken.clear();
        Set<Entry<String, List<String>>> entrySet = tokenToWords.entrySet();
        for (Entry<String, List<String>> entry : entrySet) {
            List<String> value = entry.getValue();
            IToken token = new ContentTypeToken(entry.getKey()); // This could be a ColorToken...
            for (String word : value) {
                wordToToken.put(word, token);
            }
        }
    }

    public void setSubRules(ILiClipsePredicateRule[] subRules) {
        this.subRules = subRules;
    }

    public Map<String, Object> getDump() {
        Map<String, Object> dumpedTokenToWords = new OrderedMap<String, Object>(tokenToWords.size());
        dumpedTokenToWords.putAll(tokenToWords);
        if (rulesFactory != null) {
            dumpedTokenToWords.put("sub_rules", rulesFactory.getRulesDump());
        }
        return dumpedTokenToWords;
    }

    /**
     *
     * @param map -- see getDump().
     * @param ruleAliases
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void restoreDump(Map<String, Object> map, Map<String, Object> ruleAliases) {
        this.tokenToWords.clear();
        this.subRules = null;

        Map<String, List<String>> restoredTokenToWords = new OrderedMap<String, List<String>>();

        //Iterate to check if types are correct (as restoring from yaml, types may not really
        //match in the disk).
        Set<Entry<String, Object>> entrySet = map.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            if (!(key instanceof String)) {
                throw new AssertionFailedException("Expecting String as key for scope entry.");
            }
            if (key.equals("sub_rules")) {
                List value = (List) entry.getValue();
                rulesFactory = new RulesFactory(this.language);
                List<ILiClipsePredicateRule> r = rulesFactory.load(value, ruleAliases);
                if (r != null) {
                    this.subRules = r.toArray(new ILiClipsePredicateRule[r.size()]);
                }

            } else if (key.endsWith("-prefixes")) {
                Object value = entry.getValue();
                if (!(value instanceof String)) {
                    throw new AssertionError("Key ending with -prefix: " + key
                            + " is expected to point to value which maps to a String (current maps to: "
                            + value.getClass() + ").");
                }
                key = key.substring(0, key.length() - "-prefixes".length());
                List<String> list = restoredTokenToWords.get(key);
                List<String> copy = new ArrayList<String>(list);

                List<String> split = StringUtils.splitInWhiteSpaces((String) value);
                FastStringBuffer buf = new FastStringBuffer();
                int prefixesLen = split.size();
                for (int i = 0; i < prefixesLen; i++) {
                    String prefix = split.get(i);

                    int copyLen = copy.size();
                    for (int j = 0; j < copyLen; j++) {
                        buf.clear().append(prefix).append(copy.get(j));
                        list.add(buf.toString());
                    }
                }
            } else {
                List<String> words = (List<String>) entry.getValue();
                for (String word : words) {
                    if (!(word instanceof String)) {
                        throw new AssertionError("Expecting List<String> as value for scope entry.");
                    }
                }
                restoredTokenToWords.put(key, new ArrayList<String>(words));
            }
        }

        this.tokenToWords.putAll(restoredTokenToWords);
    }

    /**
     * @return the characters that may compose a keyword. Note: only consider from the color setting.
     * By default ignores the tokens for 'bracket' or 'operator'.
     *
     * Does not necessarily provide the valid chars or separators for a language, only the ones for which we have a color.
     */
    public Set<Character> getTokenChars() {
        if (allValidCharsInWords == null) {
            HashSet<Character> temp = new HashSet<Character>();
            Set<Entry<String, List<String>>> entrySet = tokenToWords.entrySet();
            for (Entry<String, List<String>> entry : entrySet) {
                if (isSeparator(entry.getKey())) {
                    continue;
                }
                List<String> value = entry.getValue();
                int len = value.size();

                for (int i = 0; i < len; i++) {
                    String replacementString = value.get(i);
                    int length = replacementString.length();
                    for (int j = 0; j < length; j++) {
                        char c = replacementString.charAt(j);
                        temp.add(c);
                        if (caseInsensitive) {
                            char c2 = Character.toUpperCase(c);
                            if (c2 == c) {
                                //Just in case it was upper, make it lower.
                                c2 = Character.toLowerCase(c);
                            }
                            temp.add(c2);
                        }
                    }
                }
            }
            allValidCharsInWords = temp;
        }
        return allValidCharsInWords;
    }

    private boolean isSeparator(String string) {
        return "bracket".equals(string) || "operator".equals(string);
    }

    /**
     * @return the characters to be considered separators.
     */
    public Set<Character> getSeparatorChars() {
        return language.getSeparatorChars();
    }

}