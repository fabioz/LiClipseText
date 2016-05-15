/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ITextAttributeProviderToken;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.callbacks.CallbackWithListeners;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallbackListener;
import org.brainwy.liclipsetext.shared_ui.ColorCache;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;

public class LiClipseColorCache extends ColorCache implements IColorCache {

    /**
     * Called with the name of the color that was reloaded (i.e.: the name without _COLOR or _STYLE).
     */
    private final CallbackWithListeners<String> onReloadColors = new CallbackWithListeners<String>();

    @Override
    public void registerOnReloadColorsListener(ICallbackListener<String> listener) {
    	onReloadColors.registerListener(listener);
    }

    @Override
    public void unregisterOnReloadColorsListener(ICallbackListener<String> listener) {
    	onReloadColors.unregisterListener(listener);
    }

    public LiClipseColorCache(IPreferenceStore prefs) {
        super(prefs);
    }

    /**
     * @return if the given property is a color or style related property.
     */
    public static boolean isColorOrStyleProperty(String property) {
        return property.endsWith("_COLOR") || property.endsWith("_STYLE");
    }



    /* (non-Javadoc)
	 * @see org.brainwy.liclipsetext.editor.common.partitioning.IColorCache#isValidScope(java.lang.String)
	 */
    @Override
	public boolean isValidScope(String colorName) {
        String colorCode = preferences.getString(colorName + "_COLOR");
        if (colorCode.length() == 0) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
	 * @see org.brainwy.liclipsetext.editor.common.partitioning.IColorCache#reloadProperty(java.lang.String)
	 */
    @Override
    public void reloadProperty(String name) {
        super.reloadProperty(name);
        onReloadColors.call(name);
        updateTextAttributes(contentTypeToTokens, name);
    }

    /* (non-Javadoc)
	 * @see org.brainwy.liclipsetext.editor.common.partitioning.IColorCache#checkReloadProperty(java.lang.String)
	 */
    @Override
	public void checkReloadProperty(String property) {
        if (isColorOrStyleProperty(property)) {
            reloadProperty(property);
        }
    }

    private final Map<String, Set<ITextAttributeProviderToken>> contentTypeToTokens = new HashMap<>();

    /* (non-Javadoc)
	 * @see org.brainwy.liclipsetext.editor.common.partitioning.IColorCache#registerContentTypeToken(org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken, org.brainwy.liclipsetext.editor.languages.LiClipseLanguage)
	 */
    @Override
	public void registerContentTypeToken(ContentTypeToken token, LiClipseLanguage language) {
        String contentType = (String) token.getData();

        synchronized (contentTypeToTokens) {
            String colorName = getColorTokenNameForContentType(contentType, language);
            LiClipseTextAttribute textAttribute = this.getTextAttribute(contentType, colorName);
            token.setTextAttribute(textAttribute);

            Set<ITextAttributeProviderToken> weakHashSet = contentTypeToTokens.get(contentType);
            if (weakHashSet == null) {
                weakHashSet = Collections
                        .newSetFromMap(new WeakHashMap<ITextAttributeProviderToken, Boolean>());
                contentTypeToTokens.put(contentType, weakHashSet);
            }

            weakHashSet.add(token);
        }
    }

	private String getColorTokenNameForContentType(String contentType, LiClipseLanguage language) {
        String colorName = language.contentTypeToColorTokenName.get(contentType);
        if (colorName == null) {
            if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                colorName = "foreground";
            } else {
                colorName = contentType;
            }
        }
        return colorName;
    }

    /**
     * Traverse all the entries and update the text attribute if it matches the passed
     * color name.
     */
    private void updateTextAttributes(Map<String, Set<ITextAttributeProviderToken>> contentTypeToTokens2,
            String colorName) {
        Set<Entry<String, Set<ITextAttributeProviderToken>>> entrySet2 = contentTypeToTokens2.entrySet();
        for (Entry<String, Set<ITextAttributeProviderToken>> entry : entrySet2) {
            Set<ITextAttributeProviderToken> value = entry.getValue();
            for (ITextAttributeProviderToken token : value) {
                LiClipseTextAttribute data = token.getPreviouslySetTextAttribute();
                if (colorName.isEmpty() || colorName.equals(data.colorName)) {
                    LiClipseTextAttribute textAttribute = getTextAttribute(data.contentType, data.colorName);
                    token.setTextAttribute(textAttribute);
                }
            }
        }
    }

    protected LiClipseTextAttribute getTextAttribute(String contentType, String colorName) {
    	String temp = colorName;
    	if(!"".equals(preferences.getString(temp+"_COLOR"))){
    		// Ok, found.
    		return new LiClipseTextAttribute(contentType, colorName, getNamedColor(temp+"_COLOR"), null,
    				preferences.getInt(temp+"_STYLE"));
    	}
        do {
        	String mapped = TM_TO_COLOR_THEME_MAPPINGS.get(temp);
        	if(mapped != null){
        		temp = mapped;
        	}
        	if(!"".equals(preferences.getString(temp+"_COLOR"))){
        		// Ok, found.
        		return new LiClipseTextAttribute(contentType, colorName, getNamedColor(temp+"_COLOR"), null,
        				preferences.getInt(temp+"_STYLE"));
        	}else{
        		// Not found
        		int i = temp.lastIndexOf('.');
        		if (i == -1) {
        			break;
        		}
        		temp = temp.substring(0, i);
        	}
        } while (true);

        //Fallback is returning foreground color
    	return new LiClipseTextAttribute(contentType, colorName, getNamedColor("foreground_COLOR"), null,
    			preferences.getInt("foreground_STYLE"));
	}



