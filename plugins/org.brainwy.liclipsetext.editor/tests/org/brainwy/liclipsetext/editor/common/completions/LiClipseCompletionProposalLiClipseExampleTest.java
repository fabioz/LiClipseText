package org.brainwy.liclipsetext.editor.common.completions;

import java.util.List;

import org.brainwy.liclipsetext.editor.common.LiClipseContentAssistProcessor;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class LiClipseCompletionProposalLiClipseExampleTest extends CompletionsTestBase {

    public void testCssCompletionWithDashes() throws Exception {
        String txt = "b";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("liclipse_example.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        LiClipseContentAssistProcessor processor = new LiClipseContentAssistProcessor(partitioner,
                IDocument.DEFAULT_CONTENT_TYPE, null);
        List<ICompletionProposal> computeCompletions = processor.computeCompletions(txt.length(), document);
        assertEquals(0, computeCompletions.size());
    }
}
