package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.rules.FastPartitioner;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

public class LiClipseDocumentPartitionerTokensCache extends FastPartitioner {

    public static class TokensCache {

        private final TreeMap<ITypedRegion, List<SubRuleToken>> cache = new TreeMap<>(
                (ITypedRegion a, ITypedRegion b) -> {
                    return Integer.compare(a.getOffset(), b.getOffset());
                });

        public synchronized void clear() {
            this.cache.clear();
        }

        public synchronized List<SubRuleToken> getCachedTokens(IDocument doc, ITypedRegion region, long docTime) {
            Entry<ITypedRegion, List<SubRuleToken>> ceilingEntry = cache.ceilingEntry(region);
            if (ceilingEntry == null) {
                return null;
            }
            if (ceilingEntry.getKey().equals(region)) {
                return ceilingEntry.getValue();
            }
            return null;
        }

        public synchronized void setCachedTokens(IDocument doc, ITypedRegion region, long docTime,
                List<SubRuleToken> tokens) {
            int offset = region.getOffset();
            while (true) {
                Entry<ITypedRegion, List<SubRuleToken>> lastEntry = cache.lastEntry();
                if (lastEntry == null) {
                    break;
                }
                if (offset < (lastEntry.getKey().getOffset() + lastEntry.getKey().getLength())) {
                    cache.pollLastEntry();
                } else {
                    break;
                }
            }
            cache.put(region, Arrays.asList(tokens.toArray(new SubRuleToken[0])));
        }

        public synchronized void documentAboutToBeChanged(DocumentEvent event) {
            if (event.fDocument == null) {
                return;
            }
            int offset = event.getOffset();
            try {
                ITypedRegion partition = event.fDocument.getPartition(offset);
                offset = partition.getOffset();
            } catch (BadLocationException e) {
                clear();
                return;
            }

            Set<Entry<ITypedRegion, List<SubRuleToken>>> entries = this.cache.entrySet();
            Iterator<Entry<ITypedRegion, List<SubRuleToken>>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Entry<ITypedRegion, List<SubRuleToken>> next = iterator.next();
                if (offset < (next.getKey().getOffset() + next.getKey().getLength())) {
                    iterator.remove();
                    break;
                }
            }

            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }

        public int size() {
            return cache.size();
        }

        public void print() {
            Set<Entry<ITypedRegion, List<SubRuleToken>>> entrySet = this.cache.entrySet();
            for (Entry<ITypedRegion, List<SubRuleToken>> entry : entrySet) {
                System.out.println(entry.getKey());
                System.out.println(entry.getValue());
                System.out.println(" ");
            }
        }

        public ITypedRegion getLastRegionCached() {
            if (this.cache.size() == 0) {
                return null;
            }
            return this.cache.lastEntry().getKey();
        }
    }

    private final TokensCache tokensCache = new TokensCache();

    public void setCachedTokens(IDocument doc, ITypedRegion region, long docTime, List<SubRuleToken> tokens) {
        tokensCache.setCachedTokens(doc, region, docTime, tokens);
    }

    public List<SubRuleToken> getCachedTokens(IDocument doc, ITypedRegion region, long docTime) {
        return tokensCache.getCachedTokens(doc, region, docTime);
    }

    /**
     * Just for testing.
     */
    public TokensCache getTokensCache() {
        return tokensCache;
    }

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
