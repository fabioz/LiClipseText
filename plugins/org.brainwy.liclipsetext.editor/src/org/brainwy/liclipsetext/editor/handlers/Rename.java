/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;

public class Rename extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {

        try {
            ITextEditor editor = EditorUtils.getActiveEditor();
            if (!(editor instanceof BaseLiClipseEditor)) {
                return null;
            }
            BaseLiClipseEditor liClipseEditor = (BaseLiClipseEditor) editor;
            TextSelectionUtils ts = liClipseEditor.createTextSelectionUtils();
            LinkedPositionGroup group = new LinkedPositionGroup();
            Tuple<String, Integer> currToken = ts.getCurrToken();
            if (currToken == null || currToken.o1.length() == 0) {
                return null;
            }
            List<IRegion> occurrences = ts.searchOccurrences(currToken.o1);
            if (occurrences.size() == 0) {
                return null;
            }

            List<IRegion> addLater = new ArrayList<IRegion>();

            int sequence = 0;
            for (IRegion iRegion : occurrences) {
                int offset = iRegion.getOffset();
                if (offset < currToken.o2) {
                    addLater.add(iRegion);
                    continue;
                }
                LinkedPosition position = new LinkedPosition(ts.getDoc(), offset, iRegion.getLength(),
                        sequence);
                group.addPosition(position);
                sequence += 1;
            }

            for (IRegion iRegion : addLater) {
                LinkedPosition position = new LinkedPosition(ts.getDoc(), iRegion.getOffset(), iRegion.getLength(),
                        sequence);
                group.addPosition(position);
                sequence += 1;
            }

            LinkedModeModel model = new LinkedModeModel();
            model.addGroup(group);
            model.forceInstall();
            final LinkedModeUI ui = new EditorLinkedModeUI(model, liClipseEditor.getEditorSourceViewer());
            ui.setCyclingMode(LinkedModeUI.CYCLE_ALWAYS);
            ui.setExitPosition(liClipseEditor.getEditorSourceViewer(), ts.getAbsoluteCursorOffset(), 0,
                    0);
            ui.enter();
        } catch (Exception e) {
            Log.log(e);
        }
        return null;
    }
}
