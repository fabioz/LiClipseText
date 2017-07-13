package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.brainwy.liclipsetext.editor.languages.LanguageMetadata.LanguageType;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.SubLanguageToken;
import org.brainwy.liclipsetext.editor.rules.SwitchLanguageToken;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.StackElement;

public class LiClipseDocumentPartitionerTmCache extends LiClipseDocumentPartitionerTokensCache {

    public final LiClipseLanguage language;

    public LiClipseDocumentPartitionerTmCache(ICustomPartitionTokenScanner scanner, String[] legalContentTypes,
            LiClipseLanguage language) {
        super(scanner, legalContentTypes);
        this.language = language;
    }

    @Override
    public void connect(IDocument document, boolean delayInitialization) {
        docCache.clear();
        super.connect(document, delayInitialization);
    }

    @Override
    public void disconnect() {
        super.disconnect();
        docCache.clear();
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
        super.documentAboutToBeChanged(event);
        docCache.documentAboutToBeChanged(event);
    }

    /**
     * During documentChanged, we reparse needed regions and then clear our caches based on that (if this is not a tm language,
     * we should only have a cache if we have a SwitchLanguageToken related to it.
     */
    @Override
    protected void onParsingFoundSwitchLanguageTokensAndPartitioningChanged(DocumentEvent e,
            List<SwitchLanguageToken> switchLanguageTokens) {
        if (language.languageType == LanguageType.TEXT_MATE) {
            return;
        }
        docCache.onlyKeepPartitionsFromLanguageSwitchRules(e, switchLanguageTokens);
    }

    /**
     * Only public for testing (private API).
     */
    public final static class Tm4eDocCache {
        // We should have 1 entry for each partition (and must damage it accordingly).
        private final List<Tm4eScannerCache> caches = new ArrayList<>();

        public synchronized void updateFrom(IGrammar grammar, ScannerRange scannerRange)
                throws DocumentTimeStampChangedException {
            scannerRange.checkDocumentTimeStampChanged();
            Tm4eScannerCache tm4eCache = (Tm4eScannerCache) scannerRange.tm4eCache;
            if (tm4eCache == null) {
                return; // Empty line may get here.
            }
            tm4eCache.prevState = null; // This isn't valid anymore (it's only useful during the current parsing).
            for (Iterator<Tm4eScannerCache> it = caches.iterator(); it.hasNext();) {
                Tm4eScannerCache currTm4eCache = it.next();
                if (currTm4eCache.startLine == tm4eCache.startLine) {
                    it.remove();
                }
            }
            this.caches.add(tm4eCache);
        }

