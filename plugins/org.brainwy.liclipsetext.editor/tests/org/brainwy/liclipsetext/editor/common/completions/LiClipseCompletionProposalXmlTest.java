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
import org.eclipse.jface.text.templates.TemplateProposal;

public class LiClipseCompletionProposalXmlTest extends CompletionsTestBase {

    public void testXmlConvertToTagCompletion() throws Exception {
        String txt = "mytag";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("xml.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.fLanguage = partitioner.language;
        int offset = txt.length();
        viewer.setSelectedRange(offset, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, IDocument.DEFAULT_CONTENT_TYPE)
                .collectTemplateProposals(viewer, offset, computeCompletions);

        TemplateProposal completion = (TemplateProposal) filterCompletion(
                "Convert previous word to a tag.",
                computeCompletions);

        completion.apply(viewer, ' ', 0, offset);
        assertEquals("<mytag></mytag>", document.get());
    }

}
