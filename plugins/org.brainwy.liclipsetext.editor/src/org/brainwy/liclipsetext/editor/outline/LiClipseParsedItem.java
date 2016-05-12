/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.OutlineData;
import org.brainwy.liclipsetext.shared_core.model.ErrorDescription;
import org.brainwy.liclipsetext.shared_core.structure.TreeNode;
import org.brainwy.liclipsetext.shared_ui.SharedUiPlugin;
import org.brainwy.liclipsetext.shared_ui.UIConstants;
import org.brainwy.liclipsetext.shared_ui.outline.BaseParsedItem;
import org.brainwy.liclipsetext.shared_ui.outline.IParsedItem;

public class LiClipseParsedItem extends BaseParsedItem {

    private LiClipseNode node;
    private LiClipseNode[] nodeChildren;
    private Image image;

    public LiClipseParsedItem(LiClipseParsedItem parent, LiClipseNode root, LiClipseNode[] nodeChildren) {
        this(nodeChildren, null);
        this.parent = parent;
        this.node = root;
    }

    public LiClipseParsedItem(LiClipseParsedItem parent, ErrorDescription errorDesc) {
        this.parent = parent;
        this.setErrorDesc(errorDesc);
    }

    public LiClipseNode getNode() {
        return node;
    }

    public LiClipseNode[] getNodeChildren() {
        return nodeChildren;
    }

    /**
     * root
     */
    public LiClipseParsedItem(LiClipseNode[] nodeChildren, ErrorDescription errorDesc) {
        this.nodeChildren = nodeChildren;
        this.setErrorDesc(errorDesc);
    }

    @Override
    public void updateTo(IParsedItem item) {
        LiClipseParsedItem updateToItem = (LiClipseParsedItem) item;

        this.node = updateToItem.node;
        this.image = null;
        this.nodeChildren = updateToItem.nodeChildren;

        super.updateTo(item);
    }

    public int getBeginLine() {
        if (node != null && node.getData() != null) {
            return node.getData().beginLine;
        }
        return -1;
    }

    public int getBeginCol() {
        if (node != null && node.getData() != null) {
            return node.getData().beginCol;
        }
        return -1;
    }

    @SuppressWarnings("rawtypes")
    public IParsedItem[] getChildren() {
        if (children != null) {
            return children;
        }
        if (nodeChildren == null) {
            nodeChildren = new LiClipseNode[0];
        }

        ArrayList<LiClipseParsedItem> items = new ArrayList<LiClipseParsedItem>();

        if (this.parent == null && errorDesc != null && errorDesc.message != null) {
            items.add(new LiClipseParsedItem(this, errorDesc));
        }

        for (LiClipseNode c : nodeChildren) {
            List<TreeNode/*OutlineData*/> tempChildren = c.getChildren();
            items.add(new LiClipseParsedItem(this, c, tempChildren.toArray(new LiClipseNode[tempChildren.size()])));
        }
        children = items.toArray(new LiClipseParsedItem[items.size()]);
        return children;
    }

    public Image getImage() {
        if (image == null && node != null) {
            OutlineData data = node.getData();
            if (data != null) {
                image = data.getImage();
            }
        }
        if (image == null) {
            image = SharedUiPlugin.getImageCache().get(UIConstants.PUBLIC_ATTR_ICON); //default
        }
        return image;
    }

    public boolean sameNodeType(IParsedItem newItem) {
        LiClipseNode oldNode = this.node;
        LiClipseNode newNode = ((LiClipseParsedItem) newItem).node;

        if (oldNode != null && newNode != null && oldNode.getData() != null && newNode.getData() != null) {
            OutlineData oldData = oldNode.getData();
            OutlineData newData = newNode.getData();
            if (oldData != null && newData != null) {
                if (oldData.icon != null && newData.icon != null) {
                    return oldNode.getData().icon.equals(newNode.getData().icon);
                }

            }
        }
        return true;
    }

    public void updateShallow(IParsedItem newItem) {
        this.toStringCache = null;
        this.image = null;
        this.node = ((LiClipseParsedItem) newItem).node;
        setErrorDesc(newItem.getErrorDesc());
    }

    public int compareTo(Object o) {
        if (!(o instanceof IParsedItem)) {
            return 0;
        }
        return toString().compareTo(o.toString());
    }

    @Override
    protected String calcToString() {
        if (errorDesc != null && errorDesc.message != null) {
            return errorDesc.message;
        }
        if (node != null && node.getData() != null) {
            OutlineData data = node.getData();
            String caption = data.caption;
            if (caption != null) {
                return caption;
            }
        }
        return "null";
    }

}
