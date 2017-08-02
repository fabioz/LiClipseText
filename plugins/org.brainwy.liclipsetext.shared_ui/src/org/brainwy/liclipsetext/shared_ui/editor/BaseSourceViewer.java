/**
 * Copyright (c) 2014 by Brainwy Software LTDA. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.shared_ui.editor;

import java.util.Arrays;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public abstract class BaseSourceViewer extends ProjectionViewer implements ITextViewerExtensionAutoEditions {

    private boolean autoEditionsEnabled = true;

    public BaseSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
            boolean showAnnotationsOverview, int styles) {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);

        StyledText styledText = this.getTextWidget();
        styledText.setLeftMargin(Math.max(styledText.getLeftMargin(), 2));
    }

    @Override
    public boolean getAutoEditionsEnabled() {
        return autoEditionsEnabled;
    }

    @Override
    public void setAutoEditionsEnabled(boolean b) {
        this.autoEditionsEnabled = b;
    }

    @Override
    protected Layout createLayout() {
        //Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=438641
        return new RulerLayout(GAP_SIZE_1) {
            @Override
            protected void layout(Composite composite, boolean flushCache) {
                StyledText textWidget = getTextWidget();
                if (textWidget == null) {
                    Log.log("Error: textWidget is already null. SourceViewer: " + BaseSourceViewer.this + " control: "
                            + BaseSourceViewer.this.getControl());
                    return;
                }
                super.layout(composite, flushCache);
            }
        };
    }

    public static class KeepRangesUpdated implements IDocumentListener {

        private StyleRange[] savedStyleRanges;
        private IDocument savedDocument;

        public KeepRangesUpdated(StyleRange[] styleRanges, IDocument document) {
            this.savedStyleRanges = styleRanges;
            this.savedDocument = document;
            document.addDocumentListener(this);
        }

        @Override
        public void documentAboutToBeChanged(DocumentEvent event) {
            int replacedChars = event.getLength();
            int offset = event.getOffset();
            int addedChars = event.getText().length();
            //System.out.println(
            //        "Changing doc. Removed: " + replacedChars + " Added: " + addedChars + " Offset: " + offset
            //                + " Doc Len: " + this.savedDocument.getLength());
            updateRanges(offset, replacedChars, addedChars);
        }

        public void updateRanges(final int offset, final int replacedChars, final int addedChars) {
            if ((replacedChars == 0 && addedChars == 0) || offset < 0) {
                return;
            }
            if (savedStyleRanges != null && savedStyleRanges.length > 0) {
                StyleRange searchfor = new StyleRange();
                searchfor.start = offset;
                int foundAt = Arrays.binarySearch(savedStyleRanges, searchfor, (StyleRange a, StyleRange b) -> {
                    return Integer.compare(a.start, b.start);
                });
                if (foundAt < 0) {
                    // Not directly found
                    foundAt = -foundAt;
                    foundAt -= 2;
                }

                if (foundAt >= savedStyleRanges.length) {
                    foundAt = savedStyleRanges.length - 1;
                }

                if (foundAt > 0) {
                    // Go left until we find a non-empty partition
                    int initialFoundAt = foundAt;
                    while (true) {
                        StyleRange changeAtRange = savedStyleRanges[foundAt];
                        if (changeAtRange.length == 0) {
                            foundAt--;
                            if (foundAt == 0) {
                                // Did not find any non-empty partition
                                foundAt = initialFoundAt;
                                break;
                            }
                        } else {
                            // Found a non-empty partition going left
                            break;
                        }
                    }
                }

                if (foundAt > 0) {
                    // Always check from a previous pos as we may need to match the previous partition for insert
                    foundAt--;
                }

                if (foundAt < 0) {
                    foundAt = 0;
                }

                if (replacedChars > 0) {
                    for (int i = foundAt; i < savedStyleRanges.length; i++) {
                        StyleRange changeAtRange = savedStyleRanges[i];
                        if (changeAtRange.length == 0) {
                            // ignore empty partitions
                            continue;
                        }

                        // Removed chars: preference is given for the next partition (delete)
                        if (changeAtRange.start == offset || (changeAtRange.start < offset
                                && ((changeAtRange.start + changeAtRange.length) > offset))) {

                            handleRemoval(changeAtRange, offset, replacedChars, i);
                            // After the removal is done, we need to process additions, which means we have to check again the proper partition
                            // as preference should be given to the previous partition.
                            handleInsertion(foundAt, offset, addedChars);
                            return;
                        }

                        if (changeAtRange.start > offset) {
                            // We had no match for the removal (we're in a gap), thus, add/remove the chars.
                            handleRemoval(changeAtRange, offset, replacedChars, i);
                            handleInsertion(foundAt, offset, addedChars);
                        }

                    }
                } else if (addedChars > 0) {
                    handleInsertion(foundAt, offset, addedChars);
                }
            }
        }

        private void checkConsistent(int startAt) {
            int i = startAt - 4;
            if (i < 0) {
                i = 0;
            }
            int lastEndOffset = -1;
            for (; i < savedStyleRanges.length; i++) {
                StyleRange styleRange = savedStyleRanges[i];
                if (styleRange.length == 0) {
                    continue;
                }
                if (lastEndOffset > styleRange.start) {
                    System.out.println("Something is wrong!");
                }
                lastEndOffset = styleRange.start + styleRange.length;
            }

        }

        private void handleRemoval(StyleRange changeAtRange, int offset, int replacedChars, int i) {
            int ableToFromCurrentRange;
            if (changeAtRange.start > offset) {
                ableToFromCurrentRange = (offset + replacedChars) - changeAtRange.start;
                if (ableToFromCurrentRange > changeAtRange.length) {
                    ableToFromCurrentRange = changeAtRange.length;
                }

                if (ableToFromCurrentRange > 0) {
                    changeAtRange.length -= ableToFromCurrentRange;
                } else {
                    changeAtRange.start -= replacedChars;
                }
                ableToFromCurrentRange = 0;

            } else {
                ableToFromCurrentRange = changeAtRange.length - (offset - changeAtRange.start);
                if (ableToFromCurrentRange >= replacedChars) {
                    // Only remove from the current partition
                    changeAtRange.length -= replacedChars;
                    for (int j = i + 1; j < savedStyleRanges.length; j++) {
                        savedStyleRanges[j].start -= replacedChars;
                    }
                    return;
                }
            }
            // We'll need to remove from the current and next partitions
            int charsToRemove = replacedChars;

            charsToRemove -= ableToFromCurrentRange;
            changeAtRange.length -= ableToFromCurrentRange;
            int currCharsRemoved = ableToFromCurrentRange;

            int j = i + 1;

            for (; j < savedStyleRanges.length && charsToRemove > 0; j++) {
                StyleRange range = savedStyleRanges[j];
                range.start -= currCharsRemoved;

                if (range.length <= charsToRemove) {
                    currCharsRemoved += range.length;
                    charsToRemove -= range.length;
                    range.length = 0;
                } else {
                    currCharsRemoved += charsToRemove;
                    range.length -= charsToRemove;
                    charsToRemove = 0;
                }
            }

            for (; j < savedStyleRanges.length; j++) {
                savedStyleRanges[j].start -= currCharsRemoved;
            }

        }

        private void handleInsertion(final int foundAt, final int offset, final int addedChars) {
            for (int i = foundAt; i < savedStyleRanges.length; i++) {
                StyleRange changeAtRange = savedStyleRanges[i];
                if (changeAtRange.length == 0) {
                    // ignore empty partitions
                    continue;
                }

                // Add chars: preference is given for the previous partition (insert)
                if ((changeAtRange.start == offset && offset == 0) || (changeAtRange.start < offset
                        && ((changeAtRange.start + changeAtRange.length) >= offset))) {

                    changeAtRange.length += addedChars;
                    for (int j = i + 1; j < savedStyleRanges.length; j++) {
                        savedStyleRanges[j].start += addedChars;
                    }
                    return;
                }

                if (changeAtRange.start >= offset) {
                    // We had no match for the addition...
                    for (int j = i; j < savedStyleRanges.length; j++) {
                        savedStyleRanges[j].start += addedChars;
                    }
                }
            }
        }

        @Override
        public void documentChanged(DocumentEvent event) {
            //no-op
        }

        public void dispose() {
            savedDocument.removeDocumentListener(this);
        }

        public StyleRange[] createFinalStyleRanges() {
            int zeroLen = 0;
            final int savedLen = savedStyleRanges.length;
            for (int i = 0; i < savedLen; i++) {
                if (savedStyleRanges[i].length == 0) {
                    zeroLen += 1;
                }
            }
            if (zeroLen == 0) {
                return savedStyleRanges;
            }
            StyleRange[] ret = new StyleRange[savedLen - zeroLen];
            int j = 0;
            for (int i = 0; i < savedLen; i++) {
                if (savedStyleRanges[i].length != 0) {
                    ret[j] = savedStyleRanges[i];
                    j++;
                }
            }
            return ret;
        }
    }

    private KeepRangesUpdated keepRangesUpdated;

    /**
     * When redrawing is re-enabled, the previous ranges are restored to avoid flicker after a rewrite session
     * (as computing the new style ranges may happen on a thread).
     *
     * i.e.: shift+tab for dedent would invalidate the whole coloring because of a rewrite session.
     */
    @Override
    protected void enabledRedrawing(int topIndex) {
        try {
            super.enabledRedrawing(topIndex);

            StyledText textWidget = getTextWidget();
            if (!textWidget.isDisposed() && keepRangesUpdated != null && keepRangesUpdated.savedStyleRanges != null
                    && keepRangesUpdated.savedDocument == this.getDocument()) {
                //System.out.println("Enable redrawing. Doc len: " + keepRangesUpdated.savedDocument.getLength());
                StyleRange[] finalStyleRanges = keepRangesUpdated.createFinalStyleRanges();
                if (finalStyleRanges != null && finalStyleRanges.length > 0) {
                    textWidget.setStyleRanges(finalStyleRanges);
                }
            }
        } finally {
            if (keepRangesUpdated != null) {
                keepRangesUpdated.dispose();
                keepRangesUpdated = null;
            }
        }
    }

    @Override
    protected void disableRedrawing() {
        StyledText textWidget = this.getTextWidget();

        if (keepRangesUpdated != null) {
            keepRangesUpdated.dispose();
            keepRangesUpdated = null;
        }
        if (!textWidget.isDisposed()) {
            IDocument document = this.getDocument();
            keepRangesUpdated = new KeepRangesUpdated(textWidget.getStyleRanges(true), document);
            //System.out.println("\n\nDisable redrawing. Doc len: " + document.getLength());
        }
        super.disableRedrawing();
    }
}
