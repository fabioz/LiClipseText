/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.regexp;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.IEvalCaptures;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.structure.LinkedListWarningOnSlowOperations;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.joni.Matcher;
import org.joni.Region;

public class CharsRegion {

    private Utf8WithCharLen bytes;
    private Matcher matcher;
    private Region region;
    private int strLength;
    private int currentColumnInBytes;

    public CharsRegion(Matcher matcher, Utf8WithCharLen bytes) {
        this(matcher, bytes, -1, 0);
    }

    public CharsRegion(Matcher matcher, Utf8WithCharLen bytes, int strLength, int currentColumnInBytes) {
        this.matcher = matcher;
        this.bytes = bytes;
        this.region = matcher.getRegion(); // Can be null
        this.strLength = strLength;
        this.currentColumnInBytes = currentColumnInBytes;
    }

    public int getCurrentColumnInBytes() {
        return currentColumnInBytes;
    }

    public int getCurrentColumnInStr() {
        return bytes.getCharPosFromBytesPos(currentColumnInBytes);
    }

    public int getMatchLength() {
        return getStrEndPos(0) - getStrBeginPos(0);
    }

    public int getNumRegions() {
        if (region == null) {
            return 0;
        }
        return region.numRegs;
    }

    public int getStrBeginPos(int groupId) {
        if (groupId == 0) {
            return bytes.getCharPosFromBytesPos(matcher.getBegin());
        }
        if (groupId >= region.beg.length) {
            return -1;
        }
        return bytes.getCharPosFromBytesPos(region.beg[groupId]);
    }

    public int getStrEndPos(int groupId) {
        if (groupId == 0) {
            return bytes.getCharPosFromBytesPos(matcher.getEnd());
        }
        return bytes.getCharPosFromBytesPos(region.end[groupId] - 1) + 1;
    }

    public int getBytesBeginPos(int groupId) {
        if (groupId == 0) {
            return matcher.getBegin();
        }
        return region.beg[groupId];
    }

    public int getBytesEndPos(int groupId) {
        if (groupId == 0) {
            return matcher.getEnd();
        }
        return region.end[groupId];
    }

    public boolean hasGroup(int groupId) {
        return region.numRegs >= groupId && region.beg[groupId] >= 0;
    }

    public String getStrPosContents(int groupId) {
        if (region == null && groupId != 0) {
            return null;
        }
        int offset = getBytesBeginPos(groupId);
        try {
            return new String(bytes.getBytes(), offset, getBytesEndPos(groupId) - offset, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getBytesPosContents(int groupId) {
        if (region == null && groupId != 0) {
            return null;
        }
        int offset = getBytesBeginPos(groupId);
        return Arrays.copyOfRange(bytes.getBytes(), offset, getBytesEndPos(groupId) - offset);
    }

    public List<SubRuleToken> createSubTokens(int initialOffset, SortedMap<Integer, IEvalCaptures> captures,
            ScannerRange scanner) throws DocumentTimeStampChangedException {

        LinkedList<SubRuleToken> lst = new LinkedListWarningOnSlowOperations<>();
        if (captures != null && captures.size() > 0) {
            Assert.isTrue(initialOffset >= 0);
            int regionLen = this.getMatchLength();
            int lastOffsetInMatcher = this.getStrBeginPos(0);
            SubRuleToken group0SubRuleToken = null;

            //Ok, we evaluated it, now, let's convert those to actual sub tokens.
            Set<Entry<Integer, IEvalCaptures>> entrySet = captures.entrySet();

            IEvalCaptures evalCaptures = captures.get(0);
            if (evalCaptures != null) {
                if (evalCaptures.isSimple()) {
                    group0SubRuleToken = new SubRuleToken(evalCaptures.getToken(), initialOffset + lastOffsetInMatcher,
                            regionLen);
                    lst.add(group0SubRuleToken);
                } else {
                    logOnce("Currently unable to use a non simple name in captures at position 0.");
                }
            }

            int numRegions = this.getNumRegions();
            for (Entry<Integer, IEvalCaptures> entry : entrySet) {
                int groupId = entry.getKey();
                if (groupId == 0) {
                    continue; // it's already handled at this point (i.e.: set as the filler).
                }

                if (groupId <= numRegions) {
                    IEvalCaptures value = entry.getValue();
                    int start = this.getStrBeginPos(groupId);
                    if (start == -1) {
                        // This can happen if some begin/end rule is defined
                        // with a capture which is used for both (begin/end capture)
                        continue;
                    }
                    int len = this.getStrEndPos(groupId) - start;
                    if (len == 0) {
                        continue; //ignore any 0-len match.
                    }

                    if (value.isSimple()) {
                        SubRuleToken sub = new SubRuleToken(value.getToken(), initialOffset + start, len);
                        if (group0SubRuleToken != null) {
                            group0SubRuleToken.addChild(sub);
                        } else {
                            lst.add(sub);
                        }
                    } else {
                        // We actually have to scan the partition
                        IRuleWithSubRules subRules = value.getRuleWithSubRules();
                        scanner.pushRange(initialOffset + start, len);
                        try {
                            while (true) {
                                SubRuleToken evaluateSubRules = subRules.evaluateSubRules(scanner, true);
                                if (evaluateSubRules != null) {
                                    if (group0SubRuleToken != null) {
                                        group0SubRuleToken.addChild(evaluateSubRules);
                                    } else {
                                        lst.add(evaluateSubRules);
                                    }
                                } else {
                                    //No match: walk at least 1 char!
                                    int c = scanner.read();
                                    if (c == ICharacterScanner.EOF) {
                                        scanner.unread();
                                        break;
                                    }
                                }
                            }
                        } finally {
                            scanner.popRange();
                        }
                    }
                }
            }
        }
        return lst;
    }

    private static boolean logged = false;

    private static void logOnce(String msg) {
        if (logged) {
            return;
        }
        logged = true;
        Log.log(msg);
    }

    @Override
    public String toString() {
        return new FastStringBuffer("CharsRegion[", 100).appendObject(this.bytes).append(", Regions: ")
                .append(this.getNumRegions()).append("]").toString();
    }

}
