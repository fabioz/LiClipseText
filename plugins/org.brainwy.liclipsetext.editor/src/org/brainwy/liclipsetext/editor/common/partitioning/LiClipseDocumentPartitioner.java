/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.tm4e.Tm4ePartitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadata;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadata.LanguageType;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.LiClipseTm4ePartitionScanner;
import org.brainwy.liclipsetext.editor.rules.SwitchLanguageToken;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.IToken;

/**
 * Make it possible to do:
 *
 * LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
 */
public final class LiClipseDocumentPartitioner extends LiClipseDocumentPartitionerTmCache {

    public static final String PARTITION_TYPE = IDocumentExtension3.DEFAULT_PARTITIONING;

    private CustomTextAttributeTokenCreator defaultTokenCreator;

    public LiClipseDocumentPartitioner(LiClipseLanguage language) {
        this(language.getLegalContentTypes(), language, createContentTypeDefinitionScanner(language));
    }

    private LiClipseDocumentPartitioner(String[] legalContentTypes, LiClipseLanguage language,
            ICustomPartitionTokenScanner tokenScanner) {
        super(tokenScanner, legalContentTypes, language);

        defaultTokenCreator = language.getDefaultTokenCreator();
    }

    /**
     * Create the partitioner which will define the content types.
     */
    public static ICustomPartitionTokenScanner createContentTypeDefinitionScanner(
            LiClipseLanguage language) {
        if (language.languageType == LanguageType.TEXT_MATE) {
            ICustomPartitionTokenScanner scanner = new LiClipseTm4ePartitionScanner();
            scanner.setDefaultReturnToken(new ContentTypeToken(language.name));
            return scanner;
        } else {
            return new LiClipseContentTypeDefinitionScanner(language);
        }
    }

    /**
     * Here we setup scanners, which gives colors to tokens inside a partition (content type).
     */
    public IPresentationReconciler getPresentationReconciler(final IColorCache colorManager) {
        final LiClipsePresentationReconciler reconciler = new LiClipsePresentationReconciler();
        updateReconciler(colorManager, reconciler);
        return reconciler;
    }

    public void createTokenScanners(IColorCache colorManager) {
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        for (Entry<String, String> contentTypeToColor : language.contentTypeToColorTokenName.entrySet()) {
            String contentType = contentTypeToColor.getKey();
            IToken defaultReturnToken = new ContentTypeToken(contentType);
            Tuple<String, String> topLevelLanguage = SwitchLanguageToken.getSubLanguageAndContentType(contentType);
            if (topLevelLanguage != null && !topLevelLanguage.o1.equals("this")) {
                LiClipseLanguage subLanguage = languagesManager.getLanguageFromName(topLevelLanguage.o1);
                if (subLanguage != null) {
                    createTokenScannerForContentType(contentType, topLevelLanguage.o2,
                            defaultReturnToken, contentTypeToScanner, subLanguage);
                    continue;

                }
            }
            createTokenScannerForContentType(contentType, contentType, defaultReturnToken,
                    contentTypeToScanner, language);
        }
    }

    private void updateReconciler(final IColorCache colorManager, final LiClipsePresentationReconciler reconciler) {
        createTokenScanners(colorManager);

        for (Entry<String, String> contentTypeToColor : language.contentTypeToColorTokenName.entrySet()) {
            String contentType = contentTypeToColor.getKey();
            ICustomPartitionTokenScanner scanner = getTokenScannerForContentType(contentType);
            if (scanner == null) {
                Log.log("ERROR: Unable to find scanner for content type: " + contentType);
                scanner = new SingleTokenScanner();
            }
            LiClipseDamagerRepairer dr = new LiClipseDamagerRepairer(scanner, defaultTokenCreator);
            reconciler.setDamager(dr, contentType);
            reconciler.setRepairer(dr, contentType);
        }
    }

    private Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();

    /**
     * Make it possible to get the token scanner for a given content type.
     * May return null!
     */
    public ICustomPartitionTokenScanner getTokenScannerForContentType(String contentType) {
        return contentTypeToScanner.get(contentType);
    }

    /**
     * Used to get the scanner in a different context (so, the contentTypeToScanner is passed as
     * this different context -- and as a cache).
     */
    public ICustomPartitionTokenScanner obtainTokenScannerForContentType(String contentType,
            Map<String, ICustomPartitionTokenScanner> contentTypeToScanner,
            LiClipseLanguage language) {
        ICustomPartitionTokenScanner ret = contentTypeToScanner.get(contentType);
        if (ret == null) {

            LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
            Tuple<String, String> topLevelLanguage = SwitchLanguageToken.getSubLanguageAndContentType(contentType);
            if (topLevelLanguage != null && !topLevelLanguage.o1.equals("this")) {
                LiClipseLanguage subLanguage = languagesManager.getLanguageFromName(topLevelLanguage.o1);
                if (subLanguage != null) {
                    IToken defaultReturnToken = new ContentTypeToken(topLevelLanguage.o2);
                    createTokenScannerForContentType(contentType, topLevelLanguage.o2,
                            defaultReturnToken, contentTypeToScanner, subLanguage);
                    return contentTypeToScanner.get(contentType);
                }
            }

            IToken defaultReturnToken = new ContentTypeToken(contentType);

            createTokenScannerForContentType(contentType, contentType, defaultReturnToken,
                    contentTypeToScanner, language);
            ret = contentTypeToScanner.get(contentType);
        }
        return ret;
    }

    private static ICustomPartitionTokenScanner createTokenScannerForContentType(
            String contentType, String subContentType, IToken defaultReturnToken,
            Map<String, ICustomPartitionTokenScanner> contentTypeToScanner,
            LiClipseLanguage language) {
        ICustomPartitionTokenScanner scanner = contentTypeToScanner.get(contentType);
        if (scanner != null) {
            return scanner;
        }

        if (language.languageType == LanguageType.TEXT_MATE) {
            try {
                scanner = new Tm4ePartitionScanner(language);
            } catch (Exception e) {
                Log.log("Error creating tm4e parser. No coloring will be available.", e);
                scanner = new SingleTokenScanner();
            }
        } else {
            ScopeColorScanning scopeColoringScanning = language.scopeToScopeColorScanning.get(subContentType);
            if (scopeColoringScanning == null || scopeColoringScanning.empty()) {
                //Color everything with a single color
                scanner = new SingleTokenScanner();

            } else {
                scanner = new LiClipsePartitionScanner(scopeColoringScanning, language);
            }
        }
        scanner.setDefaultReturnToken(defaultReturnToken);
        contentTypeToScanner.put(contentType, scanner);
        return scanner;
    }

    public LanguageMetadata getLanguageMetadata() {
        return language.getLanguageMetadata();
    }

    public String[] getSpellCheckingContentTypes() {
        return language.getSpellCheckingContentTypes();
    }

}