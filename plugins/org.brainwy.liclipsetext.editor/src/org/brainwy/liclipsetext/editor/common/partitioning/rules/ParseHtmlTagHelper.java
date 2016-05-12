/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.rules.SubLanguageToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.core.runtime.AssertionFailedException;

public class ParseHtmlTagHelper {

    public class HtmlAttribute {

        public int stringStart = -1;
        public int stringEnd = -1;
        public final int startAttribute;
        public final int endAttribute;
        public final String attribute;
        public String string;
        private int quoteChar;

        public HtmlAttribute(int startAttribute, int endAttribute) {
            this.startAttribute = startAttribute;
            this.endAttribute = endAttribute;
            this.attribute = new String(charArray, startAttribute, endAttribute - startAttribute);
        }

        @Override
        public String toString() {
            return new FastStringBuffer("HtmlAttribute[", 50)
                    .append("\n  attr start: ")
                    .append(startAttribute)
                    .append("\n  attr end: ")
                    .append(endAttribute)
                    .append("\n  attr: ")
                    .append(attribute)
                    .append("\n  value start: ")
                    .append(stringStart)
                    .append("\n  value end: ")
                    .append(stringEnd)
                    .append("\n  value: ")
                    .append(string)
                    .append("\n] ")
                    .toString();
        }

        public void setStartEndString(int stringStart, int stringEnd, int quoteChar) {
            this.stringStart = stringStart;
            this.stringEnd = stringEnd;
            this.string = new String(charArray, stringStart, stringEnd - stringStart);
            this.quoteChar = quoteChar;
        }

    }

    // The attributes below are the 'out' attributes (they are meant to be read by users).
    public int startBracePos = -1; //position of '<' (-1 means no tag found)
    public int endBracePos = -1; //position of '>' (-1 means unclosed)
    public int startTagName = -1; //where the tag name starts -- i.e. <|script> (| marks the position)) (-1 means no name found)
    public int endTagName = -1; //where the tag name ends -- i.e.: <script|>
    public int slashPos = -1; //slash position (-1 if not found) -- i.e.: <|/script>
    public final List<HtmlAttribute> attributes = new ArrayList<HtmlAttribute>();

    /**
     * Generates the needed tokens. Must fill any empty space between the baseOffset and endOffset
     * with a default content type.
     */
    public List<SubLanguageToken> generateTokens(String baseLanguage, int baseOffset, int endOffset) {
        List<SubLanguageToken> lst = new ArrayList<SubLanguageToken>();
        if (startBracePos != -1) {
            if (slashPos == startBracePos + 1) {
                //identify </ as a close_tag
                lst.add(new SubLanguageToken(baseLanguage, "close_tag", baseOffset + startBracePos, 2));

            } else {
                //identify < as an open_tag
                lst.add(new SubLanguageToken(baseLanguage, "open_tag", baseOffset + startBracePos, 1));
            }
        }
        if (startTagName != -1) {
            if (slashPos == startBracePos + 1) {
                //Itas a closing class (not a regular one)
                lst.add(new SubLanguageToken(baseLanguage, "close_class", baseOffset + startTagName, endTagName
                        - startTagName));

            } else {
                lst.add(new SubLanguageToken(baseLanguage, "class", baseOffset + startTagName, endTagName
                        - startTagName));
            }
        }
        int size = attributes.size();
        for (int i = 0; i < size; i++) {
            HtmlAttribute htmlAttribute = attributes.get(i);
            lst.add(new SubLanguageToken(baseLanguage, "keyword", baseOffset + htmlAttribute.startAttribute,
                    htmlAttribute.endAttribute - htmlAttribute.startAttribute));
            if (htmlAttribute.stringStart != -1 && htmlAttribute.stringEnd > htmlAttribute.stringStart) {
                if (htmlAttribute.quoteChar == '\'') {
                    lst.add(new SubLanguageToken(baseLanguage, "singleQuotedString", baseOffset
                            + htmlAttribute.stringStart,
                            htmlAttribute.stringEnd - htmlAttribute.stringStart));
                } else {
                    lst.add(new SubLanguageToken(baseLanguage, "doubleQuotedString", baseOffset
                            + htmlAttribute.stringStart,
                            htmlAttribute.stringEnd - htmlAttribute.stringStart));
                }
            }

        }
        if (endBracePos != -1) {
            if (slashPos == endBracePos - 1) {
                lst.add(new SubLanguageToken(baseLanguage, "open_tag_close", baseOffset + endBracePos, 2));

            } else {
                lst.add(new SubLanguageToken(baseLanguage, "bracket", baseOffset + endBracePos, 1));
            }
        }
        SubLanguageToken.fillWithDefault(lst, baseLanguage, baseOffset, endOffset);
        return lst;
    }

