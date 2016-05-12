/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.outline;

import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.brainwy.liclipsetext.editor.common.LiClipseEditor;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.brainwy.liclipsetext.shared_core.editor.IBaseEditor;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.model.ErrorDescription;
import org.brainwy.liclipsetext.shared_core.model.ISimpleNode;
import org.brainwy.liclipsetext.shared_core.structure.TreeNode;
import org.brainwy.liclipsetext.shared_ui.outline.BaseModel;
import org.brainwy.liclipsetext.shared_ui.outline.IParsedItem;

public class LiClipseParsedModel extends BaseModel {

    public LiClipseParsedModel(IBaseEditor editor) {
        super(editor);
    }

    @Override
    protected IParsedItem createInitialRootFromEditor() {
        if (editor instanceof LiClipseEditor) {
            LiClipseEditor liClipseEditor = (LiClipseEditor) editor;
            return createParsedItemFromSimpleNode(liClipseEditor.getAst());
        }

        Log.log("Expected LiClipseEditor. Found:" + editor.getClass());
        return new LiClipseParsedItem(new LiClipseNode[0], null);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected IParsedItem createParsedItemFromSimpleNode(ISimpleNode ast) {
        if (ast == null) {
            return new LiClipseParsedItem(new LiClipseNode[0], null);
        }
        LiClipseNode node = (LiClipseNode) ast;
        List<TreeNode/*OutlineData*/> children = node.getChildren();
        return new LiClipseParsedItem(children.toArray(new LiClipseNode[children.size()]), null);
    }

    public ISimpleNode[] getSelectionPosition(StructuredSelection sel) {
        if (sel.size() == 1) { // only sync the editing view if it is a single-selection
            Object firstElement = sel.getFirstElement();
            LiClipseNode node = ((LiClipseParsedItem) firstElement).getNode();
            return new ISimpleNode[] { node };
        }
        return null;
    }

    @Override
    protected IParsedItem duplicateRootAddingError(ErrorDescription errorDesc) {
        IParsedItem currRoot = getRoot();

        IParsedItem newRoot;
        if (currRoot != null) {
            newRoot = new LiClipseParsedItem(((LiClipseParsedItem) currRoot).getNodeChildren(), errorDesc);
            newRoot.updateTo(currRoot);
        } else {
            newRoot = new LiClipseParsedItem(new LiClipseNode[0], errorDesc);
        }
        return newRoot;
    }

}
