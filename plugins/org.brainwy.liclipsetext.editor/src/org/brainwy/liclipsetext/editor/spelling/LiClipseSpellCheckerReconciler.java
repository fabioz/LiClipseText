/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.spelling;

import java.util.ArrayList;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingService;

public class LiClipseSpellCheckerReconciler extends SpellingReconcileStrategy {

    /**
     * Spelling problem collector.
     */
    private class LiClipseSpellingProblemCollector extends SpellingReconcileStrategy.SpellingProblemCollector {

        /**
         * Initializes this collector with the given annotation model.
         *
         * @param annotationModel the annotation model
         */
        public LiClipseSpellingProblemCollector(IAnnotationModel annotationModel) {
            super(annotationModel);
        }

        @Override
        public IRegion[] getSubRegionsForReconcile(IDocument document) {
            return DocumentTimeStampChangedException.retryUntilNoDocChanges(() -> {
                SubPartitionCodeReader reader = new SubPartitionCodeReader();
                IDocumentPartitioner partitioner = document.getDocumentPartitioner();

                if (partitioner instanceof LiClipseDocumentPartitioner) {
                    LiClipseDocumentPartitioner liClipsePartitioner = (LiClipseDocumentPartitioner) partitioner;
                    String[] spellCheckingContentTypes = liClipsePartitioner.getSpellCheckingContentTypes();
                    if (spellCheckingContentTypes == null || spellCheckingContentTypes.length == 0) {
                        return null;
                    }
                    reader.configurePartitions(true, document, this.baseRegionForReconcile.getOffset(),
                            spellCheckingContentTypes);

                    int finalOffset = this.baseRegionForReconcile.getOffset() + this.baseRegionForReconcile.getLength();

                    ArrayList<IRegion> regions = new ArrayList<IRegion>();
                    TypedPart read = reader.read();
                    while (read != null) {
                        if (read.offset >= finalOffset) {
                            break;
                        }
                        regions.add(new Region(read.offset, read.length));
                        read = reader.read();
                    }
                    return regions.toArray(new IRegion[regions.size()]);
                } else {
                    Log.log("Expected LiClipseDocumentPartitioner. Found: " + partitioner);
                    return null;
                }
            });
        }
    }

    private IPreferenceStore preferenceStore;

    public LiClipseSpellCheckerReconciler(ISourceViewer sourceViewer, SpellingService spellingService) {
        super(sourceViewer, spellingService);
        this.preferenceStore = EditorsUI.getPreferenceStore();
    }

    /*default for tests*/ LiClipseSpellCheckerReconciler() {
        super();
    }

    @Override
    public void reconcile(IRegion region) {
        if (!isSpellingEnabled()) {
            return;
        }
        super.reconcile(region);
    }

    protected boolean isSpellingEnabled() {
        return preferenceStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED);
    }

    @Override
    protected SpellingProblemCollector createSpellingProblemCollector(IAnnotationModel model) {
        return new LiClipseSpellingProblemCollector(model);
    }

}
