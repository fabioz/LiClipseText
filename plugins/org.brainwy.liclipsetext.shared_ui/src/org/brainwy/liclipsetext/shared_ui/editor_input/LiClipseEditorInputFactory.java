/******************************************************************************
* Copyright (C) 2011-2012  Fabio Zadrozny
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
******************************************************************************/
package org.brainwy.liclipsetext.shared_ui.editor_input;

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class LiClipseEditorInputFactory implements IElementFactory {

    public static final String FACTORY_ID = "org.brainwy.liclipsetext.editorinput.editorInputFactory";

    @Override
    public IAdaptable createElement(IMemento memento) {
        String fileStr = memento.getString(TAG_FILE);
        if (fileStr == null || fileStr.length() == 0) {
            return null;
        }

        String zipPath = memento.getString(TAG_ZIP_PATH);
        final File file = new File(fileStr);
        if (zipPath == null || zipPath.length() == 0) {
            //return EditorInputFactory.create(new File(file), false);
            final URI uri = file.toURI();
            IFile[] ret = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri,
                    IContainer.INCLUDE_HIDDEN | IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
            if (ret != null && ret.length > 0) {
                return new FileEditorInput(ret[0]);
            }
            try {
                return new FileStoreEditorInput(EFS.getStore(uri));
            } catch (CoreException e) {
                return new LiClipseFileEditorInput(file);
            }
        }

        return new LiClipseZipFileEditorInput(new LiClipseZipFileStorage(file, zipPath));
    }

    private static final String TAG_FILE = "file"; //$NON-NLS-1$

    private static final String TAG_ZIP_PATH = "zip_path"; //$NON-NLS-1$

    public static void saveState(IMemento memento, LiClipseZipFileEditorInput pydevZipFileEditorInput) {
        memento.putString(TAG_FILE, pydevZipFileEditorInput.getFile().toString());
        memento.putString(TAG_ZIP_PATH, pydevZipFileEditorInput.getZipPath());
    }

    public static void saveState(IMemento memento, LiClipseFileEditorInput pydevFileEditorInput) {
        memento.putString(TAG_FILE, pydevFileEditorInput.getFile().toString());
    }

}
