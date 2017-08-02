package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
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
import org.eclipse.jface.text.SlaveDocumentEvent;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/**
 * A text viewer with the minimum structure to work with a presentation reconciler.
 */
public final class DummyTextViewer implements ITextViewer {
    private final IDocument document;
    public final List<TextPresentation> appliedPresentations = new ArrayList<TextPresentation>();

    protected class WidgetCommand {

        /** The document event encapsulated by this command. */
        public DocumentEvent event;
        /** The start of the event. */
        public int start;
        /** The length of the event. */
        public int length;
        /** The inserted and replaced text segments of <code>event</code>. */
        public String text;
        /** The replaced text segments of <code>event</code>. */
        public String preservedText;

        /**
         * Translates a document event into the presentation coordinates of this text viewer.
         *
         * @param e the event to be translated
         */
        public void setEvent(DocumentEvent e) {

            event = e;

            start = e.getOffset();
            length = e.getLength();
            text = e.getText();

            if (length != 0) {
                try {

                    if (e instanceof SlaveDocumentEvent) {
                        SlaveDocumentEvent slave = (SlaveDocumentEvent) e;
                        DocumentEvent master = slave.getMasterEvent();
                        if (master != null) {
                            preservedText = master.getDocument().get(master.getOffset(), master.getLength());
                        }
                    } else {
                        preservedText = e.getDocument().get(e.getOffset(), e.getLength());
                    }

                } catch (BadLocationException x) {
                    preservedText = null;
                    Log.log(x);
                }
            } else {
                preservedText = null;
            }
        }
    }

    class VisibleDocumentListener implements IDocumentListener {

        /*
         * @see IDocumentListener#documentAboutToBeChanged
         */
        @Override
        public void documentAboutToBeChanged(DocumentEvent e) {
            fWidgetCommand.setEvent(e);
        }

        /*
         * @see IDocumentListener#documentChanged
         */
        @Override
        public void documentChanged(DocumentEvent e) {
            updateTextListeners(fWidgetCommand);
        }
    }

    private static class DummyTextEvent extends TextEvent {

        public DummyTextEvent(int offset, int length, String text, String replacedText, DocumentEvent event,
                boolean viewerRedrawState) {
            super(offset, length, text, replacedText, event, viewerRedrawState);
        }

    }

    protected void updateTextListeners(WidgetCommand cmd) {
        List<ITextListener> textListeners = fTextListeners;
        if (textListeners != null) {
            textListeners = new ArrayList<>(textListeners);
            DocumentEvent event = cmd.event;
            if (event instanceof SlaveDocumentEvent) {
                event = ((SlaveDocumentEvent) event).getMasterEvent();
            }

            TextEvent e = new DummyTextEvent(cmd.start, cmd.length, cmd.text, cmd.preservedText, event, true);
            for (int i = 0; i < textListeners.size(); i++) {
                ITextListener l = textListeners.get(i);
                l.textChanged(e);
            }
        }
    }

    private WidgetCommand fWidgetCommand = new WidgetCommand();
    private VisibleDocumentListener fVisibleDocumentListener;

    public DummyTextViewer(IDocument document) {
        this.document = document;
        fVisibleDocumentListener = new VisibleDocumentListener();
        document.addDocumentListener(this.fVisibleDocumentListener);
    }

    @Override
    public void setVisibleRegion(int offset, int length) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setUndoManager(IUndoManager undoManager) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setTopIndex(int index) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setTextHover(ITextHover textViewerHover, String contentType) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setTextDoubleClickStrategy(ITextDoubleClickStrategy strategy, String contentType) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setTextColor(Color color, int offset, int length, boolean controlRedraw) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setTextColor(Color color) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setSelectedRange(int offset, int length) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setIndentPrefixes(String[] indentPrefixes, String contentType) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setEventConsumer(IEventConsumer consumer) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setEditable(boolean editable) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setDocument(IDocument document, int modelRangeOffset, int modelRangeLength) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setDocument(IDocument document) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setDefaultPrefixes(String[] defaultPrefixes, String contentType) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setAutoIndentStrategy(IAutoIndentStrategy strategy, String contentType) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void revealRange(int offset, int length) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void resetVisibleRegion() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void resetPlugins() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void removeViewportListener(IViewportListener listener) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void removeTextListener(ITextListener listener) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void removeTextInputListener(ITextInputListener listener) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean overlapsWithVisibleRegion(int offset, int length) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isEditable() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void invalidateTextPresentation() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public IRegion getVisibleRegion() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getTopInset() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getTopIndexStartOffset() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getTopIndex() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StyledText getTextWidget() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ITextOperationTarget getTextOperationTarget() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Point getSelectedRange() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public IFindReplaceTarget getFindReplaceTarget() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public IDocument getDocument() {
        return document;
    }

    @Override
    public int getBottomIndexEndOffset() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getBottomIndex() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void changeTextPresentation(TextPresentation presentation, boolean controlRedraw) {
        this.appliedPresentations.add(presentation);
    }

    @Override
    public void addViewportListener(IViewportListener listener) {
        throw new RuntimeException("Not implemented");
    }

    List<ITextListener> fTextListeners = new ArrayList<>();

    @Override
    public void addTextListener(ITextListener listener) {
        fTextListeners.add(listener);
    }

    ListenerList<ITextInputListener> textInputListeners = new ListenerList<>();

    @Override
    public void addTextInputListener(ITextInputListener listener) {
        textInputListeners.add(listener);
    }

    @Override
    public void activatePlugins() {
        throw new RuntimeException("Not implemented");
    }
}