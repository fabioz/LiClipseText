package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class WordScanTest extends TestCase {

    public void testScopeScanner() throws Exception {
        ScopeColorScanning scopeColoringScanning = new ScopeColorScanning(false, null);
        scopeColoringScanning.setOperators(StringUtils.split("a b", ' '));
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner(scopeColoringScanning, new LiClipseLanguage());
        assertEquals(TestUtils.listToExpected("operator:0:1",
                "null:1:5"),
                TestUtils.scan(scanner, new Document("a = 10")));
    }

    public void testScopeScanner2() throws Exception {
        ScopeColorScanning scopeColoringScanning = new ScopeColorScanning(false, null);
        scopeColoringScanning.setOperators(StringUtils.split("a ab", ' '));
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner(scopeColoringScanning, new LiClipseLanguage());
        assertEquals(TestUtils.listToExpected("null:0:3"), TestUtils.scan(scanner, new Document("abc")));
    }

}
