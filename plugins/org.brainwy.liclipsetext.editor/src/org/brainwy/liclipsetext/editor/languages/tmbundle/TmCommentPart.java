/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle;

public class TmCommentPart implements ITmLanguagePart {

    public final String scope;
    public final String commentStart;
    public final String commentEnd;
    public final int priority;

    public TmCommentPart(String scope, String commentStart, String commentEnd, int priority) {
        this.scope = scope;
        this.commentStart = commentStart;
        this.commentEnd = commentEnd;
        this.priority = priority;
    }

}
