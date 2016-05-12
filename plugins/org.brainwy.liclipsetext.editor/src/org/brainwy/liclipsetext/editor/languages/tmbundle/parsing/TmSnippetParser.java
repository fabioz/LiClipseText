/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

/**
 *
From the textmate manual:

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
public class TmSnippetParser {

    public static SnippetToTemplateCtx createReplacement(String pattern, SnippetToTemplateCtx ctx) {
        return TmSnippetParserV2.createReplacement(pattern, ctx);
    }

}
