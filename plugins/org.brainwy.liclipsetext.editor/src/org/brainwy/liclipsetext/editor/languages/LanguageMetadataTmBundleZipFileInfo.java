/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.core.runtime.Assert;

public final class LanguageMetadataTmBundleZipFileInfo implements ILanguageMetadataFileInfo {

    private final File zipFile;
    private final String zipPath;

    public LanguageMetadataTmBundleZipFileInfo(File zipFile, String zipPath) {
        Assert.isNotNull(zipFile);
        Assert.isNotNull(zipPath);
        this.zipFile = zipFile;
        this.zipPath = zipPath;
    }

    @Override
    public long lastModified() {
        return FileUtils.lastModified(zipFile);
    }

    @Override
    public String toString() {
        return "Zip: " + this.zipFile + "/" + this.zipPath;
    }

    @Override
    public File getParentFolder() {
        return zipFile.getParentFile();
    }

    @Override
    public LiClipseLanguage loadLanguage(boolean rethrowError) throws Exception {
        try (ZipFile z = new ZipFile(zipFile)) {
            ZipEntry entry = z.getEntry(zipPath);
            if (entry == null) {
                throw new RuntimeException("Path: " + zipPath + " could not be found in zip.");
            }
            InputStream inputStream = z.getInputStream(entry);
            LiClipseLanguage ret = LiClipseLanguage.load(this, inputStream, rethrowError);
            return ret;
        }
    }

    @Override
    public IStreamProvider getStreamProvider() throws ZipException, IOException {
        final ZipFile z = new ZipFile(zipFile);

        return new IStreamProvider() {

            @Override
            public void close() throws Exception {
                z.close();
            }

            @Override
            public InputStream getStream() throws Exception {
                return z.getInputStream(z.getEntry(zipPath));
            }
        };
    }

    @Override
    public IStreamProvider getTmLanguageStreamProvider() throws ZipException, IOException {
        // In this case, it's always the same
        return getStreamProvider();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((zipFile == null) ? 0 : zipFile.hashCode());
        result = prime * result + ((zipPath == null) ? 0 : zipPath.hashCode());
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
        LanguageMetadataTmBundleZipFileInfo other = (LanguageMetadataTmBundleZipFileInfo) obj;
        if (zipFile == null) {
            if (other.zipFile != null) {
                return false;
            }
        } else if (!zipFile.equals(other.zipFile)) {
            return false;
        }
        if (zipPath == null) {
            if (other.zipPath != null) {
                return false;
            }
        } else if (!zipPath.equals(other.zipPath)) {
            return false;
        }
        return true;
    }

    @Override
    public void openEditor() {
        Log.log("Can't currently open editor for file in .zip");
    }

}