/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.content_assist;

import org.brainwy.liclipsetext.shared_ui.SharedUiPlugin;
import org.brainwy.liclipsetext.shared_ui.UIConstants;
import org.brainwy.liclipsetext.shared_ui.proposals.ILiClipseCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class CompletionError implements ICompletionProposal, ILiClipseCompletionProposal, ICompletionProposalExtension4 {

    private Throwable error;

    public CompletionError(Throwable e) {
        this.error = e;
    }

    public void apply(IDocument document) {
    }

    public String getAdditionalProposalInfo() {
        return getErrorMessage();
    }

    public IContextInformation getContextInformation() {
        return null;
    }

    public String getDisplayString() {
        return getErrorMessage();
    }

    public Image getImage() {
        return SharedUiPlugin.getImageCache().get(UIConstants.ERROR);
    }

    public Point getSelection(IDocument document) {
        return null;
    }

    public int getPriority() {
        return -1;
    }

    public boolean isAutoInsertable() {
        return false;
    }

    public String getErrorMessage() {
        String message = error.getMessage();
        if (message == null) {
            //NullPointerException
            if (error instanceof NullPointerException) {
                message = "NullPointerException";
            } else {
                message = "Null error message";
            }
        }

        return message;
    }

}
