package org.brainwy.liclipsetext.editor.tmbundle;

import java.io.File;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmBeginEndRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmMatchRule;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmLanguageHandler;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmRulesConverter;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;

public class TmBundlesParserTest extends TestCase {

    public void testTmBundles() throws Exception {
        TmLanguageHandler parser = new TmLanguageHandler();
        parser.parse(new File(TestUtils.getTestLanguagesDir(), "language_test.tmLanguage"));
        assertEquals("0F24FCF1-6543-4CC6-8B33-2EAED401FE3A", parser.getValue("bundleUUID"));
        assertEquals("PythonImproved", parser.getValue("name"));
        Object patterns = parser.getValue("patterns");
        assertTrue(patterns instanceof List);
        //parser.printLanguage();
        assertNull(parser.getValue("no-not-there"));
        Object repository = parser.getValue("repository");
        assertTrue(repository instanceof Map);
        TmRulesConverter tmRulesConverter = new TmRulesConverter(null);

        ILiClipsePredicateRule rule = tmRulesConverter.convertDictToRule(parser.getMap("repository/constant_placeholder"),
                "constant_placeholder");
        assertTrue(rule instanceof TmMatchRule);
        TmMatchRule r = (TmMatchRule) rule;
        assertEquals("constant.other.placeholder.python", r.getSuccessToken().getData());
        assertEquals("{\n" +
                "    match: (?i:%(\\([a-z_]+\\))?#?0?\\-?[ ]?\\+?([0-9]*|\\*)(\\.([0-9]*|\\*))?[hL]?[a-z%])\n" +
                "    name: constant.other.placeholder.python\n" +
                "}", r.toTmYaml());

        rule = tmRulesConverter.convertDictToRule(parser.getMap("repository/annotated_group"), "annotated_group");
        assertTrue("Found: " + rule, rule instanceof TmBeginEndRule);
        TmBeginEndRule r2 = (TmBeginEndRule) rule;
        assertEquals("constant.other.placeholder.python", r.getSuccessToken().getData());
        assertEquals(
                "{\n" +
                        "    begin: (\\()\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: punctuation.definition.parameters-group.begin.python },\n" +
                        "    }\n" +
                        "    end: (\\))\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.parameters-group.end.python },\n" +
                        "    }\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: \\b([a-zA-Z_][a-zA-Z_0-9]*)\\s*(:)\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: variable.parameter.function.python },\n" +
                        "                2: { name: punctuation.separator.annotation.python },\n" +
                        "            }\n" +
                        "            end: \\s*(?:(,)|(?=$\\n?|\\)))\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.separator.parameters.python },\n" +
                        "            }\n" +
                        "            patterns: [\n" +
                        "                { include: $self },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: \\b([a-zA-Z_][a-zA-Z_0-9]*)\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: variable.parameter.function.python },\n" +
                        "            }\n" +
                        "            end: \\s*(?:(,)|(?=$\\n?|\\)))\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.separator.parameters.python },\n" +
                        "            }\n" +
                        "        },\n" +
                        "        { include: #comments },\n" +
                        "    ]\n" +
                        "}"
                        + "",
                r2.toTmYaml());
    }

    public void testTmBundlesRst() throws Exception {
        TmLanguageHandler parser = new TmLanguageHandler();
        parser.parse(new File(TestUtils.getTestLanguagesDir(), "language_test_rst.tmLanguage"));
        assertEquals("reStructuredText Improved", parser.getValue("name"));
        Object patterns = parser.getValue("patterns");
        assertTrue(patterns instanceof List);
    }
}
