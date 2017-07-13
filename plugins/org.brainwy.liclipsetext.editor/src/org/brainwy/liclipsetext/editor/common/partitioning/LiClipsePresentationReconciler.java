/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.utils.RunInUiThread;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.DocumentPartitioningChangedEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitionerExtension2;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IDocumentPartitioningListenerExtension;
import org.eclipse.jface.text.IDocumentPartitioningListenerExtension2;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationReconcilerExtension;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;

/**
 * A copy of PresentationReconciler but with changes to do its work asynchronously.
 */
public class LiClipsePresentationReconciler implements IPresentationReconciler, IPresentationReconcilerExtension {

    /** Prefix of the name of the position category for tracking damage regions. */
    protected final static String TRACKED_PARTITION = "__reconciler_tracked_partition"; //$NON-NLS-1$

    private static final boolean UPDATE_ASYNC = true;

    private static final int SCHEDULE_PARSE_TIME = 20;

    private IDocument fDocument;

    private boolean fNewDocBeingSet;

    private static class TextPresentationStoreRanges extends TextPresentation {

        private List<StyleRange> ranges = new ArrayList<>(1000);

        private int startOffset = -1;
        private int endOffset = -1;

        @Override
        public void addStyleRange(StyleRange range) {
            ranges.add(range);
        }

        @Override
        public void replaceStyleRange(StyleRange range) {
            throw new RuntimeException("should not be used");
        }

        @Override
        public void replaceStyleRanges(StyleRange[] ranges) {
            throw new RuntimeException("should not be used");
        }

        @Override
        public void mergeStyleRange(StyleRange range) {
            throw new RuntimeException("should not be used");
        }

        @Override
        public void mergeStyleRanges(StyleRange[] ranges) {
            throw new RuntimeException("should not be used");
        }

        public void mergeRegion(IRegion lineInformationOfOffset) {
            if (startOffset == -1) {
                startOffset = lineInformationOfOffset.getOffset();
            }
            endOffset = lineInformationOfOffset.getOffset() + lineInformationOfOffset.getLength();
        }

        public TextPresentation toFinalTextPresentation() {
            TextPresentation ret = new TextPresentation(new Region(startOffset, endOffset),
                    ranges.size() > 0 ? ranges.size() : 1);
            for (StyleRange range : ranges) {
                ret.addStyleRange(range);
            }
            return ret;
        }
    }

