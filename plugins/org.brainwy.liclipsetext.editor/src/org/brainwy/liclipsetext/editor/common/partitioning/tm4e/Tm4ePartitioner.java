package org.brainwy.liclipsetext.editor.common.partitioning.tm4e;

import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
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
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
    }

    @Override
    public boolean documentChanged(DocumentEvent event) {
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

    private static class Tm4eCache {
        public StackElement prevState;
    }

    public ITokenizeLineResult tokenizeLine(int lineFromOffset, String lineContents, IGrammar grammar,
            ScannerRange scannerRange) {
        StackElement prevState = null;
        Tm4eCache tm4eCache = (Tm4eCache) scannerRange.tm4eCache;
        if (tm4eCache == null) {
            prevState = null;
            tm4eCache = (Tm4eCache) (scannerRange.tm4eCache = new Tm4eCache());
        } else {
            prevState = tm4eCache.prevState;
        }
        ITokenizeLineResult ret = grammar.tokenizeLine(lineContents, prevState);
        tm4eCache.prevState = ret.getRuleStack();
        return ret;
    }

    public void finishTm4ePartition(IGrammar grammar, ScannerRange scannerRange) {

    }

}
