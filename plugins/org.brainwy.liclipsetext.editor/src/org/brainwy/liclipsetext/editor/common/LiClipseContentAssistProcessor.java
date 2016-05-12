/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.brainwy.liclipsetext.editor.common.completions.LiClipseCompletionProposal;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateCompletionProcessor;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_ui.content_assist.AbstractCompletionProcessorWithCycling;
import org.brainwy.liclipsetext.shared_ui.content_assist.DefaultContentAssist;

public class LiClipseContentAssistProcessor extends AbstractCompletionProcessorWithCycling {

    private final LiClipseDocumentPartitioner documentPartitioner;
    private final String contentType;
    private String errorMessage = null;
    private final boolean caseInsensitive;

    public LiClipseContentAssistProcessor(LiClipseDocumentPartitioner documentPartitioner, String contentType,
            DefaultContentAssist contentAssistant) {
        super(contentAssistant);
        this.documentPartitioner = documentPartitioner;
        this.caseInsensitive = this.documentPartitioner.language.caseInsensitive;
        this.contentType = contentType;

        if (contentAssistant != null) {
            contentAssistant.addCompletionListener(new ICompletionListener() {

                public void assistSessionEnded(ContentAssistEvent event) {
                }

                public void assistSessionStarted(ContentAssistEvent event) {
                    startCycle();
                }

                public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
                    //ignore
                }

            });
        }
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        updateStatus();
        errorMessage = null;
        IDocument document = viewer.getDocument();
        LiClipseSourceViewer liClipseSourceViewer = (LiClipseSourceViewer) viewer;
        BaseLiClipseEditor liClipseEditor = liClipseSourceViewer.getLiClipseEditor();
        TextSelectionUtils ts = liClipseEditor.createTextSelectionUtils();
        LiClipseLanguage liClipseLanguage = liClipseEditor.getLiClipseLanguage();

        List<ICompletionProposal> proposals;
        if (!liClipseLanguage.useOnlyTemplatesOnCodeCompletion && ts.getSelLength() == 0) {
            // if there's some text selected, don't use this selection (use only the templates which may have a selection).
            proposals = computeCompletions(offset, document);
        } else {
            proposals = new ArrayList<>();
        }

        try {
            //templates are always there
            new LiClipseTemplateCompletionProcessor(liClipseLanguage, contentType).collectTemplateProposals(viewer,
                    offset, proposals);
        } catch (Exception e) {
            Log.log(e);
            errorMessage = e.getMessage();
        }

        doCycle();
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    public List<ICompletionProposal> computeCompletions(int offset, IDocument document) {
        TextSelectionUtils ts = new TextSelectionUtils(document, offset);
        Tuple<String, Integer> currToken;
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
        try {

            if (whatToShow == SHOW_ALL) {
                LiClipseLanguage language = this.documentPartitioner.language;
                Map<String, ScopeColorScanning> scopeToScopeColorScanning = language.scopeToScopeColorScanning;
                ScopeColorScanning scopeColorScanning = scopeToScopeColorScanning.get(contentType);
                if (scopeColorScanning != null) {
                    Set<Character> separatorChars = scopeColorScanning.getSeparatorChars();

                    currToken = ts.getCurrToken(separatorChars);
                    int delta = offset - currToken.o2;

                    String prefix = currToken.o1.substring(0, delta);
                    boolean convertToUpper = false;
                    if (caseInsensitive) {
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
                                if (caseInsensitive) {
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
