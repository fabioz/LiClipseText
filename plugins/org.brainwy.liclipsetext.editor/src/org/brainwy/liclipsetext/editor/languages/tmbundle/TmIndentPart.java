/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle;

/**
 * Handled in LanguageAutoEdit to create some AbstractScopedAutoEditRule.
 */
public class TmIndentPart implements ITmLanguagePart {

    public final String scope;
    public final String indentPattern;
    public final String dedentPattern;

    public TmIndentPart(String scope, String indentPattern, String dedentPattern) {
        this.scope = scope;
        this.indentPattern = indentPattern;
        this.dedentPattern = dedentPattern;
    }

}
