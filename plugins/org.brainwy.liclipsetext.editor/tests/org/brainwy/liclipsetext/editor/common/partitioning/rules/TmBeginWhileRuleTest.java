package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseContentTypeDefinitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmBeginEndRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.TmBeginWhileRule;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.jface.text.Document;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class TmBeginWhileRuleTest extends TestCase {

    public void testTmBeginEndRule() throws Exception {
        List<ILiClipsePredicateRule> subRules = Arrays.asList((ILiClipsePredicateRule) new TmBeginEndRule(
                "c", "d", null, null, null,
                new Token("cd"), null, 0));

        TmBeginWhileRule rule = new TmBeginWhileRule("a", "b", new HashMap<>(), new HashMap<>(),
                subRules, new Token("ab"), new Token("inner"));

        ICustomPartitionTokenScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "nnaaabbb\n"
                + "nnnbyyy\n"
                + "xx";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        assertEquals(TestUtils.listToExpected("null:0:2",
                "ab:2:15",
                "null:17:2"), TestUtils.scan(scanner, range, true));

    }

    public void testTmBeginEndRule2() throws Exception {
        List<ILiClipsePredicateRule> subRules = Arrays.asList((ILiClipsePredicateRule) new TmBeginEndRule(
                "c", "d", null, null, null,
                new Token("cd"), null, 0));

        TmBeginWhileRule rule = new TmBeginWhileRule("a", "b", new HashMap<>(), new HashMap<>(),
                subRules, new Token("ab"), new Token("inner"));

        ICustomPartitionTokenScanner scanner = new LiClipseContentTypeDefinitionScanner(rule);
        String str = "nnaaabbcb\n"
                + "nnnbyyy\n"
                + "xxd";
        ScannerRange range = scanner.createScannerRange(new Document(str), 0, str.length());
        assertEquals(TestUtils.listToExpected("null:0:2",
                "ab:2:19"), TestUtils.scan(scanner, range, true));

    }

}
