/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.io.File;

import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.dialogs.DialogHelpers;

public class CreateNewLanguageHelper {
    private static final String LANGUAGE_TEMPLATE = ""
            + "# Below is a sample structure for your new language. It specifies a language which has\n"
            + "# comments starting with '#' single line strings with ' and \" and multi-line strings with ''' and \"\"\".\n"
            + "#\n"
            + "# Later, we go on to say that whenever we find a\n"
            + "\n"
            + "scope_to_color_name: {\n"
            + " #All types of strings map to the string color\n"
            + " doubleQuotedMultiLineString: string,\n"
            + " singleQuotedMultiLineString: string,\n"
            + " doubleQuotedString: string,\n"
            + " singleQuotedString: string,\n"
            + "}\n"
            + "\n"
            + "scope_definition_rules:\n"
            + "  #Comments\n"
            + "  #singleLineComment is also the name of the color (no translation required in scope_to_color_name)\n"
            + "  - {type: EndOfLineRule, scope: singleLineComment, start: '#'} \n"
            + "\n"
            + "  #Multi Line Strings\n"
            + "  - {type: MultiLineRule, scope: singleQuotedMultiLineString, start: \"'''\", end: \"'''\", escapeCharacter: \\}\n"
            + "  - {type: MultiLineRule, scope: doubleQuotedMultiLineString, start: '\"\"\"', end: '\"\"\"', escapeCharacter: \\}\n"
            + "\n"
            + "  # Single Line Strings\n"
            + "  - {type: SingleLineRule, scope: doubleQuotedString, sequence: '\"', escapeCharacter: \\, escapeContinuesLine: true}\n"
            + "  - {type: SingleLineRule, scope: singleQuotedString, sequence: \"'\", escapeCharacter: \\, escapeContinuesLine: true}\n"
            + "\n"
            + "scope:\n"
            + "  singleLineComment:\n"
            + "    javadocTag: [TODO] #Any TODO inside a comment should have a different color.\n"
            + "  \n"
            + "  default:\n"
            + "    sub_rules: [\n"
            + "      {type: CompositeRule, sub_rules: [\n"
            + "        { type: SequenceRule, scope: keyword, sequence: 'class'}, #Define that 'class' is a keyword\n"
            + "        { type: OneOrMoreSpacesRule, scope: default},\n"
            + "        { type: AnyWordRule, scope: class }] #And any name after 'class' is the class we matched (we'll put it in the outline defining [default, class]).\n"
            + "      },\n"
            + "    ]\n"
            + "\n"
            + "    keyword: [class, pass] #Define that we want to consider 'class' and 'pass' as a keyword\n"
            + "\n"
            + "    bracket: ['(', ')', '[', ']', '{', '}']\n"
            + "\n"
            + "    operator: [<, '>', '=', +, '-', /, '*', '!', '&', '|', '%', '~', ^, ',']\n"
            + "\n"
            + "\n"
            + "file_extensions: [set_file_extension] #TODO: Fill with the file extension to match\n"
            + "filename: []\n"
            + "name: ${LANGUAGE_NAME}\n"
            + "\n"
            + "outline: #Note that we just specify 'flat' items here, the indent is later used to specify parent/children scopes.\n"
            + "  - {type: Scope, scope: [default, class], define: class} #Wherever we have a class inside the default scope we'll show a class icon in the outline.\n"
            + "\n"
            + "indent: {\n"
            + "  type: spaces, #Our example language uses spaces for indenting\n"
            + "  outline_scopes: [class], #We have to say which outline entries actually create a new scope (so, indent and outline work toghether to specify the tree).\n"
            + "}\n"
            + "\n"
            + "# Specify that the default comment action (Ctrl+/) deals with creating '#' at the start of the line\n"
            + "comment: {type: singleLine, sequence: '#'}\n"
            + "\n"
            + "";

    public static void createNewLanguage(File dir) {
        String languageName = DialogHelpers.openInputRequest("Language name",
                "Please provide the name of the language to be created.");
        if (languageName != null && languageName.length() > 0) {
            String str = LANGUAGE_TEMPLATE.replace("${LANGUAGE_NAME}", languageName);
            File f = new File(dir, languageName.toLowerCase() + ".liclipse");
            FileUtils.writeStrToFile(str, f);
            EditorUtils.openFile(f);
        }
    }

}
