package org.brainwy.liclipsetext.editor.common.partitioning.tm4e;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.StackElement;

public class Tm4ePartitioner implements IDocumentPartitioner {

    private static final String TM4E_LICLIPSE_PARTITIONING = "TM4E_LICLIPSE_PARTITIONING";
    private static final Object addPartitionerLock = new Object();

    public static synchronized Tm4ePartitioner getTm4eDocumentPartitioner(IDocument doc) {
        IDocumentExtension3 docExt3 = (IDocumentExtension3) doc;
        Tm4ePartitioner documentPartitioner = (Tm4ePartitioner) docExt3
                .getDocumentPartitioner(TM4E_LICLIPSE_PARTITIONING);
        if (documentPartitioner == null) {
            synchronized (addPartitionerLock) {
                documentPartitioner = (Tm4ePartitioner) docExt3.getDocumentPartitioner(TM4E_LICLIPSE_PARTITIONING);
                if (documentPartitioner == null) {
                    documentPartitioner = new Tm4ePartitioner();
                    try {
                        documentPartitioner.connect(doc);
                    } catch (Exception e) {
                        Log.log("Error connecting partitioner", e);
                    }
                    docExt3.setDocumentPartitioner(TM4E_LICLIPSE_PARTITIONING, documentPartitioner);
                }
                return documentPartitioner;
            }
        } else {
            return documentPartitioner;
        }
    }

    private IDocument fDocument;

    @Override
    public void connect(IDocument document) {
        this.fDocument = document;
        docCache.clear();
    }

    @Override
    public void disconnect() {
        this.fDocument = null;
        docCache.clear();
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
        docCache.documentAboutToBeChanged(event);
    }