    /**
     * Internal listener class.
     */
    class InternalListener implements
            ITextInputListener, IDocumentListener, ITextListener,
            IDocumentPartitioningListener, IDocumentPartitioningListenerExtension,
            IDocumentPartitioningListenerExtension2 {

        /** Set to <code>true</code> if between a document about to be changed and a changed event. */
        private boolean fDocumentChanging = false;
        /**
         * The cached redraw state of the text viewer.
         * @since 3.0
         */
        private boolean fCachedRedrawState = true;

        @Override
        public void inputDocumentAboutToBeChanged(IDocument oldDocument, IDocument newDocument) {
            if (oldDocument != null) {
                try {

                    fViewer.removeTextListener(this);
                    oldDocument.removeDocumentListener(this);
                    oldDocument.removeDocumentPartitioningListener(this);

                    oldDocument.removePositionUpdater(fPositionUpdater);
                    oldDocument.removePositionCategory(fPositionCategory);

                } catch (BadPositionCategoryException x) {
                    // should not happened for former input documents;
                }
            }
        }

        /*
         * @see ITextInputListener#inputDocumenChanged(IDocument, IDocument)
         */
        @Override
        public void inputDocumentChanged(IDocument oldDocument, IDocument newDocument) {

            fDocumentChanging = false;
            fNewDocBeingSet = true;
            fCachedRedrawState = true;

            try {
                fDocument = newDocument;
                if (newDocument != null) {

                    newDocument.addPositionCategory(fPositionCategory);
                    newDocument.addPositionUpdater(fPositionUpdater);

                    newDocument.addDocumentPartitioningListener(this);
                    newDocument.addDocumentListener(this);
                    fViewer.addTextListener(this);

                    setDocumentToDamagers(newDocument);
                    setDocumentToRepairers(newDocument);
                    processDamage(new Region(0, newDocument.getLength()), newDocument);
                }
            } finally {
                fNewDocBeingSet = false;
            }
        }

        @Override
        public void documentPartitioningChanged(IDocument document) {
            if (!fDocumentChanging && fCachedRedrawState) {
                processDamage(new Region(0, document.getLength()), document);
            } else {
                fDocumentPartitioningChanged = true;
            }
        }

        @Override
        public void documentPartitioningChanged(IDocument document, IRegion changedRegion) {
            if (!fDocumentChanging && fCachedRedrawState) {
                processDamage(new Region(changedRegion.getOffset(), changedRegion.getLength()), document);
            } else {
                fDocumentPartitioningChanged = true;
                fChangedDocumentPartitions = changedRegion;
            }
        }

        @Override
        public void documentPartitioningChanged(DocumentPartitioningChangedEvent event) {
            IRegion changedRegion = event.getChangedRegion(getDocumentPartitioning());
            if (changedRegion != null) {
                documentPartitioningChanged(event.getDocument(), changedRegion);
            }
        }

        @Override
        public void documentAboutToBeChanged(DocumentEvent e) {

            fDocumentChanging = true;
            if (fCachedRedrawState) {
                try {
                    int offset = e.getOffset() + e.getLength();
                    ITypedRegion region = getPartition(e.getDocument(), offset);
                    fRememberedPosition = new TypedPosition(region);
                    e.getDocument().addPosition(fPositionCategory, fRememberedPosition);
                } catch (BadLocationException x) {
                    // can not happen
                } catch (BadPositionCategoryException x) {
                    // should not happen on input elements
                }
            }
        }

        @Override
        public void documentChanged(DocumentEvent e) {
            if (fCachedRedrawState) {
                try {
                    e.getDocument().removePosition(fPositionCategory, fRememberedPosition);
                } catch (BadPositionCategoryException x) {
                    // can not happen on input documents
                }
            }
            fDocumentChanging = false;
        }

        @Override
        public void textChanged(TextEvent e) {

            fCachedRedrawState = e.getViewerRedrawState();
            if (!fCachedRedrawState) {
                return;
            }

            // If there was some change, just copy the previous style (in the UI thread).
            if (e.getDocumentEvent() != null) {
                int diff = e.getText().length() - e.getLength();
                if (diff > 0 && e.getOffset() > 0) {
                    try {
                        StyleRange range = fViewer.getTextWidget().getStyleRangeAtOffset(e.getOffset() - 1);
                        if (range != null) {
                            range.length += diff;
                            fViewer.getTextWidget().setStyleRange(range);
                        }
                    } catch (RuntimeException e1) {
                    }
                }
            }

            IRegion damage = null;
            IDocument document = null;

            if (e.getDocumentEvent() == null) {
                document = fViewer.getDocument();
                if (document != null) {
                    if (e.getOffset() == 0 && e.getLength() == 0 && e.getText() == null) {
                        // redraw state change, damage the whole document
                        damage = new Region(0, document.getLength());
                    } else {
                        IRegion region = widgetRegion2ModelRegion(e);
                        if (region != null) {
                            try {
                                String text = document.get(region.getOffset(), region.getLength());
                                DocumentEvent de = new DocumentEvent(document, region.getOffset(), region.getLength(),
                                        text);
                                damage = getDamage(de, false);
                            } catch (BadLocationException x) {
                            }
                        }
                    }
                }
            } else {
                DocumentEvent de = e.getDocumentEvent();
                document = de.getDocument();
                damage = getDamage(de, true);
            }

            if (damage != null && document != null) {
                if (damage.getOffset() > document.getLength()) {
                    // Skip it
                    Log.logInfo("Skip damaging as damage offset (" + damage.getOffset() + ") > doc len ("
                            + document.getLength() + ").");
                } else if ((damage.getOffset() + damage.getLength()) > document.getLength()) {
                    damage = new Region(damage.getOffset(), document.getLength() - damage.getOffset());
                    processDamage(damage, document);
                } else {
                    processDamage(damage, document);
                }
            }

            fDocumentPartitioningChanged = false;
            fChangedDocumentPartitions = null;
        }

        /**
         * Translates the given text event into the corresponding range of the viewer's document.
         *
         * @param e the text event
         * @return the widget region corresponding the region of the given event or
         *         <code>null</code> if none
         * @since 2.1
         */
        protected IRegion widgetRegion2ModelRegion(TextEvent e) {

            String text = e.getText();
            int length = text == null ? 0 : text.length();

            if (fViewer instanceof ITextViewerExtension5) {
                ITextViewerExtension5 extension = (ITextViewerExtension5) fViewer;
                return extension.widgetRange2ModelRange(new Region(e.getOffset(), length));
            }

            IRegion visible = fViewer.getVisibleRegion();
            IRegion region = new Region(e.getOffset() + visible.getOffset(), length);
            return region;
        }
    }

