/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseContentTypeDefinitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.ParseHtmlTagHelper.HtmlAttribute;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.editor.rules.SubLanguageToken;
import org.brainwy.liclipsetext.editor.rules.SwitchLanguageToken;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IDocumentScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.partitioner.IMarkScanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SwitchLanguageHtmlRule implements ILiClipsePredicateRule, IChangeTokenRule, ISwitchLanguageRule {

    private final Map<String, String> typeAttr;
    private final Map<String, String> languageAttr;
    private IToken fToken;

    private final Map<String, LiClipseContentTypeDefinitionScanner> fScanner = new HashMap<String, LiClipseContentTypeDefinitionScanner>();
    private ParseHtmlTagHelper parseHtmlTagHelper;
    private char[] tag;

    public SwitchLanguageHtmlRule(Map<String, String> typeAttr, Map<String, String> languageAttr, String tag,
            IToken token) {
        this.tag = tag.toLowerCase().toCharArray();
        this.typeAttr = new HashMap<String, String>();
        Set<Entry<String, String>> entrySet = typeAttr.entrySet();
        for (Entry<String, String> entry : entrySet) {
            this.typeAttr.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
        }
        this.languageAttr = new HashMap<String, String>();
        entrySet = languageAttr.entrySet();
        for (Entry<String, String> entry : entrySet) {
            this.languageAttr.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
        }
        setToken(token);
    }

    @Override
    public void setToken(IToken token) {
        this.fToken = token;
    }

    private LiClipseContentTypeDefinitionScanner getScanner(String switchToLanguageName) {
        if (switchToLanguageName == null) {
            return null;
        }
        if (fScanner.containsKey(switchToLanguageName)) { //check contains because value could be null!
            return fScanner.get(switchToLanguageName);
        }

        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        LiClipseLanguage language = languagesManager.getLanguageFromName(switchToLanguageName);
        LiClipseContentTypeDefinitionScanner scanner;
        if (language != null) {
            scanner = new LiClipseContentTypeDefinitionScanner(language);
        } else {
            scanner = null;
        }
        this.fScanner.put(switchToLanguageName, scanner);
        return scanner;

    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) throws DocumentTimeStampChangedException {
        return evaluate(scanner, false);
    }

    @Override
    public IToken getSuccessToken() {
        return this.fToken;
    }

    private static class CharWrapper {
        int c;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) throws DocumentTimeStampChangedException {
        if (resume) {
            //Non-resumable rule!
            return Token.UNDEFINED;
        }

        int initialStartScript;
        int initialEndScript;
        CharWrapper c = new CharWrapper();
        int finalStartScript;
        int finalEndScript;

        initialStartScript = -1;
        initialEndScript = -1;
        finalStartScript = -1;
        finalEndScript = -1;

        parseHtmlTagHelper = null;

        IMarkScanner markScanner = (IMarkScanner) scanner;
        final int mark = markScanner.getMark();
        c.c = scanner.read();

        if (c.c == '<') {
            ScannerRange tokenScanner = (ScannerRange) scanner;

            c.c = Character.toLowerCase(scanner.read());

            while (Character.isWhitespace(c.c)) {
                c.c = Character.toLowerCase(scanner.read());
            }
            String switchToLanguage = null;
            if (c.c == tag[0]) {
                if (!match(scanner, tag, 1)) {
                    return unreadAndReturnUndefined(markScanner, mark);
                }

                //Ok, matched <script
                c.c = scanner.read();
                if (!Character.isWhitespace(c.c)) { //we need a whitespace (a > won't do as it won't match any script)
                    return unreadAndReturnUndefined(markScanner, mark);
                }
                while (c.c != '>' && c.c != ICharacterScanner.EOF) {
                    c.c = scanner.read();
                }
                if (c.c == ICharacterScanner.EOF) {
                    return unreadAndReturnUndefined(markScanner, mark);
                }
                //Ok, we read: <script type="???" language="???' ...>
                initialStartScript = tokenScanner.getTokenOffset();
                initialEndScript = initialStartScript + tokenScanner.getTokenLength();

                IDocumentScanner docScanner = (IDocumentScanner) scanner;
                IDocument document = docScanner.getDocument();
                try {
                    String startTag = document.get(initialStartScript, initialEndScript - initialStartScript);
                    parseHtmlTagHelper = new ParseHtmlTagHelper(startTag);
                    List<HtmlAttribute> attributes = parseHtmlTagHelper.attributes;
                    OUT: for (HtmlAttribute htmlAttribute : attributes) {
                        if ("type".equalsIgnoreCase(htmlAttribute.attribute)) {
                            String val = htmlAttribute.string.toLowerCase();
                            switchToLanguage = this.typeAttr.get(val);
                            if (switchToLanguage != null) {
                                break;
                            }
                        }
                        if ("language".equalsIgnoreCase(htmlAttribute.attribute)) {
                            String val = htmlAttribute.string.toLowerCase();
                            for (Entry<String, String> s : this.languageAttr.entrySet()) {
                                if (val.startsWith(s.getKey())) {
                                    switchToLanguage = s.getValue();
                                    break OUT;
                                }
                            }
                        }
                    }
                    if (switchToLanguage == null) {
                        this.setToken(new Token("this"));
                    } else {
                        this.setToken(new Token(switchToLanguage));
                    }
                } catch (Exception e) {
                    Log.log(e);
                    //Something went wrong
                    return unreadAndReturnUndefined(markScanner, mark);
                }

                //Now, search for </???script???>
                while (true) {
                    //find <
                    while (c.c != '<') {
                        c.c = scanner.read();
                        if (c.c == ICharacterScanner.EOF) {
                            //did not find </script>, but found start, so, consider everything
                            //as the given script.
                            return createSwitchLanguageToken(scanner, parseHtmlTagHelper, null, switchToLanguage,
                                    initialStartScript, initialEndScript, finalStartScript, finalEndScript);
                        }
                    }

                    finalStartScript = tokenScanner.getTokenOffset() + tokenScanner.getTokenLength() - 1;
                    //find </
                    c.c = scanner.read();
                    if (c.c != '/') {
                        if (c.c == ICharacterScanner.EOF) {
                            return createSwitchLanguageToken(scanner, parseHtmlTagHelper, null, switchToLanguage,
                                    initialStartScript, initialEndScript, finalStartScript, finalEndScript);
                        }
                        continue;
                    }

                    IToken token = skipWhitespaces(scanner, switchToLanguage, initialStartScript, initialEndScript,
                            finalStartScript, finalEndScript, c);
                    if (token != null) {
                        return token;
                    }
                    c.c = Character.toLowerCase(c.c);
                    if (c.c == tag[0]) {

                        if (!match(scanner, tag, 1)) {
                            continue;
                        }

                        token = skipWhitespaces(scanner, switchToLanguage, initialStartScript, initialEndScript,
                                finalStartScript, finalEndScript, c);
                        if (token != null) {
                            return token;
                        }

                        if (c.c == '>') {
                            finalEndScript = tokenScanner.getTokenOffset() + tokenScanner.getTokenLength();
                            ParseHtmlTagHelper closeHtmlTagHelper = null;
                            try {
                                String startTag = document.get(finalStartScript, finalEndScript - finalStartScript);
                                closeHtmlTagHelper = new ParseHtmlTagHelper(startTag);
                            } catch (Exception e) {
                                Log.log(e);
                                //Something went wrong
                                return unreadAndReturnUndefined(markScanner, mark);
                            }

                            return createSwitchLanguageToken(scanner, parseHtmlTagHelper, closeHtmlTagHelper,
                                    switchToLanguage, initialStartScript, initialEndScript, finalStartScript,
                                    finalEndScript);
                        }
                    }

                }
            }
        }
        return unreadAndReturnUndefined(markScanner, mark);
    }

    public IToken skipWhitespaces(ICharacterScanner scanner, String switchToLanguage, final int initialStartScript,
            final int initialEndScript, final int finalStartScript, final int finalEndScript, CharWrapper c)
            throws DocumentTimeStampChangedException {
        c.c = scanner.read();
        if (c.c == ICharacterScanner.EOF) {
            return createSwitchLanguageToken(scanner, parseHtmlTagHelper, null, switchToLanguage, initialStartScript,
                    initialEndScript, finalStartScript, finalEndScript);
        }

        //skip whitespaces
        while (Character.isWhitespace(c.c)) {
            c.c = scanner.read();
            if (c.c == ICharacterScanner.EOF) {
                return createSwitchLanguageToken(scanner, parseHtmlTagHelper, null, switchToLanguage,
                        initialStartScript, initialEndScript, finalStartScript, finalEndScript);
            }
        }
        return null;
    }

    private IToken createSwitchLanguageToken(ICharacterScanner scanner, ParseHtmlTagHelper parseHtmlTagHelper,
            ParseHtmlTagHelper closeHtmlTagHelper, String switchToLanguage, final int initialStartScript,
            final int initialEndScript, final int finalStartScript, final int finalEndScript)
            throws DocumentTimeStampChangedException {
        //if it got here, it matched it.
        IDocumentScanner docScanner = (IDocumentScanner) scanner;
        ScannerRange tokenScanner = (ScannerRange) scanner;
        IDocument document = docScanner.getDocument();
        LiClipseContentTypeDefinitionScanner otherLanguageScanner = getScanner(switchToLanguage);
        int len;
        if (finalStartScript > 0) {
            len = finalStartScript - initialEndScript;
        } else {
            len = tokenScanner.getTokenOffset() + tokenScanner.getTokenLength() - initialEndScript;
        }
        ScannerRange range = null;
        if (otherLanguageScanner != null) {
            range = otherLanguageScanner.createScannerRange(document, initialEndScript, len);
        }

        List<SubLanguageToken> subTokens = new ArrayList<SubLanguageToken>();
        String base = switchToLanguage;

        //The <script a="", b=""> part
        if (parseHtmlTagHelper != null) {
            subTokens.addAll(parseHtmlTagHelper.generateTokens("this", initialStartScript, initialEndScript));
        } else {
            subTokens.add(new SubLanguageToken("this", IDocument.DEFAULT_CONTENT_TYPE, initialStartScript,
                    initialEndScript - initialStartScript));
        }

        if (len > 0) {
            if (otherLanguageScanner != null && range != null) {
                IToken tok = range.nextToken(otherLanguageScanner);
                while (!tok.isEOF()) {
                    subTokens.add(new SubLanguageToken(base, (String) range.getToken().getData(),
                            range.getTokenOffset(),
                            range.getTokenLength()));
                    tok = range.nextToken(otherLanguageScanner);
                }
            } else {
                subTokens.add(new SubLanguageToken("this", IDocument.DEFAULT_CONTENT_TYPE, initialEndScript, len));
            }
        }
        if (finalStartScript > 0 && finalEndScript > 0) {
            //The </script> part
            if (closeHtmlTagHelper != null) {
                subTokens.addAll(closeHtmlTagHelper.generateTokens("this", finalStartScript, finalEndScript));

            } else {
                subTokens.add(new SubLanguageToken("this", IDocument.DEFAULT_CONTENT_TYPE, finalStartScript,
                        finalEndScript
                                - finalStartScript));
            }
        }
        return new SwitchLanguageToken(fToken.getData(), subTokens);
    }

    private IToken unreadAndReturnUndefined(IMarkScanner scanner, int mark) {
        scanner.setMark(mark);
        return Token.UNDEFINED;
    }

    private boolean match(ICharacterScanner scanner, char[] cs, int i) {
        while (true) {
            int c = Character.toLowerCase(scanner.read());
            if (c == cs[i]) {
                i++;
                if (i == cs.length) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public List<LiClipseLanguage> getLanguages() {
        Set<String> languageNames = new HashSet<String>();
        languageNames.addAll(this.languageAttr.values());
        languageNames.addAll(this.typeAttr.values());
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        ArrayList<LiClipseLanguage> lst = new ArrayList<LiClipseLanguage>(languageNames.size());
        if (languagesManager == null) {
            return lst;
        }
        for (String l : languageNames) {
            LiClipseLanguage language = languagesManager.getLanguageFromName(l);
            if (language == null) {
                Log.log("Error: Unable to find language: " + l);
                continue;
            }
            lst.add(language);
        }
        return lst;
    }

}
