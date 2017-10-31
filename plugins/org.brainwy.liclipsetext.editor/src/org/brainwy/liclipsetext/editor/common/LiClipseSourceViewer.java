/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.utils.ArrayUtils;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
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
            if (editorCustomizer != null) {
                StyledText styledText = editorCustomizer.createTextWidget(this, parent, styles);
                if (styledText != null) {
                    return styledText;
                }
            }
        } catch (Exception e) {
            Log.log(e);
        }

        StyledText styledText = new StyledTextImproved(parent, styles);
        styledText.setLeftMargin(Math.max(styledText.getLeftMargin(), 2));
        return styledText;
    }

    public final static class StyledTextImproved extends StyledText {

        public StyledTextImproved(Composite parent, int style) {
            super(parent, style);
        }

        /**
         * Optimization:
         * The method:
         * org.eclipse.swt.custom.StyledTextRenderer.setStyleRanges(int[], StyleRange[])
         *
         * is *extremely* inefficient on huge documents with lots of styles when
         * ranges are not passed and have to be computed in the block:
         *
         * if (newRanges == null && COMPACT_STYLES) {
         *
         * So, we just pre-create the ranges here (Same thing on org.brainwy.liclipsetext.editor.common.LiClipseSourceViewer.StyledTextImproved)
         * A patch should later be given to SWT itself.
         */

        @Override
        public void setStyleRanges(StyleRange[] styles) {
            if (styles != null) {
                RangesInfo rangesInfo = createRanges(styles, this.getCharCount());
                int[] newRanges = rangesInfo.newRanges;
                styles = rangesInfo.styles;
                super.setStyleRanges(newRanges, styles);
                return;
            }
            super.setStyleRanges(styles);
        }

        @Override
        public void replaceStyleRanges(int start, int length, StyleRange[] styles) {
            checkWidget();
            if (isListening(ST.LineGetStyle)) {
                return;
            }
            if (styles == null) {
                SWT.error(SWT.ERROR_NULL_ARGUMENT);
            }
            RangesInfo rangesInfo = createRanges(styles, this.getCharCount());
            int[] newRanges = rangesInfo.newRanges;
            styles = rangesInfo.styles;
            try {
                setStyleRanges(start, length, newRanges, styles);
            } catch (Exception e) {
                Log.log(e);
            }
        }

        @Override
        public void redraw() {
            try {
                super.redraw();
            } catch (Exception e) {
                Log.log(e);
            }
        }

        public static class RangesInfo {

            public final StyleRange[] styles;
            public final int[] newRanges;

            public RangesInfo(StyleRange[] styles, int[] newRanges) {
                this.styles = styles;
                this.newRanges = newRanges;
            }
        }

        public static RangesInfo createRanges(StyleRange[] styles, int charCount) throws AssertionError {

            int[] newRanges = new int[styles.length << 1];
            int removeRangesFrom = -1;
            List<Integer> removeRanges = new ArrayList<>();

            int endOffset = -1;
            int i = 0, j = 0;
            for (; i < styles.length; i++) {
                StyleRange newStyle = styles[i];
                if (newStyle.start >= charCount) {
                    Log.log("Removing ranges past end.");
                    removeRangesFrom = i;
                    break;
                }
                if (endOffset > newStyle.start) {
                    String msg = "Error endOffset (" + endOffset + ") > next style start (" + newStyle.start + ")";
                    Log.log(msg);
                    int diff = endOffset - newStyle.start;
                    newStyle.start = endOffset;
                    newStyle.length -= diff;
                    if (newStyle.length < 0) {
                        // Unable to fix it (remove element).
                        removeRanges.add(i);
                        continue;
                    }
                }

                endOffset = newStyle.start + newStyle.length;
                if (endOffset > charCount) {
                    String msg = "Error endOffset (" + endOffset + ") > charCount (" + charCount + ")";
                    Log.log(msg);
                    newStyle.length -= endOffset - charCount;
                    if (newStyle.length < 0) {
                        Log.log("Removing ranges past end.");
                        removeRangesFrom = i;
                        break;
                    }
                }

                newRanges[j++] = newStyle.start;
                newRanges[j++] = newStyle.length;
            }
            if (j < newRanges.length - 1) {
                int[] reallocate = new int[j];
                System.arraycopy(newRanges, 0, reallocate, 0, j);
                newRanges = reallocate;
            }
            if (removeRangesFrom != -1) {
                StyleRange[] reallocate = new StyleRange[removeRangesFrom];
                System.arraycopy(styles, 0, reallocate, 0, removeRangesFrom);
                styles = reallocate;
            }
            if (removeRanges.size() > 0) {
                Collections.reverse(removeRanges);
            }
            for (int remove : removeRanges) {
                if (remove < styles.length) {
                    styles = ArrayUtils.remove(styles, remove, StyleRange.class);
                }
            }
            return new RangesInfo(styles, newRanges);
        }
    }
}