	private static Map<String, String> TM_TO_COLOR_THEME_MAPPINGS = new HashMap<>();

	static{
		//NOTE: The order of the elements is important!
		//Non-repeatable with higher priority replacements at the top.
		//Note that the color theme entries may be repeated (as we have less colors)
		//and the TM entries should not be repeated.
		TM_TO_COLOR_THEME_MAPPINGS.put("comment.block.documentation", ColorKeys.JAVADOC_KEYWORD);

		TM_TO_COLOR_THEME_MAPPINGS.put("comment.block", ColorKeys.MULTI_LINE_COMMENT);
		TM_TO_COLOR_THEME_MAPPINGS.put("comment", ColorKeys.SINGLE_LINE_COMMENT);

		TM_TO_COLOR_THEME_MAPPINGS.put("constant.numeric", ColorKeys.NUMBER);
		TM_TO_COLOR_THEME_MAPPINGS.put("constant.language", ColorKeys.CONSTANT);
		TM_TO_COLOR_THEME_MAPPINGS.put("constant.character", ColorKeys.LOCAL_VARIABLE_DECLARATION);
		TM_TO_COLOR_THEME_MAPPINGS.put("constant.other", ColorKeys.ENUM);
		TM_TO_COLOR_THEME_MAPPINGS.put("constant", ColorKeys.CONSTANT);

		TM_TO_COLOR_THEME_MAPPINGS.put("entity.other.inherited-class", ColorKeys.INHERITED_METHOD); //Subclass declaration?
		TM_TO_COLOR_THEME_MAPPINGS.put("entity.name.function", ColorKeys.METHOD_DECLARATION);
		TM_TO_COLOR_THEME_MAPPINGS.put("entity.name.tag", ColorKeys.JAVADOC_TAG);
		TM_TO_COLOR_THEME_MAPPINGS.put("entity.other.attribute-name", ColorKeys.FIELD); //Tag attribute
		TM_TO_COLOR_THEME_MAPPINGS.put("entity.name", ColorKeys.CLASS);

		TM_TO_COLOR_THEME_MAPPINGS.put("string", ColorKeys.STRING);

		TM_TO_COLOR_THEME_MAPPINGS.put("variable.parameter", ColorKeys.PARAMETER_VARIABLE);
		TM_TO_COLOR_THEME_MAPPINGS.put("variable", ColorKeys.LOCAL_VARIABLE);

		TM_TO_COLOR_THEME_MAPPINGS.put("keyword", ColorKeys.KEYWORD);

		TM_TO_COLOR_THEME_MAPPINGS.put("storage.type", ColorKeys.METHOD);

		TM_TO_COLOR_THEME_MAPPINGS.put("support.function", ColorKeys.STATIC_METHOD);
		TM_TO_COLOR_THEME_MAPPINGS.put("support.class", ColorKeys.TYPE_ARGUMENT);
		TM_TO_COLOR_THEME_MAPPINGS.put("support.type", ColorKeys.TYPE_PARAMETER);
		TM_TO_COLOR_THEME_MAPPINGS.put("support.constant", ColorKeys.STATIC_FINAL_FIELD);
		TM_TO_COLOR_THEME_MAPPINGS.put("support.variable", ColorKeys.ABSTRACT_METHOD);
		TM_TO_COLOR_THEME_MAPPINGS.put("support", ColorKeys.ABSTRACT_METHOD);

		TM_TO_COLOR_THEME_MAPPINGS.put("invalid.deprecated", ColorKeys.DEPRECATED_MEMBER);
		TM_TO_COLOR_THEME_MAPPINGS.put("invalid", ColorKeys.DELETION_INDICATION);

		TM_TO_COLOR_THEME_MAPPINGS.put("punctuation", ColorKeys.OPERATOR);

		// Lower priority replacements at the bottom (could repeat entries on some side)
		TM_TO_COLOR_THEME_MAPPINGS.put("storage", ColorKeys.KEYWORD);
		TM_TO_COLOR_THEME_MAPPINGS.put("name", ColorKeys.METHOD_DECLARATION);
		TM_TO_COLOR_THEME_MAPPINGS.put("entity", ColorKeys.CONSTANT);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup", ColorKeys.KEYWORD);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.quote", ColorKeys.STRING);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.other", ColorKeys.INTERFACE);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.list", ColorKeys.STRING);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.heading", ColorKeys.KEYWORD);
		TM_TO_COLOR_THEME_MAPPINGS.put("punctuation.definition", ColorKeys.KEYWORD);

		TM_TO_COLOR_THEME_MAPPINGS.put("markup.underline", ColorKeys.JAVADOC_LINK);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.link", ColorKeys.JAVADOC_LINK);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.italic", ColorKeys.STRING);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.bold", ColorKeys.STRING);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.heading", ColorKeys.KEYWORD);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.list", ColorKeys.NUMBER);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.quote", ColorKeys.STRING);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.raw", ColorKeys.STRING);
		TM_TO_COLOR_THEME_MAPPINGS.put("markup.other", ColorKeys.STRING);
	}

}
