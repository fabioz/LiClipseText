/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.RulesFactory;
import org.brainwy.liclipsetext.editor.common.partitioning.tokens.TokenFactory;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadata.LanguageType;
import org.brainwy.liclipsetext.editor.languages.comment.LanguageComment;
import org.brainwy.liclipsetext.editor.languages.comment.LanguageComment.CommentType;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmCommentPart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmLanguageHandler;
import org.brainwy.liclipsetext.editor.rules.SwitchLanguageToken;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.DummyToken;
import org.brainwy.liclipsetext.shared_core.partitioner.IChangeTokenRule;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.OrderedMap;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LiClipseLanguageIO {

    private final LiClipseLanguage liClipsePartitioningSetup;

    LiClipseLanguageIO(LiClipseLanguage liClipsePartitioningSetup) {
        this.liClipsePartitioningSetup = liClipsePartitioningSetup;
    }

    public static void convertMapDefaultsToIo(Map map) {
        Object existing = map.remove(IDocument.DEFAULT_CONTENT_TYPE);
        if (existing != null) {
            map.put("default", existing);
        }
    }

    public static void convertMapDefaultsFromIo(Map map) {
        Object existing = map.remove("default");
        if (existing != null) {
            map.put(IDocument.DEFAULT_CONTENT_TYPE, existing);
        }
    }

    public static void fixScopeKeyToIo(Map m) {
        if (IDocument.DEFAULT_CONTENT_TYPE.equals(m.get("scope"))) {
            m.put("scope", "default");
        }
    }

    public static void fixScopeKeyFromIo(Map m) {
        if ("default".equals(m.get("scope"))) {
            m.put("scope", IDocument.DEFAULT_CONTENT_TYPE);
        }
    }

    private void fixListScopeFromIo(List<Map<String, Object>> navigation) {
        for (Map map : navigation) {
            Object obj = map.get("scope");
            if (obj != null) {
                fixScope(map, obj, "scope");
            }

            obj = map.get("after_scope");
            if (obj != null) {
                fixScope(map, obj, "after_scope");
            }
        }

    }

    private void fixScope(Map map, String key) {
        Object object = map.get(key);
        if (object != null) {
            fixScope(map, object, key);
        }
    }

    private void fixScope(Map map, Object obj, String key) {
        if (obj instanceof List) {
            List scope = (List) obj;
            for (int i = 0; i < scope.size(); i++) {
                String element = (String) scope.get(i);
                if ("default".equals(element)) {
                    scope.set(i, IDocument.DEFAULT_CONTENT_TYPE);
                }
            }
        } else if (obj instanceof String) {
            if ("default".equals(obj)) {
                map.put(key, IDocument.DEFAULT_CONTENT_TYPE);
            }
        } else {
            throw new RuntimeException("Expected scope to be a String or List(String). Found: " + obj);
        }
    }

    public void dump(Writer output) {
        Yaml yaml = new Yaml();
        Map data = new OrderedMap();

        data.put(LiClipseLanguage.NAME, this.liClipsePartitioningSetup.name);
        //--- Dump CONTENT_TYPE_TO_COLOR
        Map<String, String> map = new OrderedMap();
        map.putAll(this.liClipsePartitioningSetup.contentTypeToColorTokenName);
        convertMapDefaultsToIo(map);

        data.put(LiClipseLanguage.CONTENT_TYPE_TO_COLOR, map);

        //--- Dump RULES
        data.put(LiClipseLanguage.RULES, this.liClipsePartitioningSetup.rulesFactory.getRulesDump());

        //--- Dump SCOPE
        Map<String, Map<String, Object>> scanner = new OrderedMap();
        for (Entry<String, ScopeColorScanning> entry : this.liClipsePartitioningSetup.scopeToScopeColorScanning
                .entrySet()) {
            String key = entry.getKey();
            ScopeColorScanning value = entry.getValue();
            scanner.put(key, value.getDump());
        }
        convertMapDefaultsToIo(scanner);
        data.put(LiClipseLanguage.SCOPE, scanner);

        data.put(LiClipseLanguage.FILE_EXTENSION, this.liClipsePartitioningSetup.fileExtensions.toArray());
        data.put(LiClipseLanguage.EDITOR_ID, this.liClipsePartitioningSetup.editorId);
        data.put(LiClipseLanguage.FILENAME, this.liClipsePartitioningSetup.filenames.toArray());
        data.put(LiClipseLanguage.TEMPLATES, this.liClipsePartitioningSetup.getTemplates().getTemplatesDump());

        //Note: not dumping navigation!
        yaml.dump(data, output);
    }

    /**
     * Fill the setup we have.
     */
    void loadInternal(ILanguageMetadataFileInfo file, InputStream contents, boolean rethrowErrors) throws Exception {
        LiClipseLanguage ret = this.liClipsePartitioningSetup;
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        BufferedInputStream buf;
        if (contents instanceof BufferedInputStream) {
            buf = (BufferedInputStream) contents;
        } else {
            buf = new BufferedInputStream(contents);
        }

        // Read a bit just to see what we're dealing with.
        buf.mark(10);
        int firstChar = buf.read();
        for (int i = 0; i < 5 && Character.isWhitespace(firstChar) && firstChar != -1; i++) {
            firstChar = buf.read();
        }
        buf.reset();

        Map<String, Object> data = new HashMap<>();

        // Clear existing rules before we start.
        ret.rules.clear();
        ret.ruleAliases.clear();
        ret.injectionRules.clear();
        ret.scopeToScopeColorScanning.clear();

        if (firstChar == '<') {
            //Dealing with textmate bundle
            loadTmLanguage(ret, buf, file.toString());
            data.put(LiClipseLanguage.NAME, ret.name);

            Object name = data.get(LiClipseLanguage.NAME);
            if (name != null && languagesManager != null) {
                applyLanguageExtensionsToStructure(data, languagesManager, name);
            }
        } else {
            data = createDataFromContents(languagesManager, buf);
            if (data.get(LiClipseLanguage.EXTEND) != null) {
                return; // if this is an extend, don't create a 'standalone' language for it (extend must be combined with the main to be loaded).
            }
        }

        ret.name = (String) getRequired(data, LiClipseLanguage.NAME, String.class);
        if (ret.caption == null) {
            ret.caption = ret.name;
        }

        boolean liclipseWithTmLanguageGrammar = false;
        if (file instanceof LanguageMetadataFileInfo) {
            File tmLanguageFile = getTmLanguageFileFromData(data, (LanguageMetadataFileInfo) file);
            if (tmLanguageFile != null) {
                ret.tmLanguageFile = tmLanguageFile;
                liclipseWithTmLanguageGrammar = true;
                try (FileInputStream stream = new FileInputStream(tmLanguageFile)) {
                    loadTmLanguage(ret, stream, tmLanguageFile.toString());
                } catch (Exception e) {
                    Log.log("Error parsing: " + tmLanguageFile, e);
                    return;
                }
            }
        }

        String editorId = (String) data.remove(LiClipseLanguage.EDITOR_ID);
        if (editorId != null) {
            ret.editorId = editorId;
        }

        data.remove(LiClipseLanguage.SHEBANG);
        ret.charSeparators = (String) data.remove(LiClipseLanguage.SEPARATORS);

        String caseChosen = ((String) getOptional(data, LiClipseLanguage.CASE, String.class, "sensitive"))
                .toLowerCase();
        if ("insensitive".equals(caseChosen)) {
            ret.caseInsensitive = true;
        } else if ("sensitive".equals(caseChosen)) {
            ret.caseInsensitive = false;
        } else {
            throw new AssertionFailedException(
                    "Could not recognize case: " + caseChosen + ". Valid: sensitive, insensitive.");
        }

        Map<String, Object> ruleAliases = null;
        if (!liclipseWithTmLanguageGrammar) {
            ruleAliases = loadRules(data, ret);
            fixTopLevelRulesToHaveValidTokens(ret);
        }

        //--- Load CONTENT_TYPE_TO_COLOR
        ret.contentTypeToColorTokenName.clear();
        Object required = getOptional(data, LiClipseLanguage.CONTENT_TYPE_TO_COLOR, Map.class, new HashMap());
        Map<String, String> temp = (Map) required;
        for (Entry<String, String> entry : temp.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new AssertionFailedException("Found non string key: " + entry.getKey());
            }
            if (!(entry.getValue() instanceof String)) {
                throw new AssertionFailedException("Found non string value for key: " + entry.getKey() + " value: "
                        + entry.getValue());
            }
        }
        convertMapDefaultsFromIo(temp);
        ret.contentTypeToColorTokenName.putAll(temp);

        //--- Load SCOPE (optional)
        if (!liclipseWithTmLanguageGrammar) {
            Map<String, Map<String, Object>> scannerWords = (Map) data.remove(LiClipseLanguage.SCOPE);
            if (scannerWords != null) {
                Set<Entry<String, Map<String, Object>>> entrySet = scannerWords.entrySet();
                for (Entry<String, Map<String, Object>> entry : entrySet) {
                    String key = entry.getKey();
                    ScopeColorScanning w = new ScopeColorScanning(ret.caseInsensitive, ret);
                    Map<String, Object> value;
                    try {
                        value = entry.getValue();
                    } catch (Exception e1) {
                        throw new RuntimeException("Unable to load scope to color for key: " + key, e1);
                    }

                    try {
                        w.restoreDump(value, ruleAliases);
                    } catch (Exception e) {
                        if (rethrowErrors) {
                            throw e;
                        }
                        Log.log(e); //Only this part will be invalid.
                    }
                    ret.scopeToScopeColorScanning.put(key, w);
                    convertMapDefaultsFromIo(ret.scopeToScopeColorScanning);
                }
            }
        }

        List list = (List) data.remove(LiClipseLanguage.FILE_EXTENSION);
        if (list != null) {
            ret.fileExtensions.addAll(list);
        }

        list = (List) data.remove(LiClipseLanguage.FILENAME);
        if (list != null) {
            ret.filenames.addAll(list);
        }

        List<IStatus> errorList = new ArrayList<IStatus>();

        Map indent = (Map) data.remove(LiClipseLanguage.INDENT);
        if (indent != null) {
            ret.getIndent().load(indent, errorList);
        }

        Map comment = (Map) data.remove(LiClipseLanguage.COMMENT);
        if (comment != null) {
            fixScope(comment, "scope");
            ret.getComment().load(comment, errorList);
        } else {
            if (languagesManager != null) {
                List<TmCommentPart> tmCommentParts = languagesManager.scopeToTmComment.get(ret.name);
                if (tmCommentParts != null) {
                    TmCommentPart usePart = null;
                    if (tmCommentParts.size() > 0) {
                        LanguageComment languageComment = ret.getComment();
                        for (TmCommentPart tmCommentPart : tmCommentParts) {
                            if (tmCommentPart.commentStart != null && tmCommentPart.commentEnd == null) {
                                //prefer single line
                                usePart = tmCommentPart;
                                languageComment.commentType = CommentType.COMMENT_TYPE_SINGLE_LINE;
                                languageComment.commentString = usePart.commentStart.trim();
                                break;
                            }
                        }
                        if (usePart == null) {
                            usePart = tmCommentParts.get(0);
                            languageComment.commentType = CommentType.COMMENT_TYPE_MULTI_LINE;
                            languageComment.commentStart = usePart.commentStart.trim();
                            languageComment.commentEnd = usePart.commentEnd.trim();
                        }
                    }
                }
            }
        }

        List autoEdit = (List) data.remove(LiClipseLanguage.AUTO_EDIT);
        if (autoEdit != null) {
            fixListScopeFromIo(autoEdit);
            ret.getAutoEdit().load(autoEdit, errorList);
        }
        ret.getAutoEdit().loadGlobals();

        //setup navigation (ctrl+shift+up/down)
        List<Map<String, Object>> navigation = (List) data.remove(LiClipseLanguage.OUTLINE);
        if (navigation != null) {
            fixListScopeFromIo(navigation);
            ret.getNavigation().load(navigation, errorList);
        }

        //setup launching
        Map<String, Object> launch = (Map) data.remove(LiClipseLanguage.LAUNCH);
        if (navigation != null) {
            ret.getLaunch().load(launch, errorList);
        }

        Map<String, Object> codeCompletionPrefs = (Map) data.remove(LiClipseLanguage.CODE_COMPLETION);
        if (codeCompletionPrefs != null) {
            Boolean useOnlyTemplatesOnCodeCompletion = (Boolean) codeCompletionPrefs.remove("use_only_templates");
            if (useOnlyTemplatesOnCodeCompletion != null) {
                ret.useOnlyTemplatesOnCodeCompletion = useOnlyTemplatesOnCodeCompletion;
            }
        }

        List<Map<String, Object>> templates = (List) data.remove(LiClipseLanguage.TEMPLATES);
        Map<String, Object> templateVariables = (Map) data.remove(LiClipseLanguage.TEMPLATE_VARIABLES);
        if (templates != null) {
            ret.getTemplates().load(templates, templateVariables, errorList);
        }

        Map spellCheckMap = (Map) data.remove(LiClipseLanguage.SPELL_CHECK);
        if (spellCheckMap != null) {
            fixScope(spellCheckMap, "scope");
            //Note: if not specified, the default will be any partition which maps to the string color.
            List<String> spellCheckScopes = (List<String>) spellCheckMap.get("scope");
            if (spellCheckScopes != null) {
                ret.setSpellCheckScopes(spellCheckScopes);
            }
        }

        if (!data.isEmpty()) {
            String message = "Unexpected top-level items in language: " + StringUtils.join(",", data.keySet());
            LiClipseTextEditorPlugin.createWarning(message, errorList);
        }

        if (errorList.size() > 0) {
            if (rethrowErrors) {
                throw new RuntimeException(StringUtils.join(", ", errorList));
            } else {
                for (IStatus s : errorList) {
                    Log.log(s.toString());
                }
            }
        }
    }

    public static File getTmLanguageFileFromData(Map<String, Object> data, LanguageMetadataFileInfo file) {
        Object tmLanguage = data.remove(LiClipseLanguage.TM_LANGUAGE);
        boolean hasTmLanguageRef = tmLanguage != null;
        if (hasTmLanguageRef) {
            if (file instanceof LanguageMetadataFileInfo) {
                File tmLanguageFile;
                try {
                    tmLanguageFile = new File(file.getFile().getParentFile(),
                            tmLanguage.toString());
                } catch (Exception e1) {
                    Log.log("Error resolving " + LiClipseLanguage.TM_LANGUAGE + ": " + tmLanguage);
                    return null;
                }
                if (!tmLanguageFile.exists()) {
                    Log.log("Expected " + tmLanguageFile + " to exist!");
                    return null;
                }
                return tmLanguageFile;
            } else {
                Log.log("Can only load tm_language from .liclipse files on file-based abstractions (as the tm_language is resolved relative to it).");
                return null;
            }
        }
        return null;

    }

    public void fixTopLevelRulesToHaveValidTokens(LiClipseLanguage ret) {
        int nextId = 1;
        // Ok, at this point rules should be loaded... Let's see if all the top-level rules have a valid token
        // (and a different id).
        Set<String> found = new HashSet<>();

        nextId = fixTopLevelRulesToHaveValidTokens(ret, ret.rules, found, nextId);
        List<ScopeSelector> injectionRules = ret.injectionRules;
        for (ScopeSelector scopeSelector : injectionRules) {
            nextId = fixTopLevelRulesToHaveValidTokens(ret, scopeSelector.getRules(), found, nextId);
        }
    }

    private int fixTopLevelRulesToHaveValidTokens(LiClipseLanguage ret, List<ILiClipsePredicateRule> rules, Set<String> found,
            int nextId) {
        for (ILiClipsePredicateRule rule : rules) {
            IToken successToken = rule.getSuccessToken();
            if (successToken == null || successToken.getData() == null) {
                successToken = new DummyToken(ret.name + "." + nextId++);
                ((IChangeTokenRule) rule).setToken(successToken);
                //System.out.println("Null token on top-level rule: " + rule);
            }
            if (successToken instanceof SwitchLanguageToken) {
                continue;
            }
            Assert.isTrue(successToken.getData() instanceof String);
            String s = (String) successToken.getData();
            if (found.contains(s)) {
                while (found.contains(s)) {
                    s += ("." + nextId++);
                }
                ((IChangeTokenRule) rule).setToken(TokenFactory.createTokenCopy(successToken, s));
            }
            found.add(s);
        }
        return nextId;
    }

    public Map<String, Object> loadRules(Map<String, Object> data, LiClipseLanguage ret)
            throws AssertionFailedException {
        //--- Load RULES
        RulesFactory factory = ret.rulesFactory;

        //Note: rules will be cleared during this process!
        Map<String, Object> ruleAliases = loadRuleAliases(data, ret, factory);
        loadRegularRules(data, ret, factory, ruleAliases);
        return ruleAliases;
    }

    private void loadRegularRules(Map<String, Object> data, LiClipseLanguage ret, RulesFactory factory,
            Map<String, Object> ruleAliases) {
        List<Object> rules = (List<Object>) getOptional(data, LiClipseLanguage.RULES, List.class, null);
        if (rules != null) {
            ret.rules.addAll(factory.load(rules, ruleAliases));
        }
    }

    public Map<String, Object> loadRuleAliases(Map<String, Object> data, LiClipseLanguage ret, RulesFactory factory)
            throws AssertionFailedException {
        Map<String, Object> ruleAliases = (Map) getOptional(data, LiClipseLanguage.RULE_ALIASES, Map.class,
                new HashMap());
        if (ruleAliases != null && ruleAliases.size() > 0) {
            Set<Entry<String, Object>> entrySet = ruleAliases.entrySet();
            Map<String, String> delayed = new HashMap<>();
            for (Entry<String, Object> entry : entrySet) {
                Object value = entry.getValue();
                if (value instanceof Map) {
                    List<Object> asList = (List<Object>) RulesFactory.copyObject(Arrays.asList(value));
                    Map rule = (Map) RulesFactory.copyObject(ruleAliases);
                    List<ILiClipsePredicateRule> loadedAlias = factory.load(asList, rule);
                    if (loadedAlias.size() != 1) {
                        throw new AssertionFailedException("Error in alias definition: " + entry.getKey()
                                + " (should point to a single rule).");
                    }
                    ret.ruleAliases.put(entry.getKey(), loadedAlias.get(0));
                } else if (value instanceof String) {
                    // We have 2 aliases to the same place... let's do this later.
                    delayed.put(entry.getKey(), (String) value);
                } else {
                    throw new AssertionFailedException(
                            "Unexpected type as value for rule alias: " + entry.getKey() + " - "
                                    + value.getClass());
                }
            }

            Set<Entry<String, String>> entrySet2 = delayed.entrySet();
            for (Entry<String, String> entry : entrySet2) {
                ILiClipsePredicateRule value = ret.ruleAliases.get(entry.getValue());
                if (value == null) {
                    Log.log("Cannot resolve alias: " + entry.getKey() + " -> " + entry.getValue());
                } else {
                    ret.ruleAliases.put(entry.getKey(), value);
                }
            }
        }
        return ruleAliases;
    }

    /**
     * For the TextMate grammars, we have to:
     *
     * - Fill the name as the scopeName (as that's how we find about it later)
     * - Load the 'regular' rules
     * - Fill scopeToScopeColorScanning to have the relation to the top-level patterns from TextMate grammars
     * - Fill fileExtensions
     * - Fill filenames
     */
    public void loadTmLanguage(LiClipseLanguage ret, InputStream stream, String fileInfo) throws Exception {
        ret.languageType = LanguageType.TEXT_MATE;
        TmLanguageHandler tmLanguageHandler = new TmLanguageHandler();
        tmLanguageHandler.parse(stream);
        Object name = tmLanguageHandler.getValue(TmLanguageHandler.SCOPE_NAME);
        if (name == null) {
            throw new RuntimeException("Unable to get: " + TmLanguageHandler.SCOPE_NAME + " from: "
                    + fileInfo);
        }
        ret.name = name.toString();
        Object caption = tmLanguageHandler.getValue(TmLanguageHandler.NAME);
        if (caption == null) {
            caption = ret.name;
        }
        ret.caption = caption.toString();

        // Commenting out: we no longer handle textmate with our own rules (it now uses tm4e
        // to do the parsing).
        // Map<String, ILiClipsePredicateRule> ruleAliases = tmLanguageHandler.loadRepositoryRules(ret);
        // ret.ruleAliases.putAll(ruleAliases);
        // LinkedList<ITextMateRule> regularRules = tmLanguageHandler.loadRegularRules(ret);
        // ret.rules.addAll(regularRules);
        // ret.injectionRules.addAll(tmLanguageHandler.loadInjectionRules(ret));

        //Important: fix before setting up the scope scanning.
        fixTopLevelRulesToHaveValidTokens(ret);

        Map<String, ScopeColorScanning> scopeToScopeColorScanning = ret.scopeToScopeColorScanning;
        for (ILiClipsePredicateRule rule : ret.rules) {
            createPartitionScanning(ret, scopeToScopeColorScanning, rule);
        }
        for (ScopeSelector s : ret.injectionRules) {
            for (ILiClipsePredicateRule rule : s.getRules()) {
                createPartitionScanning(ret, scopeToScopeColorScanning, rule);
            }
        }

        Object fileTypes = tmLanguageHandler.getValue("fileTypes");
        if (fileTypes instanceof List) {
            List list = (List) fileTypes;
            for (Object object : list) {
                ret.fileExtensions.add(object.toString());
            }
        }
    }

    private void createPartitionScanning(LiClipseLanguage ret,
            Map<String, ScopeColorScanning> scopeToScopeColorScanning, ILiClipsePredicateRule rule) {
        IToken successToken = rule.getSuccessToken();
        Object data = successToken.getData();
        if (data instanceof String) {
            ScopeColorScanning scopeScanning = new ScopeColorScanning(false, ret);
            scopeScanning.setSubRules(new ILiClipsePredicateRule[] { rule });
            scopeToScopeColorScanning.put((String) data, scopeScanning);
        }
    }

    private Map<String, Object> createDataFromContents(LanguagesManager languagesManager, InputStream stream)
            throws AssertionFailedException {
        Map<String, Object> data = loadDataFromContents(stream);
        Object inherit = data.remove(LiClipseLanguage.INHERIT);
        Object extend = data.get(LiClipseLanguage.EXTEND); //I.e.: extend is never considered at this point (it's explicitly loaded for extension languages later on).

        if (inherit != null) {
            if (languagesManager != null) {
                LiClipseLanguage languageFromName = languagesManager.getLanguageFromName(inherit.toString());
                try (IStreamProvider fileContents = languageFromName.file.getStreamProvider()) {
                    try (InputStream stream2 = fileContents.getStream()) {
                        Map<String, Object> inheritData = createDataFromContents(languagesManager, stream2); //Do it recursively!
                        Map<String, Object> currentItems = data;
                        data = inheritData;
                        Object patch = currentItems.remove(LiClipseLanguage.PATCH); //patch can only appear when inherit is there.
                        patchStructure(data, patch);
                        data.putAll(currentItems);
                        // Can be used to print the structure we have.
                        // Yaml yaml = new Yaml();
                        // String dump = yaml.dump(data);
                        // System.out.println(dump);
                    }
                } catch (Exception e) {
                    Log.log(e);
                }
            }

        } else {
            if (extend == null) {
                if (data.containsKey(LiClipseLanguage.PATCH)) {
                    throw new AssertionFailedException("'patch' can only be used if 'inherit' or 'extend' is defined.");
                }
            }
        }

        Object name = data.get(LiClipseLanguage.NAME);
        if (name != null && languagesManager != null) {
            applyLanguageExtensionsToStructure(data, languagesManager, name);
        }
        return data;
    }

    private void applyLanguageExtensionsToStructure(Map<String, Object> data, LanguagesManager languagesManager,
            Object name) throws AssertionFailedException {
        Object extend;
        List<File> lst = languagesManager.getExtensionsFor(name.toString());
        if (lst != null) {
            for (File file : lst) {
                try (FileInputStream stream = new FileInputStream(file)) {
                    Map<String, Object> extendData = loadDataFromContents(new BufferedInputStream(stream));
                    extend = extendData.remove(LiClipseLanguage.EXTEND);
                    if (extend != null) {
                        Object patch = extendData.remove(LiClipseLanguage.PATCH);
                        patchStructure(data, patch);
                    }
                } catch (Exception e) {
                    Log.log(e);
                }
            }
        }
    }

    private void patchStructure(Map<String, Object> data, Object patch) throws AssertionFailedException {
        if (patch != null) {
            if (!(patch instanceof List)) {
                if (patch instanceof Map) {
                    patch = Arrays.asList(patch);
                } else {
                    throw new AssertionFailedException("Expected patch to be a List. Found: " + patch);
                }
            }
            List list = (List) patch;
            for (Object object : list) {
                if (object instanceof Map) {
                    Map<String, Object> map = (Map) object;
                    Set<Entry<String, Object>> entrySet = map.entrySet();
                    for (Entry<String, Object> entry : entrySet) {
                        String key = entry.getKey();
                        Object toPatch = data.get(key);
                        if (LiClipseLanguage.COMMENT.equals(key)) {
                            // We can't patch comments, just set their new value (we can only
                            // have one comment approach -- although we probably should have one
                            // approach for each content type?).
                            data.put(key, entry.getValue());
                        }

                        if (toPatch instanceof Map) {
                            if (!(entry.getValue() instanceof Map)) {
                                throw new AssertionFailedException("Expected Map to patch Map on entry: " + key);
                            }
                            Map toPatchMap = (Map) toPatch;
                            Map patchMap = (Map) entry.getValue();
                            toPatchMap.putAll(patchMap);
                        } else if (toPatch instanceof List) {
                            if (!(entry.getValue() instanceof List)) {
                                throw new AssertionFailedException("Expected List to patch List on entry: " + key);
                            }
                            List toPatchList = (List) toPatch;
                            List patchList = (List) entry.getValue();
                            if (key.equals("scope_definition_rules")) {
                                toPatchList.addAll(0, patchList);
                            } else {
                                toPatchList.addAll(patchList);
                            }
                        } else if (toPatch == null) {
                            // There's nothing there, just make ours the current in the data.
                            data.put(key, entry.getValue());

                        } else {
                            throw new AssertionFailedException(
                                    "Don't know how to patch: " + toPatch.getClass() + " for entry: "
                                            + key);
                        }
                    }
                } else {
                    throw new AssertionFailedException("Don't know how to apply patch with: " + object);
                }
            }
        }
    }

    public Map<String, Object> loadDataFromContents(InputStream stream) {
        if (!(stream instanceof BufferedInputStream)) {
            stream = new BufferedInputStream(stream);
        }
        Yaml yaml = new Yaml();
        Object load = yaml.load(stream);
        if (!(load instanceof Map)) {
            if (load == null) {
                throw new RuntimeException("Expected top-level element to be a map. Found: null");
            }
            throw new RuntimeException("Expected top-level element to be a map. Found: " + load.getClass());
        }

        Map<String, Object> data = (Map) load;
        return data;
    }

    private Object getOptional(Map<String, Object> data, String key, Class expectedClass, Object defaultReturn) {
        Object required = data.remove(key);
        if (required == null) {
            return defaultReturn;
        }
        if (!expectedClass.isInstance(required)) {
            throw new RuntimeException("'" + key + "' value expected to be a " + expectedClass.getName() + " found: "
                    + required.getClass().getName());
        }
        return required;
    }

    public static Object getRequired(Map<String, Object> data, String key, Class expectedClass) {
        Object required = data.remove(key);
        if (required == null) {
            throw new RuntimeException("'" + key + "' entry not found.");
        }
        if (!expectedClass.isInstance(required)) {
            throw new RuntimeException("'" + key + "' value expected to be a " + expectedClass.getName() + " found: "
                    + required.getClass().getName());
        }
        return required;
    }

    public static char[] convertListOfStringsToArrayOfChars(List list, String attr, List<IStatus> errorList) {
        ArrayList<Character> temp = new ArrayList<Character>();
        int length = list.size();
        for (int i = 0; i < length; i++) {
            Object str = list.get(i);
            if (str instanceof Character) {
                temp.add((Character) str);
            } else if (str instanceof String) {
                String string = (String) str;
                for (int j = 0; j < string.length(); j++) {
                    temp.add(string.charAt(j));
                }
                //                if (string.length() == 1) {
                //                    temp.add(string.charAt(0));
                //                } else {
                //                    LiClipseTextEditorPlugin.createWarning(
                //                            "Expected " + attr + " to be list of strings with len==1. Found: "
                //                                    + string, errorList);
                //
                //                }
            } else {
                LiClipseTextEditorPlugin.createWarning(
                        "Expected " + attr + " to be list of strings. Found: "
                                + str.getClass(),
                        errorList);
            }
        }

        length = temp.size();
        char[] autoCloseChars = new char[length];
        for (int i = 0; i < length; i++) {
            Character c = temp.get(i);
            autoCloseChars[i] = c;
        }
        return autoCloseChars;
    }

    public static List<String> checkListOfStrings(String attr, List list, List<IStatus> errorList) {
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (!(object instanceof String)) {
                iterator.remove();
                LiClipseTextEditorPlugin.createWarning(
                        "Expected " + attr + " to be list of strings. Found: "
                                + object.getClass(),
                        errorList);
            }
        }
        return list;
    }

}