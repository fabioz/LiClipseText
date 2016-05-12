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

public class LiClipseCompletionProposalGeneralTest extends CompletionsTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.startEditorPlugin();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtils.stopEditorPlugin();
    }

    public void testCompletionsWithContext() throws Exception {
        String txt = "<>";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("language_test_scope.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(1, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, "tag")
                .collectTemplateProposals(viewer, 1, computeCompletions);

        filterCompletion("ab - ab", computeCompletions);
    }

    public void testCompletionsWithContext2() throws Exception {
        String txt = "<a >";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("language_test_scope.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(3, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, "tag")
                .collectTemplateProposals(viewer, 3, computeCompletions);

        assertNoCompletion("ab - ab", computeCompletions);
    }

    public void testCompletionsWithContext3() throws Exception {
        String txt = "<a> <>";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("language_test_scope.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(5, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, "tag")
                .collectTemplateProposals(viewer, 5, computeCompletions);

        filterCompletion("ab - ab", computeCompletions);
    }

    public void testCompletionsWithContext4() throws Exception {
        String txt = "<a >";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("language_test_scope.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(2, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, "tag")
                .collectTemplateProposals(viewer, 2, computeCompletions);

        filterCompletion("ab - ab", computeCompletions);
    }

    public void testCompletionsWithContext5() throws Exception {
        String txt = "<ab >";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("language_test_scope.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(4, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, "tag")
                .collectTemplateProposals(viewer, 4, computeCompletions);

        filterCompletion("ac - ac", computeCompletions);
        filterCompletion("ad - ad", computeCompletions);
    }

}
