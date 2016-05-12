/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.utils.BaseExtensionHelper;
import org.brainwy.liclipsetext.shared_ui.actions.ShiftLeftAction;
import org.brainwy.liclipsetext.shared_ui.editor.BaseSourceViewer;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public class LiClipseSourceViewer extends BaseSourceViewer implements ILiClipseSourceViewer, IAdaptable {

    private LiClipseSourceViewerConfiguration fConfiguration;

    private boolean configured = false;
    private boolean hasDoc = false;

    private BaseLiClipseEditor liClipseEditor;

    public LiClipseSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
            boolean showAnnotationsOverview, int styles, final BaseLiClipseEditor liClipseEditor) {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
        this.liClipseEditor = liClipseEditor;
    }

    //Note: by using it we get a lot of flicker, so, removed it for the time being.
    //    @Override
    //    protected StyledText createTextWidget(Composite parent, int styles) {
    //        StyledText ret = super.createTextWidget(parent, styles);
    //        try {
    //            ret.setAlwaysShowScrollBars(false);
    //        } catch (Throwable e) {
    //            //Only there from 4.3 onwards
    //        }
    //        return ret;
    //    }

    public BaseLiClipseEditor getLiClipseEditor() {
        return liClipseEditor;
    }

    @Override
    public void configure(SourceViewerConfiguration configuration) {
        this.fConfiguration = (LiClipseSourceViewerConfiguration) configuration;
        if (!configured && hasDoc) {
            configured = true;
            super.configure(configuration);
        }
    }

    @Override
    public void unconfigure() {
        if (configured) {
            configured = false;
            super.unconfigure();
        }
    }

    @Override
    public void setDocument(IDocument document, IAnnotationModel annotationModel, int modelRangeOffset,
            int modelRangeLength) {
        try {
            this.unconfigure();

            fConfiguration.setDocument(document);
            hasDoc = false;
            if (document != null) {
                hasDoc = true;
                this.configure(fConfiguration);
            }
            super.setDocument(document, annotationModel, modelRangeOffset, modelRangeLength);
        } catch (Exception e) {
            Log.log(e);
        }
    }

    @Override
    public void doOperation(int operation) {
        if (operation == SHIFT_LEFT) {
            doShiftLeft();
            return;
        }
        super.doOperation(operation);
    }

    /**
     * Override the shift left because the default doesn't seem to work that well...
     */
    private void doShiftLeft() {
        if (fUndoManager != null) {
            fUndoManager.beginCompoundChange();
        }

        IDocument d = getDocument();
        DocumentRewriteSession rewriteSession = null;
        try {
            if (d instanceof IDocumentExtension4) {
                IDocumentExtension4 extension = (IDocumentExtension4) d;
                rewriteSession = extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
            }
            BaseLiClipseEditor editor = this.getLiClipseEditor();
            try {
                ShiftLeftAction.perform(editor.createTextSelectionUtils(), editor.getTabWidth());
            } catch (BadLocationException e) {
                Log.log(e);
            }

        } finally {

            if (d instanceof IDocumentExtension4) {
                IDocumentExtension4 extension = (IDocumentExtension4) d;
                extension.stopRewriteSession(rewriteSession);
            }

            if (fUndoManager != null) {
                fUndoManager.endCompoundChange();
            }
        }
    }

    @Override
    public Object getAdapter(Class adapter) {
        return this.liClipseEditor.getAdapter(adapter);
    }

    @Override
    protected StyledText createTextWidget(Composite parent, int styles) {
        try {
        	ILiClipseEditorCustomizer editorCustomizer = (ILiClipseEditorCustomizer) BaseExtensionHelper.getParticipant(
        			"org.brainwy.liclipsetext.editor.liclipse_editor_customizer", false);
        	if(editorCustomizer != null){
        		StyledText styledText = editorCustomizer.createTextWidget(this, parent, styles);
        		if(styledText != null){
        			return styledText;
        		}
        	}
		} catch (Exception e) {
			Log.log(e);
		}
    	return super.createTextWidget(parent, styles);
    }

}
