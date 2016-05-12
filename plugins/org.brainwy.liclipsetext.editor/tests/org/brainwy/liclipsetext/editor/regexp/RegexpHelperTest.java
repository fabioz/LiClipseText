package org.brainwy.liclipsetext.editor.regexp;

import org.brainwy.liclipsetext.editor.regexp.RegexpHelper;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.structure.LinkedListWarningOnSlowOperations;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class RegexpHelperTest extends TestCase {

    public void testFill() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        RegexpHelper.fillWithSubToken(new Token("bar"), new Region(10, 10), lst);
        assertEquals("[SubRuleToken[bar offset: 10 len: 10]]", lst.toString());
    }

    public void testFill2() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("foo"), 12, 1));
        lst.add(new SubRuleToken(new Token("foo"), 15, 2));
        RegexpHelper.fillWithSubToken(new Token("bar"), new Region(10, 10), lst);
        assertEquals(
                "[SubRuleToken[bar offset: 10 len: 2], "
                        + "SubRuleToken[foo offset: 12 len: 1], "
                        + "SubRuleToken[bar offset: 13 len: 2], "
                        + "SubRuleToken[foo offset: 15 len: 2], "
                        + "SubRuleToken[bar offset: 17 len: 3]]",
                lst.toString());
    }

    public void testAddSubToken() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("foo"), 1, 1));
        assertEquals(
                "[SubRuleToken[foo offset: 1 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken1() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("foo"), 1, 1));
        lst.add(new SubRuleToken(new Token("foo"), 3, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("foo"), 5, 2));
        assertEquals(
                "[SubRuleToken[foo offset: 1 len: 1], "
                        + "SubRuleToken[foo offset: 3 len: 2], "
                        + "SubRuleToken[foo offset: 5 len: 2]]",
                lst.toString());

    }

    public void testAddSubToken2() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("foo"), 1, 1));
        lst.add(new SubRuleToken(new Token("foo"), 3, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("foo"), 4, 1));
        assertEquals(
                "[SubRuleToken[foo offset: 1 len: 1], "
                        + "SubRuleToken[foo offset: 3 len: 1], "
                        + "SubRuleToken[foo offset: 4 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken3() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 1));
        lst.add(new SubRuleToken(new Token("b"), 3, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 3, 1));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[c offset: 3 len: 1], "
                        + "SubRuleToken[b offset: 4 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken4() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 1));
        lst.add(new SubRuleToken(new Token("b"), 3, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 4, 2));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[b offset: 3 len: 1], "
                        + "SubRuleToken[c offset: 4 len: 2]]",
                lst.toString());

    }

    public void testAddSubToken5() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 1));
        lst.add(new SubRuleToken(new Token("b"), 4, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 3, 2));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[c offset: 3 len: 2], "
                        + "SubRuleToken[b offset: 5 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken6() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 1));
        lst.add(new SubRuleToken(new Token("b"), 4, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 4, 3));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[c offset: 4 len: 3]]",
                lst.toString());

    }

    public void testAddSubToken7() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 1));
        lst.add(new SubRuleToken(new Token("b"), 4, 5));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 5, 1));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[b offset: 4 len: 1], "
                        + "SubRuleToken[c offset: 5 len: 1], "
                        + "SubRuleToken[b offset: 6 len: 3]]",
                lst.toString());

    }

    public void testAddSubToken8() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 4, 3));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 2, 4));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[c offset: 2 len: 4], "
                        + "SubRuleToken[b offset: 6 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken9() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 4, 3));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 1, 4));
        assertEquals(
                "[SubRuleToken[c offset: 1 len: 4], "
                        + "SubRuleToken[b offset: 5 len: 2]]",
                lst.toString());
    }

    public void testAddSubToken10() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 4, 3));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 1, 2));
        assertEquals(
                "[SubRuleToken[c offset: 1 len: 2], "
                        + "SubRuleToken[a offset: 3 len: 1], "
                        + "SubRuleToken[b offset: 4 len: 3]]",
                lst.toString());

    }

    public void testAddSubToken11() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 1, 2));
        assertEquals(
                "[SubRuleToken[c offset: 1 len: 2], "
                        + "SubRuleToken[a offset: 3 len: 1], "
                        + "SubRuleToken[b offset: 5 len: 2]]",
                lst.toString());

    }

    public void testAddSubToken12() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 2, 1));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[c offset: 2 len: 1], "
                        + "SubRuleToken[a offset: 3 len: 1], "
                        + "SubRuleToken[b offset: 5 len: 2]]",
                lst.toString());

    }

    public void testAddSubToken13() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 2));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("c"), 3, 1));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 2], "
                        + "SubRuleToken[c offset: 3 len: 1], "
                        + "SubRuleToken[b offset: 5 len: 2]]",
                lst.toString());

    }

    public void testAddSubToken14() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 1));
        lst.add(new SubRuleToken(new Token("b"), 2, 1));
        lst.add(new SubRuleToken(new Token("c"), 3, 1));
        lst.add(new SubRuleToken(new Token("d"), 4, 1));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 1, 1));
        assertEquals(
                "[SubRuleToken[e offset: 1 len: 1], "
                        + "SubRuleToken[b offset: 2 len: 1], "
                        + "SubRuleToken[c offset: 3 len: 1], "
                        + "SubRuleToken[d offset: 4 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken15() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 3, 3));
        lst.add(new SubRuleToken(new Token("c"), 6, 1));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("d"), 1, 1));
        assertEquals(
                "[SubRuleToken[d offset: 1 len: 1], "
                        + "SubRuleToken[a offset: 2 len: 2], "
                        + "SubRuleToken[b offset: 3 len: 3], "
                        + "SubRuleToken[c offset: 6 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken16() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 3, 3));
        lst.add(new SubRuleToken(new Token("c"), 6, 1));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("d"), 0, 1));
        assertEquals(
                "[SubRuleToken[d offset: 0 len: 1], "
                        + "SubRuleToken[a offset: 1 len: 3], "
                        + "SubRuleToken[b offset: 3 len: 3], "
                        + "SubRuleToken[c offset: 6 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken17() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 2, 2));
        lst.add(new SubRuleToken(new Token("b"), 3, 3));
        lst.add(new SubRuleToken(new Token("c"), 6, 1));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("d"), 0, 1));
        assertEquals(
                "[SubRuleToken[d offset: 0 len: 1], "
                        + "SubRuleToken[a offset: 2 len: 2], "
                        + "SubRuleToken[b offset: 3 len: 3], "
                        + "SubRuleToken[c offset: 6 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken18() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 2, 2));
        lst.add(new SubRuleToken(new Token("b"), 3, 3));
        lst.add(new SubRuleToken(new Token("c"), 6, 1));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("d"), 0, 10));
        assertEquals(
                "[SubRuleToken[d offset: 0 len: 10]]",
                lst.toString());

    }

    public void testAddSubToken19() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 3, 3));
        lst.add(new SubRuleToken(new Token("c"), 6, 3));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("d"), 2, 8));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[d offset: 2 len: 8]]",
                lst.toString());

    }

    public void testAddSubToken20() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 3, 3));
        lst.add(new SubRuleToken(new Token("c"), 6, 3));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("d"), 2, 5));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[d offset: 2 len: 5], "
                        + "SubRuleToken[c offset: 7 len: 2]]",
                lst.toString());

    }

    public void testAddSubToken21() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 3, 3));
        lst.add(new SubRuleToken(new Token("c"), 6, 3));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("d"), 2, 3));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 1], "
                        + "SubRuleToken[d offset: 2 len: 3], "
                        + "SubRuleToken[b offset: 5 len: 1], "
                        + "SubRuleToken[c offset: 6 len: 3]]",
                lst.toString());

    }

    public void testAddSubToken22() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        lst.add(new SubRuleToken(new Token("c"), 9, 2));
        lst.add(new SubRuleToken(new Token("d"), 13, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 4, 10));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 3], "
                        + "SubRuleToken[e offset: 4 len: 10], "
                        + "SubRuleToken[d offset: 14 len: 1]]",
                lst.toString());

    }

    public void testAddSubToken23() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        lst.add(new SubRuleToken(new Token("c"), 9, 2));
        lst.add(new SubRuleToken(new Token("d"), 13, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 5, 9));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 3], "
                        + "SubRuleToken[e offset: 5 len: 9], "
                        + "SubRuleToken[d offset: 14 len: 1]]",
                lst.toString());
    }

    public void testAddSubToken24() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        lst.add(new SubRuleToken(new Token("c"), 9, 2));
        lst.add(new SubRuleToken(new Token("d"), 13, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 6, 8));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 3], "
                        + "SubRuleToken[b offset: 5 len: 1], "
                        + "SubRuleToken[e offset: 6 len: 8], "
                        + "SubRuleToken[d offset: 14 len: 1]]",
                lst.toString());
    }

    public void testAddSubToken25() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        lst.add(new SubRuleToken(new Token("c"), 9, 2));
        lst.add(new SubRuleToken(new Token("d"), 13, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 6, 7));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 3], "
                        + "SubRuleToken[b offset: 5 len: 1], "
                        + "SubRuleToken[e offset: 6 len: 7], "
                        + "SubRuleToken[d offset: 13 len: 2]]",
                lst.toString());
    }

    public void testAddSubToken26() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        lst.add(new SubRuleToken(new Token("c"), 9, 2));
        lst.add(new SubRuleToken(new Token("d"), 13, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 6, 6));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 3], "
                        + "SubRuleToken[b offset: 5 len: 1], "
                        + "SubRuleToken[e offset: 6 len: 6], "
                        + "SubRuleToken[d offset: 13 len: 2]]",
                lst.toString());
    }

    public void testAddSubToken27() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 1, 3));
        lst.add(new SubRuleToken(new Token("b"), 5, 2));
        lst.add(new SubRuleToken(new Token("c"), 9, 4));
        lst.add(new SubRuleToken(new Token("d"), 13, 2));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 6, 6));
        assertEquals(
                "[SubRuleToken[a offset: 1 len: 3], "
                        + "SubRuleToken[b offset: 5 len: 1], "
                        + "SubRuleToken[e offset: 6 len: 6], "
                        + "SubRuleToken[c offset: 12 len: 1], "
                        + "SubRuleToken[d offset: 13 len: 2]]",
                lst.toString());
    }

    public void testAddSubToken28() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 2, 3));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 1, 1));
        assertEquals(
                "[SubRuleToken[e offset: 1 len: 1], "
                        + "SubRuleToken[a offset: 2 len: 3]]",
                lst.toString());
    }

    public void testAddSubToken29() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 2, 3));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 1, 2));
        assertEquals(
                "[SubRuleToken[e offset: 1 len: 2], "
                        + "SubRuleToken[a offset: 3 len: 2]]",
                lst.toString());
    }

    public void testAddSubToken30() throws Exception {
        LinkedListWarningOnSlowOperations<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        lst.add(new SubRuleToken(new Token("a"), 3, 17));
        RegexpHelper.addSubRuleToken(lst, new SubRuleToken(new Token("e"), 3, 1));
        assertEquals(
                "[SubRuleToken[e offset: 3 len: 1], SubRuleToken[a offset: 4 len: 16]]",
                lst.toString());
    }

}
