package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitionerTmCache.Tm4eDocCache;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitionerTmCache.Tm4eScannerCache;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.internal.grammar.TokenizeLineResult;

import junit.framework.TestCase;

public class Tm4ePartitionerCacheTest extends TestCase {

    private Tm4eDocCache docCache;
    private IDocument doc;
    private Tm4eScannerCache cache0;
    private Tm4eScannerCache cache1;

    @Override
    protected void setUp() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("css.tmbundle",
                "css.tmbundle-master/Syntaxes/CSS.plist");

        doc = new Document();
        doc.set("line0\nline1\nline2\nline3\nline4\nline5");
        language.connect(doc);

        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) doc.getDocumentPartitioner();
        docCache = documentPartitioner.getDocCache();

        List<Tm4eScannerCache> caches = new ArrayList<Tm4eScannerCache>();
        cache0 = new Tm4eScannerCache();
        cache0.startLine = 0;
        cache0.lines = new ITokenizeLineResult[] {
                new TokenizeLineResult(null, new StackElement(null, 0, 0, "", null, null)),
                new TokenizeLineResult(null, new StackElement(null, 1, 1, "", null, null)),
                new TokenizeLineResult(null, new StackElement(null, 2, 2, "", null, null))
        };
        caches.add(cache0);

        cache1 = new Tm4eScannerCache();
        cache1.startLine = 3;
        cache1.lines = new ITokenizeLineResult[] {
                new TokenizeLineResult(null, new StackElement(null, 3, 3, "", null, null)),
                new TokenizeLineResult(null, new StackElement(null, 4, 4, "", null, null)),
        };
        caches.add(cache1);

        docCache.setCaches(caches);
    }

    public void testTm4ePartitionerCache1() throws Exception {
        // This should remove the first cache (as we changed the first line in the partition) and
        // move the second cache
        doc.replace(0, 0, "foo");

        List<Tm4eScannerCache> caches = docCache.getCaches();
        assertEquals(1, caches.size());
        Tm4eScannerCache tm4eScannerCache = caches.get(0);
        assertEquals(3, tm4eScannerCache.startLine);
    }

    public void testTm4ePartitionerCache2() throws Exception {
        // This should remove the second cache (as we changed the first line in the partition) and keep the first unchanged
        int lineOffset = doc.getLineOffset(3);
        doc.replace(lineOffset, 0, "foo");

        List<Tm4eScannerCache> caches = docCache.getCaches();
        assertEquals(1, caches.size());
        Tm4eScannerCache tm4eScannerCache = caches.get(0);
        assertEquals(0, tm4eScannerCache.startLine);
    }

    public void testTm4ePartitionerCache3() throws Exception {
        // This should invalidate lines 1, 2 (and keep 0) from cache 0
        int lineOffset = doc.getLineOffset(1);
        doc.replace(lineOffset, 0, "foo");

        List<Tm4eScannerCache> caches = docCache.getCaches();
        assertEquals(2, caches.size());
        Tm4eScannerCache tm4eScannerCache = caches.get(0);
        assertEquals(0, tm4eScannerCache.startLine);
        assertNotNull(tm4eScannerCache.lines[0]);
        assertEquals(1, tm4eScannerCache.lines.length);
    }

    public void testTm4ePartitionerCache4() throws Exception {
        // This should clear all caches
        doc.set("");

        List<Tm4eScannerCache> caches = docCache.getCaches();
        assertEquals(0, caches.size());
    }

    public void testTm4ePartitionerCache5() throws Exception {
        // This should invalidate lines 1, 2 (and keep 0) from cache 0 and add a new line to it.
        int lineOffset = doc.getLineOffset(1);
        doc.replace(lineOffset, 0, "\n");

        List<Tm4eScannerCache> caches = docCache.getCaches();
        assertEquals(2, caches.size());
        Tm4eScannerCache tm4eScannerCache = caches.get(0);
        assertEquals(0, tm4eScannerCache.startLine);
        assertEquals(1, tm4eScannerCache.lines.length);
        assertNotNull(tm4eScannerCache.lines[0]);
    }

    public void testTm4ePartitionerCache6() throws Exception {
        // This should invalidate lines 1, 2 (and keep 0) from cache 0 and remove a new line from it.
        int lineOffset = doc.getLineOffset(2);
        doc.replace(lineOffset - 1, 1, ""); // Remove a new line

        List<Tm4eScannerCache> caches = docCache.getCaches();
        assertEquals(2, caches.size());
        Tm4eScannerCache tm4eScannerCache = caches.get(0);
        assertEquals(0, tm4eScannerCache.startLine);
        assertEquals(1, tm4eScannerCache.lines.length);
        assertNotNull(tm4eScannerCache.lines[0]);
    }

}
