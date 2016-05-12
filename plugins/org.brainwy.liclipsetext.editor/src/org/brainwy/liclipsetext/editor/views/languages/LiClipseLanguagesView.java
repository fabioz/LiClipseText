/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.views.languages;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.LiClipseEditor;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallback;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallbackListener;
import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.structure.DataAndImageTreeNode;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.SharedUiPlugin;
import org.brainwy.liclipsetext.shared_ui.UIConstants;
import org.brainwy.liclipsetext.shared_ui.editor_input.LiClipseZipFileEditorInput;
import org.brainwy.liclipsetext.shared_ui.editor_input.LiClipseZipFileStorage;
import org.brainwy.liclipsetext.shared_ui.quick_outline.DataAndImageTreeNodeContentProvider;
import org.brainwy.liclipsetext.shared_ui.tree.PyFilteredTree;
import org.brainwy.liclipsetext.shared_ui.utils.RunInUiThread;

public class LiClipseLanguagesView extends ViewPart
        implements ISelectionChangedListener, ICallbackListener<LanguagesManager> {

    protected TreeViewer treeViewer;
    protected PatternFilter patternFilter;
    protected FilteredTree filter;
    private boolean disposed;

    public LiClipseLanguagesView() {
        disposed = false;
    }

    @Override
    public void createPartControl(Composite parent) {
        Assert.isTrue(!disposed);
        patternFilter = new PatternFilter();
        filter = PyFilteredTree.create(parent, patternFilter, false);
        treeViewer = filter.getViewer();
        treeViewer.addSelectionChangedListener(this);

        treeViewer.setContentProvider(new DataAndImageTreeNodeContentProvider());
        treeViewer.setLabelProvider(new LiClipseLanguagesViewOutlineLabelProvider());

        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(treeViewer.getTree());
        treeViewer.getTree().setMenu(menu);
        IWorkbenchPartSite site = getSite();
        site.registerContextMenu(menuManager, treeViewer);
        site.setSelectionProvider(treeViewer);

        treeViewer.addDoubleClickListener(new DoubleClickTreeItemMouseListener());
        treeViewer.addOpenListener(new OpenListener());

        this.refresh();

        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        if (languagesManager != null) {
            languagesManager.onReload.registerListener(this);
        }
    }

    /**
     * Makes the test double clicked in the tree active in the editor.
     */
    private final class DoubleClickTreeItemMouseListener implements IDoubleClickListener {
        @Override
        public void doubleClick(DoubleClickEvent event) {
            openSelection();
        }
    }

    /**
     * Makes the test with the enter pressed in the tree active in the editor.
     */
    private final class OpenListener implements IOpenListener {

        @Override
        public void open(OpenEvent event) {
            openSelection();
        }
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
            if (languagesManager != null) {
                languagesManager.onReload.unregisterListener(this);
            }
            this.treeViewer.removeSelectionChangedListener(this);
            this.treeViewer = null;
            this.patternFilter = null;
            this.filter = null;
        }
        super.dispose();
    }

    public void openSelection() {

        if (!this.disposed) {
            ISelection selection = treeViewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection iStructuredSelection = (IStructuredSelection) selection;
                Object firstElement = iStructuredSelection.getFirstElement();
                if (firstElement instanceof DataAndImageTreeNode) {
                    DataAndImageTreeNode dataAndImageTreeNode = (DataAndImageTreeNode) firstElement;
                    if (dataAndImageTreeNode instanceof ZipRootTreeNode) {
                        return;
                    }

                    if (dataAndImageTreeNode instanceof ZipPathTreeNode) {
                        ZipPathTreeNode zipPathTreeNode = (ZipPathTreeNode) dataAndImageTreeNode;
                        LiClipseZipFileStorage storage = new LiClipseZipFileStorage(zipPathTreeNode.zipFile,
                                zipPathTreeNode.zipPath);
                        LiClipseZipFileEditorInput input = new LiClipseZipFileEditorInput(storage);
                        IWorkbenchWindow activeWorkbenchWindow = EditorUtils.getActiveWorkbenchWindow();
                        if (activeWorkbenchWindow != null) {
                            IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
                            if (page != null) {
                                try {
                                    String editorId = EditorUtils.getEditorId(input, null);
                                    if (editorId == null) {
                                        editorId = LiClipseEditor.EDITOR_ID;
                                    }
                                    IDE.openEditor(page, input, editorId);
                                } catch (PartInitException e) {
                                    Log.log(e);
                                }
                            }
                        }
                        return;
                    }

                    if (dataAndImageTreeNode instanceof IAdaptable) {
                        IAdaptable iAdaptable = dataAndImageTreeNode;
                        File file = iAdaptable.getAdapter(File.class);

                        if (file != null && file.isFile()) {
                            if (!file.getName().endsWith(".tmbundle")) {
                                EditorUtils.openFile(file);
                            } else {

                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
    }

    @Override
    public void setFocus() {
        this.treeViewer.getTree().setFocus();
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
    }

    private static class FileTreeNode extends DataAndImageTreeNode<Object>implements IAdaptable {

        public FileTreeNode(DataAndImageTreeNode parent, File data, Image image) {
            super(parent, data, image);
        }

        @Override
        public <Z> Z getAdapter(Class<Z> adapter) {
            if (URI.class == adapter) {
                File d = (File) this.getData();
                return (Z) d.toURI();
            }
            if (File.class == adapter) {
                return (Z) this.getData();
            }
            return super.getAdapter(adapter);
        }
    }

    private static class ZipRootTreeNode extends DataAndImageTreeNode<Object>implements IAdaptable {

        public ZipRootTreeNode(DataAndImageTreeNode parent, File data, Image image) {
            super(parent, data, image);
        }

        @Override
        public <Z> Z getAdapter(Class<Z> adapter) {
            if (URI.class == adapter) {
                File d = (File) this.getData();
                return (Z) d.toURI();
            }
            return super.getAdapter(adapter);
        }
    }

    private static class ZipPathTreeNode extends DataAndImageTreeNode<Object>implements IAdaptable {

        public final File zipFile;
        public final String zipPath;

        public ZipPathTreeNode(DataAndImageTreeNode parent, File zipFile, String zipPath, Image image) {
            super(parent, zipPath, image);
            this.zipFile = zipFile;
            this.zipPath = zipPath;
        }

        @Override
        public <Z> Z getAdapter(Class<Z> adapter) {
            if (URI.class == adapter) {
                return (Z) zipFile.toURI();
            }
            return super.getAdapter(adapter);
        }
    }

    private void refresh() {
        // When the model was changed, we'll recreate our structure.
        RunInUiThread.async(new Runnable() {

            @Override
            public void run() {
                if (!disposed) {
                    DataAndImageTreeNode<Object> root = new DataAndImageTreeNode<>(null, null, null);

                    LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                    if (languagesManager != null) {
                        File[] languagesDir = languagesManager.getLanguagesDirectories();
                        for (File dir : languagesDir) {
                            FileTreeNode folder = new FileTreeNode(root, dir,
                                    SharedUiPlugin.getImageCache().get(UIConstants.FOLDER_ICON));
                            File[] listFiles = dir.listFiles();
                            if (listFiles != null) {
                                for (File file : listFiles) {

                                    String name = file.getName();
                                    String lowerName = name.toLowerCase();
                                    if (lowerName.endsWith(".tmbundle")) {
                                        loadTmBundle(file, folder);
                                    } else if (lowerName.endsWith(".liclipse")) {
                                        try {
                                            loadFile(file, folder);
                                        } catch (Exception e) {
                                            Log.log(e);
                                        }
                                    }
                                }
                            } else {
                                Log.log("Unable to list files in dir: " + dir);
                            }
                        }
                    }

                    treeViewer.setInput(root);
                }
            }
        }, true);
    }

    protected void loadFile(final File file, final DataAndImageTreeNode<Object> root) {
        new FileTreeNode(root, file, LiClipseTextEditorPlugin.getIcon("editor.png"));
    }

    protected void loadTmBundle(final File file, final DataAndImageTreeNode<Object> root) {
        if (file.isFile()) {
            //Let's see if we're dealing with a zipped file
            try {
                ZipRootTreeNode zipNode = new ZipRootTreeNode(root, file,
                        SharedUiPlugin.getImageCache().get(UIConstants.LIB_SYSTEM));
                try (ZipFile zipFile = new ZipFile(file)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry element = entries.nextElement();
                        String elementName = element.getName();
                        if (LanguagesManager.considerTmBundleZipName(elementName)) {
                            new ZipPathTreeNode(zipNode, file, elementName, LiClipseTextEditorPlugin.getIcon("editor.png"));
                        }
                    }
                }
            } catch (Exception e) {
                Log.log("Error in file: " + file, e);
            }
        } else if (file.isDirectory()) {
            final FileTreeNode folder = new FileTreeNode(root, file,
                    SharedUiPlugin.getImageCache().get(UIConstants.FOLDER_ICON));
            ICallback<Boolean, java.nio.file.Path> callback = new ICallback<Boolean, java.nio.file.Path>() {

                @Override
                public Boolean call(java.nio.file.Path path) {
                    String string = path.toString();
                    if (LanguagesManager.considerTmBundleZipName(string)) {
                        new FileTreeNode(folder, path.toFile(), LiClipseTextEditorPlugin.getIcon("editor.png"));
                    }

                    return true;
                }
            };
            //I.e.: search beneath the directory structure
            try {
                FileUtils.visitDirectory(file, true, callback);
            } catch (IOException e) {
                Log.log(e);
            }
        }

    }

    /**
     * Called when languagesManager does a reload.
     */
    @Override
    public Object call(LanguagesManager obj) {
        this.refresh();
        return null;
    }

}
