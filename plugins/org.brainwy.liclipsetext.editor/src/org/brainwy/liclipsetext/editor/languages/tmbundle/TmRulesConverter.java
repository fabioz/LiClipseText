/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.ILanguageDependentRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.ITextMateRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.MatchWhileAnySubRuleMatches;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmBeginEndRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmBeginWhileRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmIncludeRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmMatchRule;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.structure.OrderedMap;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;

public class TmRulesConverter {

    private LiClipseLanguage language;

    public TmRulesConverter(LiClipseLanguage language) {
        this.language = language;
    }

    @SuppressWarnings("rawtypes")
    public ITextMateRule convertDictToRule(Map map, String key) {
        Object objName = map.get("name");
        String ruleName = null;
        if (objName != null) {
            ruleName = objName.toString();
        }

        Object match = map.get("match");
        Object captures = map.get("captures");
        final ContentTypeToken token = new ContentTypeToken(ruleName);
        if (match instanceof String) {
            Map<Object, Object> capturesMap = null;
            if (captures != null) {
                if (captures instanceof Map) {
                    capturesMap = convertToCapturesmap(captures, token);

                } else {
                    Log.log("Expected: " + captures + " to be a map.");
                }
            }
            TmMatchRule tmMatchRule = new TmMatchRule((String) match, token, capturesMap);
            onCreatedRule(tmMatchRule);
            return tmMatchRule;
        }

        Object include = map.get("include");
        if (include instanceof String) {
            TmIncludeRule rule = new TmIncludeRule((String) include);
            onCreatedRule(rule);
            return rule;
        }

        Object begin = map.get("begin");
        Object end = map.get("end");
        Object whileStr = map.get("while");

        Object beginCaptures = map.get("beginCaptures");
        Object endCaptures = map.get("endCaptures");
        Object whileCaptures = map.get("whileCaptures");

        if (beginCaptures == null) {
            beginCaptures = captures;
        }
        if (endCaptures == null) {
            endCaptures = captures;
        }
        if (whileCaptures == null) {
            whileCaptures = captures;
        }
        Object patterns = map.get("patterns");
        Object applyEndPatternLast = map.get("applyEndPatternLast");
        Object contentName = map.get("contentName");

        if (begin instanceof String && whileStr instanceof String) {
            List<ILiClipsePredicateRule> subRules = creatuSubRulesFromPatterns(ruleName, patterns);
            TmBeginWhileRule rule = new TmBeginWhileRule((String) begin, (String) whileStr,
                    convertToCapturesmap(beginCaptures, token), convertToCapturesmap(whileCaptures, token),
                    subRules, token, new ContentTypeToken(contentName));
            onCreatedRule(rule);
            return rule;
        }

        if (begin instanceof String && end instanceof String && (beginCaptures == null || beginCaptures instanceof Map)
                && (endCaptures == null || endCaptures instanceof Map)) {
            List<ILiClipsePredicateRule> subRules = creatuSubRulesFromPatterns(ruleName, patterns);

            int applyLast = 0;
            if (applyEndPatternLast != null) {
                if (applyEndPatternLast instanceof Integer) {
                    applyLast = (Integer) applyEndPatternLast;
                } else {
                    try {
                        applyLast = Integer.parseInt(applyEndPatternLast.toString());
                    } catch (NumberFormatException e) {
                        Log.log("Expected applyEndPatternLast to be an integer. Found: " + applyEndPatternLast);
                    }
                }
            }
            TmBeginEndRule rule = new TmBeginEndRule((String) begin, (String) end,
                    convertToCapturesmap(beginCaptures, token),
                    convertToCapturesmap(endCaptures, token), subRules, token, new ContentTypeToken(
                            contentName),
                    applyLast);
            onCreatedRule(rule);
            return rule;
        }

        Object object = map.get("patterns");
        if (object instanceof List) {
            List list = (List) object;
            ArrayList<ILiClipsePredicateRule> patternRules = new ArrayList<>(list.size());
            for (Object o : list) {
                if (o instanceof Map) {
                    ILiClipsePredicateRule rule = convertDictToRule((Map) o, ruleName);
                    patternRules.add(rule);
                }
            }

            MatchWhileAnySubRuleMatches rule = new MatchWhileAnySubRuleMatches(patternRules, token);
            onCreatedRule(rule);
            return rule;
        }

        if (map != null && "{comment=Note how all @rules are prefixed.}".equals(map.toString())) {
            // Don't warn
        } else {
            Log.log("Could not transform: " + map + " to a rule.");
        }
        return null;
    }

    private List<ILiClipsePredicateRule> creatuSubRulesFromPatterns(String ruleName, Object patterns) {
        List<ILiClipsePredicateRule> subRules = null;
        if (patterns instanceof List) {
            List list = (List) patterns;
            subRules = new ArrayList<>(list.size());
            for (Object object : list) {
                if (object instanceof Map) {
                    ILiClipsePredicateRule converted = convertDictToRule((Map) object, ruleName);
                    if (converted != null) {
                        subRules.add(converted);
                    }
                } else {
                    Log.log("Expected: " + object + " to be a Map.");
                }
            }
        }
        return subRules;
    }

    private void onCreatedRule(ILiClipsePredicateRule rule) {
        if (rule instanceof ILanguageDependentRule) {
            ((ILanguageDependentRule) rule).setLanguage(language);
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<Object, Object> convertToCapturesmap(Object captures, ContentTypeToken token) {
        if (captures == null) {
            return null;
        }
        Set<Map.Entry> entrySet = ((Map) captures).entrySet();
        Map<Object, Object> capturesMap = new OrderedMap<>();
        for (Map.Entry entry : entrySet) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            try {
                Object scopeName;
                Object patterns;
                if (value instanceof Map) {
                    Map map = (Map) value;
                    if (map.isEmpty()) {
                        continue;
                    }
                    scopeName = map.get("name");
                    patterns = map.get("patterns");
                    int keyValueAsInt = Integer.parseInt(key.toString());

                    if (patterns != null) {
                        // Test: {
                        // 1={patterns=[
                        //              {match=[a-zA-Z0-9_]+, name=entity.other.inherited-class.php},
                        //              {match=,, name=punctuation.separator.classes.php}]},
                        // 2={name=entity.other.inherited-class.php}}
                        List<ILiClipsePredicateRule> subRulesFromPatterns = creatuSubRulesFromPatterns(null, patterns);
                        if (subRulesFromPatterns != null) {
                            MatchWhileAnySubRuleMatches matchWhileSubRulesMatches = new MatchWhileAnySubRuleMatches(
                                    subRulesFromPatterns, scopeName == null ? token : new ContentTypeToken(scopeName));
                            capturesMap.put(keyValueAsInt, matchWhileSubRulesMatches);
                        }

                    } else if (scopeName instanceof String) {
                        capturesMap.put(keyValueAsInt, new ContentTypeToken(scopeName));

                    } else {
                        Log.log("Expected: " + scopeName + " to be a String (in captures -- key: '" + key
                                + "'). Construct: "
                                + captures
                                + " Language: "
                                + this.language.name);
                    }
                } else {
                    Log.log("Expected: " + value + " to be a Map.");
                    continue;
                }
            } catch (Exception e) {
                Log.log("Error interpreting captures value: " + value + " key: '" + key + "'. Construct: "
                        + captures
                        + " Language: "
                        + this.language.name);
            }
        }
        return capturesMap;
    }
}
