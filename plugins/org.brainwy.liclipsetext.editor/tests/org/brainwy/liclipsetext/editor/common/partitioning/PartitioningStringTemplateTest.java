package org.brainwy.liclipsetext.editor.common.partitioning;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

public class PartitioningStringTemplateTest extends TestCase {

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
        LiClipseLanguage language = TestUtils.loadLanguageFile("stringtemplate.liclipse");
        String txt = ""
                + "import \"/tmp/test.st\"\n"
                + "\n"
                + "typeInit ::= [\"int\":\"0\", \"float\":\"0.0\"]\n"
                + "\n"
                + "var(type,name) ::= \"<type> <name> = <true> <if> uenoth uoesnth <typeInit.(type)>;\"\n"
                + "\n"
                + "init(v) ::= \"<if(v)> = <import> nutoh uonth u <if> <v><endif>\"\n"
                + "\n"
                + "typeInitMap ::= [\n"
                + "        \"int\":\"0\",\n"
                + "        \"long\":\"0\",\n"
                + "        \"float\":\"0.0\",\n"
                + "        \"double\":\"0.0\",\n"
                + "        \"boolean\":\"false\",\n"
                + "        \"byte\":\"0\",\n"
                + "        \"short\":\"0\",\n"
                + "        \"char\":\"0\",\n"
                + "        default:\"null\" // anything other than an atomic type\n"
                + "]\n"
                + "\n"
                + "<! a comment !>\n"
                + "\n"
                + "exampleTemplate(name) ::= <<\n"
                + "    Hello <input1.ueo()ueo>\n"
                + ">>\n"
                + "\n"
                + "templateName(arg1, arg2) ::= \"single-line template\"\n"
                + "\n"
                + "templateName(arg1, arg2) ::= <<\n"
                + "multi-line template <input.uoesnth(a=\"\")>\n"
                + "<if(scope.actions.scopeinit)>\n"
                + "    do this\n"
                + "<else>\n"
                + "    do that\n"
                + "<if>\n"
                + "<if>\n"
                + ">>\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "@templateName(arg1, arg2) ::= <%\n"
                + "<if(scope.actions.scopeinit)>\n"
                + "multi-line template that ignores indentation and newlines\n"
                + "<if(attr)>\n"
                + "  subtemplate\n"
                + "<elseif(attr2))>\n"
                + "  subtemplate2\n"
                + "<else>\n"
                + "  subtemplate3\n"
                + "<endif>\n"
                + "%>\n"
                + "\n"
                + "class(name,members,sup=\"Object\") ::= \"class <name> extends <sup> { <members> }\"\n"
                + "\n"
                + "iso(atom, k) ::= \"input.LA(<k>)==<atom> <if>\"\n"
                + "\n"
                + "something() ::=<%\n"
                + "    unteoh\n"
                + "%>\n"
                + "";

        Document document = new Document(txt);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:7",
                "singleLineContext:7:21",
                "__dftl_partition_content_type:21:37",
                "singleLineContext:37:42",
                "__dftl_partition_content_type:42:43",
                "singleLineContext:43:46",
                "__dftl_partition_content_type:46:48",
                "singleLineContext:48:55",
                "__dftl_partition_content_type:55:56",
                "singleLineContext:56:61",
                "__dftl_partition_content_type:61:83",
                "singleLineContext:83:146",
                "__dftl_partition_content_type:146:160",
                "singleLineContext:160:210",
                "__dftl_partition_content_type:210:238",
                "singleLineContext:238:243",
                "__dftl_partition_content_type:243:244",
                "singleLineContext:244:247",
                "__dftl_partition_content_type:247:257",
                "singleLineContext:257:263",
                "__dftl_partition_content_type:263:264",
                "singleLineContext:264:267",
                "__dftl_partition_content_type:267:277",
                "singleLineContext:277:284",
                "__dftl_partition_content_type:284:285",
                "singleLineContext:285:290",
                "__dftl_partition_content_type:290:300",
                "singleLineContext:300:308",
                "__dftl_partition_content_type:308:309",
                "singleLineContext:309:314",
                "__dftl_partition_content_type:314:324",
                "singleLineContext:324:333",
                "__dftl_partition_content_type:333:334",
                "singleLineContext:334:341",
                "__dftl_partition_content_type:341:351",
                "singleLineContext:351:357",
                "__dftl_partition_content_type:357:358",
                "singleLineContext:358:361",
                "__dftl_partition_content_type:361:371",
                "singleLineContext:371:378",
                "__dftl_partition_content_type:378:379",
                "singleLineContext:379:382",
                "__dftl_partition_content_type:382:392",
                "singleLineContext:392:398",
                "__dftl_partition_content_type:398:399",
                "singleLineContext:399:402",
                "__dftl_partition_content_type:402:420",
                "singleLineContext:420:426",
                "__dftl_partition_content_type:426:468",
                "multiLineComment:468:483",
                "__dftl_partition_content_type:483:511",
                "multiLineContext1:511:544",
                "__dftl_partition_content_type:544:575",
                "singleLineContext:575:597",
                "__dftl_partition_content_type:597:628",
                "multiLineContext1:628:746",
                "__dftl_partition_content_type:746:781",
                "multiLineContext2:781:961",
                "__dftl_partition_content_type:961:986",
                "singleLineContext:986:994",
                "__dftl_partition_content_type:994:1000",
                "singleLineContext:1000:1042",
                "__dftl_partition_content_type:1042:1061",
                "singleLineContext:1061:1089",
                "__dftl_partition_content_type:1089:1106",
                "multiLineContext2:1106:1122",
                "__dftl_partition_content_type:1122:"), partition);

    }
}