    /** The map of presentation damagers. */
    private Map<String, LiClipseDamagerRepairer> fDamagers;
    /** The map of presentation repairers. */
    private Map<String, LiClipseDamagerRepairer> fRepairers;
    /** The target viewer. */
    private ITextViewer fViewer;
    /** The internal listener. */
    private InternalListener fInternalListener = new InternalListener();
    /** The name of the position category to track damage regions. */
    private String fPositionCategory;
    /** The position updated for the damage regions' position category. */
    private IPositionUpdater fPositionUpdater;
    /** The positions representing the damage regions. */
    private TypedPosition fRememberedPosition;
    /** Flag indicating the receipt of a partitioning changed notification. */
    private boolean fDocumentPartitioningChanged = false;
    /** The range covering the changed partitioning. */
    private IRegion fChangedDocumentPartitions = null;
    /**
     * The partitioning used by this presentation reconciler.
     * @since 3.0
     */
    private String fPartitioning;

    /**
     * Creates a new presentation reconciler. There are no damagers or repairers
     * registered with this reconciler by default. The default partitioning
     * <code>IDocumentExtension3.DEFAULT_PARTITIONING</code> is used.
     */
    public LiClipsePresentationReconciler() {
        fPartitioning = IDocumentExtension3.DEFAULT_PARTITIONING;
        fPositionCategory = TRACKED_PARTITION + hashCode();
        fPositionUpdater = new DefaultPositionUpdater(fPositionCategory);
    }

    /**
     * Sets the document partitioning for this presentation reconciler.
     *
     * @param partitioning the document partitioning for this presentation reconciler.
     * @since 3.0
     */
    public void setDocumentPartitioning(String partitioning) {
        Assert.isNotNull(partitioning);
        fPartitioning = partitioning;
    }

    /*
     * @see org.eclipse.jface.text.presentation.IPresentationReconcilerExtension#geDocumenttPartitioning()
     * @since 3.0
     */
    @Override
    public String getDocumentPartitioning() {
        return fPartitioning;
    }

    /**
     * Registers the given presentation damager for a particular content type.
     * If there is already a damager registered for this type, the old damager
     * is removed first.
     *
     * @param damager the presentation damager to register, or <code>null</code> to remove an existing one
     * @param contentType the content type under which to register
     */
    public void setDamager(LiClipseDamagerRepairer damager, String contentType) {

        Assert.isNotNull(contentType);

        if (fDamagers == null) {
            fDamagers = new HashMap<>();
        }

        if (damager == null) {
            fDamagers.remove(contentType);
        } else {
            fDamagers.put(contentType, damager);
        }
    }

    /**
     * Registers the given presentation repairer for a particular content type.
     * If there is already a repairer registered for this type, the old repairer
     * is removed first.
     *
     * @param repairer the presentation repairer to register, or <code>null</code> to remove an existing one
     * @param contentType the content type under which to register
     */
    public void setRepairer(LiClipseDamagerRepairer repairer, String contentType) {

        Assert.isNotNull(contentType);

        if (fRepairers == null) {
            fRepairers = new HashMap<>();
        }

        if (repairer == null) {
            fRepairers.remove(contentType);
        } else {
            fRepairers.put(contentType, repairer);
        }
    }

