package org.brainwy.liclipsetext.editor.tmbundle;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.IPrintableRule;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmLanguageHandler;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;

import junit.framework.TestCase;

public class TmLanguageHandlerTest extends TestCase {

    public void testLanguageHandler() throws Exception {
        TmLanguageHandler parser = new TmLanguageHandler();
        parser.parse(new File(TestUtils.getTestLanguagesDir(), "language_test.tmLanguage"));
        Map<String, ILiClipsePredicateRule> loadRepositoryRules = parser.loadRepositoryRules(null);
        FastStringBuffer buf = new FastStringBuffer();
        for (Entry<String, ILiClipsePredicateRule> entry : loadRepositoryRules.entrySet()) {
            buf.append(entry.getKey()).append(": ").append(((IPrintableRule) entry.getValue()).toTmYaml());
            buf.append("\n");
        }
        assertEquals(
                "annotated_arguments: {\n" +
                        "    begin: \\b([a-zA-Z_][a-zA-Z_0-9]*)\\s*(:)|(?=\\()\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: variable.parameter.function.python },\n" +
                        "        2: { name: punctuation.separator.annotation.python },\n" +
                        "    }\n" +
                        "    end: \\s*(?:(,)|(?=$\\n?|[\\)\\:]))\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.separator.parameters.python },\n" +
                        "    }\n" +
                        "    patterns: [\n" +
                        "        { include: #annotated_group },\n" +
                        "        {\n" +
                        "            match: =\n" +
                        "            name: keyword.operator.assignment.python\n" +
                        "        },\n" +
                        "        { include: $self },\n" +
                        "    ]\n" +
                        "}\n" +
                        "annotated_group: {\n" +
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
                        "}\n" +
                        "builtin_exceptions: {\n" +
                        "    match: \\b((Arithmetic|Assertion|Attribute|Buffer|EOF|Environment|FloatingPoint|IO|Import|Indentation|Index|Key|Lookup|Memory|Name|NotImplemented|OS|Overflow|Reference|Runtime|Standard|Syntax|System|Tab|Type|UnboundLocal|Unicode(Encode|Decode|Translate)?|Value|VMS|Windows|ZeroDivision|([.a-zA-Z0-9_]+))Error|((Pending)?Deprecation|Runtime|Syntax|User|Future|Import|Unicode|Bytes)?Warning|SystemExit|StopIteration|NotImplemented|KeyboardInterrupt|GeneratorExit|([.a-zA-Z0-9_]+)?Exception)\\b\n"
                        +
                        "    name: support.type.exception.python\n" +
                        "}\n" +
                        "builtin_functions: {\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (?<!\\.)(__import__|ascii|abs|all|any|apply|bin|bool|buffer|bytearray|bytes|callable|chr|classmethod|cmp|coerce|compile|complex|copyright|credits|delattr|dict|dir|divmod|enumerate|eval|exec|execfile|exit|file|filter|float|format|frozenset|getattr|globals|hasattr|hash|help|hex|id|input|int|intern|isinstance|issubclass|iter|len|license|list|locals|long|map|max|memoryview|min|next|object|oct|open|ord|pow|print|property|quit|range|raw_input|reduce|reload|repr|reversed|round|set|setattr|slice|sorted|staticmethod|str|sum|super|tuple|type|unicode|unichr|vars|xrange|zip)\\s*(?=\\()\n"
                        +
                        "            beginCaptures: {\n" +
                        "                1: { name: support.function.builtin.python },\n" +
                        "            }\n" +
                        "            end: (\\))\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.arguments.end.python },\n" +
                        "            }\n" +
                        "            name: meta.function-call.python\n" +
                        "            patterns: [\n" +
                        "                {\n" +
                        "                    begin: (?=[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*\\s*\\()\n" +
                        "                    end: (?=\\s*\\()\n" +
                        "                    patterns: [\n" +
                        "                        { include: #dotted_name },\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    begin: (\\()\n" +
                        "                    beginCaptures: {\n" +
                        "                        1: { name: punctuation.definition.arguments.begin.python },\n" +
                        "                    }\n" +
                        "                    end: (?=\\))\n" +
                        "                    contentName: meta.function-call.arguments.python\n" +
                        "                    patterns: [\n" +
                        "                        { include: #keyword_arguments },\n" +
                        "                        { include: $self },\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "builtin_types: {\n" +
                        "    match: (?<!\\.)\\b(ascii|basestring|bin|bool|buffer|bytearray|bytes|classmethod|complex|dict|file|float|frozenset|hex|int|list|long|object|oct|property|reversed|set|slice|staticmethod|str|super|tuple|type|unicode|xrange)\\b\n"
                        +
                        "    name: support.type.python\n" +
                        "}\n" +
                        "character-class: {\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            match: \\\\[wWsSdDhH]|\\.\n" +
                        "            name: constant.character.character-class.regex.python\n" +
                        "        },\n" +
                        "        {\n" +
                        "            match: \\\\.\n" +
                        "            name: constant.character.escape.backslash.regex.python\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\\[)(\\^)?\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.character-class.regex.python },\n" +
                        "                2: { name: keyword.operator.negation.regex.python },\n" +
                        "            }\n" +
                        "            end: (\\])\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.character-class.regex.python },\n" +
                        "            }\n" +
                        "            name: constant.other.character-class.set.regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #character-class },\n" +
                        "                {\n" +
                        "                    match: ((\\\\.)|.)\\-((\\\\.)|[^\\]])\n" +
                        "                    captures: {\n" +
                        "                        2: { name: constant.character.escape.backslash.regex.python },\n" +
                        "                        4: { name: constant.character.escape.backslash.regex.python },\n" +
                        "                    }\n" +
                        "                    name: constant.other.character-class.range.regex.python\n" +
                        "                },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "comments: {\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            match: (#)\\s*(BUG|FIXME|TODO|XXX).*$\\n?\n" +
                        "            captures: {\n" +
                        "                1: { name: punctuation.definition.comment.python },\n" +
                        "                2: { name: comment.line.note.notation.python },\n" +
                        "            }\n" +
                        "            name: comment.line.note.python\n" +
                        "        },\n" +
                        "        {\n" +
                        "            match: (#).*$\\n?\n" +
                        "            captures: {\n" +
                        "                1: { name: punctuation.definition.comment.python },\n" +
                        "            }\n" +
                        "            name: comment.line.number-sign.python\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "constant_placeholder: {\n" +
                        "    match: (?i:%(\\([a-z_]+\\))?#?0?\\-?[ ]?\\+?([0-9]*|\\*)(\\.([0-9]*|\\*))?[hL]?[a-z%])\n" +
                        "    name: constant.other.placeholder.python\n" +
                        "}\n" +
                        "docstrings: {\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: ^\\s*(?=[uU]?[rR]?\"\"\")\n" +
                        "            end: (?<=\"\"\")\n" +
                        "            name: comment.block.python\n" +
                        "            patterns: [\n" +
                        "                { include: #string_quoted_double },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ^\\s*(?=[uU]?[rR]?''')\n" +
                        "            end: (?<=''')\n" +
                        "            name: comment.block.python\n" +
                        "            patterns: [\n" +
                        "                { include: #string_quoted_single },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "dotted_name: {\n" +
                        "    begin: (?=[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*)\n" +
                        "    end: (?![A-Za-z0-9_\\.])\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: (\\.)(?=[A-Za-z_][A-Za-z0-9_]*)\n" +
                        "            end: (?![A-Za-z0-9_])\n" +
                        "            patterns: [\n" +
                        "                { include: #magic_function_names },\n" +
                        "                { include: #magic_variable_names },\n" +
                        "                { include: #illegal_names },\n" +
                        "                { include: #generic_names },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (?<!\\.)(?=[A-Za-z_][A-Za-z0-9_]*)\n" +
                        "            end: (?![A-Za-z0-9_])\n" +
                        "            patterns: [\n" +
                        "                { include: #builtin_functions },\n" +
                        "                { include: #builtin_types },\n" +
                        "                { include: #builtin_exceptions },\n" +
                        "                { include: #illegal_names },\n" +
                        "                { include: #magic_function_names },\n" +
                        "                { include: #magic_variable_names },\n" +
                        "                { include: #language_variables },\n" +
                        "                { include: #generic_names },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "entity_name_class: {\n" +
                        "    patterns: [\n" +
                        "        { include: #illegal_names },\n" +
                        "        { include: #generic_names },\n" +
                        "    ]\n" +
                        "}\n" +
                        "entity_name_function: {\n" +
                        "    patterns: [\n" +
                        "        { include: #magic_function_names },\n" +
                        "        { include: #illegal_names },\n" +
                        "        { include: #generic_names },\n" +
                        "    ]\n" +
                        "}\n" +
                        "escaped_char: {\n" +
                        "    match: (\\\\x[0-9a-fA-F]{2})|(\\\\[0-7]{3})|(\\\\\\n)|(\\\\\\\\)|(\\\\\\\")|(\\\\')|(\\\\a)|(\\\\b)|(\\\\f)|(\\\\n)|(\\\\r)|(\\\\t)|(\\\\v)\n"
                        +
                        "    captures: {\n" +
                        "        1: { name: constant.character.escape.hex.python },\n" +
                        "        2: { name: constant.character.escape.octal.python },\n" +
                        "        3: { name: constant.character.escape.newline.python },\n" +
                        "        4: { name: constant.character.escape.backlash.python },\n" +
                        "        5: { name: constant.character.escape.double-quote.python },\n" +
                        "        6: { name: constant.character.escape.single-quote.python },\n" +
                        "        7: { name: constant.character.escape.bell.python },\n" +
                        "        8: { name: constant.character.escape.backspace.python },\n" +
                        "        9: { name: constant.character.escape.formfeed.python },\n" +
                        "        10: { name: constant.character.escape.linefeed.python },\n" +
                        "        11: { name: constant.character.escape.return.python },\n" +
                        "        12: { name: constant.character.escape.tab.python },\n" +
                        "        13: { name: constant.character.escape.vertical-tab.python },\n" +
                        "    }\n" +
                        "}\n" +
                        "escaped_unicode_char: {\n" +
                        "    match: (\\\\U[0-9A-Fa-f]{8})|(\\\\u[0-9A-Fa-f]{4})|(\\\\N\\{[a-zA-Z0-9\\, ]+\\})\n" +
                        "    captures: {\n" +
                        "        1: { name: constant.character.escape.unicode.16-bit-hex.python },\n" +
                        "        2: { name: constant.character.escape.unicode.32-bit-hex.python },\n" +
                        "        3: { name: constant.character.escape.unicode.name.python },\n" +
                        "    }\n" +
                        "}\n" +
                        "function_name: {\n" +
                        "    patterns: [\n" +
                        "        { include: #magic_function_names },\n" +
                        "        { include: #magic_variable_names },\n" +
                        "        { include: #builtin_exceptions },\n" +
                        "        { include: #builtin_functions },\n" +
                        "        { include: #builtin_types },\n" +
                        "        { include: #generic_names },\n" +
                        "    ]\n" +
                        "}\n" +
                        "generic_names: {\n" +
                        "    match: [A-Za-z_][A-Za-z0-9_]*\n" +
                        "}\n" +
                        "generic_object_names: {\n" +
                        "    match: (\\.\\b([A-Za-z_][A-Za-z0-9_]*)\\b(?!\\(|\\[)|\\b([A-Za-z_][A-Za-z0-9_]*)\\b\\.)\n"
                        +
                        "}\n" +
                        "illegal_names: {\n" +
                        "    match: \\b(and|as|assert|break|class|continue|def|del|elif|else|except|exec|finally|for|from|global|if|import|in|is|lambda|nonlocal|not|or|pass|print|raise|return|try|while|with|yield)\\b\n"
                        +
                        "    name: invalid.illegal.name.python\n" +
                        "}\n" +
                        "keyword_arguments: {\n" +
                        "    begin: \\b([a-zA-Z_][a-zA-Z_0-9]*)\\s*(=)(?!=)\n" +
                        "    beginCaptures: {\n" +
                        "        1: { name: variable.parameter.function.keyword.python },\n" +
                        "        2: { name: keyword.operator.assignment.python },\n" +
                        "    }\n" +
                        "    end: \\s*(?:(,)|(?=$\\n?|[\\)\\:]))\n" +
                        "    endCaptures: {\n" +
                        "        1: { name: punctuation.separator.parameters.python },\n" +
                        "    }\n" +
                        "    patterns: [\n" +
                        "        { include: $self },\n" +
                        "    ]\n" +
                        "}\n" +
                        "language_variables: {\n" +
                        "    match: (?<!\\.)\\b(self|cls)\\b\n" +
                        "    name: variable.language.python\n" +
                        "}\n" +
                        "line_continuation: {\n" +
                        "    match: (\\\\)(.*)$\\n?\n" +
                        "    captures: {\n" +
                        "        1: { name: punctuation.separator.continuation.line.python },\n" +
                        "    }\n" +
                        "}\n" +
                        "magic_function_names: {\n" +
                        "    match: (\\.)?\\b(__(?:abs|add|and|bool|call|ceil|cmp|coerce|complex|contains|copy|deepcopy|del|delattr|delete|delitem|delslice|dir|div|divmod|enter|eq|exit|float|floor|floordiv|format|ge|get|getattr|getattribute|getinitargs|getitem|getnewargs|getslice|getstate|gt|hash|hex|iadd|iand|idiv|ifloordiv|ilshift|imod|imul|index|init|instancecheck|int|invert|ior|ipow|irshift|isub|iter|itruediv|ixor|le|len|long|lshift|lt|missing|mod|mul|ne|neg|new|nonzero|oct|or|pos|pow|radd|rand|rdiv|rdivmod|reduce|reduce_ex|repr|reversed|rfloordiv|rlshift|rmod|rmul|ror|round|rpow|rrshift|rshift|rsub|rtruediv|rxor|set|setattr|setitem|setslice|setstate|sizeof|str|sub|subclasscheck|truediv|trunc|unicode|xor)__)\\b\n"
                        +
                        "    captures: {\n" +
                        "        2: { name: support.function.magic.python },\n" +
                        "    }\n" +
                        "}\n" +
                        "magic_variable_names: {\n" +
                        "    match: (\\.)?\\b(__(?:all|bases|class|debug|dict|doc|file|members|metaclass|methods|module|mro|name|slots|subclasses|version|weakref)__)\\b\n"
                        +
                        "    captures: {\n" +
                        "        2: { name: support.variable.magic.python },\n" +
                        "    }\n" +
                        "}\n" +
                        "regular_expressions: {\n" +
                        "    patterns: [\n" +
                        "        { include: source.regexp.python },\n" +
                        "    ]\n" +
                        "}\n" +
                        "string_quoted_double: {\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            begin: ([uU][rR])(\"\"\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\"\"\")(\")\"\"|\"\"\")\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.block.unicode-raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([bB][rR])(\"\"\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\"\"\")(\")\"\"|\"\"\")\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.block.bytes-raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([rR])(\"\"\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\"\"\")(\")\"\"|\"\"\")\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.block.raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([uU])(\"\"\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\"\"\")(\")\"\"|\"\"\")\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.block.unicode.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([bB])(\"\"\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\"\"\")(\")\"\"|\"\"\")\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.block.bytes.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([uU][rR])(\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\")(\")|\")|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "                3: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.single-line.unicode-raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([bB][rR])(\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\")(\")|\")|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "                3: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.single-line.bytes-raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([rR])(\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\")(\")|\")|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "                3: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.single-line.raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([uU])(\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\")(\")|\")|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "                3: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.single-line.unicode.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([bB])(\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\")(\")|\")|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "                3: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.single-line.bytes.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\"\"\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\"\"\")(\")\"\"|\"\"\")\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.block.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (\")\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=\")(\")|\")|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.double.python },\n" +
                        "                3: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.double.single-line.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "string_quoted_single: {\n" +
                        "    patterns: [\n" +
                        "        {\n" +
                        "            match: (?<!')(')(('))(?!')\n" +
                        "            captures: {\n" +
                        "                1: { name: punctuation.definition.string.begin.python },\n" +
                        "                2: { name: punctuation.definition.string.end.python },\n" +
                        "                3: { name: meta.empty-string.single.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.single-line.python\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([uU][rR])(''')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=''')(')''|''')\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.single.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.block.unicode-raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([bB][rR])(''')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=''')(')''|''')\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.single.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.block.bytes-raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([rR])(''')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=''')(')''|''')\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.single.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.block.raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([uU])(''')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=''')(')''|''')\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.single.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.block.unicode.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([bB])(''')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=''')(')''|''')\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.single.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.block.bytes.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([uU][rR])(')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: (')|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.single-line.unicode-raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([bB][rR])(')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: (')|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.single-line.bytes-raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([rR])(')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: (')|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.single-line.raw-regex.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_char },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #regular_expressions },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([uU])(')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: (')|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.single-line.unicode.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: ([bB])(')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: storage.type.string.python },\n" +
                        "                2: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: (')|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.single-line.bytes.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (''')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: ((?<=''')(')''|''')\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: meta.empty-string.single.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.block.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            begin: (')\n" +
                        "            beginCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.begin.python },\n" +
                        "            }\n" +
                        "            end: (')|(\\n)\n" +
                        "            endCaptures: {\n" +
                        "                1: { name: punctuation.definition.string.end.python },\n" +
                        "                2: { name: invalid.illegal.unclosed-string.python },\n" +
                        "            }\n" +
                        "            name: string.quoted.single.single-line.python\n" +
                        "            patterns: [\n" +
                        "                { include: #constant_placeholder },\n" +
                        "                { include: #escaped_unicode_char },\n" +
                        "                { include: #escaped_char },\n" +
                        "            ]\n" +
                        "        },\n" +
                        "    ]\n" +
                        "}\n" +
                        "strings: {\n" +
                        "    patterns: [\n" +
                        "        { include: #string_quoted_double },\n" +
                        "        { include: #string_quoted_single },\n" +
                        "    ]\n" +
                        "}\n" +
                        "",
                buf.toString());

    }
}
