package org.brainwy.liclipsetext.editor.outline;

import java.io.File;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline.LiClipseNode;
import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class CppCtagsOutlineTest extends TestCase {

    public void testOutline() throws Exception {

        IDocument document = new Document(""

                + "class CRectangle {\n"
                + "    int x, y;\n"
                + "  public:\n"
                + "    void set_values (int,int);\n"
                + "    int area () {return (x*y);}\n"
                + "}");
        TestUtils.connectDocumentToLanguage(document, "cpp.liclipse");
        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        LiClipseLanguage language = partitioner.language;

        LiClipseNode outline = language.getOutline().createOutline(document);
        assertEquals(""
                + "TreeNode:null\n"
                + "    TreeNode:CRectangle offset:6 len:10 beginLine:1 icon:class\n"
                + "        TreeNode:x offset:27 len:1 beginLine:2 icon:method\n"
                + "        TreeNode:y offset:30 len:1 beginLine:2 icon:method\n"
                + "        TreeNode:area offset:82 len:4 beginLine:5 icon:method\n"
                + "", outline.toStringRepr());
    }

    public void testOutline2() throws Exception {
        File testCtagsDir = TestUtils.getTestCtagsDir();
        String contents = FileUtils.getFileContents(new File(testCtagsDir, "test.cpp"));

        IDocument document = new Document(contents);
        TestUtils.connectDocumentToLanguage(document, "cpp.liclipse");
        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        LiClipseLanguage language = partitioner.language;

        LiClipseNode outline = language.getOutline().createOutline(document);
        assertEquals(""
                + "TreeNode:null\n"
                + "    TreeNode:stringAllocator_ offset:1800 len:16 beginLine:34 icon:method\n"
                + "    TreeNode:getStringAllocator offset:1861 len:18 beginLine:36 icon:method\n"
                + "    TreeNode:setStringAllocator offset:2000 len:18 beginLine:43 icon:method\n"
                + "    TreeNode:allocStringBuffer offset:2202 len:17 beginLine:49 icon:method\n"
                + "    TreeNode:deallocStringBuffer offset:2329 len:19 beginLine:54 icon:method\n"
                + "    TreeNode:getEmptyString offset:2446 len:14 beginLine:59 icon:method\n"
                + "    TreeNode:SimpleString offset:2543 len:12 beginLine:65 icon:method\n"
                + "    TreeNode:SimpleString offset:2809 len:12 beginLine:77 icon:method\n"
                + "    TreeNode:SimpleString offset:3140 len:12 beginLine:89 icon:method\n"
                + "    TreeNode:operator = offset:3343 len:8 beginLine:96 icon:method\n"
                + "    TreeNode:contains offset:3596 len:8 beginLine:107 icon:method\n"
                + "    TreeNode:containsNoCase offset:3950 len:14 beginLine:116 icon:method\n"
                + "    TreeNode:startsWith offset:4068 len:10 beginLine:122 icon:method\n"
                + "    TreeNode:endsWith offset:4332 len:8 beginLine:129 icon:method\n"
                + "    TreeNode:count offset:4764 len:5 beginLine:139 icon:method\n"
                + "    TreeNode:split offset:4964 len:5 beginLine:150 icon:method\n"
                + "    TreeNode:replace offset:5562 len:7 beginLine:173 icon:method\n"
                + "    TreeNode:replace offset:5714 len:7 beginLine:181 icon:method\n"
                + "    TreeNode:toLower offset:6456 len:7 beginLine:214 icon:method\n"
                + "    TreeNode:asCharString offset:6674 len:12 beginLine:225 icon:method\n"
                + "    TreeNode:size offset:6738 len:4 beginLine:230 icon:method\n"
                + "    TreeNode:isEmpty offset:6816 len:7 beginLine:235 icon:method\n"
                + "    TreeNode:~SimpleString offset:6860 len:13 beginLine:242 icon:method\n"
                + "    TreeNode:operator == offset:6931 len:8 beginLine:247 icon:method\n"
                + "    TreeNode:equalsNoCase offset:7099 len:12 beginLine:252 icon:method\n"
                + "    TreeNode:operator != offset:7190 len:8 beginLine:258 icon:method\n"
                + "    TreeNode:operator + offset:7312 len:8 beginLine:263 icon:method\n"
                + "    TreeNode:operator += offset:7436 len:8 beginLine:270 icon:method\n"
                + "    TreeNode:operator += offset:7538 len:8 beginLine:275 icon:method\n"
                + "    TreeNode:padStringsToSameLength offset:7847 len:22 beginLine:286 icon:method\n"
                + "    TreeNode:subString offset:8173 len:9 beginLine:299 icon:method\n"
                + "    TreeNode:at offset:8418 len:2 beginLine:311 icon:method\n"
                + "    TreeNode:find offset:8481 len:4 beginLine:316 icon:method\n"
                + "    TreeNode:findFrom offset:8549 len:8 beginLine:321 icon:method\n"
                + "    TreeNode:subStringFromTill offset:8761 len:17 beginLine:329 icon:method\n"
                + "    TreeNode:copyToBuffer offset:9104 len:12 beginLine:341 icon:method\n"
                + "    TreeNode:StringFrom offset:9402 len:10 beginLine:352 icon:method\n"
                + "    TreeNode:StringFrom offset:9515 len:10 beginLine:357 icon:method\n"
                + "    TreeNode:StringFromOrNull offset:9592 len:16 beginLine:362 icon:method\n"
                + "    TreeNode:StringFrom offset:9707 len:10 beginLine:367 icon:method\n"
                + "    TreeNode:StringFrom offset:9786 len:10 beginLine:372 icon:method\n"
                + "    TreeNode:StringFrom offset:9867 len:10 beginLine:377 icon:method\n"
                + "    TreeNode:HexStringFrom offset:9966 len:13 beginLine:382 icon:method\n"
                + "    TreeNode:convertPointerToLongValue offset:10049 len:25 beginLine:387 icon:method\n"
                + "    TreeNode:HexStringFrom offset:10421 len:13 beginLine:398 icon:method\n"
                + "    TreeNode:StringFrom offset:10539 len:10 beginLine:403 icon:method\n"
                + "    TreeNode:StringFrom offset:10649 len:10 beginLine:408 icon:method\n"
                + "    TreeNode:StringFrom offset:10729 len:10 beginLine:413 icon:method\n"
                + "    TreeNode:StringFromFormat offset:10814 len:16 beginLine:418 icon:method\n"
                + "    TreeNode:StringFrom offset:11098 len:10 beginLine:433 icon:method\n"
                + "    TreeNode:StringFrom offset:11190 len:10 beginLine:438 icon:method\n"
                + "    TreeNode:StringFrom offset:11283 len:10 beginLine:443 icon:method\n"
                + "    TreeNode:StringFrom offset:11373 len:10 beginLine:448 icon:method\n"
                + "    TreeNode:StringFrom offset:11462 len:10 beginLine:453 icon:method\n"
                + "    TreeNode:va_copy offset:11606 len:7 beginLine:462 icon:attribute\n"
                + "    TreeNode:VStringFromFormat offset:11668 len:17 beginLine:465 icon:method\n"
                + "    TreeNode:SimpleStringCollection offset:12385 len:22 beginLine:492 icon:method\n"
                + "    TreeNode:allocate offset:12498 len:8 beginLine:498 icon:method\n"
                + "    TreeNode:~SimpleStringCollection offset:12623 len:23 beginLine:506 icon:method\n"
                + "    TreeNode:size offset:12734 len:4 beginLine:511 icon:method\n"
                + "    TreeNode:operator [] offset:12805 len:8 beginLine:516 icon:method\n"
                , outline.toStringRepr());
    }

}
