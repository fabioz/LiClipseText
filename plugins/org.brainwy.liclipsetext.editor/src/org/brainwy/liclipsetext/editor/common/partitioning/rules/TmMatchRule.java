/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.brainwy.liclipsetext.editor.common.partitioning.IRuleWithSubRules2;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ContentTypeToken;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.ITokenWithReplaceOperation;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.editor.regexp.CharsRegion;
import org.brainwy.liclipsetext.editor.regexp.RegexpHelper;
import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;

public class TmMatchRule implements ILiClipsePredicateRule, IChangeTokenRule, IRuleWithSubRules, IRuleWithSubRules2,
        ITextMateRule, IPrintableRule {

    public final SortedMap<Integer, IEvalCaptures> fCaptures;
    private IToken fToken;
    private final Regex regexp;
    private final String regexpStr;
    private boolean containsSlashG;
    private Regex regexpWithoutSlashG;

    public TmMatchRule(String regexp, IToken token) {
        this(regexp, token, (SortedMap<Integer, IEvalCaptures>) null);
    }

    public TmMatchRule(String regexp, IToken token, Map<Object, Object> captures) {
        this(regexp, token, createCapturesMap(captures));
    }

    public TmMatchRule(String regexp, IToken token, SortedMap<Integer, IEvalCaptures> captures) {
        this.fCaptures = captures;
        this.regexpStr = regexp;
        this.regexp = RegexpHelper.createRegexp(regexp);
        containsSlashG = regexp.contains("\\G");
        if (containsSlashG) {
            regexpWithoutSlashG = RegexpHelper.createRegexp(RegexpHelper.regexpWithNoSlashGMatch(regexp));
        }
        this.fToken = token;
    }

    public String getRegexp() {
        return regexpStr;
    }

    public static SortedMap<Integer, IEvalCaptures> createCapturesMap(Map<Object, Object> captures) {
        if (captures == null) {
            return null;
        }
        SortedMap<Integer, IEvalCaptures> newCaptures = new TreeMap<>();
        if (captures != null) {
            Set<Entry<Object, Object>> entrySet = captures.entrySet();
            for (Entry<Object, Object> entry : entrySet) {
                Object key = entry.getKey();
                Integer newKey;
                if (key instanceof Integer) {
                    newKey = (Integer) key;
                } else {
                    newKey = Integer.parseInt(key.toString());
                }

                Object value = entry.getValue();
                IEvalCaptures newValue;
                if (value instanceof IToken) {
                    newValue = new SimpleEvalCaptures((IToken) value);
                } else if (value instanceof String) {
                    newValue = new SimpleEvalCaptures(new ContentTypeToken(value.toString()));
                } else if (value instanceof IRuleWithSubRules) {
                    newValue = new SubRuleEvalCaptures((IRuleWithSubRules) value);
                } else {
                    Log.log("Unable to handle: " + value);
                    continue;
                }
                newCaptures.put(newKey, newValue);
            }
        }
        return newCaptures;
    }

    private final static class SimpleEvalCaptures implements IEvalCaptures {

        private IToken token;

        public SimpleEvalCaptures(IToken value) {
            this.token = value;
        }

        @Override
        public boolean isSimple() {
            return true;
        }

        @Override
        public IToken getToken() {
            return token;
        }

        @Override
        public IRuleWithSubRules getRuleWithSubRules() {
            return null;
        }

        @Override
        public String toString() {
            return token.toString();
        }
    }

    private final static class SubRuleEvalCaptures implements IEvalCaptures {

        private IRuleWithSubRules ruleWithSubRules;

        public SubRuleEvalCaptures(IRuleWithSubRules value) {
            this.ruleWithSubRules = value;
        }

        @Override
        public boolean isSimple() {
            return false;
        }

        @Override
        public IToken getToken() {
            return ((ILiClipsePredicateRule) ruleWithSubRules).getSuccessToken();
        }

        @Override
        public IRuleWithSubRules getRuleWithSubRules() {
            return ruleWithSubRules;
        }

        @Override
        public String toString() {
            return ruleWithSubRules.toString();
        }
    }

    /**
     * Either will return null if it did not match or a list with the sub tokens matched.
     * @throws DocumentTimeStampChangedException
     */
    @Override
    public SubRuleToken evaluateSubRules(ScannerRange scanner, boolean generateSubRuleTokens)
            throws DocumentTimeStampChangedException {
        final int initialOffset = scanner.getMark();
        CharsRegion charsRegion = evaluateRegexp(scanner);

        if (charsRegion != null) {
            IToken token = fToken;
            if (token instanceof ITokenWithReplaceOperation) {
                ITokenWithReplaceOperation iTokenWithReplaceOperation = (ITokenWithReplaceOperation) token;
                token = iTokenWithReplaceOperation.replaceToken(charsRegion);
            }
            SubRuleToken subRuleToken = new SubRuleToken(token, initialOffset, scanner.getMark() - initialOffset);
            if (generateSubRuleTokens) {
                subRuleToken.addChildren(
                        charsRegion.createSubTokens(initialOffset - charsRegion.getCurrentColumnInStr(),
                                this.fCaptures, scanner));
            }
            return subRuleToken;
        }
        return null;
    }

    /**
     * The matcher or null.
     */
    public CharsRegion evaluateRegexp(ScannerRange range) {
        IMarkScanner mark = range;
        final int currOffset = mark.getMark();
        Regex useRegexp = this.regexp;
        if (this.containsSlashG) {
            // \G - means that we should match only if at the end of a previous match
            // so, let's see if this is the case.
            int lastRegexpMatchOffset = range.getLastRegexpMatchOffset();
            if (currOffset != lastRegexpMatchOffset) {
                useRegexp = regexpWithoutSlashG;
            }
        }

        Tuple<Utf8WithCharLen, Integer> lineFromOffsetAsBytes = range.getLineFromOffsetAsBytes(currOffset);

        if (lineFromOffsetAsBytes == null) {
            return null;
        }
        Utf8WithCharLen bytes = lineFromOffsetAsBytes.o1;
        int columnInBytes = lineFromOffsetAsBytes.o2;

        byte[] b = bytes.getBytes();
        Matcher matcher = useRegexp.matcher(b);
        int i = matcher.match(columnInBytes, b.length, Option.CAPTURE_GROUP);
        int newOffset;
        boolean found = i >= 0;
        if (found) {
            int matchEnd = matcher.getEnd();
            int end = bytes.getCharPosFromBytesPos(matchEnd);

            if (matchEnd == b.length - 1 && bytes.getLastCharEquals2LenLineDelimiter()) {
                //Workaround for \r\n match.
                end += 1;
            }

            int length = end;
            newOffset = currOffset - bytes.getCharPosFromBytesPos(columnInBytes) + length;
            if (newOffset >= currOffset) {
                mark.setMark(newOffset);
                range.setLastRegexpMatchOffset(newOffset);
                CharsRegion ret = new CharsRegion(matcher, bytes, length, columnInBytes);
                // System.out.println("Matched: " + this.regexpStr + " at: " + currOffset + " with scope: "
                //        + this.getSuccessToken() + "\n");
                return ret;
            }
        }
        newOffset = currOffset;
        mark.setMark(newOffset);
        return null;
    }

    public IToken evaluate(ScannerRange range) {
        try {
            return evaluate(range, false);
        } catch (Exception e) {
            Log.log(e);
            return Token.UNDEFINED;
        }
    }

    @Override
    public IToken getSuccessToken() {
        return fToken;
    }

    public IToken evaluate(ScannerRange range, boolean resume) {
        if (resume) {
            return Token.UNDEFINED;
        }
        if (evaluateRegexp(range) != null) {
            // At this level, just evaluate the regexp, no need for subtokens.
            return fToken;
        }
        return Token.UNDEFINED;
    }

    @Override
    public void setToken(IToken token) {
        this.fToken = token;
    }

    @Override
    public String toString() {
        return StringUtils.format("TmMatchRule (%s scope: %s captures: %s)", regexpStr,
                fToken != null ? fToken.getData() : "null",
                createCapturesStr(0));
    }

    public String createCapturesStr(int level) {
        SortedMap<Integer, IEvalCaptures> captures = fCaptures;
        return createCapturesStr(captures, level);
    }

    public static String createCapturesStr(SortedMap<Integer, IEvalCaptures> captures, int level) {
        if (captures == null || captures.size() == 0) {
            return "{}";
        }
        String baseIndent = new FastStringBuffer().appendN("    ", level).toString();
        String indent = new FastStringBuffer().appendN("    ", level + 1).toString();
        FastStringBuffer buf = new FastStringBuffer();
        buf.append("{\n");
        Set<Entry<Integer, IEvalCaptures>> entrySet = captures.entrySet();
        for (Entry<Integer, IEvalCaptures> entry : entrySet) {
            buf.append(indent);
            buf.append(entry.getKey());
            IEvalCaptures value = entry.getValue();
            if (value.isSimple()) {
                buf.append(": { name: ");
                buf.appendObject(value.getToken().getData());
                buf.append(" },");

            } else {
                buf.append(": { patterns: ");
                buf.appendObject(value.toString());
                buf.append(" },");
            }
            buf.append("\n");
        }
        buf.append(baseIndent);
        buf.append("}");
        return buf.toString();
    }

    public static String getPatternsYaml(ILiClipsePredicateRule[] subRules, int level) {
        String indent = new FastStringBuffer().appendN("    ", level + 1).toString();

        int length = subRules.length;
        FastStringBuffer buf = new FastStringBuffer(length * 20);
        buf.append("[\n");
        buf.append(indent);

        for (int i = 0; i < length; i++) {
            ILiClipsePredicateRule rule = subRules[i];
            if (rule instanceof IPrintableRule) {
                buf.append(((IPrintableRule) rule).toTmYaml(level + 1));
            } else {
                buf.appendObject(rule);
            }
            buf.append(",\n");
            buf.append(indent);
        }
        buf.deleteLastChars(4);
        buf.append(']');
        return buf.toString();
    }

    @Override
    public String toTmYaml() {
        return toTmYaml(0);
    }

    @Override
    public String toTmYaml(int level) {
        String baseIndent = new FastStringBuffer().appendN("    ", level).toString();
        String indent = new FastStringBuffer().appendN("    ", level + 1).toString();

        FastStringBuffer buf = new FastStringBuffer();
        buf.append("{");

        buf.append("\n" + indent + "match: ");
        buf.appendObject(this.regexpStr);

        if (this.fCaptures != null && this.fCaptures.size() > 0) {
            buf.append("\n" + indent + "captures: ");
            buf.appendObject(this.createCapturesStr(level + 1));
        }

        if (TmMatchRule.isValidToken(this.fToken)) {
            buf.append("\n" + indent + "name: ");
            buf.appendObject(this.fToken.getData());
        }

        buf.append("\n" + baseIndent + "}");

        return buf.toString();
    }

    public static boolean isValidToken(IToken scope) {
        return scope != null && scope.getData() != null && !("".equals(scope.getData()));
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate((ScannerRange) scanner);
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        return evaluate((ScannerRange) scanner, resume);
    }

}
