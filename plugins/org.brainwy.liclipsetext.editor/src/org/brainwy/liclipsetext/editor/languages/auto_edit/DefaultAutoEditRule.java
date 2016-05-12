/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.auto_edit;

import java.util.Map;

import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyHelper;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

/**
 * Rule examples
 * - {after: ':', trigger: '\n', action: indent, scope: default} #in a new line, indent after ':'
 * - {before: ':', trigger: ':', action: skip, scope: default} #skip ':' if already there
 * - {after_scope: class.method, trigger: '(', action: insert, text: (self):} #Note: if scope is dotted, check 'hierarchy' -- as given by outline.
 */
public class DefaultAutoEditRule extends AbstractScopedAutoEditRule {

    private String before;

    private String after;

    private String trigger;

    private String action;

    public DefaultAutoEditRule(Map map) {
        super(map);
        setAfter(fixEol(map.remove("after")));
        setBefore(fixEol(map.remove("before")));
        setTrigger(fixEol(map.remove("trigger")));
        setAction(map.remove("action"));
    }

    private String fixEol(Object str) {
        if (str != null) {
            String s = (String) str;
            return s.replace("\\r\\n", "\n").replace("\\n", "\n").replace("\\r", "\n");
        }
        return null;
    }

    private void setAction(Object action) {
        this.action = (String) action;
        Assert.isNotNull(action);
    }

    private void setTrigger(Object trigger) {
        this.trigger = (String) trigger;
        if (trigger == null) {
            throw new AssertionFailedException("auto_edit: trigger not provided.");
        }
    }

    private void setBefore(Object before) {
        this.before = (String) before;
    }

    private void setAfter(Object after) {
        this.after = (String) after;
    }

    public boolean customizeDocumentCommand(IDocument document, DocumentCommand command, AutoEditStrategyHelper helper,
            String indentString, String contentType) {
        String text = StringUtils.replaceNewLines(command.text, "\n");
        if (text.equals(trigger)) {
            if (this.after != null) {
                try {
                    int len = this.after.length();
                    String curr = document.get(command.offset - len, len);
                    if (!this.after.equals(curr)) {
                        return false;
                    }
                } catch (BadLocationException e) {
                    return false;
                }
            }
            if (this.before != null) {
                try {
                    int len = this.before.length();
                    String curr = document.get(command.offset, len);
                    if (!this.before.equals(curr)) {
                        return false;
                    }
                } catch (BadLocationException e) {
                    return false;
                }
            }

            if (this.scope != null) {
                if (!contentType.equals(this.scope)) {
                    return false;
                }
            }

            if (this.action.equals("indent")) {
                helper.handleNewLine(document, command, helper.c, indentString);
                command.text += indentString;
                return true;
            } else if (this.action.equals("skip")) {
                command.text = "";
                command.caretOffset = command.offset + trigger.length();
                return true;
            } else {
                Log.log("Action not recognized: " + this.action);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new FastStringBuffer(128)
                .append(" before:")
                .appendObject(before)
                .append(" after:")
                .appendObject(after)
                .append(" trigger:")
                .appendObject(trigger)
                .append(" action:")
                .appendObject(action)
                .append(" scope:")
                .appendObject(scope)
                .toString();
    }
}
