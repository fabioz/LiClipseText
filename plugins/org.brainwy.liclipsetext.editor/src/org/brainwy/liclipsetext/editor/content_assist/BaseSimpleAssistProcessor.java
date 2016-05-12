/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.content_assist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.content_assist.AbstractCompletionProcessorWithCycling;
import org.brainwy.liclipsetext.shared_ui.content_assist.DefaultContentAssist;

/**
 * This processor controls the completion cycle (and also works as a 'delegator' to the processor that deals
 * with actual python completions -- which may be a bit slower that simple completions).
 *
 * @author Fabio
 */
public abstract class BaseSimpleAssistProcessor implements IContentAssistProcessor {

    private class ContextInformationDelegator implements IContextInformationValidator, IContextInformationPresenter {
        private final IContextInformationValidator defaultContextInformationValidator;

        private ContextInformationDelegator(IContextInformationValidator defaultContextInformationValidator) {
            Assert.isTrue(defaultContextInformationValidator instanceof IContextInformationPresenter);
            this.defaultContextInformationValidator = defaultContextInformationValidator;
        }

        public void install(IContextInformation info, ITextViewer viewer, int offset) {
            defaultContextInformationValidator.install(info, viewer, offset);
        }

        public boolean isContextInformationValid(int offset) {
            if (showDefault()) {
                return defaultContextInformationValidator.isContextInformationValid(offset);
            }
            return true;
        }

        public boolean updatePresentation(int offset, TextPresentation presentation) {
            return ((IContextInformationPresenter) defaultContextInformationValidator).updatePresentation(offset,
                    presentation);
        }
    }

    public static final char[] ALL_ASCII_CHARS = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_' };

    //-------- cycling through simple completions and default processor
    private static final int SHOW_SIMPLE = 1;
    private static final int SHOW_DEFAULT = 2;
    private int whatToShow = SHOW_SIMPLE;

    public void startCycle() {
        whatToShow = SHOW_SIMPLE;
    }

    public void doCycle() {
        if (whatToShow == SHOW_SIMPLE) {
            whatToShow = SHOW_DEFAULT;
        }
        //cycles only once here
    }

    public void updateStatus() {
        if (whatToShow == SHOW_SIMPLE) {
            assistant.setIterationStatusMessage("Press %s for default completions.");
        }
    }

    //-------- end cycling through regular completions and templates

    /**
     * The 'default' processor (gets python completions)
     */
    private AbstractCompletionProcessorWithCycling defaultPythonProcessor;

    /**
     * The content assistant that contains this processor
     */
    private DefaultContentAssist assistant;

    /**
     * Participants for a simple completion
     */
    //    private List<ISimpleAssistParticipant> participants;

    /**
     * Whether the last completion was auto-activated or not
     */
    private boolean lastCompletionAutoActivated;

    /**
     * The last error that occurred while requesting a completion.
     */
    private String lastError = null;

    public BaseSimpleAssistProcessor(AbstractCompletionProcessorWithCycling defaultPythonProcessor,
            final DefaultContentAssist assistant) {
        this.defaultPythonProcessor = defaultPythonProcessor;
        this.assistant = assistant;
        //        this.participants = ExtensionHelper.getParticipants(ExtensionHelper.PYDEV_SIMPLE_ASSIST);

        assistant.addCompletionListener(new ICompletionListener() {
            public void assistSessionEnded(ContentAssistEvent event) {
            }

            public void assistSessionStarted(ContentAssistEvent event) {
                startCycle();
                lastCompletionAutoActivated = assistant.getLastCompletionAutoActivated();
                if (!lastCompletionAutoActivated) {
                    //user request... cycle to the default completions at once
                    doCycle();
                }
            }

            public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
                //ignore
            }
        });

    }

    /**
     * Computes the proposals (may forward for simple or 'complete' proposals)
     *
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        try {
            if (showDefault()) {
                return defaultPythonProcessor.computeCompletionProposals(viewer, offset);

            } else {
                //                updateStatus();
                //                IDocument doc = viewer.getDocument();
                //                String[] strs = PySelection.getActivationTokenAndQual(doc, offset, false);
                //
                //                String activationToken = strs[0];
                //                String qualifier = strs[1];
                //
                //                PySelection ps = edit.createPySelection();
                //                if (ps == null) {
                //                    return new ICompletionProposal[0];
                //                }
                //                List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
                //
                //                for (ISimpleAssistParticipant participant : participants) {
                //                    results.addAll(participant.computeCompletionProposals(activationToken, qualifier, ps, edit, offset));
                //                }
                //
                //                //don't matter the result... next time we won't ask for simple stuff
                //                doCycle();
                //                if (results.size() == 0) {
                //                    if (!lastCompletionAutoActivated || defaultAutoActivated(viewer, offset)
                //                            || useAutocompleteOnAllAsciiCharsCache) {
                //                        return defaultPythonProcessor.computeCompletionProposals(viewer, offset);
                //                    }
                return new ICompletionProposal[0];
                //                } else {
                //                    Collections.sort(results, IPyCodeCompletion.PROPOSAL_COMPARATOR);
                //                    return results.toArray(new ICompletionProposal[0]);
                //                }
            }
        } catch (Exception e) {
            Log.log(e);
            CompletionError completionError = new CompletionError(e);
            this.lastError = completionError.getErrorMessage();
            //Make the error visible to the user!
            return new ICompletionProposal[] { completionError };
        }
    }

    /**
     * Determines whether it was auto-activated on the default completion or in the simple one.
     * @param viewer the viewer for which this completion was requested
     * @param offset the offset at which it was requested
     * @return true if it was auto-activated for the default completion (and false if it was for the simple)
     */
    private boolean defaultAutoActivated(ITextViewer viewer, int offset) {
        try {
            char docChar = viewer.getDocument().getChar(offset - 1);
            for (char c : this.defaultPythonProcessor.getCompletionProposalAutoActivationCharacters()) {
                if (c == docChar) {
                    return true;
                }
            }

        } catch (BadLocationException e) {
        }
        return false;
    }

    /**
     * @return true if we should show the default completions (and false if we shouldn't)
     */
    private boolean showDefault() {
        return true;
        //        return whatToShow == SHOW_DEFAULT || this.participants.size() == 0;
    }

    /**
     * Compute context information
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        if (showDefault()) {
            return defaultPythonProcessor.computeContextInformation(viewer, offset);
        }
        return null;
    }

    /**
     * only very simple proposals should be here, as it is auto-activated for any character
     *
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public abstract char[] getCompletionProposalAutoActivationCharacters();

    /**
     * @return chars that are used for context information auto-activation
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /**
     * @return some error that might have happened in the completion
     */
    public String getErrorMessage() {
        String ret = this.lastError;
        if (ret == null && showDefault()) {
            ret = defaultPythonProcessor.getErrorMessage();
        }
        this.lastError = null;
        return ret;
    }

    /**
     * @return the validator we should use
     */
    public IContextInformationValidator getContextInformationValidator() {
        final IContextInformationValidator defaultContextInformationValidator = defaultPythonProcessor
                .getContextInformationValidator();
        return new ContextInformationDelegator(defaultContextInformationValidator);
    }

}
