/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.regexp;

import org.jcodings.Encoding;

public class QuoteRegexp {

    public static byte[] quote(byte[] bytes, Encoding enc) {
        int end = bytes.length;

        boolean foundSpecial = false;
        for (int pos = 0; pos < end; pos++) {
            int c = bytes[pos] & 0xff;
            int lenBytes = enc.length(bytes, pos, end);
            if (lenBytes != 1) {
                while (lenBytes-- > 0 && pos < end) {
                    pos++;
                }
                pos--;
                //skip this one (unicode)
                continue;
            }
            switch (c) {
                case '(':
                case ')':

                case '[':
                case ']':

                case '{':
                case '}':

                case '-':
                case '+':
                case '*':

                case '|':

                case '.':

                case '\\':

                case '?':

                case '^':

                case '$':

                case ' ':

                case '#':

                case '\t':
                case '\f':

                case '\r':
                case '\n':
                    foundSpecial = true;
                    break;
            }
        }
        if (!foundSpecial) {
            return bytes;
        }

        // At most we'll double it...
        byte[] obytes = new byte[end * 2];
        int i = 0;

        for (int pos = 0; pos < end; pos++) {
            int c = bytes[pos] & 0xff;
            int lenBytes = enc.length(bytes, pos, end);
            if (lenBytes != 1) {
                while (lenBytes-- > 0 && pos < end) {
                    obytes[i++] = bytes[pos++];
                }
                pos--;
                //skip this one after copying it (unicode)
                continue;
            }

            switch (c) {
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':

                case '-':
                case '+':
                case '*':

                case '|':

                case '.':

                case '\\':

                case '?':

                case '^':

                case '$':

                case '#':
                    obytes[i++] = '\\';
                    break;

                case ' ':
                    obytes[i++] = '\\';
                    obytes[i++] = ' ';
                    continue;

                case '\t':
                    obytes[i++] = '\\';
                    obytes[i++] = 't';
                    continue;

                case '\r':
                    obytes[i++] = '\\';
                    obytes[i++] = 'r';
                    continue;

                case '\n':
                    obytes[i++] = '\\';
                    obytes[i++] = 'n';
                    continue;

                case '\f':
                    obytes[i++] = '\\';
                    obytes[i++] = 'f';
                    continue;
            }
            obytes[i++] = (byte) c;
        }

        if (i == obytes.length) {
            return obytes;
        }
        byte[] obytes2 = new byte[i];
        System.arraycopy(obytes, 0, obytes2, 0, i);
        return obytes2;
    }
}