    // Used for reading
    int i = 0;
    char[] charArray;
    int len;
    int c;

    @SuppressWarnings("unused")
    public ParseHtmlTagHelper(String tag) {
        charArray = tag.toCharArray();
        len = charArray.length;

        c = read();
        while (c != '<' && c != -1) {
            c = read();
        }
        if (c == -1) {
            return;
        }
        startBracePos = i - 1;
        c = read();
        if (c == '/') {
            slashPos = i - 1;
            c = read();
        }
        skipWhitespaces();
        readTag();
        skipWhitespaces();
        while (c != '>' && c != -1 && c != '/') {
            readAttribute();
        }
        if (c == '/') {
            slashPos = i - 1;
            c = read();
        }
        if (c == '>') {
            endBracePos = i - 1;
        }
        //Only for debug
        if (false) {
            check("Start brace", startBracePos, '<');
            check("End brace", endBracePos, '>');
            check("Slash", slashPos, '/');
        }
        charArray = null; //release memory!
    }

    private void check(String msg, int pos, char d) {
        if (pos != -1) {
            if (charArray[pos] != d) {
                throw new AssertionFailedException(msg + " does not match position.");
            }
        }
    }

    private void readTag() {
        if (Character.isWhitespace(c) || c == '=' || c == -1 || c == '>' || c == '/') {
            return; //unable to read tag!
        }
        startTagName = i - 1;
        while (!Character.isWhitespace(c) && c != '>' && c != -1 && c != '/') {
            c = read();
        }
        endTagName = i - 1;
    }

    private void readAttribute() {
        int startAttribute = i - 1;
        while (!Character.isWhitespace(c) && c != '=' && c != -1 && c != '>' && c != '/') {
            c = read();
        }
        int endAttribute = i - 1;
        skipWhitespaces();
        HtmlAttribute attribute = new HtmlAttribute(startAttribute, endAttribute);
        if (c == '=') {
            c = read();
            skipWhitespaces();
            if (c == '"' || c == '\'') {
                HtmlAttribute readString = readString(attribute, c);
                if (readString != null) {
                    this.attributes.add(readString);
                }
            }
        }
    }

    private HtmlAttribute readString(HtmlAttribute attribute, int expectedFinalChar) {
        int startAttribute = i;
        c = read();
        while (c != expectedFinalChar && c != -1) {
            c = read();
        }
        if (c == -1) {
            return null;
        }
        int endAttribute = i - 1;
        attribute.setStartEndString(startAttribute, endAttribute, c);
        return attribute;
    }

    private void skipWhitespaces() {
        while (Character.isWhitespace(c) && c != -1) {
            c = read();
        }

    }

    private int read() {
        if (i >= charArray.length) {
            return -1;
        }
        char temp = charArray[i];
        i++;
        return temp;
    }

    @Override
    public String toString() {
        return new FastStringBuffer("Tag{", 60)
                .append("\nstartBracePos: ")
                .append(startBracePos)
                .append("\nslashPos: ")
                .append(slashPos)
                .append("\nstartTagName: ")
                .append(startTagName)
                .append("\nendTagName: ")
                .append(endTagName)
                .append("\nattributes:\n")
                .appendObject(StringUtils.join("\n", attributes))
                .append("\nendBranceEnd: ")
                .append(endBracePos)
                .append("\n}")
                .toString();
    }

}
