/**
 * Copyright (c) 2016 Red Hat Inc.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;

public class LiclipsePresentationReconcilierExt extends PresentationReconciler {
	
	public class LiclipseDelegatePresentationDamager implements IPresentationDamager, IPresentationRepairer {

		@Override
		public void setDocument(IDocument document) {
			initializeDelegateReconcilier(document);
		}

		@Override
		public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event,
				boolean documentPartitioningChanged) {
			if (delegate == null && viewer.getDocument() != null) {
				initializeDelegateReconcilier(viewer.getDocument());
			}
			if (delegate != null) {
				return delegate.getDamager(partition.getType()).getDamageRegion(partition, event, documentPartitioningChanged);
			} else {
				return null;
			}
		}

		@Override
		public void createPresentation(TextPresentation presentation, ITypedRegion damage) {
			if (delegate == null && viewer.getDocument() != null) {
				initializeDelegateReconcilier(viewer.getDocument());
			}
			if (delegate != null) {
				delegate.getRepairer(damage.getType()).createPresentation(presentation, damage);
			}
		}

	}

	private LiClipseDocumentPartitioner partitioner;
	private IPresentationReconciler delegate;
	private Object currentDocument;
	private ITextViewer viewer;
	private boolean inProgress;

	public LiclipsePresentationReconcilierExt() {
		LiclipseDelegatePresentationDamager damager = new LiclipseDelegatePresentationDamager();
		setDamager(damager, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(damager, IDocument.DEFAULT_CONTENT_TYPE);
	}
	
	@Override
	public void install(ITextViewer viewer) {
		this.viewer = viewer;
		super.install(viewer);
	}
	
	private IPresentationReconciler initializeDelegateReconcilier(IDocument document) {
		if (!inProgress && document != null && document != this.currentDocument) {
			inProgress = true;
			this.currentDocument = document;
			String fileName = ITextFileBufferManager.DEFAULT.getTextFileBuffer(document).getLocation().lastSegment();
		    LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
			LiClipseLanguage language = languagesManager.getLanguageForFilename(fileName);
			this.partitioner = language.connect(document);
			this.delegate = partitioner.getPresentationReconciler(new LiClipseColorCache(LiClipseTextPreferences.getChainedPreferenceStore()));
			delegate.install(viewer);
			inProgress = false;
		}
		return this.delegate;
	}

	
	@Override
	public void uninstall() {
		if (this.delegate != null) {
			this.delegate.uninstall();
			this.delegate = null;
		}
		if (this.partitioner != null) {
			this.partitioner.disconnect();
			this.partitioner = null;
		}
	}

}
