/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda and others.
 * All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.completions.LiClipseCompletionProposal;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateCompletionProcessor;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

public class LiClipseContentAssistProcessor2 implements IContentAssistProcessor {

    private LiClipseDocumentPartitioner documentPartitioner = null;
    private String errorMessage = null;

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
    	IEvaluationContext context = PlatformUI.getWorkbench().getService(IHandlerService.class).getCurrentState();
		IEditorInput editorInput = (IEditorInput) context.getVariable(ISources.ACTIVE_EDITOR_INPUT_NAME);
    	if (editorInput == null && this.documentPartitioner == null) {
    		return new ICompletionProposal[0];
    	}
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
    	LiClipseLanguage language = languagesManager.getLanguageForFilename(editorInput.getName());
    	IDocument document = viewer.getDocument();
    	if (this.documentPartitioner == null || this.documentPartitioner.language != language) {
    		// TODO : find a way to store the partitioner (a data on the widget?) to reuse it across the various
    		// services
            this.documentPartitioner = language.createPartitioner();
            this.documentPartitioner.connect(document);
    	}
        errorMessage = null;

        List<ICompletionProposal> proposals = null;
    	ISelection selection = viewer.getSelectionProvider().getSelection();
        if (!language.useOnlyTemplatesOnCodeCompletion && selection instanceof TextSelection && ((TextSelection)selection).getLength() == 0) {
        	proposals = computeCompletions(offset, document, documentPartitioner.getContentType(offset));
        } else {
        	proposals = Collections.emptyList();
        }

        try {
            //templates are always there
            new LiClipseTemplateCompletionProcessor(documentPartitioner.language, documentPartitioner.getContentType(offset)).collectTemplateProposals(viewer,
	                    offset, proposals);
        } catch (Exception e) {
            Log.log(e);
            errorMessage = e.getMessage();
        }

        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    public List<ICompletionProposal> computeCompletions(int offset, IDocument document, String contentType) {
        TextSelectionUtils ts = new TextSelectionUtils(document, offset);
        Tuple<String, Integer> currToken;
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
        try {

                LiClipseLanguage language = this.documentPartitioner.language;
                Map<String, ScopeColorScanning> scopeToScopeColorScanning = language.scopeToScopeColorScanning;
                ScopeColorScanning scopeColorScanning = scopeToScopeColorScanning.get(contentType);
                if (scopeColorScanning != null) {
                    Set<Character> separatorChars = scopeColorScanning.getSeparatorChars();

                    currToken = ts.getCurrToken(separatorChars);
                    int delta = offset - currToken.o2;

                    String prefix = currToken.o1.substring(0, delta);
                    boolean convertToUpper = false;
                    if (language.caseInsensitive) {
                        String prefixLower = prefix.toLowerCase();
                        String prefixUpper = prefix.toUpperCase();

                        //If all the chars were upper-case in the prefix, convert it to upper (check if it does not match
                        //the lower to handle cases where the chars don't have a case, such as a dash, colon, etc).
                        convertToUpper = !prefixLower.equals(prefix) && prefixUpper.equals(prefix);
                        if (convertToUpper) {
                            prefix = prefixUpper;
                        } else {
                            prefix = prefixLower;
                        }

                    } else {
                        prefix = prefix.toLowerCase();
                    }

                    if (scopeColorScanning != null) {
                        Map<String, List<String>> tokenToWords = scopeColorScanning.tokenToWords;
                        Set<Entry<String, List<String>>> entrySet = tokenToWords.entrySet();
                        for (Entry<String, List<String>> entry : entrySet) {
                            List<String> value = entry.getValue();
                            int len = value.size();

                            for (int i = 0; i < len; i++) {
                                String replacementString = value.get(i);
                                int replacementLen = replacementString.length();
                                if (replacementLen < 2) {
                                    continue;
                                }
                                if (language.caseInsensitive) {
                                    //Check if we must complete with all uppercase letters.
                                    if (convertToUpper) {
                                        replacementString = replacementString.toUpperCase();
                                        if (!replacementString.startsWith(prefix)) {
                                            continue;
                                        }
                                    } else {
                                        if (!replacementString.toLowerCase().startsWith(prefix)) {
                                            continue;
                                        }
                                    }
                                } else {
                                    if (!replacementString.toLowerCase().startsWith(prefix)) {
                                        continue;
                                    }
                                }

                                int replacementOffset = currToken.o2;
                                int replacementLength = delta;
                                int cursorPosition = replacementLen;
                                Image image = null;
                                String displayString = null;
                                IContextInformation contextInformation = null;
                                String additionalProposalInfo = null;
                                int priority = 0;
                                int onApplyAction = LiClipseCompletionProposal.ON_APPLY_DEFAULT;
                                String args = "";
                                proposals.add(new LiClipseCompletionProposal(replacementString,
                                        replacementOffset, replacementLength, cursorPosition, image,
                                        displayString, contextInformation, additionalProposalInfo, priority,
                                        onApplyAction, args, separatorChars));
                            }

                        }
                    }
                }

        } catch (Exception e) {
            Log.log(e);
            errorMessage = e.getMessage();
        }
        return proposals;
    }

    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

}