    @Override
    public boolean documentChanged(DocumentEvent event) {
        return false; // always returns false as we have no real partitioning (just used for updating internal caches).
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

    /**
     * Only public for testing (private API).
     */
    public final static class Tm4eDocCache {
        // This is meant to be a cache for any line/begin stack -> tokens, but currently the way that the grammar is done, this isn't really
        // possible as the StackElement is mutable (and has all the parent stacks referenced to get to the current context).
        // So, for now not implementing that (although it may be possible to do with changes in the grammar, it needs more time to implement).
        // This also means we always have to scan to the end of the document, even when this shouldn't be needed if we stopped at a state
        // which could be considered the same.
        //        private LRUCacheWithSoftPrunedValues<Tm4eCacheKey, Tm4eLineInfo> prevStateAndLineContentsToInfo = new LRUCacheWithSoftPrunedValues<>(
        //                10000);

        // We should have 1 entry for each partition (and must damage it accordingly).
        private final List<Tm4eScannerCache> caches = new ArrayList<>();

        public synchronized void updateFrom(IGrammar grammar, ScannerRange scannerRange) {
            //            int minCacheSize = scannerRange.getNumberOfLines();
            //            minCacheSize *= 1.5; // Min cache size should be enough to hold the document opened + 50% or 10k lines.
            //            if (minCacheSize < 10000) {
            //                minCacheSize = 10000;
            //            }
            //            if (prevStateAndLineContentsToInfo.getSpaceLimit() < minCacheSize) {
            //                prevStateAndLineContentsToInfo.setSpaceLimit(minCacheSize);
            //            }
            //
            //            Tm4eScannerCache tm4eCache = (Tm4eScannerCache) scannerRange.tm4eCache;
            //            if (tm4eCache != null) {
            //                Set<Entry<Tm4eCacheKey, Tm4eLineInfo>> entrySet = tm4eCache.prevStateAndLineContentsToInfo.entrySet();
            //                for (Entry<Tm4eCacheKey, Tm4eLineInfo> entry : entrySet) {
            //                    prevStateAndLineContentsToInfo.put(entry.getKey(), entry.getValue());
            //                }
            //            }
            Tm4eScannerCache tm4eCache = (Tm4eScannerCache) scannerRange.tm4eCache;
            tm4eCache.prevState = null; // This isn't valid anymore (it's only useful during the current parsing).
            this.caches.add(tm4eCache);
        }

        public synchronized void documentAboutToBeChanged(DocumentEvent event) {
            int replacedTextLen = event.getLength();
            int offset = event.getOffset();

            String text = event.getText();
            int linesAdded = StringUtils.countLineBreaks(text);
            int linesRemoved = 0;
            IDocument document = event.getDocument();
            try {
                if (replacedTextLen > 0) {
                    String string = document.get(offset, event.getLength());
                    linesRemoved = StringUtils.countLineBreaks(string);
                }
                int lineFromOffset = document.getLineOfOffset(offset);
                int linesDiff = linesAdded - linesRemoved;
                Position linesRemovedPosition = new Position(lineFromOffset, linesRemoved);

                for (Iterator<Tm4eScannerCache> it = caches.iterator(); it.hasNext();) {
                    Tm4eScannerCache tm4eCache = it.next();
                    if (tm4eCache.startLine == lineFromOffset || linesRemovedPosition.includes(tm4eCache.startLine)) {
                        it.remove();
                        continue;
                    }
                    if (tm4eCache.startLine < lineFromOffset && tm4eCache.endLine > lineFromOffset) {
                        // Invalidate this cache from the given line downwards.
                        StackElement[] lines = new StackElement[tm4eCache.lines.length + linesDiff];
                        System.arraycopy(tm4eCache.lines, 0, lines, 0, lineFromOffset - tm4eCache.startLine);
                        tm4eCache.lines = lines;
                        tm4eCache.endLine += linesDiff;
                        continue;
                    }
                    if (tm4eCache.startLine > lineFromOffset) {
                        tm4eCache.startLine += linesDiff;
                        tm4eCache.endLine += linesDiff;
                    }
                }
            } catch (BadLocationException e) {
                // This is bad, our assumptions are no longer correct. Clear all caches.
                Log.log("This should NEVER happen (clearing all caches)!", e);
                this.caches.clear();
                return;
            }
        }

        /**
         * Just for testing!
         */
        public synchronized void setCaches(List<Tm4eScannerCache> caches) {
            this.caches.clear();
            this.caches.addAll(caches);
        }

        public synchronized List<Tm4eScannerCache> getCaches() {
            List<Tm4eScannerCache> ret = new ArrayList<>(caches.size());
            ret.addAll(caches);
            return ret;
        }

        public synchronized void clear() {
            caches.clear();
        }

        public synchronized Tm4eScannerCache getCached(int currOffset, int lineFromOffset, String lineContents,
                ScannerRange scannerRange,
                IGrammar grammar) throws DocumentTimeStampChangedException {
            if (caches.size() == 0) {
                return null;
            }
            for (Tm4eScannerCache tm4eCache : caches) {
                if (tm4eCache.startLine <= lineFromOffset && tm4eCache.endLine > lineFromOffset) {
                    return tm4eCache;
                }
            }
            return null;
        }
    }

    private final Tm4eDocCache docCache = new Tm4eDocCache();

    /**
     * Just for testing!
     */
    public Tm4eDocCache getDocCache() {
        return docCache;
    }

    //    private static class Tm4eCacheKey {
    //
    //        private final String lineContents;
    //        private final StackElement ruleStack;
    //
    //        public Tm4eCacheKey(String lineContents, StackElement ruleStack) {
    //            this.lineContents = lineContents;
    //            this.ruleStack = ruleStack;
    //        }
    //
    //    }

    /**
     * A cache that lives inside the scanner (should always go forward and when finished scanning it updates the Tm4eDocCache).
     *
     * Only public for testing (private API).
     */
    public final static class Tm4eScannerCache {
        public StackElement prevState;
        //        private Map<Tm4eCacheKey, Tm4eLineInfo> prevStateAndLineContentsToInfo = new HashMap<>();
        public StackElement[] lines;
        public int startLine;
        public int endLine;
        public String startLineContents;

        public Tm4eScannerCache copy() {
            Tm4eScannerCache ret = new Tm4eScannerCache();
            ret.lines = lines;
            ret.startLine = startLine;
            ret.endLine = endLine;
            ret.startLineContents = startLineContents;
            return ret;
        }
    }

    public ITokenizeLineResult tokenizeLine(int currOffset, int lineFromOffset, String lineContents, IGrammar grammar,
            ScannerRange scannerRange) throws DocumentTimeStampChangedException {
        StackElement prevState = null;
        Tm4eScannerCache tm4eCache = (Tm4eScannerCache) scannerRange.tm4eCache;
        boolean firstCallInRange = tm4eCache == null;
        boolean foundInDocCache = false;
        if (firstCallInRange) {
            // Let's see if we're restarting...
            tm4eCache = docCache.getCached(currOffset, lineFromOffset, lineContents, scannerRange, grammar);
            if (tm4eCache != null) {
                int diff = lineFromOffset - tm4eCache.startLine - 1;
                if (diff >= 0 && diff < tm4eCache.lines.length) {
                    prevState = tm4eCache.lines[diff];
                    scannerRange.tm4eCache = tm4eCache;
                    foundInDocCache = true;
                } else {
                    tm4eCache = null;
                }
            }

            if (tm4eCache == null) {

                tm4eCache = (Tm4eScannerCache) (scannerRange.tm4eCache = new Tm4eScannerCache());

                try {
                    int startLine = scannerRange.getLineFromOffset(scannerRange.getRangeStartOffset());
                    int endLine = scannerRange.getLineFromOffset(scannerRange.getRangeEndOffset());
                    if (endLine < startLine) {
                        endLine = startLine;
                    }
                    int nLines = endLine - startLine + 1; // +1 because startLine may be 0.
                    tm4eCache.lines = new StackElement[nLines];
                    tm4eCache.startLine = startLine;
                    tm4eCache.startLineContents = lineContents;
                    tm4eCache.endLine = endLine;
                } catch (BadLocationException e) {
                    scannerRange.checkDocumentTimeStampChanged();
                    Log.log("This shouldn't happen!", e);
                }
                prevState = null;
            }

        } else {
            prevState = tm4eCache.prevState;
        }
        ITokenizeLineResult ret = grammar.tokenizeLine(lineContents, prevState);

        try {
            tm4eCache.lines[lineFromOffset - tm4eCache.startLine] = ret.getRuleStack();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(
                    StringUtils.format(
                            "Error trying to access index: %s. Line: %s. Cache start line: %s. Line Contents: %s. First call in range: %s. Found in doc cache: %s",
                            lineFromOffset - tm4eCache.startLine, lineFromOffset, tm4eCache.startLine, lineContents,
                            firstCallInRange,
                            foundInDocCache));
        }
        //        tm4eCache.prevStateAndLineContentsToInfo.put(new Tm4eCacheKey(lineContents, ret.getRuleStack()),
        //                new Tm4eLineInfo(ret));
        tm4eCache.prevState = ret.getRuleStack();
        return ret;
    }

    public void finishTm4ePartition(IGrammar grammar, ScannerRange scannerRange) {
        // At this point we have to persist the parsing info to the document so that we can restart the
        // partitioning later on.
        docCache.updateFrom(grammar, scannerRange);
    }

}
