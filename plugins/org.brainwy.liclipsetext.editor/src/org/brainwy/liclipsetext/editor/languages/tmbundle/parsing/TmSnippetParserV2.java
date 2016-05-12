/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.AnyOfCharsRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.DigitsRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.SingleLineRule;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
# In Snippets

## Placeholders
    $int
    ${int}
    ${int:snippet}
    ${int/regexp/format/options}
    ${int|choice 1,...,choice n|} --> transform this into a template variable!

## Code
    `code`

# In Format Strings
    $0-n
    \\U, \\L, \\E, \\u, \\l
    \\t, \\r, \\n, \\x{HHHH}, \\xHH
    variables
    (?var:if:else}
    (?var:if}

# In Both

## Variables
    ${var}
    ${var:?if:else}
    ${var:+if}
    ${var:-else}
    ${var:else}
    ${var/regexp/format/options}
    ${var:[/upcase][/downcase][/capitalize][/asciify]}


Some notes:

If we have something as ${TM_XHTML}, we actually have to replace it for the value of a "TM_XHTML" variable (which could be / in this case).
 */
public class TmSnippetParserV2 {

    private static final IToken OK_TOKEN = new Token("digits");
    private static final DigitsRule digitsRule = new DigitsRule(OK_TOKEN);
    private static final AnyOfCharsRule regexpOptionsRule = new AnyOfCharsRule(OK_TOKEN, "giems");
    private static final SingleLineRule codeRule = new SingleLineRule("`", OK_TOKEN, '\0', false);

    public static SnippetToTemplateCtx createReplacement(String snippet, SnippetToTemplateCtx ctx) {
        StringScanner scanner = new StringScanner(snippet);
        Node root = new RootNode();

        parseSnippet(scanner, root);
        root.toTemplateStr(ctx);
        return ctx;
    }

    /**
     * Convert the stream (StringScanner) into a tree (Node).
     */
    public static boolean parseSnippet(StringScanner scanner, Node root) {
        final int initialParse = scanner.getMark();
        while (true) {
            final int mark = scanner.getMark();
            if (checkPlaceholder(scanner, root)) {
                continue;
            }
            if (checkVariable(scanner, root, Parsing.PARSE_SNIPPET)) {
                continue;
            }

            if (codeRule.evaluate(scanner) == OK_TOKEN) {
                root.addChild(new CodeNode(scanner.getContents(), mark + 1, scanner.getMark() - 1));
                continue;
            }
            int c = scanner.read();
            if (c == StringScanner.EOF || scanner.isEndLevelChar(c)) {
                scanner.unread();
                // We can only return false if we didn't read anything...
                return scanner.getMark() != initialParse;
            }

            if (c == '\\') {
                int c1 = scanner.read();
                if (c == StringScanner.EOF) { //EOF at escape char (this is probably not even valid...)
                    scanner.unread();
                } else {
                    c = c1;
                }
            }

            //Ok, all failed, consider it a regular text
            root.addChild(new TextNode((char) c, scanner.getLevel()));
        }
    }

