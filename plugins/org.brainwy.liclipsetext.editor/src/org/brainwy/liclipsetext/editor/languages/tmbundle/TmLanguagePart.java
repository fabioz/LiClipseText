/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.brainwy.liclipsetext.shared_core.log.Log;

public class TmLanguagePart {

    /**
     * Creates a part of a TmLanguage.
     * @throws IOException
     */
    public static ITmLanguagePart create(File file, ZipFile zipFile, ZipEntry element) {
        try (InputStream inputStream = zipFile.getInputStream(element)) {
            return create(inputStream);

        } catch (Exception e) {
            Log.log("Error parsing: " + element.getName() + " at file: " + file, e);
        }

        return null;
    }

    public static ITmLanguagePart create(InputStream inputStream) throws Exception {
        TmLanguageHandler handler = new TmLanguageHandler();
        handler.parse(inputStream);
        //Now, let's discover what we actually have (i.e.: grammar, preference, etc.)

        Object scopeName = handler.getValue("scopeName");
        Object name = handler.getValue("name");
        Object patterns = handler.getValue("patterns");
        if (scopeName instanceof String && name instanceof String && patterns instanceof List) {
            //Ok, we have a grammar.
            return new TmGrammarPart(handler);
        }

        Object content = handler.getValue("content");
        Object tabTrigger = handler.getValue("tabTrigger");
        Object scope = handler.getValue("scope");
        if (content instanceof String && tabTrigger instanceof String && scope instanceof String
                && name instanceof String) {
            return new TmSnippetPart((String) name, (String) content, (String) scope, (String) tabTrigger);
        }

        Object settings = handler.getValue("settings");
        if (settings instanceof Map) {
            Map map = (Map) settings;

            Object indentPattern = map.get("increaseIndentPattern");
            Object dedentPattern = map.get("decreaseIndentPattern");
            if (scope instanceof String && indentPattern instanceof String && dedentPattern instanceof String) {
                return new TmIndentPart((String) scope, (String) indentPattern, (String) dedentPattern);
            }

            Object shellVariables = map.get("shellVariables");
            if (shellVariables != null) {

                if (shellVariables instanceof List) {
                    List list = (List) shellVariables;
                    Map shellVariablesMap = new HashMap<>();

                    for (Object object : list) {
                        if (object instanceof Map) {
                            Map map2 = (Map) object;
                            Object nameInMap = map2.get("name");
                            Object valueInMap = map2.get("value");
                            if (nameInMap != null && valueInMap != null) {
                                shellVariablesMap.put(nameInMap, valueInMap);
                            }
                        }
                    }

                    Object commentStart = shellVariablesMap.get("TM_COMMENT_START");
                    Object commentEnd = shellVariablesMap.get("TM_COMMENT_END");
                    if (scope instanceof String && commentStart instanceof String
                            && (commentEnd instanceof String || commentEnd == null)) {
                        return new TmCommentPart((String) scope, (String) commentStart, (String) commentEnd, 0);
                    }

                    commentStart = shellVariablesMap.get("TM_COMMENT_START_2");
                    commentEnd = shellVariablesMap.get("TM_COMMENT_END_2");
                    if (scope instanceof String && commentStart instanceof String
                            && (commentEnd instanceof String || commentEnd == null)) {
                        return new TmCommentPart((String) scope, (String) commentStart, (String) commentEnd, 1);
                    }
                }
            }

        }

        return null;
    }

    public static ITmLanguagePart create(java.nio.file.Path path) {
        try (SeekableByteChannel sbc = Files.newByteChannel(path); InputStream in = Channels.newInputStream(sbc)) {
            return create(in);
        } catch (Exception e) {
            Log.log("Error parsing: " + path, e);
        }
        return null;
    }
}
