package org.brainwy.liclipsetext.editor.spelling;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.spelling.LiClipseSpellCheckerReconciler;
import org.brainwy.liclipsetext.editor.spelling.SpellingReconcileStrategy.SpellingProblemCollector;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;

public class LiClipseSpellCheckerReconcilerTest extends TestCase {

    private final class TestReconciler extends LiClipseSpellCheckerReconciler {
        private final IAnnotationModel annotationModel;
        public IRegion[] subRegionsForReconcile;

        private TestReconciler(IAnnotationModel annotationModel) {
            this.annotationModel = annotationModel;
        }

        @Override
        protected IAnnotationModel getAnnotationModel() {
            return annotationModel;
        }

        @Override
        protected boolean isSpellingEnabled() {
            return true;
        }

        @Override
        protected void reconcileSubRegions(IRegion[] subRegionsForReconcile) {
            this.subRegionsForReconcile = subRegionsForReconcile;
        }
    }

    public void testSpellingReconciler() throws Exception {

        String str = ""
                + "scope_to_color_name: {doubleQuotedString: string}\n" //default: foreground there by default!
                + "scope_definition_rules:\n"
                + "- {type: SingleLineRule, scope: doubleQuotedString, sequence: '\"', escapeCharacter: \\, escapeContinuesLine: true}\n"
                + "scope:\n"
                + "  default:\n"
                + "    class: []\n"
                + "        \n"
                + "file_extensions: [foo]\n"
                + "filename: []\n"
                + "name: Foo\n"
                + "";
        LiClipseLanguage language = LiClipseLanguage.load(null, new ByteArrayInputStream(str.getBytes("utf-8")), true);
        String[] spellCheckingContentTypes = language.getSpellCheckingContentTypes();
        Arrays.sort(spellCheckingContentTypes);
        assertEquals(TestUtils.listToExpected("doubleQuotedString"),
                TestUtils.listToExpected(spellCheckingContentTypes));
        final IAnnotationModel annotationModel = new AnnotationModel();

        TestReconciler spellCheckerReconciler = new TestReconciler(annotationModel);
        IDocument document = new Document(""
                + "not not not = \"checked\"\n"
                + "line 2 = \"rara\"");
        language.connect(document);
        spellCheckerReconciler.setDocument(document);
        IRegion region = new Region(25, document.getLength() - 25);
        spellCheckerReconciler.reconcile(region);
        IRegion[] subRegionsForReconcile = spellCheckerReconciler.subRegionsForReconcile;
        assertEquals(1, subRegionsForReconcile.length);
        assertEquals(new Region(33, 6), subRegionsForReconcile[0]);

        region = new Region(0, 25);
        spellCheckerReconciler.reconcile(region);
        subRegionsForReconcile = spellCheckerReconciler.subRegionsForReconcile;
        assertEquals(1, subRegionsForReconcile.length);
        assertEquals(new Region(14, 9), subRegionsForReconcile[0]);

        region = new Region(16, 2);
        spellCheckerReconciler.reconcile(region);
        subRegionsForReconcile = spellCheckerReconciler.subRegionsForReconcile;
        assertEquals(1, subRegionsForReconcile.length);
        assertEquals(new Region(14, 9), subRegionsForReconcile[0]);

        SpellingProblemCollector spellingProblemCollector = spellCheckerReconciler.getSpellingProblemCollector();
        IRegion baseRegionForReconcile = spellingProblemCollector.getBaseRegionForReconcile();
        assertEquals(new Region(14, 9), baseRegionForReconcile);

    }

    public void testPartitionsForSpellCheckingNone() throws Exception {
        String str = ""
                + "scope_to_color_name: {}\n" //default: foreground there by default!
                + "scope_definition_rules:\n"
                + "- {type: SingleLineRule, scope: doubleQuotedString, sequence: '\"', escapeCharacter: \\, escapeContinuesLine: true}\n"
                + "scope:\n"
                + "  default:\n"
                + "    class: []\n"
                + "        \n"
                + "file_extensions: [foo]\n"
                + "filename: []\n"
                + "spell_check: {scope: []}\n"
                + "name: Foo\n"
                + "";
        LiClipseLanguage language = LiClipseLanguage.load(null, new ByteArrayInputStream(str.getBytes("utf-8")), true);
        String[] spellCheckingContentTypes = language.getSpellCheckingContentTypes();
        Arrays.sort(spellCheckingContentTypes);
        assertEquals(TestUtils.listToExpected(),
                TestUtils.listToExpected(spellCheckingContentTypes));
    }

    public void testPartitionsForSpellCheckingSpecify() throws Exception {
        String str = ""
                + "scope_to_color_name: {}\n" //default: foreground there by default!
                + "scope_definition_rules:\n"
                + "- {type: SingleLineRule, scope: doubleQuotedString, sequence: '\"', escapeCharacter: \\, escapeContinuesLine: true}\n"
                + "scope:\n"
                + "  default:\n"
                + "    class: []\n"
                + "        \n"
                + "file_extensions: [foo]\n"
                + "filename: []\n"
                + "spell_check: {scope: [doubleQuotedString]}\n"
                + "name: Foo\n"
                + "";
        LiClipseLanguage language = LiClipseLanguage.load(null, new ByteArrayInputStream(str.getBytes("utf-8")), true);
        String[] spellCheckingContentTypes = language.getSpellCheckingContentTypes();
        Arrays.sort(spellCheckingContentTypes);
        assertEquals(TestUtils.listToExpected("doubleQuotedString"),
                TestUtils.listToExpected(spellCheckingContentTypes));
    }
}
