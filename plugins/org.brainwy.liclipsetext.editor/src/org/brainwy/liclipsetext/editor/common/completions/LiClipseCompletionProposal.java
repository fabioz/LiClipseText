/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.completions;

import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_ui.proposals.AbstractLinkedModeCompletionProposal;

public class LiClipseCompletionProposal extends AbstractLinkedModeCompletionProposal {

    private Set<Character> separatorChars;

    public LiClipseCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
            int cursorPosition, Image image, String displayString, IContextInformation contextInformation,
            String additionalProposalInfo, int priority, int onApplyAction, String args, Set<Character> separatorChars) {
        super(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
                contextInformation, additionalProposalInfo, priority, onApplyAction, args);
        this.separatorChars = separatorChars;
    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        try {
            char c = document.getChar(offset - 1); //if right after a dot, it's not valid anymore.
            if (c == '.') {
                return false;
            }
        } catch (BadLocationException e) {
            return false;
        }

        TextSelectionUtils ts = new TextSelectionUtils(document, offset);
        Tuple<String, Integer> currToken;
        try {
            currToken = ts.getCurrToken(separatorChars);
        } catch (BadLocationException e) {
            return false;
        }
        String displayString = fReplacementString.toLowerCase();
        if (displayString.startsWith(currToken.o1.toLowerCase())) {
            return true;
        }

        return false;
    }

}
