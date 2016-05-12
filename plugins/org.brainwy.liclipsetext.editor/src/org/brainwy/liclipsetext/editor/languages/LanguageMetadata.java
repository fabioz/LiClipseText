/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.util.regex.Pattern;

public final class LanguageMetadata implements Comparable<LanguageMetadata> {

    public final String languageName;
    public final ILanguageMetadataFileInfo file;
    public final Pattern[] shebang;
    private LanguageType languageType;
    public final String languageCaption;

    public LanguageMetadata(String languageName, ILanguageMetadataFileInfo file, Pattern[] shebang,
            LanguageType languageType, String languageCaption) {
        this.languageName = languageName;
        this.file = file;
        this.shebang = shebang;
        this.languageType = languageType;
        this.languageCaption = languageCaption;
    }

    @Override
    public int hashCode() {
        return languageName.hashCode();
    }

    @Override
    public String toString() {
        return languageName + " - " + file;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LanguageMetadata)) {
            return false;
        }
        LanguageMetadata languageMetadata = (LanguageMetadata) obj;
        return languageName.equals(languageMetadata.languageName);
    }

    public int compareTo(LanguageMetadata o) {
        if (languageCaption != null && o.languageCaption != null) {
            int ret = languageCaption.compareToIgnoreCase(o.languageCaption);
            if (ret != 0) {
                return ret;
            }
        }
        return languageName.compareToIgnoreCase(o.languageName);
    }

    public enum LanguageType {
        LICLIPSE, TEXT_MATE
    }

    public LanguageType getType() {
        return this.languageType;
    }

}