    public static boolean parseFormatString(StringScanner scanner, Node root) {
        final int initialParse = scanner.getMark();
        RESTART_OUTER_LOOP: while (true) {
            final int mark = scanner.getMark();
            int c = scanner.read();
            if (c == '$') {
                //$0-n
                if (digitsRule.evaluate(scanner) == OK_TOKEN) {
                    root.addChild(new VariableNode(scanner.getContents(), mark + 1, scanner.getMark()));
                    continue;
                }
                c = scanner.read();
                if (StringUtils.isAsciiLetterOrUnderline(c)) {
                    do {
                        c = scanner.read();
                    } while (StringUtils.isAsciiLetterOrUnderlineOrNumber(c));
                    scanner.unread();
                    root.addChild(new VariableNode(scanner.getContents(), mark + 1, scanner.getMark()));
                    continue;
                } else {
                    scanner.unread();
                }
            }
            if (c == '\\') {
                // Handle the cases below AND escape.
                //\\U, \\L, \\E, \\u, \\l -- upper/lower/explicit/upper/lower -- must check better...
                //\\t, \\r, \\n, \\x{HHHH}, \\xHH
                int c1 = scanner.read();
                if (c == StringScanner.EOF) { //EOF at escape char (this is probably not even valid...)
                    scanner.unread();
                } else {
                    OUT_SWITCH: switch (c1) {
                        case 'U':
                        case 'L':
                        case 'E':
                        case 'u':
                        case 'l':
                        case 't':
                        case 'r':
                        case 'n':
                            break;
                        case 'x':
                            int initial = scanner.getMark();
                            int c2 = scanner.read();
                            if (c2 == '{') {
                                do {
                                    c2 = scanner.read();
                                    if (c2 == StringScanner.EOF) {
                                        scanner.setMark(initial);
                                        break OUT_SWITCH;
                                    }
                                } while (c2 != '}');
                                root.addChild(new TextNode(
                                        scanner.getContents().substring(initial - 2, scanner.getMark()),
                                        scanner.getLevel()));
                                continue RESTART_OUTER_LOOP;
                            }
                            break;
                        default:
                            break;
                    }
                    c = c1;
                    root.addChild(new TextNode("\\" + (char) c, scanner.getLevel()));
                    continue;
                }
            }

            scanner.unread();

            //variables
            if (checkVariable(scanner, root, Parsing.PARSE_FORMAT)) {
                continue;
            }

            //(?var:if:else}
            //(?var:if}
            if (checkCondition(scanner, root)) {
                continue;
            }

            c = scanner.read();
            if (c == StringScanner.EOF || scanner.isEndLevelChar(c)) {
                scanner.unread();
                // We can only return false if we didn't read anything...
                return scanner.getMark() != initialParse;
            }

            //Ok, all failed, consider it a regular text
            root.addChild(new TextNode((char) c, scanner.getLevel()));
        }
    }

    //(?var:if:else)
    //(?var:if) <-- note, it seems the help had a typo in the end (it had '}' instead of ')')
    private static boolean checkCondition(StringScanner scanner, Node root) {
        final int mark = scanner.getMark();
        int c = scanner.read();
        if (c == '(') {
            c = scanner.read();
            if (c == '?') {
                if (digitsRule.evaluate(scanner) == OK_TOKEN) {
                    c = scanner.read();
                    if (c == ':') {
                        ConditionNode conditionNode = new ConditionNode(scanner.getContents(), mark + 2,
                                scanner.getMark() - 1);
                        scanner.addLevelFinishingAt(':', ')');
                        parseFormatString(scanner, conditionNode);
                        scanner.popLevel();

                        int peeked = scanner.read();
                        if (peeked == ')' || peeked == ':') {
                            //Ok, if condition found.
                            IfNode ifNode = new IfNode();
                            ifNode.addChildren(conditionNode.getAndClearChildren()); //Moving the nodes from condition to if.

                            ElseNode elseNode = null;
                            if (peeked == ':') {
                                //Let's get the else condition
                                scanner.addLevelFinishingAt(')');
                                int markElseCondition = scanner.getMark();
                                parseFormatString(scanner, conditionNode);
                                scanner.popLevel();

                                peeked = scanner.read();
                                if (peeked == ')') {
                                    //Ok, found it
                                    elseNode = new ElseNode(scanner.getContents(), markElseCondition,
                                            scanner.getMark() - 1);
                                    ifNode.setElseNode(elseNode);
                                    elseNode.addChildren(conditionNode.getAndClearChildren()); //Moving the nodes from condition to else.

                                } else {
                                    //bummer...
                                    scanner.setMark(mark);
                                    return false;
                                }
                            }

                            conditionNode.addChild(ifNode);
                            root.addChild(conditionNode);
                            return true;
                        }
                        //Not this time...
                        scanner.setMark(mark);
                        return false;
                    }
                }
            }
        }
        //If it didn't return sooner we had no match, let's get back
        //to where we started.
        scanner.setMark(mark);
        return false;
    }

    private enum Parsing {
        PARSE_SNIPPET, PARSE_FORMAT
    }

