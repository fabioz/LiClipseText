/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.indent.LanguageIndent;
import org.brainwy.liclipsetext.editor.languages.indent.LanguageIndent.IndentType;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.parsing.IScopesParser;
import org.brainwy.liclipsetext.shared_core.parsing.Scopes;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionCodeReader;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionMerger;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.FastStack;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;

/**
 * This parser is a bit different from the others, as its output is not an AST, but a structure defining the scopes
 * in a document (used for doing the scope selection action).
 *
 * @author fabioz
 */
public class ScopesParser implements IScopesParser {

    public Scopes createScopes(IDocument doc) {
        this.scopes = new Scopes();
        this.doc = doc;

        //Ok, we have all the types and position (and whatever is not there is 'default').
        Position[] positions = PartitionCodeReader.getDocumentTypedPositions(doc, IDocument.DEFAULT_CONTENT_TYPE);

        List<TypedPosition> merged = PartitionMerger.sortAndMergePositions(positions, doc.getLength());
        this.merged = merged;

        return this.createScopes();
    }

    private Scopes scopes;
    private IDocument doc;
    private final LiClipseLanguage language;
    private List<TypedPosition> merged;

    public ScopesParser(LiClipseLanguage liClipseLanguage) {
        this.language = liClipseLanguage;
    }

    private Scopes createScopes() {
        int globalScope = this.scopes.startScope(0, Scopes.TYPE_MODULE);

        createInternalScopes();

        this.scopes.endScope(globalScope, doc.getLength(), Scopes.TYPE_MODULE);

        return this.scopes;

    }

    private void createInternalScopes() {
        for (TypedPosition p : merged) {
            if (!IDocument.DEFAULT_CONTENT_TYPE.equals(p.getType())) {
                int s = this.scopes.startScope(p.offset, Scopes.TYPE_SUITE);
                this.scopes.endScope(s, p.offset + p.length, Scopes.TYPE_SUITE);
            }
        }

        if (language.getIndent().getIndentType() == IndentType.INDENT_TYPE_SCOPES) {
            calculateScopes();

        } else {
            calculateBraces();
        }

    }

    private void calculateScopes() {
        LanguageIndent indent = language.getIndent();
        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        List<String> partitionsToRead = new ArrayList<String>();
        Set<String> scopeStart = indent.getScopeStart();
        partitionsToRead.addAll(scopeStart);
        Set<String> scopeEnd = indent.getScopeEnd();
        partitionsToRead.addAll(scopeEnd);

        try {
            reader.configurePartitions(true, doc, 0,
                    partitionsToRead.toArray(new String[partitionsToRead.size()]));

            FastStack<ITypedRegion> stack = new FastStack<ITypedRegion>(6);
            TypedPart c = null;
            while (true) {
                c = reader.read();
                if (c == null) {
                    break;
                }
                if (scopeStart.contains(c.type)) {
                    ITypedRegion r = doc.getPartition(c.offset);
                    stack.push(r);

                } else if (scopeEnd.contains(c.type)) {
                    ITypedRegion r = doc.getPartition(c.offset);
                    if (stack.size() > 0) {
                        ITypedRegion prev = stack.pop();
                        int s = this.scopes.startScope(prev.getOffset(), Scopes.TYPE_SUITE);
                        this.scopes.endScope(s, r.getOffset() + r.getLength(), Scopes.TYPE_SUITE);

                        s = this.scopes.startScope(prev.getOffset() + prev.getLength(), Scopes.TYPE_SUITE);
                        this.scopes.endScope(s, r.getOffset(), Scopes.TYPE_SUITE);
                    }

                }
            }

        } catch (Exception e) {
            Log.log(e);
        }
    }

    private void calculateBraces() {
        PartitionCodeReader reader = new PartitionCodeReader(IDocument.DEFAULT_CONTENT_TYPE);

        try {
            reader.configureForwardReader(doc, 0, doc.getLength());

            Map<Integer, Stack<Integer>> charToLevel = new HashMap<Integer, Stack<Integer>>();
            charToLevel.put((int) '{', new Stack<Integer>());
            charToLevel.put((int) '[', new Stack<Integer>());
            charToLevel.put((int) '(', new Stack<Integer>());

            int c;
            while (true) {
                c = reader.read();
                if (c == PartitionCodeReader.EOF) {
                    break;
                }
                Stack<Integer> curr;
                switch (c) {
                    case '{':
                    case '[':
                    case '(':
                        curr = charToLevel.get(c);
                        curr.push(reader.getOffset());

                        break;

                    case '}':
                    case ']':
                    case ')':
                        int peer = StringUtils.getPeer((char) c);
                        curr = charToLevel.get(peer);
                        if (curr.size() > 0) {
                            Integer prevOffset = curr.pop();
                            int currOffset = reader.getOffset();
                            int s = this.scopes.startScope(prevOffset, Scopes.TYPE_PEER);
                            this.scopes.endScope(s, currOffset + 1, Scopes.TYPE_PEER);

                        }
                        break;
                }
            }

        } catch (Exception e) {
            Log.log(e);
        }
    }

}
