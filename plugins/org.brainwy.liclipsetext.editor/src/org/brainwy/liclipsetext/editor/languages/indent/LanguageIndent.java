/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.indent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.editor.languages.LanguageConfig;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.OutlineData;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallback;
import org.brainwy.liclipsetext.shared_core.document.DocumentSync;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionCodeReader;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class LanguageIndent extends LanguageConfig {

    public enum IndentType {
        INDENT_DEFAULT,
        INDENT_TYPE_SPACES,
        INDENT_TYPE_BRACES,
        INDENT_TYPE_SCOPES,
    }

    private String[] outlineScopes;
    private IndentType indent = IndentType.INDENT_DEFAULT;
    private String scope;
    private Set<String> scopeStart;
    private Set<String> scopeEnd;
    private String indentString; //calculated based on tabsToSpaceEnabled and tabWidth (may be null)
    private Boolean tabsToSpaceEnabled; //may be null
    private Integer tabWidth; //may be null

    public LanguageIndent(LiClipseLanguage liClipseLanguage) {
        super(liClipseLanguage);
    }

    public Set<String> getScopeStart() {
        return scopeStart;
    }

    public Set<String> getScopeEnd() {
        return scopeEnd;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void load(Map map, List<IStatus> errorList) {
        setIndent((String) map.remove("type"));
        List<String> outlineScopes = (List<String>) map.remove("outline_scopes");
        if (outlineScopes == null) {
            outlineScopes = new ArrayList<>();
        }
        setOutlineScopes(outlineScopes);

        scope = (String) map.remove("scope");
        if ("default".equals(scope)) {
            scope = IDocument.DEFAULT_CONTENT_TYPE;
        }

        if (this.indent == IndentType.INDENT_TYPE_SCOPES) {
            this.scopeStart = new HashSet<String>(fixScopes((List<String>) map.remove("scope_start")));
            this.scopeEnd = new HashSet<String>(fixScopes((List<String>) map.remove("scope_end")));
        }

        this.tabWidth = asInt(map.remove("tab_width"), null, errorList);
        this.tabsToSpaceEnabled = (Boolean) map.remove("spaces_for_tabs");
        if (this.tabWidth != null && this.tabsToSpaceEnabled != null) {
            if (tabsToSpaceEnabled) {
                indentString = new FastStringBuffer(this.tabWidth).appendN(' ', this.tabWidth).toString();
            } else {
                indentString = "\t";
            }
        }

        if (!map.isEmpty()) {
            LiClipseTextEditorPlugin.createWarning("Fields not treated in indent: "
                    + StringUtils.join(", ", map.keySet()), errorList);
        }

    }

    private List<String> fixScopes(List<String> scopeList) {
        int size = scopeList.size();
        for (int i = 0; i < size; i++) {
            String string = scopeList.get(i);
            if (string.equals("default")) {
                scopeList.set(i, IDocument.DEFAULT_CONTENT_TYPE);

            } else if (string.startsWith("default.")) {
                scopeList.set(i, IDocument.DEFAULT_CONTENT_TYPE + string.substring(7));
            }
        }
        return scopeList;
    }

    /**
     * @return the indent string to be used.
     */
    public String getIndentString() {
        if (indentString == null) {
            return getDefaultIndentString();
        }
        return indentString;
    }

    /**
     * @return the tab width (gets from eclipse preferences if needed).
     */
    public Integer getTabWidth() {
        if (tabWidth == null) {
            try {
                IPreferenceStore preferenceStore = LiClipseTextPreferences.getChainedPreferenceStore();
                return preferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
            } catch (Exception e) {
                return 4; //default
            }

        }
        return tabWidth;
    }

    /**
     * @return whether to change tabs for spaces (gets from eclipse preferences if needed).
     */
    public Boolean getTabsToSpaceEnabled() {
        if (tabsToSpaceEnabled == null) {
            try {
                IPreferenceStore preferenceStore = LiClipseTextPreferences.getChainedPreferenceStore();
                return preferenceStore
                        .getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
            } catch (Exception e) {
                return false; //default
            }

        }
        return tabsToSpaceEnabled;
    }

    public static String getDefaultIndentString() {
        IPreferenceStore preferenceStore = LiClipseTextPreferences.getChainedPreferenceStore();
        boolean spacesForTabs = preferenceStore
                .getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
        if (spacesForTabs) {
            int tabWidth;
            try {
                tabWidth = preferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
            } catch (Exception e) {
                tabWidth = 4;
            }
            return new FastStringBuffer(tabWidth).appendN(' ', tabWidth).toString();
        } else {
            return "\t";
        }

    }

    public IndentType getIndentType() {
        return indent;
    }

    /**
     * @return an array (not a copy, so, should not be mutated)
     */
    public String[] getOutlineScopes() {
        return outlineScopes;
    }

    public void setIndent(String indent) {
        if (indent == null) {
            this.indent = IndentType.INDENT_DEFAULT;
        } else {
            indent = indent.toLowerCase();
            if ("spaces".equals(indent)) {
                this.indent = IndentType.INDENT_TYPE_SPACES;

            } else if ("braces".equals(indent)) {
                this.indent = IndentType.INDENT_TYPE_BRACES;

            } else if ("scopes".equals(indent)) {
                this.indent = IndentType.INDENT_TYPE_SCOPES;

            } else {
                throw new RuntimeException("Unexpected indent:" + indent + " expected 'spaces', 'braces' or 'scopes'.");
            }
        }
    }

    private void setOutlineScopes(List<String> outlineScopes) {
        this.outlineScopes = outlineScopes.toArray(new String[outlineScopes.size()]);
    }

    /**
     * @param outlineData: must be sorted already.
     */
    public LiClipseNode calculateOutline(IDocument document, final List<OutlineData> outlineData) {
        final LiClipseNode root = new LiClipseNode(null, null, -1, true);

        ICallback<Object, IDocument> iCallback = new ICallback<Object, IDocument>() {

            public Object call(IDocument document) {
                IndentType indentType = getIndentType();
                switch (indentType) {
                    case INDENT_TYPE_SPACES:
                        calculateIndentBased(document, outlineData, root);
                        break;

                    case INDENT_TYPE_BRACES:
                        calculateBraceBased(document, outlineData, root);
                        break;

                    case INDENT_TYPE_SCOPES:
                        calculateScopeBased(document, outlineData, root);
                        break;

                    case INDENT_DEFAULT:
                        calculateFlat(document, outlineData, root);
                        break;

                    default:
                        throw new RuntimeException("Cannot handle indentation type: " + indentType);
                }
                return null;
            }
        };
        DocumentSync.runWithDocumentSynched(document, iCallback, true);
        return root;
    }

    protected void calculateIndentBased(IDocument document, List<OutlineData> outlineData, LiClipseNode root) {
        int size = outlineData.size();
        LiClipseNode last = root;

        for (int i = 0; i < size; i++) {
            OutlineData data = outlineData.get(i);
            boolean definesNewScope = getDefinesNewScope(this.outlineScopes.length, data);

            try {
                int charPosition = TextSelectionUtils.getFirstCharRelativePosition(document,
                        data.region.getOffset());
                LiClipseNode node = null;
                while (node == null) {
                    //Not all nodes define a new scope (i.e.: comments or attributes are always leaf nodes)
                    if (!last.definesNewScope) {
                        last = last.getParent();
                    }
                    if (charPosition > last.level) {
                        //Child of last (indent)
                        node = new LiClipseNode(last, data, charPosition, definesNewScope);

                    } else if (charPosition == last.level) {
                        //Same parent (same level)
                        LiClipseNode parent = last.getParent();
                        if (parent == null) {
                            parent = root;
                        }
                        node = new LiClipseNode(parent, data, charPosition, definesNewScope);

                    } else {
                        //charPosition < lastCharPosition (dedent)
                        //In this case we have to find an ancestor that's suitable.
                        last = last.getParent();
                    }
                }
                last = node;
            } catch (BadLocationException e) {
                Log.log(e);
            }
        }
    }

    private boolean getDefinesNewScope(int scopesLen, OutlineData data) {
        boolean definesNewScope = false;
        for (int j = 0; j < scopesLen; j++) {
            if (this.outlineScopes[j].equals(data.icon)) {
                definesNewScope = true;
                break;
            }
        }
        return definesNewScope;
    }

    protected void calculateBraceBased(IDocument document, List<OutlineData> outlineData, LiClipseNode root) {
        int size = outlineData.size();
        PartitionCodeReader reader = new PartitionCodeReader(scope);

        try {
            reader.configureForwardReader(document, 0, document.getLength());
            LiClipseNode last = root;

            int level = 0;
            int c = '\0';
            for (int j = 0; j < size; j++) {
                OutlineData data = outlineData.get(j);
                boolean definesNewScope = getDefinesNewScope(this.outlineScopes.length, data);
                int offset = data.region.getOffset();

                while (true) {
                    int readerOffset = reader.getOffset();
                    if (readerOffset >= offset || c == PartitionCodeReader.EOF) {
                        if (level <= 0) { //add it to the root
                            last = new LiClipseNode(root, data, level, definesNewScope);
                        } else {
                            while (level < last.level) {
                                last = last.getParent();
                            }
                            if (level == last.level) {
                                last = new LiClipseNode(last.getParent(), data, level, definesNewScope);

                            } else if (level > last.level) {
                                last = new LiClipseNode(last, data, level, definesNewScope);
                            }
                        }
                        if (!last.definesNewScope) {
                            last = last.getParent();
                        }
                        break; //break while(true)
                    }

                    switch (c) {
                        case '{':
                            level++;
                            break;
                        case '}':
                            level--;
                            break;
                    }

                    //Read it first and only do the leveling later on, in the next iteration
                    //(because we may add partitions that are completely hidden from the scanner,
                    //as we may be going only through the default partition but add nodes for
                    //comments which are in a different partition).
                    c = reader.read();

                }
            }

        } catch (Exception e) {
            Log.log(e);
        }
    }

    protected void calculateScopeBased(IDocument document, List<OutlineData> outlineData, LiClipseNode root) {
        int size = outlineData.size();
        SubPartitionCodeReader reader = new SubPartitionCodeReader();
        List<String> partitionsToRead = new ArrayList<String>();
        partitionsToRead.addAll(this.scopeStart);
        partitionsToRead.addAll(this.scopeEnd);

        try {
            reader.configurePartitions(true, document, 0,
                    partitionsToRead.toArray(new String[partitionsToRead.size()]));
            LiClipseNode last = root;

            int level = 0;
            TypedPart c = null;
            for (int j = 0; j < size; j++) {
                OutlineData data = outlineData.get(j);
                boolean definesNewScope = getDefinesNewScope(this.outlineScopes.length, data);
                int offset = data.region.getOffset();

                while (true) {
                    c = reader.read();
                    boolean doBreak = false;
                    if (c == null || c.offset >= offset) {
                        if (level <= 0) { //add it to the root
                            last = new LiClipseNode(root, data, level, definesNewScope);
                        } else {
                            while (level < last.level) {
                                last = last.getParent();
                            }
                            if (level == last.level) {
                                last = new LiClipseNode(last.getParent(), data, level, definesNewScope);

                            } else if (level > last.level) {
                                last = new LiClipseNode(last, data, level, definesNewScope);
                            }
                        }
                        if (!last.definesNewScope) {
                            last = last.getParent();
                        }
                        doBreak = true;
                    }

                    if (c != null) {
                        if (this.scopeStart.contains(c.type)) {
                            level++;

                        } else if (this.scopeEnd.contains(c.type)) {
                            level--;

                        }
                    }
                    if (doBreak) {
                        break; //break while(true)
                    }

                }
            }

        } catch (Exception e) {
            Log.log(e);
        }
    }

    protected void calculateFlat(IDocument document, List<OutlineData> outlineData, LiClipseNode root) {
        int size = outlineData.size();

        for (int i = 0; i < size; i++) {
            OutlineData data = outlineData.get(i);
            new LiClipseNode(root, data, -1, false); //flat: all are 'root' children
        }
    }

}
