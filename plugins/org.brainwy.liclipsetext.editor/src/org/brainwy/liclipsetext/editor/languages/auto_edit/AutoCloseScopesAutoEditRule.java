/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.auto_edit;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseTextAttribute;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.CompositeRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.IEmptyMatchRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.MultiLineRuleWithSkip;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.SingleLineRule;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguageIO;
import org.brainwy.liclipsetext.shared_core.auto_edit.AutoEditStrategyHelper;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallback;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_core.utils.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;

public class AutoCloseScopesAutoEditRule extends AbstractScopedAutoEditRule {

    public static final String AUTO_CLOSE_SCOPES = "auto_close_scopes";
    private final Set<String> autoCloseScopes;
    private WeakReference<LiClipseLanguage> liClipseLanguage;
    private Map<Character, String> autoCloseCharToContentType = null;
    private Map<Character, String> autoSkipCharToContentType = null;
    private Set<Tuple<String, String>> multiLineStartEnd = null;

    @SuppressWarnings("rawtypes")
    public AutoCloseScopesAutoEditRule(Map<String, Object> map, List<IStatus> errorList,
            WeakReference<LiClipseLanguage> liClipseLanguage) {
        super(map);
        List list = (List) LiClipseLanguageIO.getRequired(map, AUTO_CLOSE_SCOPES, List.class);
        autoCloseScopes = new HashSet<String>(
                LiClipseLanguageIO.checkListOfStrings(AUTO_CLOSE_SCOPES, list, errorList));
        this.liClipseLanguage = liClipseLanguage;
    }

    public boolean customizeDocumentCommand(IDocument document, DocumentCommand command, AutoEditStrategyHelper helper,
            String indentString, String contentType) {

        if (autoCloseCharToContentType == null) {
            createInternalInfo();
        }
        // Check for multiple chars first...
        Set<Tuple<String, String>> startEnd = this.multiLineStartEnd;
        for (Tuple<String, String> tuple : startEnd) {
            int len = tuple.o1.length();
            if (len > 1) {
                if (tuple.o1.charAt(len - 1) == helper.c) {
                    //Handle case with 2 chars too...
                    if (len == 2 && tuple.o2.length() == len) {
                        try {
                            if (tuple.o1.charAt(len - 2) == document.getChar(command.offset - 1)) {
                                TextSelectionUtils ps = new TextSelectionUtils(document,
                                        new TextSelection(document, command.offset,
                                                command.length));

                                char nextChar = '\0';
                                try {
                                    nextChar = ps.getCharAtCurrentOffset();
                                } catch (BadLocationException e) {
                                }
                                if (nextChar != '\0' && Character.isJavaIdentifierPart(nextChar)) {
                                    //we're just before a word (don't try to do anything in this case)
                                    //e.g. |var (| is cursor position)
                                } else {
                                    if (nextChar == tuple.o2.charAt(1)) {
                                        command.text = Character.toString(helper.c) + tuple.o2.charAt(0);

                                    } else {
                                        command.text = Character.toString(helper.c) + tuple.o2;

                                    }

                                    command.shiftsCaret = false;
                                    command.caretOffset = command.offset + 1;
                                }

                            }
                        } catch (BadLocationException e) {
                            //ignore
                        }
                    }
                }
            }
        }

        // Check for single chars closing
        String registered = this.autoCloseCharToContentType.get(helper.c);
        if (contentType.equals(registered)) {
            helper.handleAutoClose(document, command, helper.c, helper.c);
            return true;
        }
        registered = this.autoSkipCharToContentType.get(helper.c);
        if (contentType.equals(registered)) {
            helper.handleAutoSkip(document, command, helper.c);
            return true;
        }
        return false;
    }

    private void createInternalInfo() {
        autoCloseCharToContentType = new HashMap<Character, String>();
        autoSkipCharToContentType = new HashMap<Character, String>();
        multiLineStartEnd = new HashSet<Tuple<String, String>>();

        LiClipseLanguage language = liClipseLanguage.get();
        if (language != null) {
            List<ILiClipsePredicateRule> rules = language.rules;
            for (ILiClipsePredicateRule ILiClipsePredicateRule : rules) {
                IToken successToken = ILiClipsePredicateRule.getSuccessToken();
                if (successToken == null || successToken.getData() == null) {
                    continue;
                }
                String contentTypeFromToken = LiClipseTextAttribute.getContentTypeFromToken(successToken);
                if (this.autoCloseScopes.contains(contentTypeFromToken)) {

                    if (ILiClipsePredicateRule instanceof CompositeRule) {
                        //If we're in a composite rule with only a single non-empty rule, consider it.
                        CompositeRule compositeRule = (CompositeRule) ILiClipsePredicateRule;
                        ILiClipsePredicateRule[] subRules = compositeRule.getSubRules();
                        List<ILiClipsePredicateRule> filtered = ArrayUtils.filter(subRules,
                                new ICallback<Boolean, ILiClipsePredicateRule>() {

                                    @Override
                                    public Boolean call(ILiClipsePredicateRule arg) {
                                        if (!(arg instanceof IEmptyMatchRule)) {
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                        if (filtered.size() == 1) {
                            ILiClipsePredicateRule = filtered.get(0);
                        }
                    }

                    if (ILiClipsePredicateRule instanceof MultiLineRuleWithSkip) { // MultiLineRule is a subclass
                        MultiLineRuleWithSkip multiLineRule = (MultiLineRuleWithSkip) ILiClipsePredicateRule;
                        Tuple<String, String> startEndSequence = multiLineRule.getStartEndSequence();
                        multiLineStartEnd.add(startEndSequence);

                    } else if (ILiClipsePredicateRule instanceof SingleLineRule) {
                        SingleLineRule singleLineRule = (SingleLineRule) ILiClipsePredicateRule;
                        char[] sequence = singleLineRule.getSequence();
                        if (sequence.length == 1) {
                            addToAutoClose(sequence[0], this.scope);
                            addToAutoSkip(sequence[0], contentTypeFromToken);
                        }
                    } else {
                        Log.log("Unable to handle auto close scope (" + this.scope
                                + ") because rule cannot be handled: " + ILiClipsePredicateRule);
                    }
                }
            }
        }
    }

    private void addToAutoSkip(char c, String contentTypeFromToken) {
        autoSkipCharToContentType.put(c, contentTypeFromToken);

    }

    private void addToAutoClose(char c, String contentType) {
        autoCloseCharToContentType.put(c, contentType);
    }

    public void fillWithCharsThatCreateScope(Set<Character> ret) {
        if (autoCloseCharToContentType == null) {
            createInternalInfo();
        }
        ret.addAll(autoCloseCharToContentType.keySet());
        Iterator<Tuple<String, String>> iterator = multiLineStartEnd.iterator();
        while (iterator.hasNext()) {
            try {
                String c = iterator.next().o1;
                if (c.length() == 1) {
                    ret.add(c.charAt(0));
                }
            } catch (Exception e) {
                Log.log(e);
            }
        }
    }

    public Set<Tuple<String, String>> getMultiLineStartEndSequences() {
        if (autoCloseCharToContentType == null) {
            createInternalInfo();
        }
        return multiLineStartEnd;
    }
}
