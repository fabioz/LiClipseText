package org.brainwy.liclipsetext.editor.common.completions;

import org.brainwy.liclipsetext.editor.common.ILiClipseEditor;
import org.brainwy.liclipsetext.editor.common.ILiClipseSourceViewer;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

public class DummyViewer implements ITextViewer, ILiClipseSourceViewer {

    public class DummyLiClipseEditor implements ILiClipseEditor {

        public IDocument getDocument() {
            return fDoc;
        }

        public LiClipseLanguage getLiClipseLanguage() {
            if (fLanguage == null) {
                throw new RuntimeException("test must set fLanguage");
            }
            return fLanguage;
        }

        public TextSelectionUtils createTextSelectionUtils() {
            Point range = getSelectedRange();
            return new TextSelectionUtils(fDoc, new TextSelection(fDoc, range.x, range.y - range.x));
        }

    }

    public class DummySelectionProvider implements ISelectionProvider {

        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            throw new RuntimeException("not implemented");
        }

        public ISelection getSelection() {
            return new TextSelection(fSelectedRange.x, fSelectedRange.y);
        }

        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            throw new RuntimeException("not implemented");
        }

        public void setSelection(ISelection selection) {
            throw new RuntimeException("not implemented");
        }

    }

    private Point fSelectedRange;
    public IDocument fDoc;
    public LiClipseLanguage fLanguage;

    public StyledText getTextWidget() {
        throw new RuntimeException("not implemented");
    }

    public void setUndoManager(IUndoManager undoManager) {
        throw new RuntimeException("not implemented");
    }

    public void setTextDoubleClickStrategy(ITextDoubleClickStrategy strategy, String contentType) {
        throw new RuntimeException("not implemented");
    }

    public void setAutoIndentStrategy(IAutoIndentStrategy strategy, String contentType) {
        throw new RuntimeException("not implemented");
    }

    public void setTextHover(ITextHover textViewerHover, String contentType) {
        throw new RuntimeException("not implemented");
    }

    public void activatePlugins() {
        throw new RuntimeException("not implemented");
    }

    public void resetPlugins() {
        throw new RuntimeException("not implemented");
    }

    public void addViewportListener(IViewportListener listener) {
        throw new RuntimeException("not implemented");
    }

    public void removeViewportListener(IViewportListener listener) {
        throw new RuntimeException("not implemented");
    }

    public void addTextListener(ITextListener listener) {
        throw new RuntimeException("not implemented");
    }

    public void removeTextListener(ITextListener listener) {
        throw new RuntimeException("not implemented");
    }

    public void addTextInputListener(ITextInputListener listener) {
        throw new RuntimeException("not implemented");
    }

    public void removeTextInputListener(ITextInputListener listener) {
        throw new RuntimeException("not implemented");
    }

    public void setDocument(IDocument document) {
        throw new RuntimeException("not implemented");
    }

    public IDocument getDocument() {
        if (this.fDoc == null) {
            throw new RuntimeException("fDoc must be set in test.");
        }
        return this.fDoc;
    }

    public void setEventConsumer(IEventConsumer consumer) {
        throw new RuntimeException("not implemented");
    }

    public void setEditable(boolean editable) {
        throw new RuntimeException("not implemented");
    }

    public boolean isEditable() {
        throw new RuntimeException("not implemented");
    }

    public void setDocument(IDocument document, int modelRangeOffset, int modelRangeLength) {
        throw new RuntimeException("not implemented");
    }

    public void setVisibleRegion(int offset, int length) {
        throw new RuntimeException("not implemented");
    }

    public void resetVisibleRegion() {
        throw new RuntimeException("not implemented");
    }

    public IRegion getVisibleRegion() {
        throw new RuntimeException("not implemented");
    }

    public boolean overlapsWithVisibleRegion(int offset, int length) {
        throw new RuntimeException("not implemented");
    }

    public void changeTextPresentation(TextPresentation presentation, boolean controlRedraw) {
        throw new RuntimeException("not implemented");
    }

    public void invalidateTextPresentation() {
        throw new RuntimeException("not implemented");
    }

    public void setTextColor(Color color) {
        throw new RuntimeException("not implemented");
    }

    public void setTextColor(Color color, int offset, int length, boolean controlRedraw) {
        throw new RuntimeException("not implemented");
    }

    public ITextOperationTarget getTextOperationTarget() {
        throw new RuntimeException("not implemented");
    }

    public IFindReplaceTarget getFindReplaceTarget() {
        throw new RuntimeException("not implemented");
    }

    public void setDefaultPrefixes(String[] defaultPrefixes, String contentType) {
        throw new RuntimeException("not implemented");
    }

    public void setIndentPrefixes(String[] indentPrefixes, String contentType) {
        throw new RuntimeException("not implemented");
    }

    public void setSelectedRange(int offset, int length) {
        this.fSelectedRange = new Point(offset, length);
    }

    /**
     * <code>Point</code> with x as the offset and y as the length of the current selection
     */
    public Point getSelectedRange() {
        if (this.fSelectedRange == null) {
            throw new RuntimeException("fSelectedRange must be set in test.");
        }
        return this.fSelectedRange;
    }

    public ISelectionProvider getSelectionProvider() {
        return new DummySelectionProvider();
    }

    public void revealRange(int offset, int length) {
        throw new RuntimeException("not implemented");
    }

    public void setTopIndex(int index) {
        throw new RuntimeException("not implemented");
    }

    public int getTopIndex() {
        throw new RuntimeException("not implemented");
    }

    public int getTopIndexStartOffset() {
        throw new RuntimeException("not implemented");
    }

    public int getBottomIndex() {
        throw new RuntimeException("not implemented");
    }

    public int getBottomIndexEndOffset() {
        throw new RuntimeException("not implemented");
    }

    public int getTopInset() {
        throw new RuntimeException("not implemented");
    }

    public ILiClipseEditor getLiClipseEditor() {
        return new DummyLiClipseEditor();
    }

}
