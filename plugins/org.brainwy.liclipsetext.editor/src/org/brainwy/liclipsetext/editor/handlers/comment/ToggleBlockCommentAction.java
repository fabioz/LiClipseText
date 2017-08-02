/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers.comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.IAcceptPartition;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;

public class ToggleBlockCommentAction {

    private TextSelectionUtils ts;
    private String commentStart;
    private String commentEnd;
    private String scope;
    private int spaces;
    private LiClipseLanguage language;

    public ToggleBlockCommentAction(TextSelectionUtils ts, String commentStart,
            String commentEnd, LiClipseLanguage language, int spaces) {
        this.ts = ts;
        this.commentStart = commentStart;
        this.commentEnd = commentEnd;
        this.scope = language.getComment().scope;
        this.language = language;
        this.spaces = spaces;
    }

    public Tuple<Integer, Integer> execute() throws BadLocationException {
        String spacesStr = new FastStringBuffer(spaces).appendN(' ', spaces).toString();
        IDocument doc = ts.getDoc();
        if (scope == null) {
            // text mate bundle doesn't say the comment scope.
            // this implementation is a bit more naive: if it finds the start
            selectCompleteLine(doc);
            String selectedText = ts.getSelectedText();
            String trimmed = selectedText.trim();
            FastStringBuffer buf = new FastStringBuffer(selectedText, 10);

            if (trimmed.startsWith(this.commentStart) && trimmed.endsWith(this.commentEnd)) {
                if (trimmed.startsWith(commentStart + " ")) {
                    buf.replaceFirst(commentStart + " ", "");
                } else {
                    buf.replaceFirst(commentStart, "");
                }
                if (trimmed.endsWith(" " + commentEnd)) {
                    buf.replaceLast(" " + commentEnd, "");
                } else {
                    buf.replaceLast(commentEnd, "");
                }

                doc.replace(ts.getAbsoluteCursorOffset(), ts.getSelLength(), buf.toString());
                return new Tuple<Integer, Integer>(ts.getAbsoluteCursorOffset(),
                        ts.getSelLength() - (commentStart.length() + commentEnd.length()));
            } else {
                return comment(doc, spacesStr);
            }
        }

        List<TypedPart> commentPartitionsFound = DocumentTimeStampChangedException.retryUntilNoDocChanges(() -> {
            SubPartitionCodeReader reader = new SubPartitionCodeReader();
            final int initialOffset = ts.getAbsoluteCursorOffset();

            final String startWith = scope + ".";
            final String endWith = "." + scope;
            IAcceptPartition filter = new IAcceptPartition() {

                @Override
                public boolean getRequireOnlyTop() {
                    return false;
                }

                @Override
                public boolean accept(TypedPart typedPart) {
                    return typedPart.type.startsWith(startWith) || typedPart.type.endsWith(endWith);
                }

                @Override
                public boolean accept(TypedPosition typedPosition) {
                    return true;
                }
            };
            reader.configurePartitions(true, doc, initialOffset, filter);

            //reader.configurePartitions(true, doc, initialOffset, this.scope,
            //        SwitchLanguageToken.createSubLanguageContentType("this", this.scope),
            //        SwitchLanguageToken.createSubLanguageContentType(language.name.toLowerCase(), this.scope)
            //        );

            TypedPart part = reader.read();
            int endOffset = initialOffset + ts.getSelLength();

            List<TypedPart> ret = new ArrayList<TypedPart>();
            while (part != null) {
                if (part.offset > endOffset) {
                    break;
                }
                if (part.offset <= initialOffset || part.offset + part.length >= initialOffset) {
                    ret.add(part);
                }
                part = reader.read();
            }
            return ret;
        });

        final int size = commentPartitionsFound.size();

        //No comment partition: comment it.
        if (size == 0) {
            return comment(doc, spacesStr);
        } else {
            //Our current selection has some comment.

            final int selectionStart = commentPartitionsFound.get(0).offset;
            TypedPart last = commentPartitionsFound.get(size - 1);
            int selectionEnd = last.offset + last.length;

            Collections.reverse(commentPartitionsFound); //we must replace it backwards

            for (int i = 0; i < size; i++) {
                TypedPart typedPart = commentPartitionsFound.get(i);
                FastStringBuffer contents = new FastStringBuffer(doc.get(typedPart.offset, typedPart.length), 0);
                int newEnd = removeStartEnd(selectionEnd, contents, commentStart, commentEnd, true);
                if (newEnd != selectionEnd) {
                    selectionEnd = newEnd;
                    selectionEnd = removeStartEnd(selectionEnd, contents, spacesStr, spacesStr, false);
                }
                doc.replace(typedPart.offset, typedPart.length, contents.toString());
            }
            return new Tuple<Integer, Integer>(selectionStart, selectionEnd - selectionStart);
        }
    }

    private Tuple<Integer, Integer> comment(IDocument doc, String spacesStr) throws BadLocationException {
        // Select the complete line if we have no selection at this point.
        int selLength = ts.getSelLength();
        String selectedText;
        if (selLength == 0) {
            selectCompleteLine(doc);
        } else {
            selectCompleteLine(doc);
            selectedText = ts.getSelectedText();

            if (selectedText.endsWith("\r\n")) {
                ts.setSelection(ts.getAbsoluteCursorOffset(), ts.getAbsoluteCursorOffset() + ts.getSelLength() - 2);
                selectedText = ts.getSelectedText();

            } else if (selectedText.endsWith("\r") || selectedText.endsWith("\n")) {
                ts.setSelection(ts.getAbsoluteCursorOffset(), ts.getAbsoluteCursorOffset() + ts.getSelLength() - 1);
                selectedText = ts.getSelectedText();
            }
        }
        selectedText = ts.getSelectedText();

        String replacementText = new FastStringBuffer(commentStart, selectedText.length() + commentEnd.length() + 8)
                .append(spacesStr).append(selectedText).append(spacesStr).append(commentEnd).toString();

        doc.replace(ts.getAbsoluteCursorOffset(), ts.getSelLength(), replacementText);

        return new Tuple<Integer, Integer>(ts.getAbsoluteCursorOffset() + commentStart.length()
                + spacesStr.length(),
                replacementText.length()
                        - (commentEnd.length() + spacesStr.length() + commentStart.length() + spacesStr.length()));
    }

    private void selectCompleteLine(IDocument doc) throws BadLocationException {
        if (this.ts.getSelLength() == 0 || this.ts.getStartLineIndex() != this.ts.getEndLineIndex()) {
            //If it does not span multiple lines, just comment what's selected
            this.ts.selectCompleteLine();
            int firstCharPosition = TextSelectionUtils.getFirstCharPosition(doc, this.ts.getAbsoluteCursorOffset());
            int selLen = this.ts.getSelLength();
            if (selLen > 0) {
                this.ts.setSelection(firstCharPosition, this.ts.getAbsoluteCursorOffset() + selLen);
            }
        }
    }

    private static int removeStartEnd(int selectionEnd, FastStringBuffer contents, String start, String end,
            boolean requireBoth) {
        if (requireBoth) {
            if (contents.startsWith(start) && contents.endsWith(end)) {
                contents.deleteFirstChars(start.length());
                contents.deleteLastChars(end.length());
                selectionEnd -= start.length();
                selectionEnd -= end.length();
            }
        } else {
            if (contents.startsWith(start)) {
                contents.deleteFirstChars(start.length());
                selectionEnd -= start.length();
            }
            if (contents.endsWith(end)) {
                contents.deleteLastChars(end.length());
                selectionEnd -= end.length();
            }
        }
        return selectionEnd;
    }
}
