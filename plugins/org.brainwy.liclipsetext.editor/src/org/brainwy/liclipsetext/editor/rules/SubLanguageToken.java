/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.rules;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;

public class SubLanguageToken {

    public final String baseLanguage;
    public final String tokenData;
    public final int offset;
    public int len; //can be mutated when joining 2 tokens.

    public SubLanguageToken(String baseLanguage, String tokenData, int offset, int len) {
        Assert.isTrue(len >= 0);
        this.baseLanguage = baseLanguage;
        this.tokenData = tokenData == null ? IDocument.DEFAULT_CONTENT_TYPE : tokenData;
        this.offset = offset;
        this.len = len;
    }

    @Override
    public String toString() {
        return new FastStringBuffer("SubLanguageToken[", 30)
                .appendObject(baseLanguage)
                .append('&')
                .appendObject(tokenData)
                .append(" offset: ")
                .append(offset)
                .append(" len: ")
                .append(len)
                .append(']')
                .toString();
    }

    public String getFullContentType() {
        return SwitchLanguageToken.createSubLanguageContentType(baseLanguage, tokenData);
    }

    /**
     * This method should make sure that the whole range covered from the startOffset to the endOffset
     * has a related subtoken.
     */
    public static void fillWithDefault(List<SubLanguageToken> lst, String baseLanguage, int startOffset, int endOffset) {
        //Make sure that it's sorted!
        Collections.sort(lst, new Comparator<SubLanguageToken>() {

            public int compare(SubLanguageToken o1, SubLanguageToken o2) {
                return o1.offset - o2.offset;
            }
        });

        int lastOffset = startOffset;
        int size = lst.size();
        if (size == 0) {
            //special case: empty list
            lst.add(new SubLanguageToken(baseLanguage, IDocument.DEFAULT_CONTENT_TYPE, startOffset, endOffset
                    - startOffset));
            return;
        }
        SubLanguageToken token = null;
        for (int i = 0; i < size; i++) {
            token = lst.get(i);
            if (token.offset > lastOffset) {
                lst.add(i, new SubLanguageToken(baseLanguage, IDocument.DEFAULT_CONTENT_TYPE, lastOffset, token.offset
                        - lastOffset));
                size++;
                i++;
            }
            lastOffset = token.offset + token.len;
        }
        if (endOffset > lastOffset) {
            lst.add(size, new SubLanguageToken(baseLanguage, IDocument.DEFAULT_CONTENT_TYPE, lastOffset, endOffset
                    - lastOffset));
        }
    }

}
