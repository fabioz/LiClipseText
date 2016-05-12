package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

public class PartitioningMakoTest extends TestCase {

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

    public void testPartitionerMako() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("mako.liclipse");
        String txt = "<% test %>";
        txt = txt.toUpperCase();

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("this&python_block:0:2",
                "python&__dftl_partition_content_type:2:8",
                "this&python_block:8:"), partition);

    }

    public void testPartitionerMako2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("mako.liclipse");
        String txt = "" +
                "<% a %>\n" +
                "\n" +
                "<% a %>\n" +
                "";
        txt = txt.toUpperCase();

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("this&python_block:0:2",
                "python&__dftl_partition_content_type:2:5",
                "this&python_block:5:7",
                "__dftl_partition_content_type:7:9",
                "this&python_block:9:11",
                "python&__dftl_partition_content_type:11:14",
                "this&python_block:14:16",
                "__dftl_partition_content_type:16:"), partition);

    }

    public void testPartitionerMako3() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("mako.liclipse");
        String txt = "" +
                "<% a1 = '' %>\n" +
                "";
        txt = txt.toUpperCase();

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("this&python_block:0:2",
                "python&__dftl_partition_content_type:2:8",
                "python&singleQuotedString:8:10",
                "python&__dftl_partition_content_type:10:11",
                "this&python_block:11:13",
                "__dftl_partition_content_type:13:"), partition);

    }

    public void testPartitionerMakoTags() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("mako.liclipse");
        String txt = "" +
                "<%def name='foo' />default" +
                "";
        txt = txt.toUpperCase();

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("mako_tag:0:19",
                "__dftl_partition_content_type:19:"), partition);

    }

    public void testPartitionerMakoTags2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("mako.liclipse");
        String txt = "" +
                "<%def name='foo'>default</%def>" +
                "";
        txt = txt.toUpperCase();

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("mako_tag:0:17",
                "__dftl_partition_content_type:17:24",
                "mako_tag:24:"), partition);

    }

    public void testPartitionerMako4() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("mako.liclipse");
        String txt = "" +
                "%endif" +
                "";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("python_block2:0:"), partition);

    }
}
