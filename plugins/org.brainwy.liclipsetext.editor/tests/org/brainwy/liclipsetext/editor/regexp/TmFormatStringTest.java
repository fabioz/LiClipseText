package org.brainwy.liclipsetext.editor.regexp;

import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseContentTypeDefinitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmMatchRule;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

/**
 *
From the textmate manual:

# In Snippets

## Placeholders
    $<int>
    ${<int>}
    ${<int>:<snippet>}
    ${<int>/<regexp>/<format>/<options>}
    ${<int>|<choice 1>,...,<choice n>|}

## Code
    `<code>`

# In Format Strings
    $0-n
    \\U, \\L, \\E, \\u, \\l
    \\t, \\r, \\n, \\x{HHHH}, \\xHH
    <variables>
    (?<var>:<if>:<else>}
    (?<var>:<if>}

# In Both

## Variables
    ${<var>:?<if>:<else>}
    ${<var>:+<if>}
    ${<var>:-<else>}
    ${<var>:<else>}
    ${<var>/<regexp>/<format>/<options>}
    ${<var>:[/upcase][/downcase][/capitalize][/asciify]}

 */
public class TmFormatStringTest extends TestCase {

    public void testFormatString() throws Exception {
        TmMatchRule m = new TmMatchRule("\\s*(#{1,3})?$\\n?", new ContentTypeToken(
                "markup.heading.${1/(#)(#)?(#)?/${a:?3:${b:?2:c}}/}.markdown"));

        LiClipseContentTypeDefinitionScanner scanner = new LiClipseContentTypeDefinitionScanner(m);
        ScannerRange range = scanner.createScannerRange(new Document("##"), 0, 2);
        List<SubRuleToken> evaluateSubRules = m.evaluateSubRules(range, true).flatten();
        assertEquals(1, evaluateSubRules.size());
        SubRuleToken token = evaluateSubRules.iterator().next();
        assertEquals("markup.heading.b.markdown", token.token.getData());
    }

}
