package org.brainwy.liclipsetext.editor.rules;

import java.util.ArrayList;

import org.brainwy.liclipsetext.editor.rules.SubLanguageToken;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;

import junit.framework.TestCase;

public class SubLanguageTokenTest extends TestCase {

    public void testSubLanguageTokenMerge() throws Exception {
        ArrayList<SubLanguageToken> lst = new ArrayList<SubLanguageToken>();
        SubLanguageToken.fillWithDefault(lst, "this", 5, 7);
        String s = StringUtils.join("\n", lst);
        assertEquals("SubLanguageToken[this&__dftl_partition_content_type offset: 5 len: 2]", s);
    }

    public void testSubLanguageTokenMerge2() throws Exception {
        ArrayList<SubLanguageToken> lst = new ArrayList<SubLanguageToken>();
        lst.add(new SubLanguageToken("foo", "bar", 7, 1));
        SubLanguageToken.fillWithDefault(lst, "this", 5, 10);
        String s = StringUtils.join("\n", lst);
        assertEquals(""
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 5 len: 2]\n"
                + "SubLanguageToken[foo&bar offset: 7 len: 1]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 8 len: 2]"
                + "", s);
    }

    public void testSubLanguageTokenMerge3() throws Exception {
        ArrayList<SubLanguageToken> lst = new ArrayList<SubLanguageToken>();
        lst.add(new SubLanguageToken("foo", "bar", 7, 1));
        lst.add(new SubLanguageToken("foo", "bar", 8, 1));
        SubLanguageToken.fillWithDefault(lst, "this", 5, 10);
        String s = StringUtils.join("\n", lst);
        assertEquals(""
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 5 len: 2]\n"
                + "SubLanguageToken[foo&bar offset: 7 len: 1]\n"
                + "SubLanguageToken[foo&bar offset: 8 len: 1]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 9 len: 1]"
                + "", s);
    }

    public void testSubLanguageTokenMerge4() throws Exception {
        ArrayList<SubLanguageToken> lst = new ArrayList<SubLanguageToken>();
        lst.add(new SubLanguageToken("foo", "bar", 7, 1));
        lst.add(new SubLanguageToken("foo", "bar", 9, 1));
        SubLanguageToken.fillWithDefault(lst, "this", 5, 10);
        String s = StringUtils.join("\n", lst);
        assertEquals(""
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 5 len: 2]\n"
                + "SubLanguageToken[foo&bar offset: 7 len: 1]\n"
                + "SubLanguageToken[this&__dftl_partition_content_type offset: 8 len: 1]\n"
                + "SubLanguageToken[foo&bar offset: 9 len: 1]"
                + "", s);
    }
}
