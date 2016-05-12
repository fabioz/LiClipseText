package org.brainwy.liclipsetext.editor.handlers;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.handlers.NextPrevElement;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Region;

public class NextPrevFunctionTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.clearLanguagesManager();
    }

    public void testNextPrev() throws Exception {
        String s = "\n" +
                "\n" +
                "var func = function(){\n" +
                "}\n" +
                "\n";
        Document doc = new Document(s);

        LiClipseLanguage language = loadLanguage(doc, "javascript.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(6, 4), nextPrevFunction.find(language, true, doc, 0));
    }

    public void testNextPrev1() throws Exception {
        String s = "\n" +
                "\n" +
                "function func(){\n" +
                "}\n" +
                "\n";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "javascript.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(11, 4), nextPrevFunction.find(language, true, doc, 0));
    }

    public void testNextPrev2() throws Exception {
        String s = "\n" +
                "\n" +
                "{a:function(data){\n" +
                "}}\n" +
                "\n";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "javascript.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(s.length(), 0), nextPrevFunction.find(language, true, doc, 0));
    }

    public void testNextPrev3() throws Exception {
        String s = "" +
                "\n" +
                "var f = function(){\n" +
                "	//comment\n" +
                "	/** comment **/\n" +
                "}\n";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "javascript.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(5, 1), nextPrevFunction.find(language, true, doc, 0));
    }

    public void testNextPrev4() throws Exception {
        String s = "" +
                "#comment\n" +
                "def m1():\n" +
                "	#def comment\n" +
                "   pass\n";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "python.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(13, 2), nextPrevFunction.find(language, true, doc, 0));
    }

    public void testNextPrev5() throws Exception {
        //backwards now
        String s = "" +
                "#comment\n" +
                "def m1():\n" +
                "	#def comment\n" +
                "   pass\n";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "python.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(13, 2), nextPrevFunction.find(language, false, doc, doc.getLength()));
    }

    public void testNextPrev6() throws Exception {
        //multiple
        String s = "" +
                "def m1():\n" +
                "   pass\n" +
                "def m2():\n" +
                "   pass\n"
                + "";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "python.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(22, 2), nextPrevFunction.find(language, true, doc, 12)); //after the first one
    }

    public void testNextPrev7() throws Exception {
        //multiple
        String s = "" +
                "class m1():\n" +
                "   pass\n" +
                "def m2():\n" +
                "   pass\n"
                + "";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "python.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(24, 2), nextPrevFunction.find(language, false, doc, doc.getLength())); //after the first one
    }

    public void testNextPrevRegexp() throws Exception {
        String s = "" +
                "#--- test:\n" +
                "\n" +
                "\n" +
                "#--- test2:\n" +
                "\n"
                + "";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "python.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(13, 11), nextPrevFunction.find(language, false, doc, doc.getLength())); //after the first one
    }

    public void testNextPrevRegexp2() throws Exception {
        String s = "" +
                "#--- test:\n" +
                "\n" +
                "\n" +
                "# test2:\n" +
                "\n"
                + "";
        Document doc = new Document(s);
        LiClipseLanguage language = loadLanguage(doc, "python.liclipse");
        NextPrevElement nextPrevFunction = new NextPrevElement();
        assertEquals(new Region(0, 10), nextPrevFunction.find(language, false, doc, doc.getLength())); //after the first one
    }

    public LiClipseLanguage loadLanguage(Document doc, String loadLanguage) throws Exception {
        return TestUtils.connectDocumentToLanguage(doc, loadLanguage);
    }

}
