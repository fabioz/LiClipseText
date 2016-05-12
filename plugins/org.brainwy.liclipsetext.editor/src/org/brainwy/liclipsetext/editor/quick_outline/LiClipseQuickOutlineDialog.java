/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.quick_outline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.OutlineData;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.DataAndImageTreeNode;
import org.brainwy.liclipsetext.shared_core.structure.TreeNode;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.quick_outline.BaseQuickOutlineSelectionDialog;
import org.brainwy.liclipsetext.shared_ui.quick_outline.DataAndImageTreeNodeContentProvider;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class LiClipseQuickOutlineDialog extends BaseQuickOutlineSelectionDialog {

    private final BaseLiClipseEditor liClipseEditor;

    public LiClipseQuickOutlineDialog(Shell shell, BaseLiClipseEditor pyEdit, TreeNode<OutlineData> outline) {
        super(shell, LiClipseTextEditorPlugin.PLUGIN_ID, new LiClipseOutlineLabelProvider(),
                new DataAndImageTreeNodeContentProvider(), false);
        this.liClipseEditor = pyEdit;
        TextSelectionUtils ps = this.liClipseEditor.createTextSelectionUtils();
        startLineIndex = ps.getStartLineIndex() + 1; // +1 because the outline elements start at 1
        setRootAndInitialSelection(outline);
        setInput(root);

        if (initialSelection != null) {
            this.setInitialSelections(new Object[] { initialSelection });
        }
    }

    private void setRootAndInitialSelection(TreeNode<OutlineData> outline) {
        DataAndImageTreeNode<Object> root = new DataAndImageTreeNode<Object>(null, null, null);

        Map<TreeNode, DataAndImageTreeNode<Object>> entryToTreeNode = new HashMap<TreeNode, DataAndImageTreeNode<Object>>();

        List<TreeNode<OutlineData>> flattened = outline.flattenChildren();
        for (Iterator iterator = flattened.iterator(); iterator.hasNext();) {
            TreeNode<OutlineData> next = (TreeNode<OutlineData>) iterator.next();

            DataAndImageTreeNode<Object> n;
            Object nextParent = next.getParent();
            if (nextParent != null && nextParent != outline) {
                DataAndImageTreeNode<Object> parent = entryToTreeNode.get(nextParent);
                if (parent == null) {
                    Log.log("Error: child found before parent!");
                    parent = root;
                }
                n = new DataAndImageTreeNode<Object>(parent, next, next.getData().getImage());

            } else {
                n = new DataAndImageTreeNode<Object>(root, next, next.getData().getImage());
            }

            if (((TreeNode<OutlineData>) n.data).getData().beginLine <= startLineIndex) {
                initialSelection = n;
            }

            entryToTreeNode.put(next, n);

        }
        this.root = root;

    }

    @Override
    protected void calculateHierarchy() {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    protected void calculateHierarchyWithParents() {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    protected Control createContents(Composite parent) {
        Control ret = super.createContents(parent);
        LiClipseTextEditorPlugin.setCssId(parent, "liclipse-outline-selection-dialog", true);
        return ret;
    }

    @Override
    public int open() {
        int ret = super.open();
        if (ret == OK) {
            Object[] result = getResult();
            if (result != null && result.length > 0) {
                DataAndImageTreeNode<Object> n = (DataAndImageTreeNode<Object>) result[0];
                TreeNode<OutlineData> outlineEntry = (TreeNode<OutlineData>) n.data;
                IRegion region = outlineEntry.getData().region;
                EditorUtils.showInEditor(liClipseEditor, region);
            }
        }
        return ret;
    }

}
