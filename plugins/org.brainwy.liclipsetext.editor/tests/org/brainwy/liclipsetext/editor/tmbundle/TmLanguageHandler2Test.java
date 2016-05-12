package org.brainwy.liclipsetext.editor.tmbundle;

import java.io.File;
import java.util.LinkedList;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.IPrintableRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.ITextMateRule;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmLanguageHandler;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.rules.IPredicateRule;

import junit.framework.TestCase;

public class TmLanguageHandler2Test extends TestCase {

    public void testLanguageHandler() throws Exception {
        TmLanguageHandler parser = new TmLanguageHandler();
        parser.parse(new File(TestUtils.getTestLanguagesDir(), "language_test.tmLanguage"));
        LinkedList<ITextMateRule> loadRegularRules = parser.loadRegularRules(null);

        FastStringBuffer buf = new FastStringBuffer();
        for (IPredicateRule entry : loadRegularRules) {
            buf.append(((IPrintableRule) entry).toTmYaml());
            buf.append("\n");
        }

        assertEquals(
                "{\n" +
                        "    match: (^In \\[(\\d+)\\]\\:)\n" +
                        "    captures: {\n" +
                        "        2: { name: support.ipython.cell-number.python },\n" +
                        "    }\n" +
                        "    name: support.ipython.in.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: (^Out\\[(\\d+)\\]\\:)\n" +
                        "    captures: {\n" +
                        "        2: { name: support.ipython.cell-number.python },\n" +
                        "    }\n" +
                        "    name: support.ipython.out.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: (#)\\s*(BUG|FIXME|TODO|XXX).*$\\n?\n" +
                        "    captures: {\n" +
                        "        1: { name: punctuation.definition.comment.python },\n" +
                        "        2: { name: comment.line.note.notation.python },\n" +
                        "    }\n" +
                        "    name: comment.line.note.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: (#).*$\\n?\n" +
                        "    captures: {\n" +
                        "        1: { name: punctuation.definition.comment.python },\n" +
                        "    }\n" +
                        "    name: comment.line.number-sign.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b([A-Z_][A-Z0-9_]*)\\b(?![\\.\\(])\n" +
                        "    name: constant.other.allcaps.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(0x\\h*)L)\n" +
                        "    name: constant.numeric.integer.long.hexadecimal.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(0x\\h*))\n" +
                        "    name: constant.numeric.integer.hexadecimal.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(0b[01]+)L)\n" +
                        "    name: constant.numeric.integer.long.binary.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(0b[01]+))\n" +
                        "    name: constant.numeric.integer.binary.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(0[o]?[0-7]+)L)\n" +
                        "    name: constant.numeric.integer.long.octal.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(0[o]?[0-7]+))\n" +
                        "    name: constant.numeric.integer.octal.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(((\\d+(\\.(?=[^a-zA-Z_])\\d*)?|(?<=[^0-9a-zA-Z_])\\.\\d+)(e[\\-\\+]?\\d+)?))J)\n"
                        +
                        "    name: constant.numeric.complex.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(\\d+\\.\\d*(e[\\-\\+]?\\d+)?))(?=[^a-zA-Z_])\n" +
                        "    name: constant.numeric.float.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: (?<=[^0-9a-zA-Z_])(?i:(\\.\\d+(e[\\-\\+]?\\d+)?))\n" +
                        "    name: constant.numeric.float.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:(\\d+e[\\-\\+]?\\d+))\n" +
                        "    name: constant.numeric.float.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?i:([1-9]+[0-9]*|0)L)\n" +
                        "    name: constant.numeric.integer.long.decimal.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b([1-9]+[0-9]*|0)\n" +
                        "    name: constant.numeric.integer.decimal.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(None|True|False|Ellipsis|NotImplemented)\\b\n" +
                        "    name: constant.language.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(global|nonlocal)\\b\n" +
                        "    name: storage.modifier.declaration.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(?:(import|from|as))\\b\n" +
                        "    name: keyword.control.import.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(elif|else|except|finally|for|if|try|while|with|break|continue|pass|raise|return|yield)\\b\n"
                        +
                        "    name: keyword.control.flow.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(and|in|is|not|or)\\b\n" +
                        "    name: keyword.operator.logical.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(assert|del)\\b\n" +
                        "    name: keyword.other.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: <>\n" +
                        "    name: invalid.deprecated.operator.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: <\\=|>\\=|\\=\\=|<|>|\\!\\=\n" +
                        "    name: keyword.operator.comparison.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\+\\=|-\\=|\\*\\=|/\\=|//\\=|%\\=|&\\=|\\|\\=|\\^\\=|>>\\=|<<\\=|\\*\\*\\=\n" +
                        "    name: keyword.operator.assignment.augmented.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\+|\\-|\\*|\\*\\*|/|//|%|<<|>>|&|\\||\\^|~\n" +
                        "    name: keyword.operator.arithmetic.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\=\n" +
                        "    name: keyword.operator.assignment.python\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: \\s*(class)\\s+(?=[a-zA-Z_][a-zA-Z_0-9]*\\s*\\:)\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: storage.type.class.python },\n" +
                        "    }\n" +
                        "    end: \\s*(:)\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.section.class.begin.python },\n" +
                        "    }\n" +
                        "    contentName: entity.name.type.class.python\n" +
                        "    name: meta.class.old-style.python\n" +
                        "    patterns: [\n" +
                        "        { include: #entity_name_class },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: \\s*(class)\\s+(?=[a-zA-Z_][a-zA-Z_0-9]*\\s*\\()\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: storage.type.class.python },\n" +
                        "    }\n" +
                        "    end: (\\))\\s*(?:(\\:)|(.*$\\n?))\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.inheritance.end.python },\n" +
                        "        2: { name: punctuation.section.class.begin.python },\n" +
                        "        3: { name: invalid.illegal.missing-section-begin.python },\n" +
                        "    }\n" +
                        "    name: meta.class.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?=[A-Za-z_][A-Za-z0-9_]*)\n" +
                        "            end: (?![A-Za-z0-9_])\n" +
                        "            contentName: entity.name.type.class.python\n" +
                        "            patterns: [\n" +
                        "                { include: #entity_name_class },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\\()\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.inheritance.begin.python },\n" +
                        "            }\n" +
                        "            end: (?=\\)|:)\n" +
                        "            contentName: meta.class.inheritance.python\n" +
                        "            patterns: [\n" +
                        "                {\n" +
                        "                    begin: (?<=\\(|,)\\s*\n" +
                        "                    end: \\s*(?:(,)|(?=\\)))\n" +
                        "                    endCaptures: {\n" +
                        "                        1: { name: punctuation.separator.inheritance.python },\n" +
                        "                    }\n" +
                        "                    contentName: entity.other.inherited-class.python\n" +
                        "                    patterns: [\n" +
                        "                        { include: $self },\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: \\s*(class)\\s+(?=[a-zA-Z_][a-zA-Z_0-9])\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: storage.type.class.python },\n" +
                        "    }\n" +
                        "    end: (\\()|\\s*($\\n?|#.*$\\n?)\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.inheritance.begin.python },\n" +
                        "        2: { name: invalid.illegal.missing-inheritance.python },\n" +
                        "    }\n" +
                        "    name: meta.class.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?=[A-Za-z_][A-Za-z0-9_]*)\n" +
                        "            end: (?![A-Za-z0-9_])\n" +
                        "            contentName: entity.name.type.class.python\n" +
                        "            patterns: [\n" +
                        "                { include: #entity_name_function },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: \\s*(def)\\s+(?=[A-Za-z_][A-Za-z0-9_]*\\s*\\()\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: storage.type.function.python },\n" +
                        "    }\n" +
                        "    end: (\\:)\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.section.function.begin.python },\n" +
                        "    }\n" +
                        "    name: meta.function.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?=[A-Za-z_][A-Za-z0-9_]*)\n" +
                        "            end: (?![A-Za-z0-9_])\n" +
                        "            contentName: entity.name.function.python\n" +
                        "            patterns: [\n" +
                        "                { include: #entity_name_function },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\\()\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.parameters.begin.python },\n" +
                        "            }\n" +
                        "            end: (?=\\)\\s*(?:\\:|-\\>))\n" +
                        "            contentName: meta.function.parameters.python\n" +
                        "            patterns: [\n" +
                        "                { include: #annotated_arguments },\n" +
                        "                { include: #keyword_arguments },\n" +
                        "                { include: #comments },\n" +
                        "                {\n" +
                        "                    match: \\b(?:(self|cls)|([a-zA-Z_][a-zA-Z_0-9]*))\\s*(?:(,)|(?=[\\n\\)]))\n"
                        +
                        "                    captures: {\n" +
                        "                        1: { name: variable.parameter.function.language.python },\n" +
                        "                        2: { name: variable.parameter.function.python },\n" +
                        "                        3: { name: punctuation.separator.parameters.python },\n" +
                        "                    }\n" +
                        "                },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\\))\\s*(\\->)\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.parameters.end.python },\n" +
                        "                2: { name: punctuation.separator.annotation.result.python },\n" +
                        "            }\n" +
                        "            end: (?=\\:)\n" +
                        "            patterns: [\n" +
                        "                { include: $self },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: \\s*(def)\\s+(?=[A-Za-z_][A-Za-z0-9_]*)\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: storage.type.function.python },\n" +
                        "    }\n" +
                        "    end: (\\()|\\s*($\\n?|#.*$\\n?)\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.parameters.begin.python },\n" +
                        "        2: { name: invalid.illegal.missing-parameters.python },\n" +
                        "    }\n" +
                        "    name: meta.function.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?=[A-Za-z_][A-Za-z0-9_]*)\n" +
                        "            end: (?![A-Za-z0-9_])\n" +
                        "            contentName: entity.name.function.python\n" +
                        "            patterns: [\n" +
                        "                { include: #entity_name_function },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: (lambda)(?=\\s+|:)\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: storage.type.function.inline.python },\n" +
                        "    }\n" +
                        "    end: (\\:)\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.parameters.end.python },\n" +
                        "        2: { name: punctuation.section.function.begin.python },\n" +
                        "        3: { name: invalid.illegal.missing-section-begin.python },\n" +
                        "    }\n" +
                        "    name: meta.function.inline.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: \\s+\n" +
                        "            end: (?=\\:)\n" +
                        "            contentName: meta.function.inline.parameters.python\n" +
                        "            patterns: [\n" +
                        "                { include: #keyword_arguments },\n" +
                        "                {\n" +
                        "                    match: \\b([a-zA-Z_][a-zA-Z_0-9]*)\\s*(?:(,)|(?=[\\n\\)\\:]))\n" +
                        "                    captures: {\n" +
                        "                        1: { name: variable.parameter.function.python },\n" +
                        "                        2: { name: punctuation.separator.parameters.python },\n" +
                        "                    }\n" +
                        "                },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: ^\\s*(?=@\\s*[A-Za-z_][A-Za-z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z_0-9]*)*\\s*\\()\n" +
                        "    end: (\\))\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.arguments.end.python },\n" +
                        "    }\n" +
                        "    name: meta.function.decorator.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?=(@)\\s*[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*\\s*\\()\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.decorator.python },\n" +
                        "            }\n" +
                        "            end: (?=\\s*\\()\n" +
                        "            contentName: entity.name.function.decorator.python\n" +
                        "            patterns: [\n" +
                        "                { include: #dotted_name },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\\()\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.arguments.begin.python },\n" +
                        "            }\n" +
                        "            end: (?=\\))\n" +
                        "            contentName: meta.function.decorator.arguments.python\n" +
                        "            patterns: [\n" +
                        "                { include: #keyword_arguments },\n" +
                        "                { include: $self },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: ^\\s*(?=@\\s*[A-Za-z_][A-Za-z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z_0-9]*)*)\n" +
                        "    end: (?=\\s|$\\n?|#)\n" +
                        "    contentName: entity.name.function.decorator.python\n" +
                        "    name: meta.function.decorator.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?=(@)\\s*[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*)\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.decorator.python },\n" +
                        "            }\n" +
                        "            end: (?=\\s|$\\n?|#)\n" +
                        "            patterns: [\n" +
                        "                { include: #dotted_name },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: (?<=\\)|\\])\\s*(\\()\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: punctuation.definition.arguments.begin.python },\n" +
                        "    }\n" +
                        "    end: (\\))\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.arguments.end.python },\n" +
                        "    }\n" +
                        "    contentName: meta.function-call.arguments.python\n" +
                        "    name: meta.function-call.python\n" +
                        "    patterns: [\n" +
                        "        { include: #keyword_arguments },\n" +
                        "        { include: $self },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{ include: #builtin_functions }\n" +
                        "{ include: #builtin_types }\n" +
                        "{ include: #builtin_exceptions }\n" +
                        "{ include: #docstrings }\n" +
                        "{ include: #magic_function_names }\n" +
                        "{ include: #magic_variable_names }\n" +
                        "{ include: #language_variables }\n" +
                        "{ include: #generic_object_names }\n" +
                        "{\n" +
                        "    begin: (?:\\.)?([a-zA-Z_][a-zA-Z_0-9]*)\\s*(?=\\()\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: meta.function-call.generic.python },\n" +
                        "    }\n" +
                        "    end: (\\))\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.arguments.end.python },\n" +
                        "    }\n" +
                        "    name: meta.function-call.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?=[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*\\s*\\()\n" +
                        "            end: (?=\\s*\\()\n" +
                        "            patterns: [\n" +
                        "                { include: #dotted_name },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\\()\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.arguments.begin.python },\n" +
                        "            }\n" +
                        "            end: (?=\\))\n" +
                        "            contentName: meta.function-call.arguments.python\n" +
                        "            patterns: [\n" +
                        "                { include: #keyword_arguments },\n" +
                        "                { include: $self },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: (?=[A-Za-z_][A-Za-z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z_0-9]*)*\\s*\\[)\n" +
                        "    end: (\\])\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.arguments.end.python },\n" +
                        "    }\n" +
                        "    name: meta.item-access.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?=[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*\\s*\\[)\n" +
                        "            end: (?=\\s*\\[)\n" +
                        "            patterns: [\n" +
                        "                { include: #dotted_name },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\\[)\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.arguments.begin.python },\n" +
                        "            }\n" +
                        "            end: (?=\\])\n" +
                        "            contentName: meta.item-access.arguments.python\n" +
                        "            patterns: [\n" +
                        "                { include: $self },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: (?<=\\)|\\])\\s*(\\[)\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: punctuation.definition.arguments.begin.python },\n" +
                        "    }\n" +
                        "    end: (\\])\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.arguments.end.python },\n" +
                        "    }\n" +
                        "    contentName: meta.item-access.arguments.python\n" +
                        "    name: meta.item-access.python\n" +
                        "    patterns: [\n" +
                        "        { include: $self },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(def|lambda)\\b\n" +
                        "    captures: {\n" +
                        "        1: { name: storage.type.function.python },\n" +
                        "    }\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(class)\\b\n" +
                        "    captures: {\n" +
                        "        1: { name: storage.type.class.python },\n" +
                        "    }\n" +
                        "}\n" +
                        "{ include: #line_continuation }\n" +
                        "{ include: #language_variables }\n" +
                        "{ include: #string_quoted_single }\n" +
                        "{ include: #string_quoted_double }\n" +
                        "{ include: #dotted_name }\n" +
                        "{\n" +
                        "    begin: (\\()\n" +
                        "    end: (\\))\n" +
                        "    patterns: [\n" +
                        "        { include: $self },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    match: (\\[)(\\s*(\\]))\\b\n" +
                        "    captures: {\n" +
                        "        1: { name: punctuation.definition.list.begin.python },\n" +
                        "        2: { name: meta.empty-list.python },\n" +
                        "        3: { name: punctuation.definition.list.end.python },\n" +
                        "    }\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: (\\[)\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: punctuation.definition.list.begin.python },\n" +
                        "    }\n" +
                        "    end: (\\])\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.list.end.python },\n" +
                        "    }\n" +
                        "    name: meta.structure.list.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?<=\\[|\\,)\\s*(?![\\],])\n" +
                        "            end: \\s*(?:(,)|(?=\\]))\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.separator.list.python },\n" +
                        "            }\n" +
                        "            contentName: meta.structure.list.item.python\n" +
                        "            patterns: [\n" +
                        "                { include: $self },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "{\n" +
                        "    match: \\b(\\()(\\s*(\\)))\\b\n" +
                        "    captures: {\n" +
                        "        1: { name: punctuation.definition.tuple.begin.python },\n" +
                        "        2: { name: meta.empty-tuple.python },\n" +
                        "        3: { name: punctuation.definition.tuple.end.python },\n" +
                        "    }\n" +
                        "    name: meta.structure.tuple.python\n" +
                        "}\n" +
                        "{\n" +
                        "    match: (\\{)(\\s*(\\}))\n" +
                        "    captures: {\n" +
                        "        1: { name: punctuation.definition.dictionary.begin.python },\n" +
                        "        2: { name: meta.empty-dictionary.python },\n" +
                        "        3: { name: punctuation.definition.dictionary.end.python },\n" +
                        "    }\n" +
                        "    name: meta.structure.dictionary.python\n" +
                        "}\n" +
                        "{\n" +
                        "    begin: (\\{)\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: punctuation.definition.dictionary.begin.python },\n" +
                        "    }\n" +
                        "    end: (\\})\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.definition.dictionary.end.python },\n" +
                        "    }\n" +
                        "    name: meta.structure.dictionary.python\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?<=\\{|\\,|^)\\s*(?![\\},])\n" +
                        "            end: \\s*(?:(?=\\})|(\\:))\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.separator.valuepair.dictionary.python },\n" +
                        "            }\n" +
                        "            contentName: meta.structure.dictionary.key.python\n" +
                        "            patterns: [\n" +
                        "                { include: $self },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (?<=\\:|^)\\s*\n" +
                        "            end: \\s*(?:(?=\\})|(,))\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.separator.dictionary.python },\n" +
                        "            }\n" +
                        "            contentName: meta.structure.dictionary.value.python\n" +
                        "            patterns: [\n" +
                        "                { include: $self },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "",
                buf.toString());
    }
}
