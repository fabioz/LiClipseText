/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.views.partitioning;

import org.brainwy.liclipsetext.editor.common.LiClipseEditor;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.model.ErrorDescription;
import org.brainwy.liclipsetext.shared_core.model.IModelListener;
import org.brainwy.liclipsetext.shared_core.model.ISimpleNode;
import org.brainwy.liclipsetext.shared_core.structure.DataAndImageTreeNode;
import org.brainwy.liclipsetext.shared_core.structure.TreeNode;
import org.brainwy.liclipsetext.shared_ui.SharedUiPlugin;
import org.brainwy.liclipsetext.shared_ui.UIConstants;
import org.brainwy.liclipsetext.shared_ui.quick_outline.DataAndImageTreeNodeContentProvider;
import org.brainwy.liclipsetext.shared_ui.tree.PyFilteredTree;
import org.brainwy.liclipsetext.shared_ui.utils.RunInUiThread;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

public class LiClipsePartitioningView extends ViewPart implements ISelectionChangedListener, IPartListener,
        IModelListener {

    protected TreeViewer treeViewer;
    protected PatternFilter patternFilter;
    protected FilteredTree filter;
    private boolean disposed;
    private LiClipseEditor currentEditor;

    public LiClipsePartitioningView() {
        disposed = false;
    }

    @Override
    public void createPartControl(Composite parent) {
        Assert.isTrue(!disposed);
        patternFilter = new PatternFilter();
        filter = PyFilteredTree.create(parent, patternFilter, false);
        treeViewer = filter.getViewer();
        treeViewer.addSelectionChangedListener(this);

        getSite().getPage().addPartListener(this);
        treeViewer.setContentProvider(new DataAndImageTreeNodeContentProvider());
        treeViewer.setLabelProvider(new LiClipsePartitioningOutlineLabelProvider());
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            getSite().getPage().removePartListener(this);
            this.treeViewer.removeSelectionChangedListener(this);
            if (this.currentEditor != null) {
                this.currentEditor.removeModelListener(this);
            }
            this.currentEditor = null;
            this.treeViewer = null;
            this.patternFilter = null;
            this.filter = null;
        }
        super.dispose();
    }

    /**
     * When the editor is set, we start listening its changes.
     */
    private void setCurrentEditor(LiClipseEditor part) {
        if (this.currentEditor == part) {
            return;
        }
        if (this.currentEditor != null) {
            this.currentEditor.removeModelListener(this);
        }
        this.currentEditor = part;
        if (part != null) {
            part.addModelListener(this);
        }
        refresh();
    }

    @Override
    public void modelChanged(ISimpleNode ast) {
        refresh();
    }

    private void refresh() {
        // When the model was changed, we'll recreate our structure.
        RunInUiThread.async(new Runnable() {

            @Override
            public void run() {
                if (!disposed) {
                    LiClipseEditor editor = currentEditor;
                    if (editor != null && !editor.isDisposed()) {
                        IDocument document = editor.getDocument();

                        DataAndImageTreeNode<Object> rootInput = DocumentTimeStampChangedException
                                .retryUntilNoDocChanges(() -> {
                                    DataAndImageTreeNode<Object> root = new DataAndImageTreeNode<>(null, null, null);
                                    SubPartitionCodeReader subPartitionCodeReader = new SubPartitionCodeReader();
                                    subPartitionCodeReader.configureReadAllTopPartition(true, document, 0);

                                    Image image = SharedUiPlugin.getImageCache().get(UIConstants.PUBLIC_ATTR_ICON);
                                    int i = 0;
                                    while (true) {
                                        TypedPart read;
                                        read = subPartitionCodeReader.read();
                                        if (read == null) {
                                            break;
                                        }
                                        i += 1;
                                        if (i > 500) {
                                            new DataAndImageTreeNode<>(root,
                                                    "Too many items to show ( > 500), bailing out.",
                                                    image);
                                            break;
                                        } else {
                                            new DataAndImageTreeNode<>(root, read, image);
                                        }
                                    }
                                    return root;
                                });
                        treeViewer.setInput(rootInput);
                    } else {
                        DataAndImageTreeNode<Object> root = new DataAndImageTreeNode<>(null, null, null);
                        treeViewer.setInput(root);
                    }
                }
            }
        }, true);
    }

    @Override
    public void errorChanged(ErrorDescription errorDesc) {

    }

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
    }

    @Override
    public void setFocus() {
        this.treeViewer.getTree().setFocus();
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if (!this.disposed) {
            LiClipseEditor editor = this.currentEditor;
            if (editor != null && !editor.isDisposed()) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection iStructuredSelection = (IStructuredSelection) selection;
                    Object firstElement = iStructuredSelection.getFirstElement();
                    if (firstElement instanceof DataAndImageTreeNode) {
                        DataAndImageTreeNode dataAndImageTreeNode = (DataAndImageTreeNode) firstElement;
                        Object data = dataAndImageTreeNode.data;
                        if (data instanceof TypedPart) {
                            final TypedPart typedPart = (TypedPart) data;
                            editor.setSelection(typedPart.offset, typedPart.length);

                            if (!dataAndImageTreeNode.hasChildren()) {
                                loadChildren(editor, dataAndImageTreeNode, typedPart);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void loadChildren(LiClipseEditor editor, DataAndImageTreeNode dataAndImageTreeNode,
            final TypedPart typedPart) {
        {

            SubPartitionCodeReader subPartitionCodeReader = new SubPartitionCodeReader();
            IDocument document = editor.getDocument();
            subPartitionCodeReader.configurePartitions(true, document, typedPart.offset,
                    new SubPartitionCodeReader.IAcceptPartition() {

                        @Override
                        public boolean getRequireOnlyTop() {
                            return false;
                        }

                        @Override
                        public boolean accept(TypedPart p) {
                            //This is for the sub-partitions + top
                            if (p.offset < typedPart.offset + typedPart.length
                                    && (p.offset != typedPart.offset || p.length != typedPart.length)
                                    && p.offset >= typedPart.offset) {
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public boolean accept(TypedPosition p) {
                            if (p.offset == typedPart.offset && p.length == typedPart.length) {
                                //This is for the top partition
                                return true;
                            }
                            return false;
                        }
                    });
            Image image = SharedUiPlugin.getImageCache().get(UIConstants.PRIVATE_FIELD_ICON);
            boolean[] created = new boolean[1];
            DataAndImageTreeNode<Object> rootInput = DocumentTimeStampChangedException.retryUntilNoDocChanges(() -> {
                created[0] = false;
                int i = 0;
                DataAndImageTreeNode<Object> root = new DataAndImageTreeNode<>(null, null, null);
                while (true) {
                    i += 1;
                    TypedPart read;
                    read = subPartitionCodeReader.read();
                    if (read == null) {
                        break;
                    }
                    created[0] = true;
                    if (i > 500) {
                        new DataAndImageTreeNode<>(root, "Too many items to show ( > 500), bailing out.",
                                image);
                        break;
                    } else {
                        new DataAndImageTreeNode<>(root, read, image);
                    }
                }
                return root;
            });
            for (TreeNode c : rootInput.getChildren()) {
                c.setParent(dataAndImageTreeNode, false);
            }

            if (created[0]) {
                treeViewer.refresh(dataAndImageTreeNode);
            }
        }
    }

    public ISelection getSelection() {
        if (treeViewer == null) {
            return StructuredSelection.EMPTY;
        }
        return treeViewer.getSelection();
    }

    public void setSelection(ISelection selection) {
        if (treeViewer != null) {
            treeViewer.setSelection(selection);
        }
    }

    public Control getControl() {
        if (filter == null) {
            return null;
        }
        return filter;
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
        if (part instanceof IEditorPart) {
            if (part instanceof LiClipseEditor) {
                setCurrentEditor((LiClipseEditor) part);
            } else {
                setCurrentEditor(null);
            }
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
        partActivated(part);
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
        if (part == this.currentEditor) {
            setCurrentEditor(null);
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {

    }

    @Override
    public void partOpened(IWorkbenchPart part) {

    }

}