    //${var}
    //${var:?if:else}
    //${var:+if}
    //${var:-else}
    //${var:else}
    //${var/regexp/format/options}
    //${var:[/upcase][/downcase][/capitalize][/asciify]}
    private static boolean checkVariable(StringScanner scanner, Node root, Parsing parsing) {
        final int mark = scanner.getMark();
        int c = scanner.read();
        if (c == '$') {
            c = scanner.read();

            if (c == '{') {
                // Some thigs to work on:
                // ${MY_VAR}
                // ${1:/downcase} -- could also be upcase, downcase, capitalize, asciify
                // ${1/(#)(#)?(#)?(#)?(#)?(#)?/${6:?6:${5:?5:${4:?4:${3:?3:${2:?2:1}}}}}/}

                while (true) {
                    c = scanner.read();
                    if (c == StringScanner.EOF) {
                        //EOF in the middle of a var (seems like an error).
                        break;
                    }
                    if (c == '\\') {
                        int c1 = scanner.read();
                        if (c == StringScanner.EOF) { //EOF at escape char (this is probably not even valid...)
                            scanner.unread();
                        } else {
                            c = c1;
                        }
                    }
                    if (c == '}') {
                        //${var}
                        //Ok, we have something as ${MY_VAR} at this point
                        root.addChild(new VariableNode(scanner.getContents(), mark + 2, scanner.getMark() - 1));
                        return true;

                    }
                    if (c == ':') {
                        //${var:?if:else} -- note: it seems the if/else are defined by how we started parsing (snippets/format string).
                        //${var:+if}
                        //${var:-else}
                        //${var:else}
                        //${var:[/upcase][/downcase][/capitalize][/asciify]}
                        VariableNode var = new VariableNode(scanner.getContents(), mark + 2, scanner.getMark() - 1);
                        c = scanner.read();
                        if (c == '?') {
                            //${var:?if:else}
                            scanner.addLevelFinishingAt(':');
                            try {
                                IfNode ifNode = new IfNode();
                                ElseNode elseNode = new ElseNode("", 0, 0);
                                var.setIfNode(ifNode);
                                ifNode.setElseNode(elseNode);

                                if (chooseParsing(scanner, ifNode, parsing)) {
                                    if (scanner.read() != ':') {
                                        break;
                                    }
                                    scanner.popLevel();
                                    scanner.addLevelFinishingAt('}');
                                    if (chooseParsing(scanner, elseNode, parsing)) {
                                        if (scanner.read() != '}') {
                                            break;
                                        }
                                        root.addChild(var);
                                        return true;
                                    }
                                } else {
                                    break;
                                }
                            } finally {
                                scanner.popLevel();
                            }

                        } else if (c == '+') {
                            //${var:+if}
                            scanner.addLevelFinishingAt('}');
                            try {
                                IfNode ifNode = new IfNode();
                                var.setIfNode(ifNode);
                                if (chooseParsing(scanner, ifNode, parsing)) {
                                    if (scanner.read() != '}') {
                                        break;
                                    }
                                    root.addChild(var);
                                    return true;
                                }

                            } finally {
                                scanner.popLevel();
                            }

                        } else if (c == '/') {
                            //${var:[/upcase][/downcase][/capitalize][/asciify]}
                            int initial = scanner.getMark();

                            while (true) {
                                c = scanner.read();
                                if (c == StringScanner.EOF) {
                                    scanner.setMark(mark);
                                    return false;

                                } else if (c == '}') {
                                    var.addConvertNode(
                                            new ConvertNode(
                                                    scanner.getContents().substring(initial, scanner.getMark() - 1),
                                                    scanner.getLevel()));
                                    root.addChild(var);
                                    return true;

                                } else if (c == '/') {
                                    var.addConvertNode(
                                            new ConvertNode(
                                                    scanner.getContents().substring(initial, scanner.getMark() - 1),
                                                    scanner.getLevel()));
                                    initial = scanner.getMark();

                                }
                            }

                        } else {
                            //Well, we could check for the '-', but it seems '-else' and 'else' get here??
                            //${var:-else}
                            if (c != '-') {
                                scanner.unread();
                            }
                            scanner.addLevelFinishingAt('}');
                            try {
                                IfNode elseNode = new IfNode();
                                elseNode.setNotIfNode(true);
                                var.setIfNode(elseNode);
                                if (chooseParsing(scanner, elseNode, parsing)) {
                                    if (scanner.read() != '}') {
                                        break;
                                    }
                                    root.addChild(var);
                                    return true;
                                }

                            } finally {
                                scanner.popLevel();
                            }
                        }
                    }
                    if (c == '/') {
                        //${var/regexp/format/options}
                        VariableNode var = new VariableNode(scanner.getContents(), mark + 2, scanner.getMark() - 1);
                        if (regexpFormatAndOptions(scanner, mark, var)) {
                            root.addChild(var);
                            return true;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        //If it didn't return sooner we had no match, let's get back
        //to where we started.
        scanner.setMark(mark);
        return false;
    }

    private static boolean chooseParsing(StringScanner scanner, Node var, Parsing parsing) {
        if (parsing == Parsing.PARSE_FORMAT) {
            return parseFormatString(scanner, var);

        } else {
            return parseSnippet(scanner, var);

        }
    }

    // Placeholders
    // $int
    // ${int}
    // ${int:snippet} <-- see the recursion?
    // ${int/regexp/format/options}
    // ${int|choice 1,...,choice n|} --> transform this into a template variable!
    private static boolean checkPlaceholder(StringScanner scanner, Node root) {
        final int mark = scanner.getMark();
        int c = scanner.read();
        if (c == '$') {
            //$int
            if (digitsRule.evaluate(scanner) == OK_TOKEN) {
                root.addChild(new PlaceholderNode(scanner.getContents(), mark + 1, scanner.getMark()));
                return true;
            }

            c = scanner.read();
            if (c == '{') {
                c = scanner.read();
                if (Character.isDigit(c)) {
                    do {
                        c = scanner.read();
                    } while (Character.isDigit(c));

                    PlaceholderNode placeHolder = new PlaceholderNode(scanner.getContents(), mark + 2,
                            scanner.getMark() - 1);
                    if (c == '}') {
                        //${int}
                        root.addChild(placeHolder);
                        return true;

                    } else if (c == ':') {
                        //${int:snippet} <-- see the recursion?

                        scanner.addLevelFinishingAt('}');
                        parseSnippet(scanner, placeHolder);
                        scanner.popLevel();

                        c = scanner.read();
                        if (c == '}') {
                            root.addChild(placeHolder);
                            return true;
                        } else {
                            scanner.unread();
                        }

                    } else if (c == '/') {
                        // ${int/regexp/format/options} <-- The format string is actually something which
                        // can be considered separate.
                        if (regexpFormatAndOptions(scanner, mark, placeHolder)) {
                            root.addChild(placeHolder);
                            return true;
                        }
                    } else if (c == '|') {
                        //${int|choice 1,...,choice n|} --> transform this into a template variable!
                        scanner.addLevelFinishingAt('|', ',');
                        try {
                            ChoiceNode choiceNode = new ChoiceNode();
                            while (parseFormatString(scanner, choiceNode)) {
                                c = scanner.read();
                                if (c == StringScanner.EOF) {
                                    break;
                                }
                                if (c == ',') {
                                    placeHolder.addChoiceNode(choiceNode);
                                    choiceNode = new ChoiceNode();
                                    continue;
                                }
                                if (c == '|') {
                                    placeHolder.addChoiceNode(choiceNode);
                                    c = scanner.read();
                                    if (c == '}') {
                                        root.addChild(placeHolder);
                                        return true;
                                    } else {
                                        break;
                                    }
                                }
                                break;
                            }
                        } finally {
                            scanner.popLevel();
                        }

                    } else {
                        scanner.unread();
                    }
                } else {
                    scanner.unread();
                }
            }
        }

        //If it didn't return sooner we had no match, let's get back
        //to where we started.
        scanner.setMark(mark);
        return false;
    }

    private static boolean regexpFormatAndOptions(StringScanner scanner, final int mark,
            NodeWithRegexp placeHolder) {
        int c;
        int startRegexp = scanner.getMark();
        c = scanner.read();
        while (c != '/' && c != StringScanner.EOF) {
            c = scanner.read();
        }
        if (c == StringScanner.EOF) {
            scanner.unread();
        } else {
            placeHolder
                    .setRegexp(scanner.getContents().substring(startRegexp, scanner.getMark() - 1));
        }
        // c == '/' at this point

        //Now, let's pass the format string (which can be quite tricky on itself).
        Node node = new FmtNode();

        scanner.addLevelFinishingAt('/');
        parseFormatString(scanner, node);
        scanner.popLevel();
        placeHolder.setFormatNode(node);

        // c == '/' at this point
        c = scanner.read();
        if (c != '/') {
            scanner.setMark(mark);
            return false;
        }

        //Now, check the options (the valid chars there are g, i, e, m, s)
        int startRegexpOptions = scanner.getMark();
        regexpOptionsRule.evaluate(scanner);
        placeHolder.setRegexpOptions(
                scanner.getContents().substring(startRegexpOptions, scanner.getMark()));
        c = scanner.read();

        //Only valid if we stopped at a } (otherwise this was all in vain...)
        if (c == '}') {
            return true;
        } else {
            scanner.unread();
        }
        return false;
    }

}
