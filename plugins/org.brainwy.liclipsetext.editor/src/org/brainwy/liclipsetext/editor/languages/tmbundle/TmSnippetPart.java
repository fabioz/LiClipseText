/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle;

import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.SnippetToTemplateCtx;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.TmSnippetParser;

public class TmSnippetPart implements ITmLanguagePart {

    private final String name;
    private final String content;
    private final String scope;
    private final String tabTrigger;

    public static String fixPattern(String pattern) {
        SnippetToTemplateCtx ctx = new SnippetToTemplateCtx();
        return TmSnippetParser.createReplacement(pattern, ctx).getReplaced();
    }

    public TmSnippetPart(String name, String content, String scope, String tabTrigger) {
        this.content = content;
        this.name = name;
        this.scope = scope;
        this.tabTrigger = tabTrigger;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getScope() {
        return scope;
    }

    public String getTabTrigger() {
        return tabTrigger;
    }

}
