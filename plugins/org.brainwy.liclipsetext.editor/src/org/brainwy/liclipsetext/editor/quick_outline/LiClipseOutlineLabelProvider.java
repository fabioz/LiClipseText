/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.quick_outline;

import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.OutlineData;
import org.brainwy.liclipsetext.shared_core.structure.DataAndImageTreeNode;
import org.brainwy.liclipsetext.shared_core.structure.TreeNode;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class LiClipseOutlineLabelProvider extends LabelProvider {

    public Image getImage(Object element) {
        if (element instanceof DataAndImageTreeNode) {
            DataAndImageTreeNode dataAndImageTreeNode = (DataAndImageTreeNode) element;
            return dataAndImageTreeNode.image;
        }
        if (element instanceof TreeNode) {
            TreeNode<OutlineData> node = (TreeNode) element;
            return node.getData().getImage();
        }
        return null;
    }

    public String getText(Object element) {
        if (element instanceof DataAndImageTreeNode) {
            DataAndImageTreeNode dataAndImageTreeNode = (DataAndImageTreeNode) element;
            element = dataAndImageTreeNode.data;
        }
        if (element instanceof TreeNode) {
            TreeNode<OutlineData> node = (TreeNode) element;
            return node.getData().caption;
        }
        return element.toString();
    }

}