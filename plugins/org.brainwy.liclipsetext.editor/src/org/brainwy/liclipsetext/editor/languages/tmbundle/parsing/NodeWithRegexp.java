/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

public abstract class NodeWithRegexp extends NodeWithContents {

    protected String regexp;
    protected String regexpOptions;
    protected Node formatNode;

    public NodeWithRegexp(String string, int begin, int end) {
        super(string, begin, end);
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public void setRegexpOptions(String string) {
        this.regexpOptions = string;
    }

    public void setFormatNode(Node node) {
        this.formatNode = node;
    }

}
