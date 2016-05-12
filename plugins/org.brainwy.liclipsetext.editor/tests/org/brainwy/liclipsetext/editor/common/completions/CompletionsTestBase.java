package org.brainwy.liclipsetext.editor.common.completions;

import java.util.List;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/*default*/class CompletionsTestBase extends TestCase {

    protected void checkOnlyWithPrefix(String prefix, List<ICompletionProposal> computeCompletions) {
        FastStringBuffer buf = new FastStringBuffer();
        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            if (!iCompletionProposal.getDisplayString().startsWith(prefix)) {
                buf.append(iCompletionProposal.getDisplayString()).append('\n');
            }
        }
        if (buf.length() > 0) {
            throw new AssertionError("Completions below do not start with prefix: " + prefix + "\n" + buf);
        }

    }

    protected ICompletionProposal filterCompletion(String string, List<ICompletionProposal> computeCompletions) {
        FastStringBuffer buf = new FastStringBuffer();
        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            buf.append(iCompletionProposal.getDisplayString());
            buf.append("\n");
            if (iCompletionProposal.getDisplayString().equals(string)) {
                return iCompletionProposal;
            }
        }
        throw new AssertionError("Unable to find: " + string + "\nFound:\n" + buf);
    }

    protected void assertNoCompletion(String string, List<ICompletionProposal> computeCompletions) {
        FastStringBuffer buf = new FastStringBuffer();
        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            buf.append(iCompletionProposal.getDisplayString());
            buf.append("\n");
            if (iCompletionProposal.getDisplayString().equals(string)) {
                throw new AssertionError("Not expecting to find: " + string);
            }
        }
    }

}
