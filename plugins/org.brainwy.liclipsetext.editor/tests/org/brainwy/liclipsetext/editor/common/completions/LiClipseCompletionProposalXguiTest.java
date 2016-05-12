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

public class LiClipseCompletionProposalXguiTest extends CompletionsTestBase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.clearLanguagesManager();
    }

    public void testXguiCompletions() throws Exception {
        String txt = ""
                + "a = Widget\n"
                + "    .cl";
        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("xgui20.liclipse");
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
            if (t.getTemplateUsed().getName().equals(".class_name")) {
                found = true;
            }
        }
        if (!found) {
            fail("Expecing to find .class_name.");
        }
    }
}
