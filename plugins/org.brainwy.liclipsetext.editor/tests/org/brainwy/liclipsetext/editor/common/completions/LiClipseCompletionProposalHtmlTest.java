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

public class LiClipseCompletionProposalHtmlTest extends CompletionsTestBase {

    public void testHtmlCompletionsWithContext() throws Exception {
        String txt = "";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("html.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(0, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, IDocument.DEFAULT_CONTENT_TYPE)
                .collectTemplateProposals(viewer, 0, computeCompletions);

        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            if (iCompletionProposal.getDisplayString().startsWith("class - ")) {
                fail("Not expecting to find class completion at this scope.");
            }
        }
        filterCompletion("a - The <a> element represents a hyperlink.", computeCompletions);
    }

    public void testHtmlScriptCompletion() throws Exception {
        String txt = "";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("html.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(0, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, IDocument.DEFAULT_CONTENT_TYPE)
                .collectTemplateProposals(viewer, 0, computeCompletions);

        filterCompletion(
                "script - The <script> element allows authors to include dynamic script and data blocks in their documents.",
                computeCompletions);
    }

    public void testHtmlCompletionsWithContext2() throws Exception {
        String txt = "<a ></a>";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("html.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(3, 0); //right after <a |
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, "tag")
                .collectTemplateProposals(viewer, 3, computeCompletions);

        boolean foundTarget = false;
        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            //System.out.println(iCompletionProposal.getDisplayString());
            if (iCompletionProposal.getDisplayString().startsWith("src - ")) {
                fail("Not expecting to find src completion at this scope (as it's from the img).");
            }
            if (iCompletionProposal.getDisplayString().startsWith("a - Anchor")) {
                fail("Not expecting to find a - Anchor completion at this scope.");
            }
            if (iCompletionProposal.getDisplayString().startsWith("target -")) {
                foundTarget = true;
            }
        }
        if (!foundTarget) {
            fail("Expected target completion to be found for <a>");
        }

    }

    public void testHtmlCompletionsWithContext3() throws Exception {
        String txt = "<>";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("html.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(1, 0); //right after <|
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, "tag")
                .collectTemplateProposals(viewer, 1, computeCompletions);

        boolean foundTarget = false;
        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            //            System.out.println(iCompletionProposal.getDisplayString());
            if (iCompletionProposal.getDisplayString().startsWith("class -")) {
                fail("Not expecting to find class completion at this scope.");
            }
            if (iCompletionProposal.getDisplayString().startsWith("a -")) {
                foundTarget = true;
            }
        }
        if (!foundTarget) {
            fail("Expected target completion to be found for <a>");
        }

    }

    public void testHtmlCompletionsWithContext4() throws Exception {
        String txt = "test";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("html.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(0, 4); //select the whole text
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, IDocument.DEFAULT_CONTENT_TYPE)
                .collectTemplateProposals(viewer, 0, computeCompletions);

        boolean foundTarget = false;
        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            //            System.out.println(iCompletionProposal.getDisplayString());
            if (iCompletionProposal.getDisplayString().equals("Surround with tag")) {
                foundTarget = true;
            }
        }
        if (!foundTarget) {
            fail("Expected target completion to be found for <a>");
        }

    }

}
