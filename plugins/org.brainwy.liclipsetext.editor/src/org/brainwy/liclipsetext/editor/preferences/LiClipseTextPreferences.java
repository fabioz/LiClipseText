/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.preferences;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.ILiClipseEditorCustomizer;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCacheProvider;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.utils.BaseExtensionHelper;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class LiClipseTextPreferences {

    private static IPreferenceStore chainedPreferenceStore;

    public static IPreferenceStore getChainedPreferenceStore() {
        if (chainedPreferenceStore == null) {
            List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>();
            if (LiClipseTextEditorPlugin.PLUGIN_STARTED) {

                try {
                	ILiClipseEditorCustomizer editorCustomizer = (ILiClipseEditorCustomizer) BaseExtensionHelper.getParticipant(
                			"org.brainwy.liclipsetext.editor.liclipse_editor_customizer", false);
                	if(editorCustomizer != null){
                		IPreferenceStore preferenceStore = editorCustomizer.getPreferenceStore();
                		if(preferenceStore != null){
                			stores.add(preferenceStore);
                		}
                	}
    			} catch (Exception e) {
    				Log.log(e);
    			}


                //Note: the order is important
                stores.add(LiClipseTextEditorPlugin.getDefault().getPreferenceStore());
                stores.add(EditorsUI.getPreferenceStore());

            } else {
                //In tests
                stores.add(new PreferenceStore());
            }

            chainedPreferenceStore = new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
        }
        return chainedPreferenceStore;
    }

    public static List<IPreferenceStore> getDefaultStores(boolean addEditorsUIStore) {
        List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>();
        stores.add(LiClipseTextEditorPlugin.getDefault().getPreferenceStore());
        if (addEditorsUIStore) {
            stores.add(EditorsUI.getPreferenceStore());
        }
        return stores;
    }

    public static final String USE_MARK_OCCURRENCES = "LICLIPSE_USE_MARK_OCCURRENCES";
    public static final boolean DEFAULT_USE_MARK_OCCURRENCES = true;

    public static final String USE_MATCHING_BRACKETS = "USE_MATCHING_BRACKETS";
    public static final boolean DEFAULT_USE_MATCHING_BRACKETS = true;

    //List string, string, int from swt constants.
    public static List<Object[]> NAME_COLOR_AND_STYLE = new ArrayList<Object[]>();

    public static final int NORMAL = 0;

    public static final int BOLD = 1 << 0;

    public static final int ITALIC = 1 << 1;

    //See: TextAttribute.UNDERLINE
    public static final int UNDERLINE = 1 << 30;

    //See: TextAttribute.STRIKETHROUGH
    public static final int STRIKETHROUGH = 1 << 29;

    static {
        NAME_COLOR_AND_STYLE.add(new Object[] { ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE, "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "foreground", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "singleLineComment", "63,127,95", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "multiLineComment", "63,127,95", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "background", "255,255,255", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "number", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "string", "42,0,255", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "bracket", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "operator", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "keyword", "127,0,85", BOLD });
        NAME_COLOR_AND_STYLE.add(new Object[] { "class", "0,0,0", BOLD });
        NAME_COLOR_AND_STYLE.add(new Object[] { "interface", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "enum", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "method", "0,0,0", BOLD });
        NAME_COLOR_AND_STYLE.add(new Object[] { "methodDeclaration", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "annotation", "100,100,100", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "localVariable", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "localVariableDeclaration", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "inheritedMethod", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "abstractMethod", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "staticMethod", "0,0,192", ITALIC });
        NAME_COLOR_AND_STYLE.add(new Object[] { "javadoc", "63,95,191", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "javadocTag", "127,127,159", BOLD });
        NAME_COLOR_AND_STYLE.add(new Object[] { "javadocKeyword", "127,159,191", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "javadocLink", "63,63,191", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "field", "0,0,192", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "staticField", "0,0,192", ITALIC });
        NAME_COLOR_AND_STYLE.add(new Object[] { "staticFinalField", "0,0,192", ITALIC });
        NAME_COLOR_AND_STYLE.add(new Object[] { "parameterVariable", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "typeArgument", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "typeParameter", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "deprecatedMember", "0,0,0", STRIKETHROUGH });
        NAME_COLOR_AND_STYLE.add(new Object[] { "constant", "0,0,0", NORMAL });
        NAME_COLOR_AND_STYLE.add(new Object[] { "matchingBracket", "64,128,128", NORMAL });
    }

    public static final String MATCHING_BRACKETS_COLOR = "matchingBracket_COLOR";

}
