/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.spelling.SpellingService;
import org.brainwy.liclipsetext.editor.autoedit.BaseAutoEditStrategy;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.content_assist.BaseSimpleAssistProcessor;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.indent.LanguageIndent;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.preferences.LiClipseCodeCompletionPreferencesPage;
import org.brainwy.liclipsetext.editor.spelling.LiClipseSpellCheckerReconciler;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_ui.content_assist.AbstractCompletionProcessorWithCycling;
import org.brainwy.liclipsetext.shared_ui.content_assist.DefaultContentAssist;

public class LiClipseSourceViewerConfiguration extends TextSourceViewerConfiguration {

    private static final class LiClipseSimpleAssistProcessor extends BaseSimpleAssistProcessor {

        private LiClipseSimpleAssistProcessor(AbstractCompletionProcessorWithCycling defaultPythonProcessor,
                DefaultContentAssist assistant) {
            super(defaultPythonProcessor, assistant);
        }

        @Override
        public char[] getCompletionProposalAutoActivationCharacters() {
            if (LiClipseCodeCompletionPreferencesPage.useAutocompleteOnAllAsciiChars()) {
                return ALL_ASCII_CHARS;
            }
            return null;
        }
    }

    private IColorCache colorManager;
    private LiClipseDocumentPartitioner documentPartitioner;

    public LiClipseSourceViewerConfiguration(IColorCache colorManager, IPreferenceStore preferenceStore) {
        super(preferenceStore);
        this.colorManager = colorManager;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return documentPartitioner != null ? documentPartitioner.getLegalContentTypes()
                : new String[] { ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE };
    }

    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(
            ISourceViewer sourceViewer, String contentType) {
        return super.getDoubleClickStrategy(sourceViewer, contentType);
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer) {
        if (this.documentPartitioner == null) {
            Log.log("When getting presentation reconciler, document is expected to be configured already.");
            return new PresentationReconciler();
        } else {
            return documentPartitioner.getPresentationReconciler(this.colorManager);
        }
    }

    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
        SpellingService spellingService = EditorsUI.getSpellingService();
        if (spellingService.getActiveSpellingEngineDescriptor(fPreferenceStore) == null) {
            return null;
        }

        IReconcilingStrategy strategy = new LiClipseSpellCheckerReconciler(sourceViewer, spellingService);

        boolean isIncremental = true;
        MonoReconciler reconciler = new MonoReconciler(strategy, isIncremental);
        reconciler.setProgressMonitor(new NullProgressMonitor());
        reconciler.setDelay(500);
        return reconciler;
    }

    /**
     * Note: prefer setDocument(IDocument) instead.
     */
    public void setPartitioner(LiClipseDocumentPartitioner partitioner) {
        this.documentPartitioner = partitioner;
    }

    public void setDocument(IDocument document) {
        if (document != null) {
            LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                    .getDocumentPartitioner();
            this.documentPartitioner = documentPartitioner;
        } else {
            this.documentPartitioner = null;
        }
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        DefaultContentAssist contentAssistant = new DefaultContentAssist();

        if (documentPartitioner != null) {
            String[] legalContentTypes = documentPartitioner.getLegalContentTypes();
            for (String contentType : legalContentTypes) {
                IContentAssistProcessor processor = new LiClipseContentAssistProcessor(documentPartitioner,
                        contentType, contentAssistant);
                //Note: Code below can be used for having content assist called on all chars for the given partitions.
                //                if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                processor = new LiClipseSimpleAssistProcessor((LiClipseContentAssistProcessor) processor,
                        contentAssistant);
                //
                //                }
                contentAssistant.setContentAssistProcessor(processor, contentType);
            }
        }
        contentAssistant.setAutoActivationDelay(0);
        contentAssistant.enableAutoActivation(true); //always true, but the chars depend on whether it is activated or not in the preferences

        return contentAssistant;
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
        return new IAutoEditStrategy[] { new BaseAutoEditStrategy(this) };
    }

    @Override
    public int getTabWidth(ISourceViewer sourceViewer) {
        LiClipseLanguage language = this.getLanguage();
        if (language != null) {
            LanguageIndent indent = language.getIndent();
            Integer ret = indent.getTabWidth();
            if (ret != null) {
                return ret;
            }
        }
        return super.getTabWidth(sourceViewer);
    }

    public String getIndentString() {
        LiClipseLanguage language = this.getLanguage();
        return getIndentString(language);
    }

    public static String getIndentString(LiClipseLanguage language) {
        if (language != null) {
            LanguageIndent indent = language.getIndent();
            String ret = indent.getIndentString();
            if (ret != null) {
                return ret;
            }
        }
        return LanguageIndent.getDefaultIndentString();
    }

    /**
     * Prefixes used when indenting with shift+tab or just tab.
     */
    @Override
    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
        String[] indentPrefixes = new String[2];
        int tabWidth = this.getTabWidth(null);
        boolean spacesFirst = AbstractLiClipseEditor.isTabsToSpacesConversionEnabled(this.getLanguage());

        FastStringBuffer spaces = new FastStringBuffer(tabWidth).appendN(' ', tabWidth);

        if (spacesFirst) {
            indentPrefixes[0] = spaces.toString();
            indentPrefixes[1] = "\t";
        } else {
            indentPrefixes[0] = "\t";
            indentPrefixes[1] = spaces.toString();
        }

        return indentPrefixes;
    }

    public LiClipseLanguage getLanguage() {
        if (this.documentPartitioner == null) {
            return null;
        }
        return this.documentPartitioner.language;
    }

}