    @Override
    public void install(ITextViewer viewer) {
        Assert.isNotNull(viewer);

        fViewer = viewer;
        fViewer.addTextInputListener(fInternalListener);

        IDocument document = viewer.getDocument();
        if (document != null) {
            fInternalListener.inputDocumentChanged(null, document);
        }
    }

    @Override
    public void uninstall() {
        fViewer.removeTextInputListener(fInternalListener);

        // Ensure we uninstall all listeners
        fInternalListener.inputDocumentAboutToBeChanged(fViewer.getDocument(), null);
    }

    @Override
    public LiClipseDamagerRepairer getDamager(String contentType) {

        if (fDamagers == null) {
            return null;
        }

        return fDamagers.get(contentType);
    }

    @Override
    public LiClipseDamagerRepairer getRepairer(String contentType) {

        if (fRepairers == null) {
            return null;
        }

        return fRepairers.get(contentType);
    }

    /**
     * Informs all registered damagers about the document on which they will work.
     *
     * @param document the document on which to work
     */
    protected void setDocumentToDamagers(IDocument document) {
        if (fDamagers != null) {
            Iterator<LiClipseDamagerRepairer> e = fDamagers.values().iterator();
            while (e.hasNext()) {
                LiClipseDamagerRepairer damager = e.next();
                damager.setDocument(document);
            }
        }
    }

    /**
     * Informs all registered repairers about the document on which they will work.
     *
     * @param document the document on which to work
     */
    protected void setDocumentToRepairers(IDocument document) {
        if (fRepairers != null) {
            Iterator<LiClipseDamagerRepairer> e = fRepairers.values().iterator();
            while (e.hasNext()) {
                LiClipseDamagerRepairer repairer = e.next();
                repairer.setDocument(document);
            }
        }
    }

