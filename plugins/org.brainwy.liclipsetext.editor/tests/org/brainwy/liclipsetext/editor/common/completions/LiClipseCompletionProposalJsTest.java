package org.brainwy.liclipsetext.editor.common.completions;

import java.util.List;

import org.brainwy.liclipsetext.editor.common.LiClipseContentAssistProcessor;
import org.brainwy.liclipsetext.editor.common.completions.LiClipseCompletionProposal;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class LiClipseCompletionProposalJsTest extends CompletionsTestBase {

    public void testJsCompletions() throws Exception {
        String txt = "fun";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("javascript.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        DummyViewer viewer = new DummyViewer();
        viewer.fDoc = document;
        viewer.fLanguage = partitioningSetup;

        LiClipseContentAssistProcessor processor = new LiClipseContentAssistProcessor(partitioner,
                IDocument.DEFAULT_CONTENT_TYPE, null);
        List<ICompletionProposal> computeCompletions = processor.computeCompletions(txt.length(), document);

        LiClipseCompletionProposal completion = (LiClipseCompletionProposal) filterCompletion("function",
                computeCompletions);
        completion.apply(viewer, ' ', 0, 3);
        assertEquals("function", document.get());
    }
}
