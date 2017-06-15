package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;

import org.brainwy.liclipsetext.editor.languages.LanguageMetadataTmBundleZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.rules.TypedRegionWithSubTokens;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.partitioner.TypedPositionWithSubTokens;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import junit.framework.TestCase;

public class PartitioningPhpLanguageTest extends TestCase {

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

    public void testChangePartitioning() throws Exception {
        String txt = "<?php\n$a=10;\n?>";
        IDocument document = new Document(txt);

        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "php.tmbundle"), "php.tmbundle-master/Syntaxes/PHP.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);

        language.connect(document);

        TypedRegionWithSubTokens partition = (TypedRegionWithSubTokens) document.getPartition(10);
        assertEquals(document.getLength(), partition.getLength());
        SubRuleToken subRuleToken = partition.getSubRuleToken();
        assertEquals(document.getLength(), subRuleToken.len);

        document.replace(txt.length() - "?>".length(), 0, "$b=10;\n");

        partition = (TypedRegionWithSubTokens) document.getPartition(10);
        assertEquals(document.getLength(), partition.getLength());
        subRuleToken = partition.getSubRuleToken();
        assertEquals(document.getLength(), subRuleToken.len);

        document.replace(txt.length() - "$b=10;\n?>".length(), "$b=10;\n".length(), "");

        partition = (TypedRegionWithSubTokens) document.getPartition(10);
        assertEquals(document.getLength(), partition.getLength());
        subRuleToken = partition.getSubRuleToken();
        assertEquals(document.getLength(), subRuleToken.len);
    }

    public void testChangePartitioning2() throws Exception {
        String txt = "<?php\n" +
                "\n" +
                "$a = 10;\n" +
                "$a = 10\n" +
                "$a = 10\n" +
                "$a = 10\n" +
                "\n" +
                "\n" +
                "$a = 10\n" +
                "$a = 10\n" +
                "$a = 10\n" +
                "$a = 10\n" +
                "\n" +
                "?>\n" +
                "\n" +
                "<?php\n" +
                "\n" +
                "$a = 10;\n" +
                "$a = 10;\n" +
                "\n" +
                "?>\n" +
                "\n" +
                "";
        IDocument document = new Document(txt);

        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "php.tmbundle"), "php.tmbundle-master/Syntaxes/PHP.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);

        language.connect(document);

        checkPositions(document, 79);

        document.replace(("<?php\n" +
                "\n" +
                "$a = 10;\n" +
                "$a = 10\n" +
                "$a = 10\n" +
                "$a = 10\n" +
                "\n" +
                "\n" +
                "$a = 10\n" +
                "$a = 10").length(), 0, ";");

        checkPositions(document, 80);

    }

    private void checkPositions(IDocument document, int pos2Offset) throws BadPositionCategoryException {
        String[] positionCategories = document.getPositionCategories();
        for (String category : positionCategories) {
            if (!category.equals("__dflt_position_category")) {
                Position[] positions = document.getPositions(category);
                assertEquals(positions.length, 2);
                TypedPositionWithSubTokens pos = (TypedPositionWithSubTokens) positions[0];
                assertEquals(0, pos.offset);
                assertEquals(0, pos.getSubRuleToken().offset);
                assertEquals(pos.length, pos.getSubRuleToken().len);

                TypedPositionWithSubTokens pos2 = (TypedPositionWithSubTokens) positions[1];
                assertEquals(pos2Offset, pos2.offset);
                assertEquals(0, pos2.getSubRuleToken().offset);
                assertEquals(pos2.length, pos2.getSubRuleToken().len);
            }
        }
    }

}
