/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.io.File;

public interface ILanguageMetadataFileInfo {

    long lastModified();

    LiClipseLanguage loadLanguage(boolean rethrowError) throws Exception;

    int hashCode();

    boolean equals(Object obj);

    /**
     * Something as EditorUtils.openFile(file)
     */
    void openEditor();

    IStreamProvider getStreamProvider() throws Exception;

    /**
     * Could be null.
     */
    File getParentFolder();

    IStreamProvider getTmLanguageStreamProvider() throws Exception;
}