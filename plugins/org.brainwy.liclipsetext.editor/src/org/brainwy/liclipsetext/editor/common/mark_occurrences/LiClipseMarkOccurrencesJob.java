/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.mark_occurrences;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_ui.editor.BaseEditor;
import org.brainwy.liclipsetext.shared_ui.mark_occurrences.BaseMarkOccurrencesJob;

public class LiClipseMarkOccurrencesJob extends BaseMarkOccurrencesJob {

    protected final static class LiClipseMarkOccurrencesRequest extends MarkOccurrencesRequest {

        protected LiClipseMarkOccurrencesRequest(boolean proceedWithMarkOccurrences) {
            super(proceedWithMarkOccurrences);
        }
    }

    public LiClipseMarkOccurrencesJob(WeakReference<BaseEditor> editor, TextSelectionUtils ps) {
        super(editor, ps);
    }

    public static void scheduleRequest(WeakReference<BaseEditor> weakReference, TextSelectionUtils ps) {
        BaseMarkOccurrencesJob.scheduleRequest(new LiClipseMarkOccurrencesJob(weakReference, ps));
    }

    public static void scheduleRequest(WeakReference<BaseEditor> weakReference, TextSelectionUtils ps, int time) {
        BaseMarkOccurrencesJob.scheduleRequest(new LiClipseMarkOccurrencesJob(weakReference, ps), time);
    }

    @Override
    protected MarkOccurrencesRequest createRequest(BaseEditor baseEditor, IDocumentProvider documentProvider,
            IProgressMonitor monitor) throws Exception {
        if (!LiClipseMarkOccurrencesPreferencesPage.useMarkOccurrences()) {
            return new LiClipseMarkOccurrencesRequest(false);
        }
        return new LiClipseMarkOccurrencesRequest(true);
    }

    @Override
    protected Map<Annotation, Position> getAnnotationsToAddAsMap(BaseEditor baseEditor,
            IAnnotationModel annotationModel, MarkOccurrencesRequest ret, IProgressMonitor monitor)
            throws BadLocationException {
        Tuple<String, Integer> currToken = ps.getCurrToken();
        if (currToken == null || currToken.o1.length() == 0) {
            return null;
        }
        List<IRegion> occurrences = ps.searchOccurrences(currToken.o1);
        if (occurrences.size() == 0) {
            return null;
        }
        Map<Annotation, Position> toAddAsMap = new HashMap<Annotation, Position>();
        for (Iterator<IRegion> it = occurrences.iterator(); it.hasNext();) {
            IRegion iRegion = it.next();

            try {
                Annotation annotation = new Annotation(getOccurrenceAnnotationsType(), false, "occurrence");
                Position position = new Position(iRegion.getOffset(), iRegion.getLength());
                toAddAsMap.put(annotation, position);

            } catch (Exception e) {
                Log.log(e);
            }
        }
        return toAddAsMap;

    }

    private static final String ANNOTATIONS_CACHE_KEY = "LiClipseMarkOccurrencesJob Annotations";
    private static final String OCCURRENCE_ANNOTATION_TYPE = "com.brainwy.liclipse.occurrences";

    @Override
    protected String getOccurrenceAnnotationsCacheKey() {
        return ANNOTATIONS_CACHE_KEY;
    }

    @Override
    protected String getOccurrenceAnnotationsType() {
        return OCCURRENCE_ANNOTATION_TYPE;
    }

}
