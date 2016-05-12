package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.editor.common.partitioning.DummyColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.JSRegexRule;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.LiClipseRuleBasedPartitionScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.core.internal.filebuffers.SynchronizableDocument;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class JSRegexRuleTest extends TestCase {

    public void testJSRegexRuleSimple() throws Exception {
        JSRegexRule rule = new JSRegexRule(new Token("ok"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();
        String str = "/som/i";
        IDocument document = configureDocument(str, "javascript.liclipse");
        ScannerRange range = scanner.createScannerRange(document, 0, str.length());
        assertEquals(0, range.getMark());

        assertFalse(rule.evaluate(range).isUndefined());
        assertEquals(str.length(), range.getMark());
    }

    public void testJSRegexRuleSimpleComments() throws Exception {
        JSRegexRule rule = new JSRegexRule(new Token("ok"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();
        String str = "/s\\//i"; // Note that \\ is a single slash actually...
        IDocument document = configureDocument(str, "javascript.liclipse");
        ScannerRange range = scanner.createScannerRange(document, 0, str.length());
        assertEquals(0, range.getMark());

        assertFalse(rule.evaluate(range).isUndefined());
        assertEquals(str.length(), range.getMark());
    }

    public void testJSRegexRuleSimpleComments2() throws Exception {
        JSRegexRule rule = new JSRegexRule(new Token("ok"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();
        String str = "//";
        IDocument document = configureDocument(str, "javascript.liclipse");
        ScannerRange range = scanner.createScannerRange(document, 0, str.length());
        assertEquals(0, range.getMark());

        assertTrue(rule.evaluate(range).isUndefined());
        assertEquals(0, range.getMark());
    }

    public void testJSRegexRuleSimpleComments3() throws Exception {
        JSRegexRule rule = new JSRegexRule(new Token("ok"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();
        String str = "//i";
        IDocument document = configureDocument(str, "javascript.liclipse");
        ScannerRange range = scanner.createScannerRange(document, 0, str.length());
        assertEquals(0, range.getMark());

        assertTrue(rule.evaluate(range).isUndefined());
        assertEquals(0, range.getMark());
    }

    public void testJSRegexRuleNoMatch() throws Exception {
        JSRegexRule rule = new JSRegexRule(new Token("ok"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();
        String str = "1  /som";
        IDocument document = configureDocument(str, "javascript.liclipse");
        ScannerRange range = scanner.createScannerRange(document, 0, str.length());
        assertEquals(0, range.getMark());
        range.setMark(1);

        assertTrue(rule.evaluate(range).isUndefined());
    }

    public void testJSRegexRuleNoMatch2() throws Exception {
        JSRegexRule rule = new JSRegexRule(new Token("ok"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();
        String str = "i++/som";
        IDocument document = configureDocument(str, "javascript.liclipse");
        ScannerRange range = scanner.createScannerRange(document, 0, str.length());
        assertEquals(0, range.getMark());
        range.setMark(1);

        assertTrue(rule.evaluate(range).isUndefined());
    }

    public void testJSRegexRuleMatch() throws Exception {
        JSRegexRule rule = new JSRegexRule(new Token("ok"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();
        String str = "+/ueo/";
        IDocument document = configureDocument(str, "javascript.liclipse");
        ScannerRange range = scanner.createScannerRange(document, 0, str.length());
        assertEquals(0, range.getMark());
        range.setMark(1);

        assertFalse(rule.evaluate(range).isUndefined());
    }

    public void testJSRegexRuleMatch2() throws Exception {
        JSRegexRule rule = new JSRegexRule(new Token("ok"));
        LiClipseRuleBasedPartitionScanner scanner = new LiClipseRuleBasedPartitionScanner();
        String str = "+  /ueo/";
        IDocument document = configureDocument(str, "javascript.liclipse");
        ScannerRange range = scanner.createScannerRange(document, 0, str.length());
        assertEquals(0, range.getMark());
        range.setMark(3);

        assertFalse(rule.evaluate(range).isUndefined());
    }

    private IDocument configureDocument(String txt, String language) {
        @SuppressWarnings("restriction")
        final IDocument document = new SynchronizableDocument();
        document.set(txt);

        LiClipseLanguage partitioningSetup;
        try {
            partitioningSetup = TestUtils.loadLanguageFile(language);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        IColorCache colorManager = new DummyColorCache();
        partitioner.createTokenScanners(colorManager);
        return document;
    }

}
