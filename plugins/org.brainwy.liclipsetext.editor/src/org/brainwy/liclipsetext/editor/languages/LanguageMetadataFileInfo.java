/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;

public final class LanguageMetadataFileInfo implements ILanguageMetadataFileInfo {

    private File file;

    public LanguageMetadataFileInfo(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "File: " + file;
    }

    @Override
    public long lastModified() {
        return FileUtils.lastModified(file);
    }

    @Override
    public File getParentFolder() {
        return file.getParentFile();
    }

    @Override
    public LiClipseLanguage loadLanguage(boolean rethrowError) throws Exception {
        try (IStreamProvider provider = getStreamProvider()) {
            try (InputStream stream = provider.getStream()) {
                return LiClipseLanguage.load(this, stream, rethrowError);
            }
        }
    }

    @Override
    public IStreamProvider getStreamProvider() {
        return new IStreamProvider() {

            @Override
            public void close() throws Exception {
            }

            @Override
            public InputStream getStream() throws FileNotFoundException {
                return new FileInputStream(file);
            }
        };
    }

    public File getFile() {
        return file;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LanguageMetadataFileInfo other = (LanguageMetadataFileInfo) obj;
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        return true;
    }

    @Override
    public void openEditor() {
        EditorUtils.openFile(file);
    }

}