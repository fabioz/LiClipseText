package org.brainwy.liclipsetext.editor.common.partitioning.tm4e;

import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.lru_space_cache.LRUCacheWithSoftPrunedValues;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.StackElement;

public class Tm4ePartitioner implements IDocumentPartitioner {

    @Override
    public void connect(IDocument document) {
        System.out.println("connect");
    }

    @Override
    public void disconnect() {
        System.out.println("disconnect");
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
        System.out.println("About to be changed");
    }

    @Override
    public boolean documentChanged(DocumentEvent event) {
        System.out.println("changed");
        return false;
    }

    @Override
    public String[] getLegalContentTypes() {
        return null;
    }

    @Override
    public String getContentType(int offset) {
        return null;
    }

    @Override
    public ITypedRegion[] computePartitioning(int offset, int length) {
        return null;
    }

    @Override
    public ITypedRegion getPartition(int offset) {
        return null;
    }

    private static class CacheKey {

    }

    private LRUCacheWithSoftPrunedValues<CacheKey, ITokenizeLineResult> cache;

    public ITokenizeLineResult tokenizeLine(int lineFromOffset, String lineContents, IGrammar grammar,
            ScannerRange scannerRange) {
        System.out.println("TODO: This needs to be finished!!!");
        StackElement prevState = null;
        return grammar.tokenizeLine(lineContents, prevState);
    }

}
