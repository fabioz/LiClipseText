/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.editor.languages.LanguageTemplates.LiClipseVariableResolver;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.FastStack;

public class SnippetToTemplateCtx {

    private FastStack<FastStringBuffer> bufStack = new FastStack<FastStringBuffer>(3);

    public SnippetToTemplateCtx() {
        bufStack.push(new FastStringBuffer());
    }

    public SnippetToTemplateCtx append(String string) {
        this.bufStack.peek().append(string);
        return this;
    }

    public String getReplaced() {
        return this.bufStack.peek().toString();
    }

    public LiClipseVariableResolver obtainValidTemplateVariableName(String sub) {
        FastStringBuffer buf = new FastStringBuffer();
        int length = sub.length();
        for (int i = 0; i < length; i++) {
            char c = sub.charAt(i);
            if (StringUtils.isAsciiLetterOrUnderlineOrNumber(c)) {
                buf.append(c);
            }
        }
        String type = buf.toString();
        if (type.equals("")) {
            type = "empty";
        }
        return new LiClipseVariableResolver(type, Arrays.asList(sub));
    }

    public void pushBuffer(FastStringBuffer buffer) {
        this.bufStack.push(buffer);
    }

    public void popBuffer() {
        this.bufStack.pop();
    }

    private Map<String, LiClipseVariableResolver> typeToResolver = new HashMap<>();

    public SnippetToTemplateCtx append(LiClipseVariableResolver resolver) {
        FastStringBuffer buf = this.bufStack.peek();

        boolean isTemplateBefore = false;
        boolean wasTemplateRemovedBecauseItWasEmpty = false;
        //There's a catch here: if the previous one is empty, we have to remove it,
        //otherwise it'd conflict with this one.
        int bufLen = buf.length();
        if (bufLen > 2) {
            int i = bufLen - 1;
            //at least 3 chars
            FastStringBuffer temp = new FastStringBuffer();
            char lastChar = buf.charAt(i);
            if (lastChar == '}') {
                i--;
                temp.append(lastChar);
                for (; i >= 0; i--) {
                    lastChar = buf.charAt(i);
                    temp.append(lastChar);
                    if (!StringUtils.isAsciiLetterOrUnderlineOrNumber(lastChar)) {
                        i--;
                        if (i >= 0) {
                            temp.append(buf.charAt(i));
                        }
                        break;
                    }
                }
                temp.reverse();
                if (temp.length() > 2) {
                    if (temp.charAt(0) == '$' && temp.charAt(1) == '{' && temp.charAt(temp.length() - 1) == '}') {
                        isTemplateBefore = true;
                        String type = temp.toString().substring(2, temp.length() - 1);
                        LiClipseVariableResolver liClipseVariableResolver = this.typeToResolver.get(type);
                        if (liClipseVariableResolver != null) {
                            if (liClipseVariableResolver.value != null && liClipseVariableResolver.value.length == 1) {
                                if (liClipseVariableResolver.value[0].isEmpty()) {
                                    wasTemplateRemovedBecauseItWasEmpty = true;
                                    buf.deleteLastChars(temp.length());
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isTemplateBefore && !wasTemplateRemovedBecauseItWasEmpty) {
            //Skip adding a new empty template if there's one not empty just before it and this one is empty.
            if (resolver.value[0].isEmpty()) {
                return this;
            }
        }

        buf.append("${");
        buf.append(resolver.getType());
        buf.append("}");
        typeToResolver.put(resolver.getType(), resolver);
        return this;
    }

}
