/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.util.List;
import java.util.ListIterator;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * This is more of an experiment right now (not really working).
 */
public class LanguageLoaderUew {

    public final LiClipseLanguage setup = new LiClipseLanguage();
    private String file;
    private ListIterator<String> currentLineIterator;

    /** =================================================================
     * Start Helper for block comments
      ===================================================================*/
    private String blockCommentStartTemp;
    private String blockCommentEndTemp;
    private boolean throwErrors;

    private void checkBlockCommentFinished() {
        if (blockCommentStartTemp != null && blockCommentEndTemp != null) {
            setup.setupBlockComment(blockCommentStartTemp, blockCommentEndTemp, '\0');
        }
    }

    public void setupBlockCommentStart(String next) {
        blockCommentStartTemp = next;
        checkBlockCommentFinished();
    }

    public void setupBlockCommentEnd(String next) {
        blockCommentEndTemp = next;
        checkBlockCommentFinished();
    }

    /** =================================================================
     * End Helper for block comments
      ===================================================================*/

    public void load(String file, String contents) {
        this.file = file;
        contents = contents.trim();

        if (contents.length() == 0) {
            log("No lines found");
            return;
        }

        List<String> splitInLines = StringUtils.splitInLines(contents, false);
        if (splitInLines.size() == 0) {
            log("No lines found");
            return;
        }

        //Defines the language.
        //i.e.:
        // /L20"YAML" String Chars = " Line Comment = # Escape Char = \ File Extensions = YAML
        // /L20"Java 1.4" JAVA_LANG Line Comment = // Block Comment On = /* Block Comment Off = */ Escape Char = \ File Extensions = JAV JAVA
        // /L20"JavaScript" JSCRIPT_LANG Line Comment = // Block Comment On = /* Block Comment Off = */ File Extensions = JS
        // /L20"SAS 9.1.3" Nocase Line Comment = * Block Comment On = /* Block Comment Off = */ String Chars = "' DisableMLS File Extensions = SAS LOG
        // /L20"Scheme" Line Comment Num = 1; File Extensions = SCM
        // /L10"Python" PYTHON_LANG Line Comment = # Escape Char = \ String Literal Prefix = r File Extensions = PY PYW
        // /L20"Makefiles" Line Comment = # String Chars = "' File Extensions = MAK MK
        // /L1"C/C++/Objective C" C_LANG Line Comment = // Block Comment On = /* Block Comment Off = */ Escape Char = \ String Chars = "' File Extensions = C CPP CC CXX H HPP AWK M
        // /L20"ASPX" Nocase ASP_LANG Line Comment = ' String Chars = " Block Comment On = <!-- Block Comment Off = --> Block Comment On Alt = <% Block Comment Off Alt = %>  File Extensions = ASPX

        String line0 = StringUtils.removeBom(splitInLines.remove(0).trim());
        while (!line0.startsWith("/L")) {
            try {
                line0 = StringUtils.removeBom(splitInLines.remove(0).trim());
            } catch (Exception e) {
                log("Found no line starting with /L");
                return;
            }
        }

        readLanguageSetup(file, line0);

    }

