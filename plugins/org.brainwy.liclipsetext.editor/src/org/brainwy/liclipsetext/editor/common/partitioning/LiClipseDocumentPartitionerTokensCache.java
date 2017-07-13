package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.rules.FastPartitioner;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

public class LiClipseDocumentPartitionerTokensCache extends FastPartitioner {

    public static class TokensCache {

        public void clear() {
            // TODO Auto-generated method stub

        }

        public void documentAboutToBeChanged(DocumentEvent event) {
            // TODO Auto-generated method stub

        }

    }

    public final TokensCache tokensCache = new TokensCache();

    public LiClipseDocumentPartitionerTokensCache(ICustomPartitionTokenScanner scanner, String[] legalContentTypes) {
        super(scanner, legalContentTypes);
    }

    @Override
    public void connect(IDocument document, boolean delayInitialization) {
        tokensCache.clear();
        super.connect(document, delayInitialization);
    }

    @Override
    public void disconnect() {
        super.disconnect();
        tokensCache.clear();
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
        super.documentAboutToBeChanged(event);
        tokensCache.documentAboutToBeChanged(event);
    }

}
