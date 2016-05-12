package org.brainwy.liclipsetext.editor.common.completions;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateCompletionProcessor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class LiClipseCompletionProposalJulialangTest extends CompletionsTestBase {

    public void testJulialangCompletionsWithContext() throws Exception {
        String txt = "";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("julia.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(0, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, IDocument.DEFAULT_CONTENT_TYPE)
                .collectTemplateProposals(viewer, 0, computeCompletions);

        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            if (iCompletionProposal.getDisplayString().startsWith("Surround with")) {
                fail("Not expecting to find 'Surround with' without selection.");
            }
        }
    }
}
