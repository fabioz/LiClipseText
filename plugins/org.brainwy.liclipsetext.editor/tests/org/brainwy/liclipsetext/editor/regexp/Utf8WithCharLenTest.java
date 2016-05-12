package org.brainwy.liclipsetext.editor.regexp;

import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.shared_core.testutils.TestUtils;

import junit.framework.TestCase;

public class Utf8WithCharLenTest extends TestCase {

    public void testUtf8WithCharLen2() throws Exception {
        String string = "str";
        Utf8WithCharLen utf8WithCharLen = new Utf8WithCharLen(string);
        assertEquals(0, utf8WithCharLen.getBytesPosFromCharPos(0));
        assertEquals(1, utf8WithCharLen.getBytesPosFromCharPos(1));
        assertEquals(2, utf8WithCharLen.getBytesPosFromCharPos(2));
        assertEquals(3, utf8WithCharLen.getBytesPosFromCharPos(3));
        try {
            utf8WithCharLen.getBytesPosFromCharPos(4);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            utf8WithCharLen.getBytesPosFromCharPos(-1);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    public void testUtf8WithCharLen2ConstructedWithBytes() throws Exception {
        String string = "str";
        Utf8WithCharLen utf8WithCharLen = new Utf8WithCharLen(string.getBytes("utf-8"));
        assertEquals(0, utf8WithCharLen.getBytesPosFromCharPos(0));
        assertEquals(1, utf8WithCharLen.getBytesPosFromCharPos(1));
        assertEquals(2, utf8WithCharLen.getBytesPosFromCharPos(2));
        assertEquals(3, utf8WithCharLen.getBytesPosFromCharPos(3));
        try {
            utf8WithCharLen.getBytesPosFromCharPos(4);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            utf8WithCharLen.getBytesPosFromCharPos(-1);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    public void testUtf8WithCharLen() throws Exception {
        String string = "myááçóúôõaab";
        Utf8WithCharLen utf8WithCharLen = new Utf8WithCharLen(string);

        assertEquals(0, utf8WithCharLen.getBytesPosFromCharPos(0));
        assertEquals(8, utf8WithCharLen.getBytesPosFromCharPos(5));
        assertEquals(10, utf8WithCharLen.getBytesPosFromCharPos(6));
        assertEquals(12, utf8WithCharLen.getBytesPosFromCharPos(7));
        try {
            utf8WithCharLen.getBytesPosFromCharPos(55);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        assertEquals(0, utf8WithCharLen.getCharPosFromBytesPos(0));
        assertEquals(5, utf8WithCharLen.getCharPosFromBytesPos(8));
        assertEquals(6, utf8WithCharLen.getCharPosFromBytesPos(10));
        assertEquals(7, utf8WithCharLen.getCharPosFromBytesPos(12));
        try {
            utf8WithCharLen.getCharPosFromBytesPos(55);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        assertEquals(utf8WithCharLen.getBytesLen(), string.getBytes("utf-8").length);
        assertEquals(
                TestUtils.arrayToExpected(string.getBytes("utf-8")),
                TestUtils.arrayToExpected(utf8WithCharLen.getBytes()));

        assertEquals(
                TestUtils.listToExpected("0",
                        "1",
                        "2",
                        "2",
                        "3",
                        "3",
                        "4",
                        "4",
                        "5",
                        "5",
                        "6",
                        "6",
                        "7",
                        "7",
                        "8",
                        "8",
                        "9",
                        "10",
                        "11"),
                TestUtils.arrayToExpected(utf8WithCharLen.getDiscoragedAccessCharsLenInBytes()));
    }

    public void testUtf8WithCharLenConstructedWithBytes() throws Exception {
        String string = "myááçóúôõaab";
        Utf8WithCharLen utf8WithCharLen = new Utf8WithCharLen(string.getBytes("utf-8"));

        assertEquals(0, utf8WithCharLen.getBytesPosFromCharPos(0));
        assertEquals(8, utf8WithCharLen.getBytesPosFromCharPos(5));
        assertEquals(10, utf8WithCharLen.getBytesPosFromCharPos(6));
        assertEquals(12, utf8WithCharLen.getBytesPosFromCharPos(7));
        try {
            utf8WithCharLen.getBytesPosFromCharPos(55);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        assertEquals(0, utf8WithCharLen.getCharPosFromBytesPos(0));
        assertEquals(5, utf8WithCharLen.getCharPosFromBytesPos(8));
        assertEquals(6, utf8WithCharLen.getCharPosFromBytesPos(10));
        assertEquals(7, utf8WithCharLen.getCharPosFromBytesPos(12));
        try {
            utf8WithCharLen.getCharPosFromBytesPos(55);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        assertEquals(utf8WithCharLen.getBytesLen(), string.getBytes("utf-8").length);
        assertEquals(
                TestUtils.arrayToExpected(string.getBytes("utf-8")),
                TestUtils.arrayToExpected(utf8WithCharLen.getBytes()));

        assertEquals(
                TestUtils.listToExpected("0",
                        "1",
                        "2",
                        "2",
                        "3",
                        "3",
                        "4",
                        "4",
                        "5",
                        "5",
                        "6",
                        "6",
                        "7",
                        "7",
                        "8",
                        "8",
                        "9",
                        "10",
                        "11"),
                TestUtils.arrayToExpected(utf8WithCharLen.getDiscoragedAccessCharsLenInBytes()));
    }

    public void testUtf8WithCharLenConstructedWithBytes2() throws Exception {
        String string = "ô";
        Utf8WithCharLen utf8WithCharLen = new Utf8WithCharLen(string.getBytes("utf-8"));

        assertEquals(0, utf8WithCharLen.getBytesPosFromCharPos(0));
        assertEquals(2, utf8WithCharLen.getBytesPosFromCharPos(1));
    }

    public void testUtf8WithCharLenConstructedWithString2() throws Exception {
        String string = "ô";
        Utf8WithCharLen utf8WithCharLen = new Utf8WithCharLen(string);

        assertEquals(0, utf8WithCharLen.getBytesPosFromCharPos(0));
        assertEquals(2, utf8WithCharLen.getBytesPosFromCharPos(1));
    }
}
