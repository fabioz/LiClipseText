package org.brainwy.liclipsetext.editor.common.completions;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateCompletionProcessor;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateProposal;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class LiClipseCompletionProposalLiClipseLanguageTest extends CompletionsTestBase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.clearLanguagesManager();
    }

    public void testLiClipseLanguageCompletions() throws Exception {
        String txt = ""
                + "optionalseq";
        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("liclipse.liclipse");
        Document document = new Document(txt);
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        int offset = txt.length();
        viewer.setSelectedRange(offset, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, IDocument.DEFAULT_CONTENT_TYPE)
                .collectTemplateProposals(viewer, offset, computeCompletions);

        boolean found = false;
        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            LiClipseTemplateProposal t = (LiClipseTemplateProposal) iCompletionProposal;
            //            System.out.println(t.getDisplayString());
            if (t.getTemplateUsed().getName().equals("OptionalSequenceRule")) {
                found = true;
            }
        }
        if (!found) {
            fail("Expecing to find OptionalSequenceRule");
        }
    }
}
