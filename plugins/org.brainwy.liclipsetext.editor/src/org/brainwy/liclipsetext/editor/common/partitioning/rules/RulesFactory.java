/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguageIO;
import org.brainwy.liclipsetext.editor.rules.EndOfLineRule;
import org.brainwy.liclipsetext.editor.rules.PatternRule;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.structure.LowMemoryArrayList;
import org.brainwy.liclipsetext.shared_core.structure.OrderedMap;
import org.eclipse.core.runtime.AssertionFailedException;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class RulesFactory {

    private Stack s = new Stack();
    private final LiClipseLanguage liClipseLanguage;

    public RulesFactory(LiClipseLanguage liClipseLanguage) {
        this.liClipseLanguage = liClipseLanguage;
    }

    private void pushRulesDumpRepresentation() {
        s.push(rulesDumpRepresentation);
        rulesDumpRepresentation = new ArrayList();
    }

    private List popRulesDumpRepresentation() {
        List curr = rulesDumpRepresentation;
        rulesDumpRepresentation = (List) s.pop();
        return curr;
    }

    private List rulesDumpRepresentation = new ArrayList();

    private void addToIo(Map m) {
        LiClipseLanguageIO.fixScopeKeyToIo(m);
        rulesDumpRepresentation.add(m);
    }

    public List getRulesDump() {
        return rulesDumpRepresentation;
    }

    private void checkCleared(Map m) {
        if (m.size() > 0) {
            throw new RuntimeException("Unexpected keys: " + m.keySet());
        }
    }

    private Object removeFromMap(Map<String, Object> map, String key) {
        return removeFromMap(map, key, null);
    }

    private int removeFromMap(Map<String, Object> map, String key, int defaultReturn) {
        Object ret = map.remove(key);
        if (ret == null) {
            return defaultReturn;
        }
        if (ret instanceof Integer) {
            return (Integer) ret;
        }
        return Integer.parseInt(ret.toString());
    }

    private Object removeFromMap(Map<String, Object> map, String key, Object defaultReturn) {
        Object ret = map.remove(key);
        if (ret == null) {
            if (defaultReturn != null) {
                return defaultReturn;
            }
            throw new RuntimeException("Error: expected " + key + " to be defined.");
        }
        return ret;
    }

    //=============================================================================== Composite rule
    //=============================================================================== Composite rule
    //=============================================================================== Composite rule
    // Note: kept out of cog because it's a special case (sub rules)
    private static final String COMPOSITE_RULE = "CompositeRule";

    private ILiClipsePredicateRule createCompositeRule(Map<String, Object> m) {
        ILiClipsePredicateRule rule = createCompositeRule((List) removeFromMap(m, "sub_rules"));
        checkCleared(m);
        return rule;
    }

    private ILiClipsePredicateRule createCompositeRule(List<Map> subRules) {
        List<String> subRulesDumpRepresentation = null;
        List<ILiClipsePredicateRule> loadedSubRules = null;

        pushRulesDumpRepresentation();
        loadedSubRules = this.load(subRules);
        subRulesDumpRepresentation = popRulesDumpRepresentation();

        Map m = new OrderedMap(2);
        m.put("type", COMPOSITE_RULE);
        m.put("sub_rules", subRulesDumpRepresentation);
        addToIo(m);
        return new CompositeRule(loadedSubRules);
    }

    //============================================================================= TmBeginEndRule
    //============================================================================= TmBeginEndRule
    //============================================================================= TmBeginEndRule
    private static final String TM_BEGIN_END_RULE = "TmBeginEndRule";

    public ILiClipsePredicateRule createTmBeginEndRule(Map m) {
        ILiClipsePredicateRule createTmBeginEndRule = createTmBeginEndRule(
                (String) removeFromMap(m, "begin"),
                (String) removeFromMap(m, "end"),
                (Map) removeFromMap(m, "beginCaptures", new TreeMap<>()),
                (Map) removeFromMap(m, "endCaptures", new TreeMap<>()),
                (String) removeFromMap(m, "scope"),
                (String) removeFromMap(m, "contentScope", ""),
                (List<Map>) removeFromMap(m, "patterns", new LowMemoryArrayList<>()),
                removeFromMap(m, "applyEndPatternLast", 0));
        checkCleared(m);
        return createTmBeginEndRule;
    }

    public ILiClipsePredicateRule createTmBeginEndRule(String begin, String end, Map beginCaptures, Map endCaptures,
            String scope, String contentScope, List<Map> subRules, int applyEndPatternLast) {

        List<String> subRulesDumpRepresentation = null;
        List<ILiClipsePredicateRule> loadedSubRules = null;

        pushRulesDumpRepresentation();
        loadedSubRules = this.load(subRules);
        subRulesDumpRepresentation = popRulesDumpRepresentation();

        Map m = new OrderedMap(9);
        m.put("type", TM_BEGIN_END_RULE);
        m.put("begin", begin);
        m.put("end", end);
        m.put("beginCaptures", beginCaptures);
        m.put("endCaptures", endCaptures);
        m.put("scope", scope);
        m.put("contentScope", contentScope);
        m.put("patterns", subRules);
        m.put("applyEndPatternLast", applyEndPatternLast);
        addToIo(m);

        return new TmBeginEndRule(begin, end, beginCaptures, endCaptures, loadedSubRules, new ContentTypeToken(scope),
                new ContentTypeToken(contentScope), applyEndPatternLast);
    }

    //=============================================================================== SingleLine rule with skip
    //=============================================================================== SingleLine rule with skip
    //=============================================================================== SingleLine rule with skip
    // Note: kept out of cog because it's a special case (sub rules)
    private static final String SINGLE_LINE_RULE_WITH_SKIP = "SingleLineRuleWithSkip";

    private ILiClipsePredicateRule createSingleLineRuleWithSkip(Map<String, Object> m) {
        ArrayList<Object> dummyDefaultReturn = new ArrayList<>();
        List skipRules = (List) removeFromMap(m, "skip_rules", dummyDefaultReturn);
        if (skipRules == dummyDefaultReturn) {
            skipRules = (List) removeFromMap(m, "skipRules");
        }

        ILiClipsePredicateRule rule = createSingleLineRuleWithSkip((String) removeFromMap(m, "start"),
                (String) removeFromMap(m, "scope"),
                extractChar(removeFromMap(m, "escapeCharacter")),
                (Boolean) removeFromMap(m, "escapeContinuesLine"),
                skipRules);
        checkCleared(m);
        return rule;
    }

    /**
     * @param start can be either a string with a sequence or a CompositeRule (Map)
     */
    private ILiClipsePredicateRule createSingleLineRuleWithSkip(String start, String scope,
            Character escapeCharacter, boolean escapeContinuesLine, List<Map> subRules) {

        List<String> subRulesDumpRepresentation = null;
        List<ILiClipsePredicateRule> loadedSubRules = null;

        // Skip rules always a list
        pushRulesDumpRepresentation();
        loadedSubRules = this.load(subRules);
        subRulesDumpRepresentation = popRulesDumpRepresentation();

        //Start can be either a String or list of rules.
        Map m = new OrderedMap(6);
        m.put("type", SINGLE_LINE_RULE_WITH_SKIP);
        m.put("start", start);

        m.put("scope", scope);
        m.put("escapeCharacter", escapeCharacter);
        m.put("escapeContinuesLine", escapeContinuesLine);
        m.put("skipRules", subRulesDumpRepresentation);
        addToIo(m);
        return new SingleLineRuleWithSkip(start, new ContentTypeToken(scope), escapeCharacter, escapeContinuesLine,
                loadedSubRules);

    }

    //=============================================================================== MultiLine rule with skip
    //=============================================================================== MultiLine rule with skip
    //=============================================================================== MultiLine rule with skip
    // Note: kept out of cog because it's a special case (sub rules)
    private static final String MULTI_LINE_RULE_WITH_SKIP = "MultiLineRuleWithSkip";

    private ILiClipsePredicateRule createMultiLineRuleWithSkip(Map<String, Object> m) {
        ArrayList<Object> dummyDefaultReturn = new ArrayList<>();
        List skipRules = (List) removeFromMap(m, "skip_rules", dummyDefaultReturn);
        if (skipRules == dummyDefaultReturn) {
            skipRules = (List) removeFromMap(m, "skipRules");
        }
        ILiClipsePredicateRule rule = createMultiLineRuleWithSkip(removeFromMap(m, "start"),
                (String) removeFromMap(m, "end"),
                (String) removeFromMap(m, "scope"),
                extractChar(removeFromMap(m, "escapeCharacter")),
                skipRules);
        checkCleared(m);
        return rule;
    }

    /**
     * @param start can be either a string with a sequence or a CompositeRule (Map)
     */
    private ILiClipsePredicateRule createMultiLineRuleWithSkip(Object start, String end, String scope,
            Character escapeCharacter, List<Map> subRules) {

        List<String> subRulesDumpRepresentation = null;
        List<ILiClipsePredicateRule> loadedSubRules = null;
        List<String> startSubRulesDumpRepresentation = null;
        List<ILiClipsePredicateRule> startLoadedSubRules = null;

        // Skip rules always a list
        pushRulesDumpRepresentation();
        loadedSubRules = this.load(subRules);
        subRulesDumpRepresentation = popRulesDumpRepresentation();

        //Start can be either a String or list of rules.

        if (start instanceof String) {
        } else {
            //Expect it to be a list of rules.
            pushRulesDumpRepresentation();
            startLoadedSubRules = this.load((List) start);
            startSubRulesDumpRepresentation = popRulesDumpRepresentation();
        }

        Map m = new OrderedMap(6);
        m.put("type", MULTI_LINE_RULE_WITH_SKIP);
        if (startSubRulesDumpRepresentation == null) {
            m.put("start", start);
        } else {
            m.put("start", startSubRulesDumpRepresentation);
        }

        m.put("end", end);
        m.put("scope", scope);
        m.put("escapeCharacter", escapeCharacter);
        m.put("skip_rules", subRulesDumpRepresentation);
        addToIo(m);
        if (startLoadedSubRules == null) {
            return new MultiLineRuleWithSkip((String) start, end, new ContentTypeToken(scope), escapeCharacter,
                    loadedSubRules);
        } else {
            return new MultiLineRuleWithSkipAndStartRules(startLoadedSubRules, end, new ContentTypeToken(scope),
                    escapeCharacter,
                    loadedSubRules);
        }

    }

    //=============================================================================== MultiLine rule
    //=============================================================================== MultiLine rule
    //=============================================================================== MultiLine rule
    // Note: kept out of cog because it's a special case (sub rules)
    private static final String MULTI_LINE_RULE_RECURSIVE = "MultiLineRuleRecursive";

    private ILiClipsePredicateRule createMultiLineRuleRecursive(Map<String, Object> m) {
        ILiClipsePredicateRule rule = createMultiLineRuleRecursive(removeFromMap(m, "start"),
                (String) removeFromMap(m, "end"),
                (String) removeFromMap(m, "scope"),
                extractChar(removeFromMap(m, "escapeCharacter")),
                (List) removeFromMap(m, "skip_rules"));
        checkCleared(m);
        return rule;
    }

    /**
     * @param start can be either a string with a sequence or a CompositeRule (Map)
     */
    private ILiClipsePredicateRule createMultiLineRuleRecursive(Object start, String end, String scope,
            Character escapeCharacter, List<Map> subRules) {

        List<String> subRulesDumpRepresentation = null;
        List<ILiClipsePredicateRule> loadedSubRules = null;
        List<String> startSubRulesDumpRepresentation = null;
        List<ILiClipsePredicateRule> startLoadedSubRules = null;

        // Skip rules always a list
        pushRulesDumpRepresentation();
        loadedSubRules = this.load(subRules);
        subRulesDumpRepresentation = popRulesDumpRepresentation();

        //Start can be either a String or list of rules.

        if (start instanceof String) {
        } else {
            //Expect it to be a list of rules.
            pushRulesDumpRepresentation();
            startLoadedSubRules = this.load((List) start);
            startSubRulesDumpRepresentation = popRulesDumpRepresentation();
        }

        Map m = new OrderedMap(6);
        m.put("type", MULTI_LINE_RULE_RECURSIVE);
        if (startSubRulesDumpRepresentation == null) {
            m.put("start", start);
        } else {
            m.put("start", startSubRulesDumpRepresentation);
        }

        m.put("end", end);
        m.put("scope", scope);
        m.put("escapeCharacter", escapeCharacter);
        m.put("skip_rules", subRulesDumpRepresentation);
        addToIo(m);
        //        if (startLoadedSubRules == null) {
        return new MultiLineRuleRecursive((String) start, end, new ContentTypeToken(scope), escapeCharacter,
                loadedSubRules);
        // Start rules not supported in this case (for now).
        //        } else {
        //            return new MultiLineRuleRecursiveAndStartRules(startLoadedSubRules, end, new ContentTypeToken(scope), escapeCharacter,
        //                    loadedSubRules);
        //        }

    }

    //============================================================================= IndentedBlockRule
    //============================================================================= IndentedBlockRule
    //============================================================================= IndentedBlockRule
    // Note: kept out of cog because it's a special case (sub rules)
    private static final String INDENTED_BLOCK_RULE = "IndentedBlockRule";

    public ILiClipsePredicateRule createIndentedBlockRule(Map m) {
        ILiClipsePredicateRule createIndentedBlockRule = createIndentedBlockRule(
                (String) removeFromMap(m, "start"),
                (String) removeFromMap(m, "scope"),
                (List) removeFromMap(m, "additional_start", new ArrayList()),
                removeFromMap(m, "column", 0));
        checkCleared(m);
        return createIndentedBlockRule;
    }

    public ILiClipsePredicateRule createIndentedBlockRule(String start, String scope, List<Map> additionalStart, int column) {
        List<String> subRulesDumpRepresentation = null;
        List<ILiClipsePredicateRule> loadedSubRules = null;

        pushRulesDumpRepresentation();
        loadedSubRules = this.load(additionalStart);
        subRulesDumpRepresentation = popRulesDumpRepresentation();

        Map m = new OrderedMap(4);
        m.put("type", INDENTED_BLOCK_RULE);
        m.put("start", start);
        m.put("scope", scope);
        m.put("additional_start", subRulesDumpRepresentation);
        m.put("column", column);
        addToIo(m);

        return new IndentedBlockRule(start, new ContentTypeToken(scope), loadedSubRules, column);
    }

    /*[[[cog
    #Note: run the install.py to regenerate the rules!
    import cog

    ALL = [
        ('MULTI_LINE_RULE', ['String start', 'String end', 'String scope', 'Character escapeCharacter'], ''),
        ('OPTIONAL_MULTI_LINE_RULE', ['String start', 'String end', 'String scope', 'Character escapeCharacter'], ''),
        ('ANY_WORD_RULE', ['String scope', 'List<String> except', 'String additionalChars:""', 'Boolean mustStartUppercase:false'], ''),
        ('REGEXP_RULE', ['String regexp', 'String scope'], ''),
        ('PATTERN_RULE', ['String startSequence', 'String endSequence', 'String scope', 'Character escapeCharacter', 'Boolean breaksOnEOL', 'Boolean breaksOnEOF', 'Boolean escapeContinuesLine'], ''),
        ('SINGLE_LINE_RULE', ['String sequence', 'String scope', 'Character escapeCharacter', 'Boolean escapeContinuesLine'], ''),
        ('SEQUENCE_RULE', ['String sequence', 'String scope'], ''),
        ('WORD_SEPARATOR_RULE', ['String scope'], ''),
        ('SEQUENCES_RULE', ['List<String> sequences', 'String scope'], ''),
        ('ONE_OR_MORE_SPACES_RULE', ['String scope'], ''),
        ('END_OF_LINE_RULE', ['String start', 'String scope'], ''),
        ('ZERO_OR_MORE_SPACES_RULE', ['String scope'], ''),
        ('NUMBER_RULE', ['String scope'], ''),
        ('NIM_NUMBER_RULE', ['String scope'], ''),
        ('PREV_CHAR_NOT_IN', ['String scope', 'String chars'], ''),
        ('MATCH_LINE_START_RULE', ['String scope'], ''),
        ('SKIP_LINE_RULE', ['String scope'], ''),
        ('TM_MATCH_RULE', ['String match', 'String scope', 'Map captures'], ''),
        ('TM_INCLUDE_RULE', ['String include'], ''),
        ('J_S_REGEX_RULE', ['String scope'], ''),
        ('REPEAT_CHAR_TO_EOL_RULE', ['String scope', 'List<String> chars'], ''),
        ('OPTIONAL_SEQUENCE_RULE', ['String sequence', 'String scope'], ''),
        ('SWITCH_LANGUAGE_RULE', ['String start', 'String end', 'String scope', 'String language'], ''),
        ('SWITCH_LANGUAGE_HTML_RULE', ['Map type_attr', 'Map language_attr', 'String tag', 'String scope'], ''),
    ]

    for constant, type_and_params, additional_call in ALL:
        rule = constant.title().replace('_', '')
        cog.outl('private static final String %(constant)s = "%(rule)s";' % dict(constant=constant, rule=rule));

        params = [x.split()[1] for x in type_and_params]
        remove_from_dict = []
        put_in_dict = []
        new_type_and_params = []
        for p in type_and_params:
            type_and_param = p
            type, p = p.split()
            if type == 'Character':
                remove_from_dict.append('            (%s) extractChar(removeFromMap(m, "%s"))' % (type, p))
                new_type_and_params.append(type_and_param)

            elif type.startswith('List<'):
                remove_from_dict.append('            (%s) removeFromMap(m, "%s", Collections.EMPTY_LIST)' % (type, p))
                new_type_and_params.append(type_and_param)

            else:
                if ':' in p:
                    p, default = p.split(':')
                    remove_from_dict.append('            (%s) removeFromMap(m, "%s", %s)' % (type, p, default))
                    new_type_and_params.append(type_and_param[:type_and_param.index(':')])
                else:
                    remove_from_dict.append('            (%s) removeFromMap(m, "%s")' % (type, p))
                    new_type_and_params.append(type_and_param)
            put_in_dict.append('    m.put("%s", %s)' % (p, p))

        type_and_params = new_type_and_params

        remove_from_dict = ',\n'.join(remove_from_dict)
        put_in_dict = ';\n'.join(put_in_dict)


        call_params = []
        for p in type_and_params:
            name = p.split()[1]
            if name == 'scope':
                call_params.append('new ContentTypeToken(scope)')
            else:
                call_params.append(name)
        call_params = ', '.join(call_params)

        template = '''
    //============================================================================= %(rule)s
    //============================================================================= %(rule)s

    public ILiClipsePredicateRule create%(rule)s(Map m) {
        ILiClipsePredicateRule create%(rule)s = create%(rule)s(
    %(remove_from_dict)s);
        checkCleared(m);
        return create%(rule)s;
    }''' % (dict(rule=rule, remove_from_dict=remove_from_dict))

        cog.outl(template)

        template2 = '''
    public ILiClipsePredicateRule create%(rule)s(%(typed_parameters)s) {
        Map m = new OrderedMap(%(params_count)s);
        m.put("type", %(constant)s);
    %(put_in_dict)s;
        addToIo(m);

        return new %(rule)s(%(call_params)s%(additional_call)s);
    }
    ''' % (dict(
        rule=rule,
        constant=constant,
        put_in_dict=put_in_dict,
        typed_parameters=', '.join(type_and_params),
        call_params=call_params,
        additional_call=additional_call,
        params_count=len(type_and_params) + 1,
        ))
        cog.outl(template2)


    ifs = []

    for constant, type_and_params, additional_call in ALL:
        rule = constant.title().replace('_', '')
        ifs.append('''if(type.equals(%(constant)s)){
                    addToRulesLoaded(rulesLoaded, create%(rule)s(map));

                ''' % dict(constant=constant, rule=rule))

    ifs = '} else '.join(ifs)

    template3 = '''
    //Note: cog-generated!
    private Stack<Map> ruleAliases = new Stack<>();

    //Note: cog-generated!
    public List<ILiClipsePredicateRule> load(List<Object> rulesToLoad, Map ruleAliases) {
        this.ruleAliases.push(ruleAliases);
        try {
            return load(rulesToLoad);
        } finally {
            this.ruleAliases.pop();
        }
    }

    //rulesToLoad is List with Maps (rules) or Strings (aliases).
    //Note: will mutate internal maps (clear them). Pass a copy if that's not Ok.
    //Note: cog-generated!
    private List<ILiClipsePredicateRule> load(List rulesToLoad) {
        List<ILiClipsePredicateRule> rulesLoaded = new ArrayList<ILiClipsePredicateRule>();
        for (Object ruleToLoad : rulesToLoad) {
            Map map;
            if (ruleToLoad instanceof Map) {
                map = (Map) ruleToLoad;

            } else if (ruleToLoad instanceof String) {
                Object ruleFound = ruleAliases.peek().get(ruleToLoad);
                int i = 0;
                while (ruleFound instanceof String) {
                    i++;
                    if (i > 200) {
                        throw new AssertionFailedException("Rule: " + ruleFound
                                + " seems to have some recursion in its definition.");
                    }
                    ruleFound = ruleAliases.peek().get(ruleFound);
                }
                map = (Map) ruleFound;
                if (map == null) {
                    Log.log("Unable to get rule in aliases:" + ruleToLoad);
                } else {
                    map = (Map) copyObject(map); //Create a copy so we don't destroy the alias.
                }

            } else {
                Log.log("Expected rule to be a Map with definition or String with alias.");
                continue;
            }
            String type = (String) removeFromMap(map, "type");
            try{
                LiClipseLanguageIO.fixScopeKeyFromIo(map);
                %(ifs)s} else if (type.equals(COMPOSITE_RULE)) {
                    addToRulesLoaded(rulesLoaded, createCompositeRule(map));

                } else if (type.equals(TM_BEGIN_END_RULE)) {
                    addToRulesLoaded(rulesLoaded, createTmBeginEndRule(map));

                } else if (type.equals(INDENTED_BLOCK_RULE)) {
                    addToRulesLoaded(rulesLoaded, createIndentedBlockRule(map));

                } else if (type.equals(MULTI_LINE_RULE_WITH_SKIP)) {
                    addToRulesLoaded(rulesLoaded, createMultiLineRuleWithSkip(map));

                } else if (type.equals(SINGLE_LINE_RULE_WITH_SKIP)) {
                    addToRulesLoaded(rulesLoaded, createSingleLineRuleWithSkip(map));

                } else if (type.equals(MULTI_LINE_RULE_RECURSIVE)) {
                    addToRulesLoaded(rulesLoaded, createMultiLineRuleRecursive(map));

                } else {
                    throw new RuntimeException("Unable to recognize rule with type: " + type + "\\nFull: " + map);
                }
            }catch(Exception e){
                throw new RuntimeException("Error loading rule type: " + type + ":\\n" + map, e);
            }
        }
        return rulesLoaded;
    }
    '''

    cog.outl(template3 % dict(ifs=ifs))
    ]]]*/
    private static final String MULTI_LINE_RULE = "MultiLineRule";

    //============================================================================= MultiLineRule
    //============================================================================= MultiLineRule

    public ILiClipsePredicateRule createMultiLineRule(Map m) {
        ILiClipsePredicateRule createMultiLineRule = createMultiLineRule(
                (String) removeFromMap(m, "start"),
                (String) removeFromMap(m, "end"),
                (String) removeFromMap(m, "scope"),
                (Character) extractChar(removeFromMap(m, "escapeCharacter")));
        checkCleared(m);
        return createMultiLineRule;
    }

    public ILiClipsePredicateRule createMultiLineRule(String start, String end, String scope, Character escapeCharacter) {
        Map m = new OrderedMap(5);
        m.put("type", MULTI_LINE_RULE);
        m.put("start", start);
        m.put("end", end);
        m.put("scope", scope);
        m.put("escapeCharacter", escapeCharacter);
        addToIo(m);

        return new MultiLineRule(start, end, new ContentTypeToken(scope), escapeCharacter);
    }

    private static final String OPTIONAL_MULTI_LINE_RULE = "OptionalMultiLineRule";

    //============================================================================= OptionalMultiLineRule
    //============================================================================= OptionalMultiLineRule

    public ILiClipsePredicateRule createOptionalMultiLineRule(Map m) {
        ILiClipsePredicateRule createOptionalMultiLineRule = createOptionalMultiLineRule(
                (String) removeFromMap(m, "start"),
                (String) removeFromMap(m, "end"),
                (String) removeFromMap(m, "scope"),
                (Character) extractChar(removeFromMap(m, "escapeCharacter")));
        checkCleared(m);
        return createOptionalMultiLineRule;
    }

    public ILiClipsePredicateRule createOptionalMultiLineRule(String start, String end, String scope, Character escapeCharacter) {
        Map m = new OrderedMap(5);
        m.put("type", OPTIONAL_MULTI_LINE_RULE);
        m.put("start", start);
        m.put("end", end);
        m.put("scope", scope);
        m.put("escapeCharacter", escapeCharacter);
        addToIo(m);

        return new OptionalMultiLineRule(start, end, new ContentTypeToken(scope), escapeCharacter);
    }

    private static final String ANY_WORD_RULE = "AnyWordRule";

    //============================================================================= AnyWordRule
    //============================================================================= AnyWordRule

    public ILiClipsePredicateRule createAnyWordRule(Map m) {
        ILiClipsePredicateRule createAnyWordRule = createAnyWordRule(
                (String) removeFromMap(m, "scope"),
                (List<String>) removeFromMap(m, "except", Collections.EMPTY_LIST),
                (String) removeFromMap(m, "additionalChars", ""),
                (Boolean) removeFromMap(m, "mustStartUppercase", false));
        checkCleared(m);
        return createAnyWordRule;
    }

    public ILiClipsePredicateRule createAnyWordRule(String scope, List<String> except, String additionalChars, Boolean mustStartUppercase) {
        Map m = new OrderedMap(5);
        m.put("type", ANY_WORD_RULE);
        m.put("scope", scope);
        m.put("except", except);
        m.put("additionalChars", additionalChars);
        m.put("mustStartUppercase", mustStartUppercase);
        addToIo(m);

        return new AnyWordRule(new ContentTypeToken(scope), except, additionalChars, mustStartUppercase);
    }

    private static final String REGEXP_RULE = "RegexpRule";

    //============================================================================= RegexpRule
    //============================================================================= RegexpRule

    public ILiClipsePredicateRule createRegexpRule(Map m) {
        ILiClipsePredicateRule createRegexpRule = createRegexpRule(
                (String) removeFromMap(m, "regexp"),
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createRegexpRule;
    }

    public ILiClipsePredicateRule createRegexpRule(String regexp, String scope) {
        Map m = new OrderedMap(3);
        m.put("type", REGEXP_RULE);
        m.put("regexp", regexp);
        m.put("scope", scope);
        addToIo(m);

        return new RegexpRule(regexp, new ContentTypeToken(scope));
    }

    private static final String PATTERN_RULE = "PatternRule";

    //============================================================================= PatternRule
    //============================================================================= PatternRule

    public ILiClipsePredicateRule createPatternRule(Map m) {
        ILiClipsePredicateRule createPatternRule = createPatternRule(
                (String) removeFromMap(m, "startSequence"),
                (String) removeFromMap(m, "endSequence"),
                (String) removeFromMap(m, "scope"),
                (Character) extractChar(removeFromMap(m, "escapeCharacter")),
                (Boolean) removeFromMap(m, "breaksOnEOL"),
                (Boolean) removeFromMap(m, "breaksOnEOF"),
                (Boolean) removeFromMap(m, "escapeContinuesLine"));
        checkCleared(m);
        return createPatternRule;
    }

    public ILiClipsePredicateRule createPatternRule(String startSequence, String endSequence, String scope, Character escapeCharacter, Boolean breaksOnEOL, Boolean breaksOnEOF, Boolean escapeContinuesLine) {
        Map m = new OrderedMap(8);
        m.put("type", PATTERN_RULE);
        m.put("startSequence", startSequence);
        m.put("endSequence", endSequence);
        m.put("scope", scope);
        m.put("escapeCharacter", escapeCharacter);
        m.put("breaksOnEOL", breaksOnEOL);
        m.put("breaksOnEOF", breaksOnEOF);
        m.put("escapeContinuesLine", escapeContinuesLine);
        addToIo(m);

        return new PatternRule(startSequence, endSequence, new ContentTypeToken(scope), escapeCharacter, breaksOnEOL, breaksOnEOF, escapeContinuesLine);
    }

    private static final String SINGLE_LINE_RULE = "SingleLineRule";

    //============================================================================= SingleLineRule
    //============================================================================= SingleLineRule

    public ILiClipsePredicateRule createSingleLineRule(Map m) {
        ILiClipsePredicateRule createSingleLineRule = createSingleLineRule(
                (String) removeFromMap(m, "sequence"),
                (String) removeFromMap(m, "scope"),
                (Character) extractChar(removeFromMap(m, "escapeCharacter")),
                (Boolean) removeFromMap(m, "escapeContinuesLine"));
        checkCleared(m);
        return createSingleLineRule;
    }

    public ILiClipsePredicateRule createSingleLineRule(String sequence, String scope, Character escapeCharacter, Boolean escapeContinuesLine) {
        Map m = new OrderedMap(5);
        m.put("type", SINGLE_LINE_RULE);
        m.put("sequence", sequence);
        m.put("scope", scope);
        m.put("escapeCharacter", escapeCharacter);
        m.put("escapeContinuesLine", escapeContinuesLine);
        addToIo(m);

        return new SingleLineRule(sequence, new ContentTypeToken(scope), escapeCharacter, escapeContinuesLine);
    }

    private static final String SEQUENCE_RULE = "SequenceRule";

    //============================================================================= SequenceRule
    //============================================================================= SequenceRule

    public ILiClipsePredicateRule createSequenceRule(Map m) {
        ILiClipsePredicateRule createSequenceRule = createSequenceRule(
                (String) removeFromMap(m, "sequence"),
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createSequenceRule;
    }

    public ILiClipsePredicateRule createSequenceRule(String sequence, String scope) {
        Map m = new OrderedMap(3);
        m.put("type", SEQUENCE_RULE);
        m.put("sequence", sequence);
        m.put("scope", scope);
        addToIo(m);

        return new SequenceRule(sequence, new ContentTypeToken(scope));
    }

    private static final String WORD_SEPARATOR_RULE = "WordSeparatorRule";

    //============================================================================= WordSeparatorRule
    //============================================================================= WordSeparatorRule

    public ILiClipsePredicateRule createWordSeparatorRule(Map m) {
        ILiClipsePredicateRule createWordSeparatorRule = createWordSeparatorRule(
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createWordSeparatorRule;
    }

    public ILiClipsePredicateRule createWordSeparatorRule(String scope) {
        Map m = new OrderedMap(2);
        m.put("type", WORD_SEPARATOR_RULE);
        m.put("scope", scope);
        addToIo(m);

        return new WordSeparatorRule(new ContentTypeToken(scope));
    }

    private static final String SEQUENCES_RULE = "SequencesRule";

    //============================================================================= SequencesRule
    //============================================================================= SequencesRule

    public ILiClipsePredicateRule createSequencesRule(Map m) {
        ILiClipsePredicateRule createSequencesRule = createSequencesRule(
                (List<String>) removeFromMap(m, "sequences", Collections.EMPTY_LIST),
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createSequencesRule;
    }

    public ILiClipsePredicateRule createSequencesRule(List<String> sequences, String scope) {
        Map m = new OrderedMap(3);
        m.put("type", SEQUENCES_RULE);
        m.put("sequences", sequences);
        m.put("scope", scope);
        addToIo(m);

        return new SequencesRule(sequences, new ContentTypeToken(scope));
    }

    private static final String ONE_OR_MORE_SPACES_RULE = "OneOrMoreSpacesRule";

    //============================================================================= OneOrMoreSpacesRule
    //============================================================================= OneOrMoreSpacesRule

    public ILiClipsePredicateRule createOneOrMoreSpacesRule(Map m) {
        ILiClipsePredicateRule createOneOrMoreSpacesRule = createOneOrMoreSpacesRule(
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createOneOrMoreSpacesRule;
    }

    public ILiClipsePredicateRule createOneOrMoreSpacesRule(String scope) {
        Map m = new OrderedMap(2);
        m.put("type", ONE_OR_MORE_SPACES_RULE);
        m.put("scope", scope);
        addToIo(m);

        return new OneOrMoreSpacesRule(new ContentTypeToken(scope));
    }

    private static final String END_OF_LINE_RULE = "EndOfLineRule";

    //============================================================================= EndOfLineRule
    //============================================================================= EndOfLineRule

    public ILiClipsePredicateRule createEndOfLineRule(Map m) {
        ILiClipsePredicateRule createEndOfLineRule = createEndOfLineRule(
                (String) removeFromMap(m, "start"),
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createEndOfLineRule;
    }

    public ILiClipsePredicateRule createEndOfLineRule(String start, String scope) {
        Map m = new OrderedMap(3);
        m.put("type", END_OF_LINE_RULE);
        m.put("start", start);
        m.put("scope", scope);
        addToIo(m);

        return new EndOfLineRule(start, new ContentTypeToken(scope));
    }

    private static final String ZERO_OR_MORE_SPACES_RULE = "ZeroOrMoreSpacesRule";

    //============================================================================= ZeroOrMoreSpacesRule
    //============================================================================= ZeroOrMoreSpacesRule

    public ILiClipsePredicateRule createZeroOrMoreSpacesRule(Map m) {
        ILiClipsePredicateRule createZeroOrMoreSpacesRule = createZeroOrMoreSpacesRule(
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createZeroOrMoreSpacesRule;
    }

    public ILiClipsePredicateRule createZeroOrMoreSpacesRule(String scope) {
        Map m = new OrderedMap(2);
        m.put("type", ZERO_OR_MORE_SPACES_RULE);
        m.put("scope", scope);
        addToIo(m);

        return new ZeroOrMoreSpacesRule(new ContentTypeToken(scope));
    }

    private static final String NUMBER_RULE = "NumberRule";

    //============================================================================= NumberRule
    //============================================================================= NumberRule

    public ILiClipsePredicateRule createNumberRule(Map m) {
        ILiClipsePredicateRule createNumberRule = createNumberRule(
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createNumberRule;
    }

    public ILiClipsePredicateRule createNumberRule(String scope) {
        Map m = new OrderedMap(2);
        m.put("type", NUMBER_RULE);
        m.put("scope", scope);
        addToIo(m);

        return new NumberRule(new ContentTypeToken(scope));
    }

    private static final String NIM_NUMBER_RULE = "NimNumberRule";

    //============================================================================= NimNumberRule
    //============================================================================= NimNumberRule

    public ILiClipsePredicateRule createNimNumberRule(Map m) {
        ILiClipsePredicateRule createNimNumberRule = createNimNumberRule(
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createNimNumberRule;
    }

    public ILiClipsePredicateRule createNimNumberRule(String scope) {
        Map m = new OrderedMap(2);
        m.put("type", NIM_NUMBER_RULE);
        m.put("scope", scope);
        addToIo(m);

        return new NimNumberRule(new ContentTypeToken(scope));
    }

    private static final String PREV_CHAR_NOT_IN = "PrevCharNotIn";

    //============================================================================= PrevCharNotIn
    //============================================================================= PrevCharNotIn

    public ILiClipsePredicateRule createPrevCharNotIn(Map m) {
        ILiClipsePredicateRule createPrevCharNotIn = createPrevCharNotIn(
                (String) removeFromMap(m, "scope"),
                (String) removeFromMap(m, "chars"));
        checkCleared(m);
        return createPrevCharNotIn;
    }

    public ILiClipsePredicateRule createPrevCharNotIn(String scope, String chars) {
        Map m = new OrderedMap(3);
        m.put("type", PREV_CHAR_NOT_IN);
        m.put("scope", scope);
        m.put("chars", chars);
        addToIo(m);

        return new PrevCharNotIn(new ContentTypeToken(scope), chars);
    }

    private static final String MATCH_LINE_START_RULE = "MatchLineStartRule";

    //============================================================================= MatchLineStartRule
    //============================================================================= MatchLineStartRule

    public ILiClipsePredicateRule createMatchLineStartRule(Map m) {
        ILiClipsePredicateRule createMatchLineStartRule = createMatchLineStartRule(
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createMatchLineStartRule;
    }

    public ILiClipsePredicateRule createMatchLineStartRule(String scope) {
        Map m = new OrderedMap(2);
        m.put("type", MATCH_LINE_START_RULE);
        m.put("scope", scope);
        addToIo(m);

        return new MatchLineStartRule(new ContentTypeToken(scope));
    }

    private static final String SKIP_LINE_RULE = "SkipLineRule";

    //============================================================================= SkipLineRule
    //============================================================================= SkipLineRule

    public ILiClipsePredicateRule createSkipLineRule(Map m) {
        ILiClipsePredicateRule createSkipLineRule = createSkipLineRule(
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createSkipLineRule;
    }

    public ILiClipsePredicateRule createSkipLineRule(String scope) {
        Map m = new OrderedMap(2);
        m.put("type", SKIP_LINE_RULE);
        m.put("scope", scope);
        addToIo(m);

        return new SkipLineRule(new ContentTypeToken(scope));
    }

    private static final String TM_MATCH_RULE = "TmMatchRule";

    //============================================================================= TmMatchRule
    //============================================================================= TmMatchRule

    public ILiClipsePredicateRule createTmMatchRule(Map m) {
        ILiClipsePredicateRule createTmMatchRule = createTmMatchRule(
                (String) removeFromMap(m, "match"),
                (String) removeFromMap(m, "scope"),
                (Map) removeFromMap(m, "captures"));
        checkCleared(m);
        return createTmMatchRule;
    }

    public ILiClipsePredicateRule createTmMatchRule(String match, String scope, Map captures) {
        Map m = new OrderedMap(4);
        m.put("type", TM_MATCH_RULE);
        m.put("match", match);
        m.put("scope", scope);
        m.put("captures", captures);
        addToIo(m);

        return new TmMatchRule(match, new ContentTypeToken(scope), captures);
    }

    private static final String TM_INCLUDE_RULE = "TmIncludeRule";

    //============================================================================= TmIncludeRule
    //============================================================================= TmIncludeRule

    public ILiClipsePredicateRule createTmIncludeRule(Map m) {
        ILiClipsePredicateRule createTmIncludeRule = createTmIncludeRule(
                (String) removeFromMap(m, "include"));
        checkCleared(m);
        return createTmIncludeRule;
    }

    public ILiClipsePredicateRule createTmIncludeRule(String include) {
        Map m = new OrderedMap(2);
        m.put("type", TM_INCLUDE_RULE);
        m.put("include", include);
        addToIo(m);

        return new TmIncludeRule(include);
    }

    private static final String J_S_REGEX_RULE = "JSRegexRule";

    //============================================================================= JSRegexRule
    //============================================================================= JSRegexRule

    public ILiClipsePredicateRule createJSRegexRule(Map m) {
        ILiClipsePredicateRule createJSRegexRule = createJSRegexRule(
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createJSRegexRule;
    }

    public ILiClipsePredicateRule createJSRegexRule(String scope) {
        Map m = new OrderedMap(2);
        m.put("type", J_S_REGEX_RULE);
        m.put("scope", scope);
        addToIo(m);

        return new JSRegexRule(new ContentTypeToken(scope));
    }

    private static final String REPEAT_CHAR_TO_EOL_RULE = "RepeatCharToEolRule";

    //============================================================================= RepeatCharToEolRule
    //============================================================================= RepeatCharToEolRule

    public ILiClipsePredicateRule createRepeatCharToEolRule(Map m) {
        ILiClipsePredicateRule createRepeatCharToEolRule = createRepeatCharToEolRule(
                (String) removeFromMap(m, "scope"),
                (List<String>) removeFromMap(m, "chars", Collections.EMPTY_LIST));
        checkCleared(m);
        return createRepeatCharToEolRule;
    }

    public ILiClipsePredicateRule createRepeatCharToEolRule(String scope, List<String> chars) {
        Map m = new OrderedMap(3);
        m.put("type", REPEAT_CHAR_TO_EOL_RULE);
        m.put("scope", scope);
        m.put("chars", chars);
        addToIo(m);

        return new RepeatCharToEolRule(new ContentTypeToken(scope), chars);
    }

    private static final String OPTIONAL_SEQUENCE_RULE = "OptionalSequenceRule";

    //============================================================================= OptionalSequenceRule
    //============================================================================= OptionalSequenceRule

    public ILiClipsePredicateRule createOptionalSequenceRule(Map m) {
        ILiClipsePredicateRule createOptionalSequenceRule = createOptionalSequenceRule(
                (String) removeFromMap(m, "sequence"),
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createOptionalSequenceRule;
    }

    public ILiClipsePredicateRule createOptionalSequenceRule(String sequence, String scope) {
        Map m = new OrderedMap(3);
        m.put("type", OPTIONAL_SEQUENCE_RULE);
        m.put("sequence", sequence);
        m.put("scope", scope);
        addToIo(m);

        return new OptionalSequenceRule(sequence, new ContentTypeToken(scope));
    }

    private static final String SWITCH_LANGUAGE_RULE = "SwitchLanguageRule";

    //============================================================================= SwitchLanguageRule
    //============================================================================= SwitchLanguageRule

    public ILiClipsePredicateRule createSwitchLanguageRule(Map m) {
        ILiClipsePredicateRule createSwitchLanguageRule = createSwitchLanguageRule(
                (String) removeFromMap(m, "start"),
                (String) removeFromMap(m, "end"),
                (String) removeFromMap(m, "scope"),
                (String) removeFromMap(m, "language"));
        checkCleared(m);
        return createSwitchLanguageRule;
    }

    public ILiClipsePredicateRule createSwitchLanguageRule(String start, String end, String scope, String language) {
        Map m = new OrderedMap(5);
        m.put("type", SWITCH_LANGUAGE_RULE);
        m.put("start", start);
        m.put("end", end);
        m.put("scope", scope);
        m.put("language", language);
        addToIo(m);

        return new SwitchLanguageRule(start, end, new ContentTypeToken(scope), language);
    }

    private static final String SWITCH_LANGUAGE_HTML_RULE = "SwitchLanguageHtmlRule";

    //============================================================================= SwitchLanguageHtmlRule
    //============================================================================= SwitchLanguageHtmlRule

    public ILiClipsePredicateRule createSwitchLanguageHtmlRule(Map m) {
        ILiClipsePredicateRule createSwitchLanguageHtmlRule = createSwitchLanguageHtmlRule(
                (Map) removeFromMap(m, "type_attr"),
                (Map) removeFromMap(m, "language_attr"),
                (String) removeFromMap(m, "tag"),
                (String) removeFromMap(m, "scope"));
        checkCleared(m);
        return createSwitchLanguageHtmlRule;
    }

    public ILiClipsePredicateRule createSwitchLanguageHtmlRule(Map type_attr, Map language_attr, String tag, String scope) {
        Map m = new OrderedMap(5);
        m.put("type", SWITCH_LANGUAGE_HTML_RULE);
        m.put("type_attr", type_attr);
        m.put("language_attr", language_attr);
        m.put("tag", tag);
        m.put("scope", scope);
        addToIo(m);

        return new SwitchLanguageHtmlRule(type_attr, language_attr, tag, new ContentTypeToken(scope));
    }


    //Note: cog-generated!
    private Stack<Map> ruleAliases = new Stack<>();

    //Note: cog-generated!
    public List<ILiClipsePredicateRule> load(List<Object> rulesToLoad, Map ruleAliases) {
        this.ruleAliases.push(ruleAliases);
        try {
            return load(rulesToLoad);
        } finally {
            this.ruleAliases.pop();
        }
    }

    //rulesToLoad is List with Maps (rules) or Strings (aliases).
    //Note: will mutate internal maps (clear them). Pass a copy if that's not Ok.
    //Note: cog-generated!
    private List<ILiClipsePredicateRule> load(List rulesToLoad) {
        List<ILiClipsePredicateRule> rulesLoaded = new ArrayList<ILiClipsePredicateRule>();
        for (Object ruleToLoad : rulesToLoad) {
            Map map;
            if (ruleToLoad instanceof Map) {
                map = (Map) ruleToLoad;

            } else if (ruleToLoad instanceof String) {
                Object ruleFound = ruleAliases.peek().get(ruleToLoad);
                int i = 0;
                while (ruleFound instanceof String) {
                    i++;
                    if (i > 200) {
                        throw new AssertionFailedException("Rule: " + ruleFound
                                + " seems to have some recursion in its definition.");
                    }
                    ruleFound = ruleAliases.peek().get(ruleFound);
                }
                map = (Map) ruleFound;
                if (map == null) {
                    Log.log("Unable to get rule in aliases:" + ruleToLoad);
                } else {
                    map = (Map) copyObject(map); //Create a copy so we don't destroy the alias.
                }

            } else {
                Log.log("Expected rule to be a Map with definition or String with alias.");
                continue;
            }
            String type = (String) removeFromMap(map, "type");
            try{
                LiClipseLanguageIO.fixScopeKeyFromIo(map);
                if(type.equals(MULTI_LINE_RULE)){
                    addToRulesLoaded(rulesLoaded, createMultiLineRule(map));

                } else if(type.equals(OPTIONAL_MULTI_LINE_RULE)){
                    addToRulesLoaded(rulesLoaded, createOptionalMultiLineRule(map));

                } else if(type.equals(ANY_WORD_RULE)){
                    addToRulesLoaded(rulesLoaded, createAnyWordRule(map));

                } else if(type.equals(REGEXP_RULE)){
                    addToRulesLoaded(rulesLoaded, createRegexpRule(map));

                } else if(type.equals(PATTERN_RULE)){
                    addToRulesLoaded(rulesLoaded, createPatternRule(map));

                } else if(type.equals(SINGLE_LINE_RULE)){
                    addToRulesLoaded(rulesLoaded, createSingleLineRule(map));

                } else if(type.equals(SEQUENCE_RULE)){
                    addToRulesLoaded(rulesLoaded, createSequenceRule(map));

                } else if(type.equals(WORD_SEPARATOR_RULE)){
                    addToRulesLoaded(rulesLoaded, createWordSeparatorRule(map));

                } else if(type.equals(SEQUENCES_RULE)){
                    addToRulesLoaded(rulesLoaded, createSequencesRule(map));

                } else if(type.equals(ONE_OR_MORE_SPACES_RULE)){
                    addToRulesLoaded(rulesLoaded, createOneOrMoreSpacesRule(map));

                } else if(type.equals(END_OF_LINE_RULE)){
                    addToRulesLoaded(rulesLoaded, createEndOfLineRule(map));

                } else if(type.equals(ZERO_OR_MORE_SPACES_RULE)){
                    addToRulesLoaded(rulesLoaded, createZeroOrMoreSpacesRule(map));

                } else if(type.equals(NUMBER_RULE)){
                    addToRulesLoaded(rulesLoaded, createNumberRule(map));

                } else if(type.equals(NIM_NUMBER_RULE)){
                    addToRulesLoaded(rulesLoaded, createNimNumberRule(map));

                } else if(type.equals(PREV_CHAR_NOT_IN)){
                    addToRulesLoaded(rulesLoaded, createPrevCharNotIn(map));

                } else if(type.equals(MATCH_LINE_START_RULE)){
                    addToRulesLoaded(rulesLoaded, createMatchLineStartRule(map));

                } else if(type.equals(SKIP_LINE_RULE)){
                    addToRulesLoaded(rulesLoaded, createSkipLineRule(map));

                } else if(type.equals(TM_MATCH_RULE)){
                    addToRulesLoaded(rulesLoaded, createTmMatchRule(map));

                } else if(type.equals(TM_INCLUDE_RULE)){
                    addToRulesLoaded(rulesLoaded, createTmIncludeRule(map));

                } else if(type.equals(J_S_REGEX_RULE)){
                    addToRulesLoaded(rulesLoaded, createJSRegexRule(map));

                } else if(type.equals(REPEAT_CHAR_TO_EOL_RULE)){
                    addToRulesLoaded(rulesLoaded, createRepeatCharToEolRule(map));

                } else if(type.equals(OPTIONAL_SEQUENCE_RULE)){
                    addToRulesLoaded(rulesLoaded, createOptionalSequenceRule(map));

                } else if(type.equals(SWITCH_LANGUAGE_RULE)){
                    addToRulesLoaded(rulesLoaded, createSwitchLanguageRule(map));

                } else if(type.equals(SWITCH_LANGUAGE_HTML_RULE)){
                    addToRulesLoaded(rulesLoaded, createSwitchLanguageHtmlRule(map));

                } else if (type.equals(COMPOSITE_RULE)) {
                    addToRulesLoaded(rulesLoaded, createCompositeRule(map));

                } else if (type.equals(TM_BEGIN_END_RULE)) {
                    addToRulesLoaded(rulesLoaded, createTmBeginEndRule(map));

                } else if (type.equals(INDENTED_BLOCK_RULE)) {
                    addToRulesLoaded(rulesLoaded, createIndentedBlockRule(map));

                } else if (type.equals(MULTI_LINE_RULE_WITH_SKIP)) {
                    addToRulesLoaded(rulesLoaded, createMultiLineRuleWithSkip(map));

                } else if (type.equals(SINGLE_LINE_RULE_WITH_SKIP)) {
                    addToRulesLoaded(rulesLoaded, createSingleLineRuleWithSkip(map));

                } else if (type.equals(MULTI_LINE_RULE_RECURSIVE)) {
                    addToRulesLoaded(rulesLoaded, createMultiLineRuleRecursive(map));

                } else {
                    throw new RuntimeException("Unable to recognize rule with type: " + type + "\nFull: " + map);
                }
            }catch(Exception e){
                throw new RuntimeException("Error loading rule type: " + type + ":\n" + map, e);
            }
        }
        return rulesLoaded;
    }

    /*[[[end]]]*/

    private void addToRulesLoaded(List<ILiClipsePredicateRule> rulesLoaded, ILiClipsePredicateRule rule) {
        rulesLoaded.add(rule);
        if (rule instanceof ILanguageDependentRule) {
            ILanguageDependentRule languageDependentRule = (ILanguageDependentRule) rule;
            languageDependentRule.setLanguage(this.liClipseLanguage);
        }
    }

    public static Object copyObject(Object object) {
        if (object instanceof Map) {
            HashMap<Object, Object> copy = new HashMap<>();
            Map mapToCopy = (Map) object;
            Set<Map.Entry> entrySet = mapToCopy.entrySet();
            for (Map.Entry entry : entrySet) {
                copy.put(copyObject(entry.getKey()), copyObject(entry.getValue()));
            }
            return copy;
        }
        if (object instanceof List) {
            List list = (List) object;
            ArrayList<Object> lst = new ArrayList<>(list.size());
            for (Object obj : list) {
                lst.add(copyObject(obj));
            }

            return lst;
        }
        if (object instanceof Boolean || object instanceof String || object instanceof Integer
                || object instanceof Float
                || object instanceof Long || object instanceof Short) {
            return object;
        }
        if (object == null) {
            return null;
        }
        throw new AssertionFailedException("Unable to copy: " + object);
    }

    private Character extractChar(Object obj) {
        if (obj == null) {
            return '\0';
        }
        String escapeStr;
        if (obj instanceof byte[]) {
            byte[] bs = (byte[]) obj;
            escapeStr = new String(bs);
        } else {
            escapeStr = obj.toString();
        }

        if (escapeStr.length() == 0) {
            return '\0';
        } else if (escapeStr.length() == 1) {
            return escapeStr.charAt(0);
        } else if (escapeStr.equals("\\0")) {
            return '\0';
        } else {
            throw new AssertionFailedException("Expected to find a char. Found: " + obj);
        }
    }

}