        private synchronized void onlyKeepPartitionsFromLanguageSwitchRules(DocumentEvent e,
                List<SwitchLanguageToken> switchLanguageTokens) {

            IDocument document = e.getDocument();
            List<TypedPosition> lineRegions = new ArrayList<>();
            for (SwitchLanguageToken switchLanguageToken : switchLanguageTokens) {
                SubLanguageToken[] subTokens = switchLanguageToken.subTokens;
                if (subTokens.length > 0) {
                    int start = subTokens[0].offset;
                    int end = subTokens[subTokens.length - 1].offset + subTokens[subTokens.length - 1].len;

                    try {
                        int startLine = document.getLineOfOffset(start);
                        int endLine = document.getLineOfOffset(end);
                        lineRegions.add(new TypedPosition(startLine, endLine, (String) switchLanguageToken.getData()));
                    } catch (BadLocationException e1) {
                        // This shouldn't happen (we should be consistent at this point).
                        Log.log(e1);
                    }
                }
            }

            for (Iterator<Tm4eScannerCache> it = caches.iterator(); it.hasNext();) {
                Tm4eScannerCache tm4eCache = it.next();
                boolean found = false;
                for (TypedPosition p : lineRegions) {
                    if (p.includes(tm4eCache.startLine)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    it.remove();
                }
            }
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
                    if (tm4eCache.startLine < lineFromOffset && tm4eCache.lines.length >= lineFromOffset) {
                        // Invalidate this cache from the given line downwards.
                        StackElement[] lines = new StackElement[lineFromOffset - tm4eCache.startLine];
                        // Note: we keep only the valid ones (i.e.: don't keep any which have nulls).
                        System.arraycopy(tm4eCache.lines, 0, lines, 0, lineFromOffset - tm4eCache.startLine);
                        tm4eCache.lines = lines;
                        continue;
                    }
                    if (tm4eCache.startLine > lineFromOffset) {
                        tm4eCache.startLine += linesDiff;
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

        public synchronized Tm4eScannerCache getCachedCopy(int partitionStartLine, String lineContents,
                ScannerRange scannerRange, IGrammar grammar) throws DocumentTimeStampChangedException {
            if (caches.size() == 0) {
                return null;
            }
            for (Tm4eScannerCache tm4eCache : caches) {
                if (tm4eCache.startLine == partitionStartLine) {
                    return tm4eCache.copy();
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

    /**
     * A cache that lives inside the scanner (should always go forward and when finished scanning it updates the Tm4eDocCache).
     *
     * Only public for testing (private API).
     */
    public final static class Tm4eScannerCache {
        public StackElement prevState;
        public StackElement[] lines;
        public int startLine;
        public String startLineContents;
        public ITypedRegion partition;

        public Tm4eScannerCache copy() {
            Tm4eScannerCache ret = new Tm4eScannerCache();
            ret.lines = lines;
            ret.startLine = startLine;
            ret.startLineContents = startLineContents;
            ret.partition = partition;
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
            try {
                ITypedRegion partition = fDocument.getPartition(currOffset);
                int partitionStartLine = scannerRange.getLineFromOffset(partition.getOffset());
                int partitionEndLine = scannerRange.getLineFromOffset(partition.getOffset() + partition.getLength());
                if (partitionEndLine < partitionStartLine) {
                    partitionEndLine = partitionStartLine;
                }
                int nLines = partitionEndLine - partitionStartLine + 1; // +1 because startLine may be 0.

                tm4eCache = docCache.getCachedCopy(partitionStartLine, lineContents, scannerRange, grammar);
                if (tm4eCache != null) {
                    if (tm4eCache.startLine != partitionStartLine) {
                        tm4eCache = null;
                    } else {
                        int diff = lineFromOffset - tm4eCache.startLine - 1;
                        while (diff >= tm4eCache.lines.length) {
                            // Empty lines may not have an entry there when invalidated.
                            diff--;
                        }
                        if (diff < 0) {
                            prevState = null;
                        } else {
                            try {
                                StackElement result = tm4eCache.lines[diff];
                                while (result == null && diff > 0) {
                                    // If we parse line-by-line and we have an empty line, the cache
                                    // may be empty, so, return from the previous line.
                                    diff--;
                                    result = tm4eCache.lines[diff];
                                }
                                if (result == null) {
                                    prevState = null;
                                } else {
                                    prevState = result;
                                }
                            } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
                                throw new ArrayIndexOutOfBoundsException(
                                        "Error at index " + e + ": " + diff +
                                                " max index: " + tm4eCache.lines.length +
                                                " partitionStartLine: " + partitionStartLine +
                                                " partitionEndLine: " + partitionEndLine +
                                                " cacheFinalResult: " + scannerRange.getCacheFinalResult() +
                                                " \nThread: " + Thread.currentThread().getName());
                            }
                        }
                        scannerRange.tm4eCache = tm4eCache;
                        foundInDocCache = true;
                        StackElement[] newLines = new StackElement[nLines];
                        System.arraycopy(tm4eCache.lines, 0, newLines, 0, tm4eCache.lines.length);
                        tm4eCache.lines = newLines;
                    }
                }

                if (tm4eCache == null) {
                    tm4eCache = (Tm4eScannerCache) (scannerRange.tm4eCache = new Tm4eScannerCache());
                    tm4eCache.lines = new StackElement[nLines];
                    tm4eCache.startLine = partitionStartLine;
                    tm4eCache.startLineContents = lineContents;
                    prevState = null;
                }

                tm4eCache.partition = partition;
            } catch (BadLocationException e) {
                scannerRange.checkDocumentTimeStampChanged();
                Log.log("This shouldn't happen!", e);
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
        tm4eCache.prevState = ret.getRuleStack();
        return ret;
    }

    public void finishTm4ePartition(IGrammar grammar, ScannerRange scannerRange)
            throws DocumentTimeStampChangedException {
        if (scannerRange.getCacheFinalResult()) {
            // At this point we have to persist the parsing info to the document so that we can restart the
            // partitioning later on.
            docCache.updateFrom(grammar, scannerRange);
        }
    }
}