    private void readLanguageSetup(String file, String line0) {
        List<String> parts = StringUtils.split(line0, ' ');
        ListIterator<String> currentLineIterator = parts.listIterator();
        this.currentLineIterator = currentLineIterator;
        if (!currentLineIterator.hasNext()) {
            log("Line starting with /L empty.");
            return;
        }
        String next = currentLineIterator.next();
        while (!next.endsWith("\"") && currentLineIterator.hasNext()) {
            next = currentLineIterator.next();
        }

        while (currentLineIterator.hasNext()) {
            if (checkNext("Line")) {
                if (expectOccurrence("Comment")) {
                    ignoreNextNoCase("Alt");
                    if (checkNext("Valid")) {
                        if (expectOccurrence("Columns", "=")) {
                            //I.e.: comment would only be valid at the columns presented (say, 1, 2)
                            //ignoring this for now.
                            skipNext();
                        }
                    } else if (checkNext("Num")) {
                        if (expectOccurrence("=")) {
                            //Get the number
                            String lineComment = currentLineIterator.next();
                            int number = Integer.parseInt(lineComment.charAt(0) + "");

                            FastStringBuffer generatedLineComment = new FastStringBuffer();
                            //remove the number
                            generatedLineComment.append(lineComment.substring(1));

                            //Fill the remainder with spaces
                            while (generatedLineComment.length() < number) {
                                generatedLineComment.append(' ');
                            }
                            lineComment = generatedLineComment.toString();
                            setup.setupLineComment(lineComment);
                        }

                    } else if (checkNext("Preceding")) {
                        if (expectOccurrence("Chars", "=")) {
                            String chars = currentLineIterator.next();
                            for (char c : chars.toCharArray()) {
                                setup.setupLineComment(c + "");
                            }
                        }
                    } else if (expectOccurrence("=")) {
                        setup.setupLineComment(currentLineIterator.next());
                    }
                }
            }

            else if (checkNext("File")) {
                if (checkNext("Names")) {
                    if (expectOccurrence("=")) {
                        while (currentLineIterator.hasNext()) {
                            setup.addFileName(currentLineIterator.next());
                        }
                    }
                } else if (expectOccurrence("Extensions")) {
                    if (expectOccurrence("=")) {
                        while (currentLineIterator.hasNext()) {
                            setup.addFileExtension(currentLineIterator.next());
                        }
                    }
                }
            }

            else if (checkNext("Escape")) {
                if (expectOccurrence("Char")) {
                    if (expectOccurrence("=")) {
                        skipNext(); //Skip scape char.
                        //setup.setupEscapeChar(currentLineIterator.next());
                    }
                }
            }

            else if (checkNext("String")) {
                if (checkNext("Literal", "Prefix", "=")) {
                    skipNext(); //I.e.: @"string" in csharp or r"string" in python

                } else if (checkNext("Char", "=") || expectOccurrence("Chars", "=")) {
                    String chars = currentLineIterator.next();
                    char[] charArray = chars.toCharArray();
                    for (char c : charArray) {
                        setup.setupSingleLineStringChar(String.valueOf(c));
                    }
                }
            }

            else if (checkNext("Block")) {
                if (expectOccurrence("Comment")) {
                    if (checkNext("On", "=") || checkNext("On", "Alt", "=")) {
                        setupBlockCommentStart(currentLineIterator.next());

                    } else if (checkNext("Off", "Alt", "=") || expectOccurrence("Off", "=")) {
                        setupBlockCommentEnd(currentLineIterator.next());
                    }
                }
            }

            else {
                String ignoredWord = currentLineIterator.next();
                System.out.println("Ignored: " + ignoredWord + " word:"
                        + currentLineIterator.previousIndex() + " at " + file);
            }
        }
    }

    private void skipNext() {
        if (!currentLineIterator.hasNext()) {
            return;
        }
        currentLineIterator.next();
    }

    private void ignoreNextNoCase(String string) {
        if (!currentLineIterator.hasNext()) {
            return;
        }
        String next = currentLineIterator.next();
        if (!next.equalsIgnoreCase(string)) {
            currentLineIterator.previous(); // backup if it didn't match
        }
    }

    private boolean expectOccurrence(String... strings) {
        boolean b = checkNext(strings);
        if (!b) {
            log("Expected to find '" + strings[0] + "' found '" + currentLineIterator.next() + "' instead.");
            currentLineIterator.previous();
        }
        return b;
    }

    private boolean expectOccurrence(String string) {
        boolean b = checkNext(string);
        if (!b) {
            log("Expected to find '" + string + "' found '" + currentLineIterator.next() + "' instead.");
            currentLineIterator.previous();
        }
        return b;
    }

    private boolean checkNext(String... strings) {
        int walked = 0;
        for (int i = 0; i < strings.length; i++) {
            if (checkNext(strings[i])) {
                walked += 1;
                if (i == strings.length - 1) {
                    return true;
                }
            } else {
                if (i < strings.length - 1) {
                    if (checkNext(strings[i] + strings[i + 1])) {
                        walked += 1;
                        //Work around bug in specification, such as Chars= (without the space).
                        i++;
                        if (i == strings.length - 1) {
                            return true;
                        }
                    }
                }
                for (int j = 0; j < walked; j++) {
                    currentLineIterator.previous(); //backout what we matched.
                }
                return false;
            }
        }
        throw new AssertionFailedException("Should not get here!");
    }

    private boolean checkNext(String string) {
        if (!currentLineIterator.hasNext()) {
            return false;
        }
        String next = currentLineIterator.next();
        if (next.equalsIgnoreCase(string)) {
            return true;
        }
        currentLineIterator.previous(); // backup
        return false;
    }

    private void log(String string) {
        String message = string + " (" + file + ")";
        Log.log(message);
        if (this.throwErrors) {
            throw new RuntimeException(message);
        }
    }

    public void setThrowErrors(boolean b) {
        this.throwErrors = b;
    }
}
