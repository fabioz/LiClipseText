/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.Token;

public class SwitchLanguageToken extends Token {

    public final SubLanguageToken[] subTokens;

    public SwitchLanguageToken(Object data, List<SubLanguageToken> subTokens) {
        super(data);
        int size = subTokens.size();
        ArrayList<SubLanguageToken> lst = new ArrayList<>(size);
        //Let's merge the tokens where we can (i.e.: if we have 2 tokens which when scanned go side by side, join them)
        //If we don't do that, the positions in the final document won't be correct.
        for (ListIterator<SubLanguageToken> it = subTokens.listIterator(); it.hasNext();) {
            SubLanguageToken curr = it.next();
            while (it.hasNext()) {
                SubLanguageToken lookahead = it.next();
                if (lookahead.offset == curr.offset + curr.len && lookahead.tokenData.equals(curr.tokenData)
                        && lookahead.baseLanguage.equals(curr.baseLanguage)) {
                    curr.len += lookahead.len;
                    //We have consumed the lookahead and it won't be in the final result.
                } else {
                    it.previous(); //go back so that we get the lookahead as the next 'curr'.
                    break;
                }
            }
            lst.add(curr);
        }

        this.subTokens = lst.toArray(new SubLanguageToken[lst.size()]);
        //        this.subTokens = subTokens.toArray(new SubLanguageToken[subTokens.size()]);
    }

    public static String createSubLanguageContentType(String baseData, String data) {
        if (data == null) {
            data = IDocument.DEFAULT_CONTENT_TYPE;
        }
        return new FastStringBuffer(baseData, data.length() + 1).append('&').append(data).toString();
    }

    public static boolean isSubLanguagePartition(TypedPosition partition) {
        String type = partition.getType();
        return type.indexOf('&') != -1;
    }

    public static boolean isSubLanguagePartition(String contentType) {
        return contentType.indexOf('&') != -1;
    }

    /**
     * @return null if it's not valid as a sub-language or a tuple with the language and the content
     * type in that language.
     */
    public static Tuple<String, String> getSubLanguageAndContentType(String contentType) {
        int i = contentType.indexOf('&');
        if (i != -1) {
            return new Tuple<String, String>(contentType.substring(0, i), contentType.substring(i + 1,
                    contentType.length()));
        }
        return null;
    }

    @Override
    public String toString() {
        return new FastStringBuffer("SwitchLanguageToken:\n", 50)
                .appendObject(getData())
                .append("\n{\n  ")
                .appendObject(StringUtils.join("\n  ", (Object[]) subTokens))
                .append("\n}")
                .toString();
    }
}
