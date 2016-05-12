/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.compare;

import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.DefaultScopeCreatingCharsProvider;
import org.brainwy.liclipsetext.editor.common.ILiClipseLanguageProvider;
import org.brainwy.liclipsetext.editor.common.LiClipseSourceViewerConfiguration;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyBackspaceHelper;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyScopeCreationHelper;
import org.brainwy.liclipsetext.shared_core.auto_edit.IIndentationStringProvider;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class AbstractMergeViewerCreator implements IViewerCreator {

    private String type;

    public AbstractMergeViewerCreator(String type) {
        this.type = type;
    }

    public Viewer createViewer(Composite parent, CompareConfiguration mp) {
        return new MergeViewer(parent, SWT.NULL, createNewCompareConfiguration(mp), type);
    }

    /**
     * Creates a new configuration with the pydev preference store so that the colors appear correctly when using
     * Aptana themes.
     *
     * Also copies the available data from the original compare configuration to the new configuration.
     */
    private CompareConfiguration createNewCompareConfiguration(CompareConfiguration mp) {
        List<IPreferenceStore> stores = LiClipseTextPreferences.getDefaultStores(false);
        IPreferenceStore prefs = mp.getPreferenceStore();
        if (prefs != null) {
            //Note, we could use the CompareUIPlugin.getDefault().getPreferenceStore() directly, but it's access
            //is restricted, so, we go to the preferences of the previously created compare configuration.
            stores.add(prefs);
        }

        CompareConfiguration cc = new CompareConfiguration(new ChainedPreferenceStore(
                stores.toArray(new IPreferenceStore[stores.size()])));
        cc.setAncestorImage(mp.getAncestorImage(null));
        cc.setAncestorLabel(mp.getAncestorLabel(null));

        cc.setLeftImage(mp.getLeftImage(null));
        cc.setLeftLabel(mp.getLeftLabel(null));
        cc.setLeftEditable(mp.isLeftEditable());

        cc.setRightImage(mp.getRightImage(null));
        cc.setRightLabel(mp.getRightLabel(null));
        cc.setRightEditable(mp.isRightEditable());

        try {
            cc.setContainer(mp.getContainer());
        } catch (Throwable e) {
            //Ignore: not available in Eclipse 3.2.
        }

        return cc;
    }

    static class MergeViewer extends TextMergeViewer implements ILiClipseLanguageProvider {

        private final String type;
        private final LiClipseLanguage language;
        private LiClipseDocumentPartitioner defaultPartitioner;

        public MergeViewer(Composite parent, int style, CompareConfiguration configuration, String type) {
            super(parent, style | SWT.LEFT_TO_RIGHT, configuration);
            this.type = type;

            LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
            this.language = languagesManager.getLanguageFromName(this.type);
            defaultPartitioner = language.createPartitioner();
        }

        @Override
        protected IDocumentPartitioner getDocumentPartitioner() {
            return defaultPartitioner;
        }

        @Override
        protected void setupDocument(IDocument document) {
            String partitioning = getDocumentPartitioning();
            IDocumentExtension3 ex3 = (IDocumentExtension3) document;
            if (ex3.getDocumentPartitioner(partitioning) == null) {
                IDocumentPartitioner partitioner = language.createPartitioner();
                if (partitioner != null) {
                    ex3.setDocumentPartitioner(partitioning, partitioner);
                    partitioner.connect(document);
                }
            }
        }

        /**
         * Overridden to handle the partitioning (will only work on Eclipse 3.3)
         */
        @Override
        protected String getDocumentPartitioning() {
            return LiClipseDocumentPartitioner.PARTITION_TYPE;
            //return IPythonPartitions.PYTHON_PARTITION_TYPE;
        }

        /**
         * Overridden to handle backspace (will only be called on Eclipse 3.5)
         */
        @Override
        protected SourceViewer createSourceViewer(Composite parent, int textOrientation) {
            final SourceViewer viewer = super.createSourceViewer(parent, textOrientation);

            IIndentationStringProvider indentProvider = new IIndentationStringProvider() {

                @Override
                public String getIndentationString() {
                    return LiClipseSourceViewerConfiguration.getIndentString(language);
                }
            };
            viewer.appendVerifyKeyListener(AutoEditStrategyBackspaceHelper.createVerifyKeyListener(viewer,
                    indentProvider));

            viewer.appendVerifyKeyListener(AutoEditStrategyScopeCreationHelper.createVerifyKeyListener(viewer,
                    new DefaultScopeCreatingCharsProvider(this)));

            //IWorkbenchPart workbenchPart = getCompareConfiguration().getContainer().getWorkbenchPart();
            //
            ////Note that any site should be OK as it's just to know if a keybinding is active.
            //IWorkbenchPartSite site = null;
            //if (workbenchPart != null) {
            //    site = workbenchPart.getSite();
            //} else {
            //    IWorkbenchWindow window = EditorUtils.getActiveWorkbenchWindow();
            //    if (window != null) {
            //        IWorkbenchPage activePage = window.getActivePage();
            //        if (activePage != null) {
            //            IWorkbenchPart activePart = activePage.getActivePart();
            //            if (activePart != null) {
            //                site = activePart.getSite();
            //            }
            //        }
            //    }
            //}
            //VerifyKeyListener createVerifyKeyListener = FirstCharAction.createVerifyKeyListener(viewer, site, true);
            //if (createVerifyKeyListener != null) {
            //    viewer.appendVerifyKeyListener(createVerifyKeyListener);
            //}
            return viewer;
        }

        @Override
        protected void configureTextViewer(TextViewer textViewer) {
            if (!(textViewer instanceof SourceViewer)) {
                return;
            }
            SourceViewer sourceViewer = (SourceViewer) textViewer;

            LiClipseSourceViewerConfiguration configuration = new LiClipseSourceViewerConfiguration(
                    LiClipseTextEditorPlugin.getDefault()
                            .getColorManager(), LiClipseTextPreferences.getChainedPreferenceStore());
            configuration.setPartitioner(defaultPartitioner); //Note: create a new partitioner each time so that we don't have the same for 2 different documents!
            sourceViewer.configure(configuration);
        }

        @Override
        protected void handleDispose(DisposeEvent event) {
            super.handleDispose(event);
        }

        @Override
        public LiClipseLanguage getLanguage() {
            return this.language;
        }

    }

}
