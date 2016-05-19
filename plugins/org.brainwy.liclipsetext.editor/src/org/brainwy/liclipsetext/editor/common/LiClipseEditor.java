/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.brainwy.liclipsetext.editor.handlers.extensions.ILiClipseErrorCollector;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.markers.InMemoryMarker;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.utils.BaseExtensionHelper;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.utils.PyMarkerUtils;
import org.brainwy.liclipsetext.shared_ui.utils.RunInUiThread;

/**
 * Editor which can add annotations for external files based on in-memory markers.
 */
@SuppressWarnings("rawtypes")
public class LiClipseEditor extends BaseLiClipseEditor {

    protected boolean isExternal = false;
    protected IResource resource = null;
    private MarkerAnnotation[] currentAnnotations;

    // Kept in com.brainwy.liclipse for backward-compatibility.
    public static final String EDITOR_ID = "com.brainwy.liclipse.editor.common.LiClipseEditor";

    public LiClipseEditor() {
		super();
		LiClipseTextShowBrowserMessage.show();
	}

    private void updateMarkerResource(IEditorInput input) {
        resource = input.getAdapter(IResource.class);
        isExternal = false;
        if (resource == null) {
            isExternal = true;
            resource = null;
        }
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        super.doSetInput(input);

        updateMarkerResource(input);

        try {
            clearMarkers();
            ILiClipseErrorCollector errorCollector = getErrorCollector();
            if (errorCollector != null) {
                collectErrors(errorCollector);
            }
        } catch (Exception e) {
            Log.log(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected ILiClipseErrorCollector getErrorCollector() {
        BaseLiClipseEditor liClipseEditor = this;
        LiClipseLanguage language = liClipseEditor.getLanguage();
        if (language == null) {
            return null;
        }

        ILiClipseErrorCollector handler = null;
        List<ILiClipseErrorCollector> participants = BaseExtensionHelper
                .getParticipants("org.brainwy.liclipsetext.editor.liclipse_error_collector");
        for (ILiClipseErrorCollector iLiClipseFormatHandler : participants) {
            if (iLiClipseFormatHandler.canHandle(language)) {
                handler = iLiClipseFormatHandler;
                break;
            }
        }
        if (handler == null) {
            return null;
        }
        return handler;
    }

    /**
     * Subclasses should override to collect errors for the given editor. Note that the editor
     * may be viewing an external file (in which case the resource that backs it up may be
     * unavailable).
     * @param temp3
     * @param temp2
     * @throws Exception
     */
    protected List<Map<String, Object>> collectErrors(IEditorInput iEditorInput, File editorFile, String docContents,
            ILiClipseErrorCollector handler) throws Exception {
        return handler.collectErrors(iEditorInput, editorFile, docContents);
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);

        try {
            ILiClipseErrorCollector errorCollector = getErrorCollector();
            if (errorCollector != null) {
                collectErrors(errorCollector);
            }
        } catch (Exception e) {
            Log.log(e);
        }
    }

    private final class CollectErrorsJob extends Job {

        private Object lock = new Object();
        private File fileFromEditorInput;
        private IEditorInput iEditorInput;
        private String docContents;
        private ILiClipseErrorCollector errorCollector;

        private CollectErrorsJob(String name) {
            super(name);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            File temp0;
            String temp1;
            ILiClipseErrorCollector temp2;
            IEditorInput temp3;
            synchronized (lock) {
                temp0 = fileFromEditorInput;
                temp1 = docContents;
                temp2 = errorCollector;
                temp3 = iEditorInput;
            }
            final List<Map<String, Object>> markerAttributes = collectHandlingErrors(temp3, temp0, temp1, temp2);
            if (markerAttributes == null) {
                return Status.OK_STATUS;
            }

            RunInUiThread.async(new Runnable() {

                public void run() {
                    if (disposed) {
                        return;
                    }
                    setErrors(getDocument(), markerAttributes);
                }
            });
            return Status.OK_STATUS;
        }

        private List<Map<String, Object>> collectHandlingErrors(IEditorInput temp3, File temp0, String temp1,
                ILiClipseErrorCollector temp2) {
            try {
                return collectErrors(temp3, temp0, temp1, temp2);
            } catch (Exception e) {
                Log.log("Error collecting errors at: " + temp0, e);

                ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(IMarker.MESSAGE, "Error analyzing file: " + e.getMessage());
                map.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                map.put(IMarker.LINE_NUMBER, 1);
                map.put(IMarker.TRANSIENT, true);
                arrayList.add(map);
                return arrayList;
            }
        }

        public void setInput(IEditorInput iEditorInput, File fileFromEditorInput, String docContents,
                ILiClipseErrorCollector errorCollector) {

            synchronized (lock) {
                this.iEditorInput = iEditorInput;
                this.fileFromEditorInput = fileFromEditorInput;
                this.docContents = docContents;
                this.errorCollector = errorCollector;
            }
        }
    }

    CollectErrorsJob collectErrorsJob = new CollectErrorsJob("Collect errors");

    private void collectErrors(ILiClipseErrorCollector errorCollector) {
        collectErrorsJob.setInput(getEditorInput(), EditorUtils.getFileFromEditorInput(getEditorInput()), getDocument()
                .get(),
                errorCollector);
        collectErrorsJob.schedule();
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            clearMarkers();
        } catch (Exception e) {
            Log.log(e);
        }
    }

    private void clearMarkers() throws CoreException {
        if (isExternal) {
            IAnnotationModel model = getAnnotationModel();
            if (model != null) { //on dispose can be null
                IAnnotationModelExtension modelExtension = (IAnnotationModelExtension) model;
                modelExtension.replaceAnnotations(currentAnnotations, new HashMap()); //Remove them
            }
            currentAnnotations = null;
        } else {
            if (resource.exists()) {
                resource.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
            }
        }

    }

    private void setErrors(IDocument initialDocument, List<Map<String, Object>> markerAttributes) {
        final IDocument unsynchedDocument = new Document(initialDocument.get());
        if (isExternal) {
            try {
                clearMarkers();
            } catch (CoreException e) {
                Log.log(e);
            }
            IAnnotationModel model = getAnnotationModel();
            if (model == null) {
                currentAnnotations = null;
                return;
            }
            IAnnotationModelExtension modelExtension = (IAnnotationModelExtension) model;

            int size = markerAttributes.size();
            Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();
            for (int i = 0; i < size; i++) {
                Map<String, Object> map = markerAttributes.get(i);
                updateMapWithCharStartAndEnd(unsynchedDocument, map);

                InMemoryMarker marker = new InMemoryMarker(map);
                MarkerAnnotation newAnnotation = new MarkerAnnotation(marker);
                Position pos = PyMarkerUtils.getMarkerPosition(unsynchedDocument, marker, model);
                if (pos != null) {
                    annotationsToAdd.put(newAnnotation, pos);
                }
            }
            currentAnnotations = annotationsToAdd.keySet().toArray(new MarkerAnnotation[annotationsToAdd.size()]);

            Annotation[] empty = new Annotation[0];
            modelExtension.replaceAnnotations(empty, annotationsToAdd);
        } else {
            try {
                final Map[] markersInfo = markerAttributes.toArray(new Map[markerAttributes.size()]);
                IWorkspaceRunnable r = new IWorkspaceRunnable() {
                    @SuppressWarnings("unchecked")
                    public void run(IProgressMonitor monitor) throws CoreException {
                        try {
                            clearMarkers();
                        } catch (CoreException e) {
                            Log.log(e);
                        }
                        for (Map attributes : markersInfo) {
                            IMarker marker = resource.createMarker(IMarker.PROBLEM);
                            updateMapWithCharStartAndEnd(unsynchedDocument, attributes);
                            marker.setAttributes(attributes);
                        }
                    }
                };

                resource.getWorkspace().run(r, null, IWorkspace.AVOID_UPDATE, null);
            } catch (Exception e) {
                Log.log(e);
            }

        }
    }

    private void updateMapWithCharStartAndEnd(IDocument document, Map<String, Object> map) {
        Object object = map.get(IMarker.LINE_NUMBER);
        Object start = map.get(IMarker.CHAR_START);
        Object end = map.get(IMarker.CHAR_END);
        if (object instanceof Integer && start == null && end == null) {
            Integer line = (Integer) object;
            try {
                IRegion lineInformation = document.getLineInformation(line - 1);
                if (lineInformation.getLength() > 0) {
                    map.put(IMarker.CHAR_START, lineInformation.getOffset());
                    map.put(IMarker.CHAR_END, lineInformation.getOffset() + lineInformation.getLength());
                }
            } catch (Exception e) {
                Log.log(e);
            }
        }
    }

}
