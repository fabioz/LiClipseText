package org.brainwy.liclipsetext.shared_core.partitioner;

import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;

public interface ILiClipsePredicateRule {
    /**
     * Evaluates the rule by examining the characters available from the provided character scanner.
     * The token returned by this rule returns <code>true</code> when calling
     * <code>isUndefined</code>, if the text that the rule investigated does not match the rule's
     * requirements
     *
     * @param scanner the character scanner to be used by this rule
     * @return the token computed by the rule
     * @throws DocumentTimeStampChangedException
     */
    IToken evaluate(ICharacterScanner scanner) throws DocumentTimeStampChangedException;

    /**
     * Returns the success token of this predicate rule.
     *
     * @return the success token of this rule
     */
    IToken getSuccessToken();

    /**
     * Evaluates the rule by examining the characters available from
     * the provided character scanner. The token returned by this rule
     * returns <code>true</code> when calling <code>isUndefined</code>,
     * if the text that the rule investigated does not match the rule's requirements. Otherwise,
     * this method returns this rule's success token. If this rules relies on a text pattern
     * comprising a opening and a closing character sequence this method can also be called
     * when the scanner is positioned already between the opening and the closing sequence.
     * In this case, <code>resume</code> must be set to <code>true</code>.
     *
     * @param scanner the character scanner to be used by this rule
     * @param resume indicates that the rule starts working between the opening and the closing character sequence
     * @return the token computed by the rule
     * @throws DocumentTimeStampChangedException
     */
    IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException;
}
