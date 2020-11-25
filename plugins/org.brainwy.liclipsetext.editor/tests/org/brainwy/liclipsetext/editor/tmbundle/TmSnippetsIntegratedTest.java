package org.brainwy.liclipsetext.editor.tmbundle;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.completions.DummyViewer;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateCompletionProcessor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import junit.framework.TestCase;

public class TmSnippetsIntegratedTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtils.clearLanguagesManager();
    }

    public void testPHPSnippetsIntegrated() throws Exception {
        String txt = "";
        final IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = LiClipseTextEditorPlugin.getLanguagesManager()
                .getLanguageFromName("text.html.php");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();

        List<ICompletionProposal> computeCompletions = new ArrayList<ICompletionProposal>();

        DummyViewer viewer = new DummyViewer();
        viewer.setSelectedRange(0, 0);
        viewer.fDoc = document;
        new LiClipseTemplateCompletionProcessor(partitioningSetup, IDocument.DEFAULT_CONTENT_TYPE)
                .collectTemplateProposals(viewer, 0, computeCompletions);

        ArrayList<String> lst = new ArrayList<>();
        for (ICompletionProposal iCompletionProposal : computeCompletions) {
            lst.add(iCompletionProposal.getDisplayString().replaceAll("\\{var(\\d)*\\}", "{var}"));
        }
        assertEquals(TestUtils.listToExpected("echo - <?${var} ${var} ?>${cursor}",
                "echoh - <?${var} htmlentities(${var}, ENT_QUOTES, 'utf-8') ?>${cursor}",
                "else - <?${var} else: ?>",
                "foreach - <?${var} foreach ($$${variable} as $$${key}${var}): ?>\n\t${cursor}\n<?${var} endforeach ?>",
                "ifelse - <?${var} if (${condition}): ?>\n\t${var}\n<?${var} else: ?>\n\t${cursor}\n<?${var} endif ?>",
                "if - <?${var} if (${condition}): ?>\n\t${cursor}\n<?${var} endif ?>"),
                TestUtils.listToExpected(lst));
    }
}
