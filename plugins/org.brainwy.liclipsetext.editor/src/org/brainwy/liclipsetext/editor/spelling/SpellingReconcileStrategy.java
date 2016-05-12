/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fabio Zadrozny - making it suitable for incremental reconciling
 *******************************************************************************/
package org.brainwy.liclipsetext.editor.spelling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;
import org.eclipse.ui.texteditor.spelling.SpellingService;

/**
 * Reconcile strategy used for spell checking.
 *
 * @since 3.3
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class SpellingReconcileStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

    /**
     * Spelling problem collector.
     */
    public static abstract class SpellingProblemCollector implements ISpellingProblemCollector {

        /** Annotation model. */
        protected final IAnnotationModel fAnnotationModel;

        /** Annotations to add. */
        private Map fAddAnnotations;

        /** Lock object for modifying the annotations. */
        private Object fLockObject;

        protected IRegion baseRegionForReconcile;

        /**
         * Initializes this collector with the given annotation model.
         *
         * @param annotationModel the annotation model
         */
        public SpellingProblemCollector(IAnnotationModel annotationModel) {
            Assert.isLegal(annotationModel != null);
            fAnnotationModel = annotationModel;
            if (fAnnotationModel instanceof ISynchronizable) {
                fLockObject = ((ISynchronizable) fAnnotationModel).getLockObject();
            } else {
                fLockObject = fAnnotationModel;
            }
        }

        /*
         * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#accept(org.eclipse.ui.texteditor.spelling.SpellingProblem)
         */
        public void accept(SpellingProblem problem) {
            fAddAnnotations
                    .put(new SpellingAnnotation(problem), new Position(problem.getOffset(), problem.getLength()));
        }

        /*
         * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#beginCollecting()
         */
        public void beginCollecting() {
            fAddAnnotations = new HashMap();
        }

        /*
         * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#endCollecting()
         */
        public void endCollecting() {
            if (baseRegionForReconcile == null) {
                Log.log("baseRegionForReconcile must be set before collecting.");
                return;
            }
            List toRemove = new ArrayList();

            //let other threads execute before getting the lock on the annotation model
            Thread.yield();

            Thread thread = Thread.currentThread();
            int initiaThreadlPriority = thread.getPriority();
            try {
                //before getting the lock, let's execute with normal priority, to optimize the time that we'll
                //retain that object locked (the annotation model is used on lots of places, so, retaining the lock
                //on it on a minimum priority thread is not a good thing.
                thread.setPriority(Thread.NORM_PRIORITY);

                synchronized (fLockObject) {
                    Iterator iter = fAnnotationModel.getAnnotationIterator();
                    while (iter.hasNext()) {
                        Annotation annotation = (Annotation) iter.next();
                        if (SpellingAnnotation.TYPE.equals(annotation.getType())) {
                            Position position = fAnnotationModel.getPosition(annotation);
                            //Only remove the ones which we intersect.
                            if (position != null
                                    && position.overlapsWith(baseRegionForReconcile.getOffset(),
                                            baseRegionForReconcile.getLength())) {
                                toRemove.add(annotation);
                            }
                        }
                    }
                }
                Annotation[] annotationsToRemove = (Annotation[]) toRemove.toArray(new Annotation[toRemove.size()]);
                //let other threads execute before getting the lock (again) on the annotation model
                Thread.yield();
                synchronized (fLockObject) {
                    ((IAnnotationModelExtension) fAnnotationModel).replaceAnnotations(annotationsToRemove,
                            fAddAnnotations);
                }
            } finally {
                thread.setPriority(initiaThreadlPriority);
            }

            fAddAnnotations = null;
        }

        public void setBaseRegionForReconcile(IRegion region) {
            this.baseRegionForReconcile = region;
        }

        public IRegion getBaseRegionForReconcile() {
            return baseRegionForReconcile;
        }

        public void fixBaseRegionFromSubRegions(IRegion[] subRegionsForReconcile) {
            //If the final area is bigger than the initial one, we have to update it!
            int len = subRegionsForReconcile.length;

            int currOffsetStart = this.baseRegionForReconcile.getOffset();
            int currOffsetEnd = this.baseRegionForReconcile.getOffset() + this.baseRegionForReconcile.getLength();

            for (int i = 0; i < len; i++) {
                IRegion iRegion = subRegionsForReconcile[i];
                int regionOffsetStart = iRegion.getOffset();
                int regionOffsetEnd = regionOffsetStart + iRegion.getLength();

                if (regionOffsetStart < currOffsetStart) {
                    currOffsetStart = regionOffsetStart;
                }
                if (regionOffsetEnd > currOffsetEnd) {
                    currOffsetEnd = regionOffsetEnd;
                }
            }

            this.baseRegionForReconcile = new Region(currOffsetStart, currOffsetEnd - currOffsetStart);
        }

        /**
         * Subclasses must implement to return the actual regions for doing the reconciling based on the region changed.
         *
         * May return null or empty IRegion if there are no regions to reconcile.
         */
        public abstract IRegion[] getSubRegionsForReconcile(IDocument fDocument);

    }

    /** Text content type */
    private static final IContentType TEXT_CONTENT_TYPE = getTextContentType();

    private static IContentType getTextContentType() {
        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        if (contentTypeManager == null) {
            return null;
        }
        return contentTypeManager.getContentType(IContentTypeManager.CT_TEXT);
    }

    /** The text editor to operate on. */
    private ISourceViewer fViewer;

    /** The document to operate on. */
    private IDocument fDocument;

    /** The progress monitor. */
    private IProgressMonitor fProgressMonitor;

    private SpellingService fSpellingService;

    private SpellingProblemCollector fSpellingProblemCollector;

    public SpellingProblemCollector getSpellingProblemCollector() {
        return fSpellingProblemCollector;
    }

    /** The spelling context containing the Java source content type. */
    private SpellingContext fSpellingContext;

    private static final Set modelBeingChecked = new HashSet();

    /**
     * Creates a new comment reconcile strategy.
     *
     * @param viewer the source viewer
     * @param spellingService the spelling service to use
     */
    public SpellingReconcileStrategy(ISourceViewer viewer, SpellingService spellingService) {
        Assert.isNotNull(viewer);
        Assert.isNotNull(spellingService);
        fViewer = viewer;
        fSpellingService = spellingService;
        fSpellingContext = new SpellingContext();
        fSpellingContext.setContentType(getContentType());

    }

    protected SpellingReconcileStrategy() {
        //Only for tests!
    }

    /*
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
     */
    public void initialReconcile() {
        reconcile(new Region(0, fDocument.getLength()));
    }

    /*
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,org.eclipse.jface.text.IRegion)
     */
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        try {
            IRegion startLineInfo = fDocument.getLineInformationOfOffset(subRegion.getOffset());
            IRegion endLineInfo = fDocument.getLineInformationOfOffset(subRegion.getOffset()
                    + Math.max(0, subRegion.getLength() - 1));
            if (startLineInfo.getOffset() == endLineInfo.getOffset()) {
                subRegion = startLineInfo;
            } else {
                subRegion = new Region(startLineInfo.getOffset(), endLineInfo.getOffset()
                        + Math.max(0, endLineInfo.getLength() - 1) - startLineInfo.getOffset());
            }

        } catch (BadLocationException e) {
            subRegion = new Region(0, fDocument.getLength());
        }
        reconcile(subRegion);
    }

    /*
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
     */
    public void reconcile(IRegion region) {
        IAnnotationModel annotationModel = getAnnotationModel();
        if (annotationModel == null || fSpellingProblemCollector == null) {
            return;
        }

        //When having multiple editors for the same document, only one of the reconcilers actually needs to
        //work (because the others are binded to the same annotation model, so, having one do the work is enough)
        Tuple tuple = new Tuple(annotationModel, region);
        synchronized (modelBeingChecked) {
            if (modelBeingChecked.contains(tuple)) {
                return;
            }
            modelBeingChecked.add(tuple);
        }

        try {
            fSpellingProblemCollector.setBaseRegionForReconcile(region);
            IRegion[] subRegionsForReconcile = fSpellingProblemCollector.getSubRegionsForReconcile(fDocument);
            if (subRegionsForReconcile == null || subRegionsForReconcile.length == 0) {
                return;
            }
            fSpellingProblemCollector.fixBaseRegionFromSubRegions(subRegionsForReconcile);
            reconcileSubRegions(subRegionsForReconcile);
        } finally {
            synchronized (modelBeingChecked) {
                modelBeingChecked.remove(tuple);
            }
        }
    }

    protected void reconcileSubRegions(IRegion[] subRegionsForReconcile) {
        fSpellingService.check(fDocument,
                subRegionsForReconcile,
                fSpellingContext,
                fSpellingProblemCollector,
                fProgressMonitor);
    }

    /**
     * Returns the content type of the underlying editor input.
     *
     * @return the content type of the underlying editor input or
     *         <code>null</code> if none could be determined
     */
    protected IContentType getContentType() {
        return TEXT_CONTENT_TYPE;
    }

    /**
     * Returns the document which is spell checked.
     *
     * @return the document
     */
    protected final IDocument getDocument() {
        return fDocument;
    }

    /*
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
     */
    public void setDocument(IDocument document) {
        fDocument = document;
        IAnnotationModel model = getAnnotationModel();
        if (model == null) {
            fSpellingProblemCollector = null;

        } else {
            fSpellingProblemCollector = createSpellingProblemCollector(model);

        }
    }

    /**
     * Creates a new spelling problem collector.
     *
     * Usually: new SpellingProblemCollector(model) -- model is guaranteed not to be null.
     */
    protected abstract SpellingProblemCollector createSpellingProblemCollector(IAnnotationModel model);

    /*
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
     */
    public final void setProgressMonitor(IProgressMonitor monitor) {
        fProgressMonitor = monitor;
    }

    /**
     * Returns the annotation model to be used by this reconcile strategy.
     *
     * @return the annotation model of the underlying editor input or
     *         <code>null</code> if none could be determined
     */
    protected IAnnotationModel getAnnotationModel() {
        return fViewer.getAnnotationModel();
    }

}
