/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import java.util.ListResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.mark_occurrences.LiclipseMarkOccurrencesDispatcher;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCacheProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseColorCache;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.OutlineData;
import org.brainwy.liclipsetext.editor.outline.LiClipseOutlinePage;
import org.brainwy.liclipsetext.editor.outline.LiClipseParsedModel;
import org.brainwy.liclipsetext.editor.parsing.LiClipseParserManager;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditPairMatcher;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyBackspaceHelper;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyHelper;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyScopeCreationHelper;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallbackListener;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.model.ErrorDescription;
import org.brainwy.liclipsetext.shared_core.model.ISimpleNode;
import org.brainwy.liclipsetext.shared_core.parsing.BaseParserManager;
import org.brainwy.liclipsetext.shared_core.parsing.IParser;
import org.brainwy.liclipsetext.shared_core.parsing.IScopesParser;
import org.brainwy.liclipsetext.shared_core.string.ICharacterPairMatcher2;
import org.brainwy.liclipsetext.shared_core.utils.BaseExtensionHelper;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.editor.IEditListener;
import org.brainwy.liclipsetext.shared_ui.outline.IOutlineModel;
import org.brainwy.liclipsetext.shared_ui.utils.RunInUiThread;

public class BaseLiClipseEditor extends AbstractLiClipseEditor implements ILiClipseLanguageProvider,
        IExecutableExtension {

    private static final String CONTEXT_MENU_ID = "#LiClipseContext";
    private static final String CONTEXT_RULER_MENU_ID = "#LiClipseRulerContext";
    private ICallbackListener<String> listener;
    private DefaultCharacterPairMatcher pairMatcher;
    protected boolean disposed = false;
    private LiClipseNode root;
    private String initialLanguage;
    private volatile long astModificationStamp;
    private IContentOutlinePage outlinePage;

    public BaseLiClipseEditor() {
        super();
        setSourceViewerConfiguration(new LiClipseSourceViewerConfiguration(LiClipseTextEditorPlugin.getDefault()
                .getColorManager(), LiClipseTextPreferences.getChainedPreferenceStore()));
        LiClipseDocumentProvider documentProvider = new LiClipseDocumentProvider(this.getClass());
        setDocumentProvider(documentProvider);
        notifier.notifyEditorCreated();

        IEditListener liclipseMarkOccurrencesDispatcher = new LiclipseMarkOccurrencesDispatcher();
        this.addPyeditListener(liclipseMarkOccurrencesDispatcher);
    }

    @Override
    public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
        super.setInitializationData(cfig, propertyName, data);
        if (data instanceof String) {
            this.initialLanguage = (String) data;
            IDocumentProvider documentProvider = this.getDocumentProvider();
            if (documentProvider instanceof LiClipseDocumentProvider) {
                LiClipseDocumentProvider liClipseDocumentProvider = (LiClipseDocumentProvider) documentProvider;
                liClipseDocumentProvider.setInitialLanguage(this.initialLanguage);
            }
        } else if (data != null) {
            Log.log("Expected string in initialization data. Received: " + data);
        }
    }

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        fAnnotationAccess = getAnnotationAccess();
        fOverviewRuler = createOverviewRuler(getSharedColors());

        LiClipseSourceViewer viewer = new LiClipseSourceViewer(parent, ruler, getOverviewRuler(),
                isOverviewRulerVisible(), styles, this);
        viewer.appendVerifyKeyListener(AutoEditStrategyBackspaceHelper.createVerifyKeyListener(viewer, this));

        viewer.appendVerifyKeyListener(AutoEditStrategyScopeCreationHelper.createVerifyKeyListener(viewer,
                new DefaultScopeCreatingCharsProvider(this)));

        // ensure decoration support has been created and configured.
        getSourceViewerDecorationSupport(viewer);

        return viewer;
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);
        RunInUiThread.async(new Runnable() {

            public void run() {
                if (!BaseLiClipseEditor.this.disposed) {
                    // When using rules with the OFF_PARTITION, if we don't invalidate the text presentation
                    // the rules may be invalid (because the way that we compute the deltas is not smart
                    // enough for this rule), so, for now, always invalidate the text presentation
                    // on a save so that things do appear properly at least after saving the file.
                    ISourceViewer sourceViewer = getSourceViewer();
                    IRegion visibleRegion = sourceViewer.getVisibleRegion();
                    ((ITextViewerExtension2) sourceViewer).invalidateTextPresentation(visibleRegion.getOffset(),
                            visibleRegion.getLength());
                }
            }
        });
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        updateStateDependentActions(); //Needed to enable actions dependent on state (i.e.: code-completion).
    }

    public String getIndentationString() {
        LiClipseSourceViewerConfiguration sourceViewerConfiguration = (LiClipseSourceViewerConfiguration) getSourceViewerConfiguration();
        return sourceViewerConfiguration.getIndentString();
    }

    @Override
    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);

        support.setCharacterPairMatcher(getCharacterPairMatcher());

        support.setMatchingCharacterPainterPreferenceKeys(LiClipseTextPreferences.USE_MATCHING_BRACKETS,
                LiClipseTextPreferences.MATCHING_BRACKETS_COLOR);
    }

    public ICharacterPairMatcher getCharacterPairMatcher() {
        if (pairMatcher == null) {
            //TODO: Set the matcher depending on the partition and language.
            pairMatcher = new DefaultCharacterPairMatcher(
                    new char[] { '(', ')', '{', '}', '[', ']', '<', '>' }, IDocumentExtension3.DEFAULT_PARTITIONING);
        }
        return pairMatcher;
    }

    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { "org.brainwy.liclipsetext.editor.context" });
    }

    public static class ActionsResourceBundle extends ListResourceBundle {
        @Override
        public Object[][] getContents() {
            return contents;
        }

        static final Object[][] contents = { { "CorrectionAssist", "CorrectionAssist" },
                { "ContentAssistProposal", "ContentAssistProposal" }, { "TemplateProposals", "TemplateProposals" }, };
    }

    @Override
    protected void createActions() {
        super.createActions();

        ActionsResourceBundle resources = new ActionsResourceBundle();

        notifier.notifyOnCreateActions(resources);
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        try {
            IPreferenceStore chainedPreferenceStore = LiClipseTextPreferences.getChainedPreferenceStore();
            this.setPreferenceStore(chainedPreferenceStore);
            setEditorContextMenuId(CONTEXT_MENU_ID);
            setRulerContextMenuId(CONTEXT_RULER_MENU_ID);

            listener = createColorManagerListener(this);
            IColorCache colorManager = LiClipseTextEditorPlugin.getDefault().getColorManager();
            colorManager.registerOnReloadColorsListener(listener);
        } catch (Throwable e) {
            Log.log(e);
        }
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        markInitFinished();
    }

    @Override
    public void dispose() {
        if (!this.disposed) {
            this.disposed = true;
            this.outlinePage = null;
            if (listener != null) {
                try {
                    IColorCache colorManager = LiClipseTextEditorPlugin.getDefault().getColorManager();
                    colorManager.unregisterOnReloadColorsListener(listener);
                } catch (Exception e) {
                    Log.log(e);
                }
            }
            try {
                notifier.notifyOnDispose();
                getParserManager().notifyEditorDisposed(this);

                cache.clear();
                cache = null;

                synchronized (registeredEditListeners) {
                    registeredEditListeners.clear();
                }

            } catch (Throwable e) {
                Log.log(e);
            }
        }
        super.dispose();
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    /**
     * Helper to invalidate the presentation only once!
     */
    private static final class InvalidateTextPresentation implements Runnable {
        private final BaseLiClipseEditor editor;

        private InvalidateTextPresentation(BaseLiClipseEditor editor) {
            this.editor = editor;
        }

        public void run() {
            editor.getSourceViewer().invalidateTextPresentation();
            synchronized (scheduledLock) {
                scheduled = null;
            }
        }
    }

    /**
     * If we have an instance here, don't create another as an invalidate is already scheduled.
     */
    private static InvalidateTextPresentation scheduled;
    private static final Object scheduledLock = new Object();

    public static ICallbackListener<String> createColorManagerListener(final BaseLiClipseEditor editor) {
        return new ICallbackListener<String>() {

            /**
             * Note: only called if a color has changed, so, always invalidate (no need to check).
             */
            public Object call(String colorName) {
                try {
                    synchronized (scheduledLock) {
                        if (scheduled == null) {
                            scheduled = new InvalidateTextPresentation(editor);
                            //Do it asynchronously (so that if many colors change at once we only have a single
                            //update).
                            RunInUiThread.async(scheduled);
                        }
                    }
                } catch (Exception e) {
                    Log.log(e);
                }
                return null;
            }
        };
    }

    public String getEncoding() {
        final IFile file = getEditorInput().getAdapter(IFile.class);
        String encoding = "utf-8";
        if (file != null) {
            try {
                encoding = file.getCharset();
            } catch (CoreException e) {
                Log.log(e);
            }
        }
        return encoding;
    }

    @Override
    public IStatusLineManager getStatusLineManager() {
        return EditorUtils.getStatusLineManager(this);
    }

    @Override
    public void revealModelNodes(ISimpleNode[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return; // nothing to see here
        }

        IDocument document = getDocument();
        if (document == null) {
            return;
        }

        ISimpleNode node = nodes[0];
        if (node instanceof LiClipseNode) {
            LiClipseNode liClipseNode = (LiClipseNode) node;
            OutlineData data = liClipseNode.getData();
            IRegion region = data.region;
            setSelection(region.getOffset(), region.getLength());
        }
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (IContentOutlinePage.class.equals(adapter)) {
            return getOutlinePage();
        }
        return super.getAdapter(adapter);
    }

    private IContentOutlinePage getOutlinePage() {
        if (this.outlinePage == null) {
            this.outlinePage = new LiClipseOutlinePage(this);
        }
        return this.outlinePage;
    }

    @Override
    public IOutlineModel createOutlineModel() {
        return new LiClipseParsedModel(this);
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        IEditorInput oldInput = this.getEditorInput();

        super.doSetInput(input);

        try {
            IDocument document = null;
            IDocumentProvider documentProvider = getDocumentProvider();
            if (documentProvider != null) {
                document = documentProvider.getDocument(input);
            }
            LiClipseParserManager.getParserManager().attachParserTo(this);
            notifier.notifyInputChanged(oldInput, input);
            notifier.notifyOnSetDocument(document);
        } catch (Exception e) {
            Log.log(e);
        }
    }

    @Override
    protected BaseParserManager getParserManager() {
        return LiClipseParserManager.getParserManager();
    }

    public void parserChanged(ISimpleNode root, IAdaptable file, IDocument doc, long docModificationStamp) {
        fireModelChanged(root);
        this.root = (LiClipseNode) root;
        this.astModificationStamp = docModificationStamp;
    }

    public LiClipseNode getOutline() {
        return this.root;
    }

    public LiClipseNode getAst() {
        return this.root;
    }

    public void parserError(Throwable error, IAdaptable file, IDocument doc) {
        //TODO: We need a better error reporting for ast errors.
        fireParseErrorChanged(new ErrorDescription(error.getMessage(), 0, 0, 0));
    }

    public void reparseAndInvalidateTextPresentation() {
        IParser parser = LiClipseParserManager.getParserManager().getParser(this);
        if (parser != null) {
            parser.forceReparse();
        }
        LiClipseSourceViewer sourceViewer = getEditorSourceViewer();
        sourceViewer.invalidateTextPresentation();
    }

    /**
     * Can be null.
     */
    public LiClipseLanguage getLanguage() {
        return this.getLiClipseLanguage();
    }

    @Override
    public ICharacterPairMatcher2 getPairMatcher() {
        return new AutoEditPairMatcher(AutoEditStrategyHelper.BRACKETS, IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Override
    public IScopesParser createScopesParser() {
        return getLanguage().createScopesParser();
    }

    public int getTabWidth() {
        return this.getSourceViewerConfiguration().getTabWidth(getSourceViewer());
    }

    @Override
    public IAnnotationAccess getAnnotationAccess() {
    	return super.getAnnotationAccess();
    }
    @Override
    public MarkerAnnotationPreferences getAnnotationPreferences() {
    	return super.getAnnotationPreferences();
    }
    @Override
    protected IOverviewRuler createOverviewRuler(ISharedTextColors sharedColors) {
        try {
        	ILiClipseEditorCustomizer editorCustomizer = (ILiClipseEditorCustomizer) BaseExtensionHelper.getParticipant(
        			"org.brainwy.liclipsetext.editor.liclipse_editor_customizer", false);
        	if(editorCustomizer != null){
        		IOverviewRuler ruler = editorCustomizer.createOverviewRuler(this, sharedColors);
        		if(ruler != null){
        			return ruler;
        		}
        	}
		} catch (Exception e) {
			Log.log(e);
		}
    	return super.createOverviewRuler(sharedColors);
    }

}
