/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.tokens;

import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.CustomTextAttributeTokenCreator;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseTextAttribute;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.ReplaceContext;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.RootNode;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.StringScanner;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.TmSnippetParserV2;
import org.brainwy.liclipsetext.editor.regexp.CharsRegion;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class ContentTypeToken extends Token implements ITextAttributeProviderToken, ITokenWithReplaceOperation {

    private RootNode replaceNode;

    public ContentTypeToken(Object data) {
        super(IDocument.DEFAULT_CONTENT_TYPE.equals(data) ? "foreground" : data);
        Object data2 = this.getData();
        if (data2 instanceof String) {
            String string = (String) data2;
            if (string.contains("$")) {
                try {
                    RootNode root = new RootNode();
                    if (TmSnippetParserV2.parseFormatString(new StringScanner(string), root)) {
                        this.replaceNode = root;
                    }
                } catch (Exception e) {
                    //TODO: FINISH THIS (Don't fail silently when we have all the support in place).
                }
            }
        }
    }

    Map<String, IToken> tokenCache = new HashMap<>();

    @Override
    public IToken replaceToken(CharsRegion charsRegion) {
        if (this.replaceNode != null) {
            try {
                ReplaceContext ctx = new ReplaceContext(charsRegion);
                FastStringBuffer buf = new FastStringBuffer();
                ctx.pushBuffer(buf);
                this.replaceNode.applyReplace(ctx);
                String string = buf.toString();
                IToken iToken = tokenCache.get(string);
                if (iToken == null) {
                    iToken = TokenFactory.createTokenCopy(this, string);
                    tokenCache.put(string, iToken);
                }
                return iToken;
            } catch (Exception e) {
                //TODO: FINISH THIS (Don't fail silently when we have all the support in place).
            }
        }
        return this;
    }

    private LiClipseTextAttribute textAttribute;

    public void setTextAttribute(LiClipseTextAttribute textAttribute) {
        this.textAttribute = textAttribute;
    }

    public LiClipseTextAttribute getTokenTextAttribute(CustomTextAttributeTokenCreator defaultTokenCreator) {
        if (textAttribute == null || textAttribute.isDisposed()) {
            defaultTokenCreator.registerContentTypeToken(this);
        }
        return textAttribute;
    }

    @Override
    public LiClipseTextAttribute getPreviouslySetTextAttribute() {
        return textAttribute;
    }

    @Override
    public String toString() {
        return "ContentTypeToken: " + this.getData();
    }

}