    /**
     * Constructs a "repair description" for the given damage and returns this
     * description as a text presentation. For this, it queries the partitioning
     * of the damage region and asks the appropriate presentation repairer for
     * each partition to construct the "repair description" for this partition.
     *
     * @param damage the damage to be repaired
     * @param document the document whose presentation must be repaired
     * @return the presentation repair description as text presentation or
     *         <code>null</code> if the partitioning could not be computed
     */
    private void applyPresentation(IRegion damage, IDocument document) {
        try {
            if (fRepairers == null || fRepairers.isEmpty()) {
                TextPresentation presentation = new TextPresentation(damage, 100);
                presentation.setDefaultStyleRange(new StyleRange(damage.getOffset(), damage.getLength(), null, null));
                applyTextRegionCollection(presentation);
            } else {
                int totalDamageLen = 0;
                IDocument doc = null;
                List<IRegion> finalRegions;
                synchronized (repairListLock) {
                    repairList.add(new RepairListEntry(damage, document));
                    List<IRegion> regions = new ArrayList<>();
                    for (RepairListEntry repairListEntry : repairList) {
                        if (doc != repairListEntry.doc) {
                            if (doc != null) {
                                Log.logInfo("Doc changed (clearing regions).");
                            }
                            doc = repairListEntry.doc;
                            regions.clear();
                        }
                        regions.add(repairListEntry.damage);
                    }
                    repairList.clear();

                    if (doc == null) {
                        return;
                    }
                    final int docLength = doc.getLength();
                    IDocumentExtension4 docExt = (IDocumentExtension4) doc;
                    final long modificationStamp = docExt.getModificationStamp();

                    finalRegions = new ArrayList<>(regions.size());

                    // Ok, we now have the regions and the docs, let's join the regions we can...
                    for (IRegion iRegion : regions) {
                        if (DEBUG) {
                            System.out.println("Initial: " + iRegion);
                            try {
                                System.out.println(doc.get(iRegion.getOffset(), iRegion.getLength()));
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                        boolean found = false;
                        for (ListIterator<IRegion> it = finalRegions.listIterator(); it.hasNext();) {
                            IRegion existing = it.next();
                            if (existing != iRegion) {
                                if (TextUtilities.overlaps(iRegion, existing)) {
                                    int startIndex = Math.min(existing.getOffset(), iRegion.getOffset());
                                    int endIndex = Math.max(existing.getOffset() + existing.getLength(),
                                            iRegion.getOffset() + iRegion.getLength());

                                    if (endIndex > docLength) {
                                        endIndex = docLength;
                                    }
                                    IRegion merged = new Region(startIndex, endIndex - startIndex);
                                    it.set(merged);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            finalRegions.add(iRegion);
                        }
                    }

                    Collections.sort(finalRegions,
                            (IRegion a, IRegion b) -> Integer.compare(a.getOffset(), b.getOffset()));

                    for (ListIterator<IRegion> it = finalRegions.listIterator(); it.hasNext();) {
                        IRegion r = it.next();
                        if (r.getOffset() > docLength) {
                            it.remove();
                            continue;
                        }
                        if ((r.getOffset() + r.getLength()) > docLength) {
                            it.set(new Region(r.getOffset(), docLength - r.getOffset()));
                        }
                        totalDamageLen += r.getLength();
                    }

                    if (UPDATE_ASYNC && totalDamageLen > 10000) {
                        if (DEBUG) {
                            System.out.println("Making async damage update");
                        }
                        // This code tried to pre process a changed line in the UI thread to avoid flicker. Doesn't work well. Must investigate.
                        // long initialTime = System.currentTimeMillis();
                        //
                        // // Do what we can in the main thread and reschedule for a thread only the remainder.
                        // int maxMillisToParse = 20;
                        // if (fNewDocBeingSet) {
                        //     // If it's a new doc, give it a bit more time so that we can parse more.
                        //     maxMillisToParse = 500;
                        // }
                        // try {
                        //     for (ListIterator<IRegion> it = finalRegions.listIterator(); it.hasNext()
                        //             && System.currentTimeMillis() < initialTime + maxMillisToParse;) {
                        //         TextPresentationStoreRanges rangesStore = new TextPresentationStoreRanges();
                        //         final IRegion region = it.next();
                        //
                        //         final int regionEndOffset = region.getOffset() + region.getLength();
                        //
                        //         final int damagedLine = doc.getLineOfOffset(region.getOffset());
                        //         int endDamagedLine;
                        //         try {
                        //             endDamagedLine = doc.getLineOfOffset(regionEndOffset);
                        //         } catch (BadLocationException e) {
                        //             endDamagedLine = document.getNumberOfLines();
                        //         }
                        //         final int numberOfLines = Math.min(endDamagedLine, document.getNumberOfLines());
                        //
                        //         IRegion lastDamage = null;
                        //         for (int i = damagedLine; i < numberOfLines; i++) {
                        //             // Repair damage by line synchronously in the available time.
                        //             IRegion lineInformationOfOffset = doc.getLineInformation(i); //Note: not getting new lines
                        //             if ((lineInformationOfOffset.getOffset()
                        //                     + lineInformationOfOffset.getLength()) > regionEndOffset) {
                        //                 lineInformationOfOffset = new Region(lineInformationOfOffset.getOffset(),
                        //                         regionEndOffset - lineInformationOfOffset.getOffset());
                        //             }
                        //             rangesStore.mergeRegion(lineInformationOfOffset);
                        //             processRepairListJob.repairDamage(Arrays.asList(lineInformationOfOffset), doc,
                        //                     false,
                        //                     rangesStore);
                        //             lastDamage = lineInformationOfOffset;
                        //             if (System.currentTimeMillis() > initialTime + maxMillisToParse) {
                        //                 break;
                        //             }
                        //         }
                        //
                        //         if (lastDamage != null) {
                        //             int endDamageOffset = lastDamage.getOffset() + lastDamage.getLength();
                        //             if (endDamageOffset >= regionEndOffset) {
                        //                 // No need to re-add it as we consumed all of it.
                        //                 it.remove();
                        //             } else {
                        //                 it.set(new Region(endDamageOffset, regionEndOffset - endDamageOffset));
                        //             }
                        //         }
                        //         applyTextRegionCollection(rangesStore.toFinalTextPresentation());
                        //     }
                        // } catch (BadLocationException e) {
                        //     Log.log(e);
                        // }

                        for (IRegion r : finalRegions) {
                            repairList.add(new RepairListEntry(r, doc));
                        }
                        if (repairList.size() > 0) {
                            processRepairListJob.schedule(SCHEDULE_PARSE_TIME);
                        }
                    } else {
                        if (DEBUG) {
                            System.out.println("Making sync damage update");
                        }
                        for (IRegion r : finalRegions) {
                            repairList.add(new RepairListEntry(r, doc));
                        }
                        processRepairListJob.doRun(modificationStamp);
                    }
                }
            }

        } catch (Exception e) {
            Log.log(e);
        }
    }

    private final List<RepairListEntry> repairList = new LinkedList<>();
    private final Object repairListLock = new Object();

    private static class RepairListEntry {

        private final IDocument doc;
        private final IRegion damage;

        public RepairListEntry(IRegion damage, IDocument document) {
            Assert.isNotNull(document);
            Assert.isNotNull(damage);
            this.doc = document;
            this.damage = damage;
        }
    }

    private static final boolean DEBUG = false;

    private class ProcessRepairListJob extends Job {

        public ProcessRepairListJob() {
            super("ProcessRepairList");
            setSystem(true);
            setPriority(Job.INTERACTIVE);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            doRun(-1);
            return Status.OK_STATUS;
        }

        protected void doRun(long modificationStamp) {
            List<IRegion> finalRegions = new ArrayList<>();
            IDocument doc = null;
            synchronized (repairListLock) {
                if (DEBUG) {
                    System.out.println("Processing damaged regions...");
                }
                List<IRegion> regions = new ArrayList<>();
                for (RepairListEntry repairListEntry : repairList) {
                    if (doc != repairListEntry.doc) {
                        if (doc != null) {
                            Log.logInfo("Doc changed (clearing regions).");
                        }
                        doc = repairListEntry.doc;
                        regions.clear();
                    }
                    regions.add(repairListEntry.damage);
                }
                finalRegions.addAll(regions);
                repairList.clear();
            }

            if (doc == null) {
                return;
            }
            if (modificationStamp == -1) {
                IDocumentExtension4 docExt = (IDocumentExtension4) doc;
                modificationStamp = docExt.getModificationStamp();
            }

            repairDamage(finalRegions, doc, true, null, modificationStamp, true);
        }

        private void repairDamage(List<IRegion> finalRegions, IDocument doc,
                boolean rescheduleLaterIfChanged, TextPresentation applyToPresentation, final long modificationStamp,
                final boolean cacheFinalResult) {
            List<TextPresentation> presentations = new ArrayList<>(finalRegions.size());
            final IDocumentExtension4 docExt = (IDocumentExtension4) doc;

            final IDocument finalDoc = doc;
            for (IRegion damage : finalRegions) {
                if (DEBUG) {
                    System.out.println("Final: " + damage);
                }
                if ((damage.getOffset() + damage.getLength()) > doc.getLength()) {
                    if (modificationStamp != docExt.getModificationStamp()) {
                        rescheduleLater(finalRegions, finalDoc, rescheduleLaterIfChanged);
                        return;
                    }
                    if (Display.getCurrent() != null) {
                        // Only report if in main thread (this will be auto-fixed later on and is expected
                        // if we're not in the main thread as the damage was scheduled at a different time).
                        Log.logInfo("Damage end offset (" + (damage.getOffset() + damage.getLength()) + ") > doc len ("
                                + doc.getLength() + ").");
                    }
                }
                TextPresentation presentation = new TextPresentation(damage, 1000);
                if (applyToPresentation == null) {
                    presentation = new TextPresentation(damage, 1000);
                } else {
                    presentation = applyToPresentation;
                }
                ITypedRegion[] partitioning;
                try {

                    int offset = damage.getOffset();
                    if (offset > doc.getLength()) {
                        continue;
                    }
                    int endOffset = offset + damage.getLength();
                    if (endOffset > doc.getLength()) {
                        endOffset = doc.getLength();
                    }
                    if (offset < 0) {
                        Log.logInfo("Damage offset < 0 (" + offset + ")! Setting to 0.");
                        offset = 0;
                    }

                    ITypedRegion partition = doc.getPartition(offset);
                    int partitionStart = partition.getOffset();
                    if (partitionStart < offset) {
                        // Unless we're at the start of a partition, we should always work with full lines.
                        IRegion lineInformationOfOffset = doc.getLineInformationOfOffset(offset);
                        // otherwise, make it go to the line (or partition) start
                        offset = Math.max(partitionStart, lineInformationOfOffset.getOffset());
                    }

                    partition = ((IDocumentPartitionerExtension2) doc.getDocumentPartitioner()).getPartition(endOffset,
                            true);
                    if (partition.getOffset() + partition.getLength() != endOffset) { // if equal to the end of partition, it's ok
                        // Note: doesn't get new lines
                        IRegion lineInformationOfOffset = doc.getLineInformationOfOffset(endOffset);
                        endOffset = Math.min(partition.getOffset() + partition.getLength(),
                                lineInformationOfOffset.getOffset() + lineInformationOfOffset.getLength());
                    }

                    if (modificationStamp != docExt.getModificationStamp()) {
                        rescheduleLater(finalRegions, doc, rescheduleLaterIfChanged);
                        return;
                    }

                    partitioning = TextUtilities.computePartitioning(doc, getDocumentPartitioning(),
                            offset, endOffset - offset, false);
                    for (ITypedRegion r : partitioning) {
                        if (r.getOffset() + r.getLength() > doc.getLength()) {
                            Log.logInfo("Region end offset (" + (r.getOffset() + r.getLength()) + ") > doc len ("
                                    + doc.getLength() + ").");
                        }

                        LiClipseDamagerRepairer repairer = getRepairer(r.getType());
                        repairer.setDocument(doc);
                        if (modificationStamp != docExt.getModificationStamp()) {
                            rescheduleLater(finalRegions, doc, rescheduleLaterIfChanged);
                            return;
                        }
                        if (repairer != null) {
                            repairer.createPresentation(presentation, r, modificationStamp, cacheFinalResult);
                        }
                    }
                    presentations.add(presentation);
                } catch (Exception e) {
                    if (modificationStamp != docExt.getModificationStamp()) {
                        rescheduleLater(finalRegions, finalDoc, rescheduleLaterIfChanged);
                        return;
                    }
                    Log.log(e);
                }
            }

            if (modificationStamp != docExt.getModificationStamp()) {
                rescheduleLater(finalRegions, doc, rescheduleLaterIfChanged);
                return;
            }

            if (applyToPresentation == null) {
                RunInUiThread.async(new Runnable() { // Note: if possible will do in the current thread.

                    @Override
                    public void run() {
                        // Never apply if the document changed in the meanwhile...
                        if (modificationStamp != docExt.getModificationStamp()) {
                            rescheduleLater(finalRegions, finalDoc, rescheduleLaterIfChanged);
                            return;
                        }
                        for (TextPresentation presentation : presentations) {
                            if (DEBUG) {
                                System.out.println("Applying text presentation.");
                            }
                            applyTextRegionCollection(presentation);
                        }
                        if (DEBUG) {
                            System.out.println("\n.");
                        }
                    }
                }, true);
            }

        }

        private void rescheduleLater(List<IRegion> finalRegions, IDocument doc, boolean rescheduleLaterIfChanged) {
            if (rescheduleLaterIfChanged) {
                synchronized (repairListLock) {
                    if (DEBUG) {
                        System.out.println("Document changed. Rescheduling (1).\n\n");
                    }
                    for (IRegion r : finalRegions) {
                        repairList.add(0, new RepairListEntry(r, doc));
                    }
                    schedule(SCHEDULE_PARSE_TIME);
                }
            }
        }
    }

    private ProcessRepairListJob processRepairListJob = new ProcessRepairListJob();

    /**
     * Checks for the first and the last affected partition affected by a
     * document event and calls their damagers. Invalidates everything from the
     * start of the damage for the first partition until the end of the damage
     * for the last partition.
     *
     * @param e the event describing the document change
     * @param optimize <code>true</code> if partition changes should be
     *        considered for optimization
     * @return the damaged caused by the change or <code>null</code> if
     *         computing the partitioning failed
     * @since 3.0
     */
    private IRegion getDamage(DocumentEvent e, boolean optimize) {
        int length = e.getText() == null ? 0 : e.getText().length();

        if (fDamagers == null || fDamagers.isEmpty()) {
            length = Math.max(e.getLength(), length);
            length = Math.min(e.getDocument().getLength() - e.getOffset(), length);
            return new Region(e.getOffset(), length);
        }

        boolean isDeletion = length == 0;
        IRegion damage = null;
        try {
            int offset = e.getOffset();
            if (isDeletion) {
                offset = Math.max(0, offset - 1);
            }
            ITypedRegion partition = getPartition(e.getDocument(), offset);
            LiClipseDamagerRepairer damager = getDamager(partition.getType());
            if (damager == null) {
                return null;
            }

            IRegion r = damager.getDamageRegion(partition, e, fDocumentPartitioningChanged);
            if (!fDocumentPartitioningChanged && optimize && !isDeletion) {
                damage = r;
            } else {

                int damageStart = r.getOffset();
                int damageEnd = getDamageEndOffset(e);

                if (fChangedDocumentPartitions != null) {
                    damageStart = Math.min(damageStart, fChangedDocumentPartitions.getOffset());
                    damageEnd = Math.max(damageEnd,
                            fChangedDocumentPartitions.getOffset() + fChangedDocumentPartitions.getLength());
                }

                damage = damageEnd == -1 ? r : new Region(damageStart, damageEnd - damageStart);
            }
            if (DEBUG) {
                System.out.println("Computed damage: " + e.getDocument().get(damage.getOffset(), damage.getLength()));
            }
        } catch (BadLocationException x) {
        }

        return damage;
    }

    /**
     * Returns the end offset of the damage. If a partition has been split by
     * the given document event also the second half of the original
     * partition must be considered. This is achieved by using the remembered
     * partition range.
     *
     * @param e the event describing the change
     * @return the damage end offset (excluding)
     * @exception BadLocationException if method accesses invalid offset
     */
    private int getDamageEndOffset(DocumentEvent e) throws BadLocationException {

        IDocument d = e.getDocument();

        int length = 0;
        if (e.getText() != null) {
            length = e.getText().length();
            if (length > 0) {
                --length;
            }
        }

        ITypedRegion partition = getPartition(d, e.getOffset() + length);
        int endOffset = partition.getOffset() + partition.getLength();
        if (endOffset == e.getOffset()) {
            return -1;
        }

        int end = fRememberedPosition == null ? -1 : fRememberedPosition.getOffset() + fRememberedPosition.getLength();
        if (endOffset < end && end < d.getLength()) {
            partition = getPartition(d, end);
        }

        LiClipseDamagerRepairer damager = getDamager(partition.getType());
        if (damager == null) {
            return -1;
        }

        IRegion r = damager.getDamageRegion(partition, e, fDocumentPartitioningChanged);

        return r.getOffset() + r.getLength();
    }

    /**
     * Processes the given damage.
     * @param damage the damage to be repaired
     * @param document the document whose presentation must be repaired
     */
    private void processDamage(IRegion damage, IDocument document) {
        if (damage != null && damage.getLength() > 0) {
            applyPresentation(damage, document);
        }
    }

    /**
     * Applies the given text presentation to the text viewer the presentation
     * reconciler is installed on.
     *
     * @param presentation the text presentation to be applied to the text viewer
     */
    private void applyTextRegionCollection(TextPresentation presentation) {
        // System.out.println("Start fViewer.changeTextPresentation(presentation, false) -- " + presentation.getCoverage()
        //         + " -- " + presentation.getDenumerableRanges());
        // long start = System.currentTimeMillis();
        fViewer.changeTextPresentation(presentation, false);
        // System.out.println("Time Elapsed (secs):" + (System.currentTimeMillis() - start) / 1000.0);
        // Note: faster with StyledText subclass which precomputes ranges.
    }

    /**
     * Returns the partition for the given offset in the given document.
     *
     * @param document the document
     * @param offset the offset
     * @return the partition
     * @throws BadLocationException if offset is invalid in the given document
     * @since 3.0
     */
    private ITypedRegion getPartition(IDocument document, int offset) throws BadLocationException {
        return TextUtilities.getPartition(document, getDocumentPartitioning(), offset, false);
    }
}
