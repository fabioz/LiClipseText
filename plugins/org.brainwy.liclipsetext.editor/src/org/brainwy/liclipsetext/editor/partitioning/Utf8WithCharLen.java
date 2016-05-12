/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.partitioning;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.jcodings.specific.UTF8Encoding;

public class Utf8WithCharLen {

    private byte[] bytes;
    private int[] charsPosFromBytePos;
    private int bytesLen;
    private int charsLen;
    private boolean frozen;
    private boolean lastCharEquals2LenLineDelimiter;

    public Utf8WithCharLen(String string) {
        bytes = new byte[string.length() * 4];
        charsPosFromBytePos = new int[bytes.length];

        int length = string.length();
        for (int i = 0; i < length; i++) {
            int codeLen = UTF8Encoding.INSTANCE.codeToMbc(string.charAt(i), bytes, bytesLen);
            for (int i1 = 0; i1 < codeLen; i1++) {
                charsPosFromBytePos[bytesLen + i1] = charsLen;
            }
            bytesLen += codeLen;
            charsLen += 1;
        }
    }

    /**
     * Constructor receiving utf-8 bytes directly.
     */
    public Utf8WithCharLen(byte[] bytesFoundInTargetGroup) {
        bytes = bytesFoundInTargetGroup;
        charsPosFromBytePos = new int[bytes.length];

        int length = bytes.length;
        for (int i = 0; i < length;) {
            int codeLen = UTF8Encoding.INSTANCE.length(bytes, i, length);
            for (int i1 = 0; i1 < codeLen; i1++) {
                charsPosFromBytePos[bytesLen + i1] = charsLen;
            }
            bytesLen += codeLen;
            i += codeLen;
            charsLen += 1;
        }

    }

    @Override
    public String toString() {
        try {
            return new FastStringBuffer("Utf8WithCharLen[", this.charsLen + 5).append(new String(bytes, "utf-8"))
                    .append(']').toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Important: clients should not mutate the returned bytes[]
     */
    public byte[] getBytes() {
        freeze();

        return bytes;
    }

    public int getBytesLen() {
        return bytesLen;
    }

    /**
     * Important: clients should not mutate the returned int[]
     *
     * Just for testing! May be null.
     */
    public int[] getDiscoragedAccessCharsLenInBytes() {
        freeze();

        return charsPosFromBytePos;
    }

    private void freeze() {
        if (!frozen) {
            frozen = true;

            if (bytes.length != bytesLen) {
                byte[] b = new byte[bytesLen];
                System.arraycopy(bytes, 0, b, 0, bytesLen);
                bytes = b;

            }

            if (bytesLen == charsLen) {
                //Optimization: don't keep this in memory
                //as the length is the same we can do
                //things easier.
                charsPosFromBytePos = null;
            } else {
                if (charsPosFromBytePos.length != bytesLen) {
                    int[] b = new int[bytesLen];
                    System.arraycopy(charsPosFromBytePos, 0, b, 0, bytesLen);

                    charsPosFromBytePos = b;
                }
            }
        }
    }

    public int getCharPosFromBytesPos(int posInBytes) {
        if (charsPosFromBytePos == null) {
            return posInBytes;
        }
        if (posInBytes < 0) {
            return -1;
        }
        if (posInBytes >= charsPosFromBytePos.length) {
            //One off can happen when finding the end of a regexp (it's the right boundary).
            return charsPosFromBytePos[posInBytes - 1] + 1;
        }
        return charsPosFromBytePos[posInBytes];
    }

    public int getBytesPosFromCharPos(int posInChars) {
        if (bytesLen == charsLen) {
            freeze(); // Only freeze here (the code below will freeze on getDiscoragedAccessCharsLenInBytes).

            // Same conditions as code below, but taking into account that the
            // bytes and chars len are the same.
            if (posInChars < 0 || bytes.length == 0 || posInChars > bytes.length) {
                throw new ArrayIndexOutOfBoundsException(posInChars);
            }
            return posInChars;
        }

        int[] charsLenInBytes = getDiscoragedAccessCharsLenInBytes();
        if (posInChars < 0 || charsLenInBytes.length == 0) {
            throw new ArrayIndexOutOfBoundsException(posInChars);
        }
        if (posInChars == 0) {
            return 0;
        }

        int last = charsLenInBytes[charsLenInBytes.length - 1];
        if (last < posInChars) {
            if (last == posInChars - 1) {
                return charsLenInBytes.length;
            } else {
                throw new ArrayIndexOutOfBoundsException(posInChars);
            }
        }

        int index = Arrays.binarySearch(charsLenInBytes, posInChars);
        while (index > 0) {
            if (charsLenInBytes[index - 1] == posInChars) {
                index--;
            } else {
                break;
            }
        }
        return index;
    }

    public void setLastCharEquals2LenLineDelimiter(boolean b) {
        this.lastCharEquals2LenLineDelimiter = b;
    }

    public boolean getLastCharEquals2LenLineDelimiter() {
        return this.lastCharEquals2LenLineDelimiter;
    }

    public int getCharsLen() {
        return this.charsLen;
    }

}
