/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.regexp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.rules.IToken;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;

public class RegexpHelper {

    public static Regex createRegexp(String regexp) {
        byte[] bytes = getUtfBytes(regexp);
        return new Regex(bytes, 0, bytes.length, Option.CAPTURE_GROUP, UTF8Encoding.INSTANCE);
    }

    public static Regex createRegexp(byte[] bytes) {
        return new Regex(bytes, 0, bytes.length, Option.CAPTURE_GROUP, UTF8Encoding.INSTANCE);
    }

    private static byte[] getUtfBytes(String string) {
        try {
            return string.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.log(e);
            throw new RuntimeException(e);
        }
    }

    public static Utf8WithCharLen getBytes(String string) {
        return new Utf8WithCharLen(string);
    }

    public static class ReplaceInfo {

        public int groupId;
        public int strBeginPos;
        public int strEndPos;

        public ReplaceInfo(int groupId, int strBeginPos, int strEndPos) {
            this.groupId = groupId;
            this.strBeginPos = strBeginPos;
            this.strEndPos = strEndPos;
        }

        @Override
        public String toString() {
            return "Replace[groupId: " + groupId + " begin: " + strBeginPos + " end:" + strEndPos + "]";
        }
    }

    public static List<ReplaceInfo> createReplaces(String end) {
        Regex regexp = RegexpHelper.createRegexp("(\\\\)(\\d+)"); // That's actually to match a number (\d+)

        // Replace \\ to // so that we don't replace escaped slashes (which shouldn't be replaced).
        Utf8WithCharLen bytes = RegexpHelper.getBytes(StringUtils.replaceAll(end, "\\\\", "//"));
        Matcher matcher = regexp.matcher(bytes.getBytes());

        List<ReplaceInfo> posToOffsetAndLen = new ArrayList<>();
        int last = 0;
        int bytesLen = bytes.getBytesLen();
        while (last < bytesLen) {
            int search = matcher.search(last, bytesLen, Option.CAPTURE_GROUP);
            if (search >= 0) {
                CharsRegion region = new CharsRegion(matcher, bytes);
                posToOffsetAndLen.add(new ReplaceInfo(
                        Integer.parseInt(end.substring(region.getStrBeginPos(2), region.getStrEndPos(2))),
                        region.getStrBeginPos(0), region.getStrEndPos(0)));

                last = region.getBytesEndPos(0);
            } else {
                break;
            }
        }
        return posToOffsetAndLen;
    }

    public static String regexpWithNoSlashGMatch(String string) {
        FastStringBuffer buf = new FastStringBuffer(string, 0);

        Regex regexp = RegexpHelper.createRegexp("(\\\\G)");

        // Replace \\ to // so that we don't replace escaped slashes (which shouldn't be replaced).
        Utf8WithCharLen bytes = RegexpHelper.getBytes(StringUtils.replaceAll(string, "\\\\", "//"));
        Matcher matcher = regexp.matcher(bytes.getBytes());
        int last = 0;
        int bytesLen = bytes.getBytesLen();

        while (last < bytesLen) {
            int search = matcher.search(last, bytesLen, Option.CAPTURE_GROUP);
            if (search >= 0) {
                CharsRegion region = new CharsRegion(matcher, bytes);
                buf.replace(region.getStrBeginPos(1), region.getStrEndPos(1), "\0");
                last = region.getBytesEndPos(0);
            } else {
                break;
            }

        }
        return buf.toString();
    }

    public static String replaceWithMap(String string, CharsRegion evaluate,
            List<ReplaceInfo> replacesMap) {
        //iterate backwards
        ListIterator<ReplaceInfo> it = replacesMap.listIterator(replacesMap.size());
        FastStringBuffer buf = new FastStringBuffer(string, replacesMap.size() * 5);
        while (it.hasPrevious()) {
            ReplaceInfo previous = it.previous();
            Integer groupId = previous.groupId;
            String posContents = evaluate.getStrPosContents(groupId);
            if (posContents != null) {
                buf.replace(previous.strBeginPos, previous.strEndPos,
                        getUtf8AsString(QuoteRegexp.quote(getUtfBytes(posContents), UTF8Encoding.INSTANCE)));
            }
        }
        return buf.toString();
    }

    public static String getUtf8AsString(byte[] quote) {
        try {
            return new String(quote, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.log(e);
            throw new RuntimeException(e);
        }
    }

    public static void fillWithSubToken(IToken contentScope, IRegion contentRegion, LinkedList<SubRuleToken> lst) {
        SubRuleToken.fillWithSubToken(contentScope, contentRegion, lst);
    }

    public static void fillWithSubToken(IToken contentScope, final int offset, final int len,
            LinkedList<SubRuleToken> lst) {
        SubRuleToken.fillWithSubToken(contentScope, offset, len, lst);
    }

    /**
     * Adds a sub rule token to a list with existing sub-rule tokens. It fixes existing sub-rule tokens
     * so that they do not overlap. Note that list is always kept ordered by the offset/len.
     */
    public static void addSubRuleToken(LinkedList<SubRuleToken> lst, SubRuleToken subRuleToken) {
        SubRuleToken.addSubRuleToken(lst, subRuleToken);
    }

}
