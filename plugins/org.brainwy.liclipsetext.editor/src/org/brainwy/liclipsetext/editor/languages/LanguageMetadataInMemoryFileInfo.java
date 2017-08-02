/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.brainwy.liclipsetext.shared_core.log.Log;

public final class LanguageMetadataInMemoryFileInfo implements ILanguageMetadataFileInfo {

    private String contents;

    public LanguageMetadataInMemoryFileInfo(String contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "LanguageMetadataInMemoryFileInfo";
    }

    @Override
    public long lastModified() {
        return 1;
    }

    @Override
    public LiClipseLanguage loadLanguage(boolean rethrowError) throws Exception {
        LiClipseLanguage ret = LiClipseLanguage.load(this, new ByteArrayInputStream(contents.getBytes("utf-8")),
                rethrowError);
        return ret;
    }

    @Override
    public IStreamProvider getStreamProvider() throws Exception {
        return new IStreamProvider() {

            @Override
            public void close() throws Exception {
            }

            @Override
            public InputStream getStream() throws FileNotFoundException, Exception {
                return new ByteArrayInputStream(contents.getBytes("utf-8"));
            }
        };
    }

    @Override
    public IStreamProvider getTmLanguageStreamProvider() throws Exception {
        throw new RuntimeException("Not available for in memory language metadata.");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contents == null) ? 0 : contents.hashCode());
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
        LanguageMetadataInMemoryFileInfo other = (LanguageMetadataInMemoryFileInfo) obj;
        if (contents == null) {
            if (other.contents != null) {
                return false;
            }
        } else if (!contents.equals(other.contents)) {
            return false;
        }
        return true;
    }

    @Override
    public void openEditor() {
        Log.log("Can't currently open editor for in memory file info.");
    }

    @Override
    public File getParentFolder() {
        return null;
    }

}