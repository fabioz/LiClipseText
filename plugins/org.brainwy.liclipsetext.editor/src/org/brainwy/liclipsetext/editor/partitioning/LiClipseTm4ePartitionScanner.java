package org.brainwy.liclipsetext.editor.partitioning;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.Token;

public class LiClipseTm4ePartitionScanner extends AbstractLiClipseRuleBasedScanner {

    @Override
    public void nextToken(ScannerRange range) {
        boolean resume = (range.fPartitionOffset > -1 && range.fPartitionOffset < range.fOffset);
        range.fTokenOffset = resume ? range.fPartitionOffset : range.fOffset;

        if (range.read() == ICharacterScanner.EOF) {
            range.setToken(Token.EOF);
            return;
        } else {
            range.setMark(range.getRangeEndOffset());
            range.setToken(fDefaultReturnToken);
        }
    }
}
