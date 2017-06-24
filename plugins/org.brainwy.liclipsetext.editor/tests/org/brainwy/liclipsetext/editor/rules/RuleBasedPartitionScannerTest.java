package org.brainwy.liclipsetext.editor.rules;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.SingleLineRule;
import org.brainwy.liclipsetext.shared_core.partitioner.CustomRuleBasedPartitionScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.testutils.TestUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class RuleBasedPartitionScannerTest extends TestCase {

    public void testRuleBasedPartitioning() throws Exception {

        String str = "var foo = 'test str'\nfoo";
        Document doc = new Document(str);
        CustomRuleBasedPartitionScanner partitionScanner = new CustomRuleBasedPartitionScanner();
        partitionScanner
                .setRules(new ILiClipsePredicateRule[] { new SingleLineRule("'", new Token("string"), '\0', false) });

        partitionScanner.setRange(doc, 0, doc.getLength());

        assertEquals(TestUtils.listToExpected("null:0:1",
                "null:1:1",
                "null:2:1",
                "null:3:1",
                "null:4:1",
                "null:5:1",
                "null:6:1",
                "null:7:1",
                "null:8:1",
                "null:9:1",
                "string:10:10",
                "null:20:1",
                "null:21:1",
                "null:22:1",
                "null:23:1"),
                TestUtils.scan(partitionScanner, doc));
    }

    public void testNoRuleRuleBasedPartitioning() throws Exception {

        String str = "var foo = 'test str'\nfoo";
        Document doc = new Document(str);
        CustomRuleBasedPartitionScanner partitionScanner = new CustomRuleBasedPartitionScanner();

        partitionScanner.setRange(doc, 0, doc.getLength());
        int tokensFound = 0;
        IToken token;
        do {
            token = partitionScanner.nextToken();
            tokensFound++;
        } while (!token.isEOF());
        assertEquals(2, tokensFound); //default and EOF
    }

}
