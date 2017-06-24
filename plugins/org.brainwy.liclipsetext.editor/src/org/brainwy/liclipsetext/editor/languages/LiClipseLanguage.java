/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.CustomTextAttributeTokenCreator;
import org.brainwy.liclipsetext.editor.common.partitioning.DummyColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.CompositeRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.IPrintableRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.ISwitchLanguageRule;
import org.brainwy.liclipsetext.editor.common.partitioning.rules.RulesFactory;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadata.LanguageType;
import org.brainwy.liclipsetext.editor.languages.auto_edit.LanguageAutoEdit;
import org.brainwy.liclipsetext.editor.languages.comment.LanguageComment;
import org.brainwy.liclipsetext.editor.languages.indent.LanguageIndent;
import org.brainwy.liclipsetext.editor.languages.launch.LanguageLaunch;
import org.brainwy.liclipsetext.editor.languages.navigation.LanguageNavigation;
import org.brainwy.liclipsetext.editor.languages.outline.LanguageOutline;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.rules.SwitchLanguageToken;
import org.brainwy.liclipsetext.editor.scopes.ScopesParser;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplate;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateContextType;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.parsing.IScopesParser;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.OrderedMap;
import org.brainwy.liclipsetext.shared_core.structure.OrderedSet;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.IDocument;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.eclipse.jface.text.rules.IToken;

/**
 * Setup the partitioning based on the language (note that we may need to inspect the document
 * to decide the language).
 */
public class LiClipseLanguage {

    public static final String FILENAME = "filename";

    public static final String FILE_EXTENSION = "file_extensions";

    public static final String NAME = "name";

    public static final String SHEBANG = "shebang";

    public static final String CONTENT_TYPE_TO_COLOR = "scope_to_color_name";

    public static final String RULE_ALIASES = "rule_aliases";

    public static final String OUTLINE = "outline";

    public static final String LAUNCH = "launch";

    public static final String RULES = "scope_definition_rules";

    public static final String EDITOR_ID = "editor_id";

    public static final String SCOPE = "scope";

    public static final String INDENT = "indent";

    public static final String AUTO_EDIT = "auto_edit";

    public static final String COMMENT = "comment";

    public static final String TEMPLATES = "templates";

    public static final String TEMPLATE_VARIABLES = "template_variables";

    public static final String SPELL_CHECK = "spell_check";

    public static final String CASE = "case";

    public static final String INHERIT = "inherit";

    public static final String EXTEND = "extend";

    public static final String PATCH = "patch";

    public static final String SEPARATORS = "separators";

    public static final String CODE_COMPLETION = "code_completion";

    public static final String TM_LANGUAGE = "tm_language";

    public void dump(Writer output) {
        new LiClipseLanguageIO(this).dump(output);
    }

    private void loadInternal(ILanguageMetadataFileInfo file, InputStream stream, boolean rethrowError)
            throws Exception {
        this.file = file;
        new LiClipseLanguageIO(this).loadInternal(file, stream, rethrowError);
        fixLoad();
    }

    /**
     * Lets do checks to see if the content types are properly configured.
     */
    private void fixLoad() {
        Set<String> contentTypes = new HashSet<String>();
        collect(rules.toArray(new ILiClipsePredicateRule[rules.size()]), contentTypes);
        if (injectionRules != null) {
            for (ScopeSelector s : injectionRules) {
                ILiClipsePredicateRule[] array = s.getRules().toArray(new ILiClipsePredicateRule[0]);
                collect(array, contentTypes);
            }
        }

        if (this.languageType == LanguageType.TEXT_MATE) {
            contentTypes.add(this.name);
        }
        if (!contentTypes.contains(IDocument.DEFAULT_CONTENT_TYPE)) {
            contentTypes.add(IDocument.DEFAULT_CONTENT_TYPE);
        }

        Set<Entry<String, ScopeColorScanning>> entrySet3 = scopeToScopeColorScanning.entrySet();
        for (Entry<String, ScopeColorScanning> entry : entrySet3) {
            ILiClipsePredicateRule[] subRules = entry.getValue().getSubRules();
            if (subRules != null) {
                collect(subRules, contentTypes);
            }
        }

        List<Tuple<ILiClipsePredicateRule, LiClipseLanguage>> subLanguages = this.getSubLanguages();
        boolean hasSubLanguages = subLanguages.size() > 0;

        // Fixing the contentTypeToColorTokenName so that we have all the keys needed. If this is
        // not done, the partitions generated won't remain as valid content types later on (and
        // thus won't be correctly treated).
        for (String contentType : contentTypes) {
            if (!this.contentTypeToColorTokenName.containsKey(contentType)) {
                String colorName = contentType;
                if (IDocument.DEFAULT_CONTENT_TYPE.equals(colorName)) {
                    //'default' has foreground as default
                    colorName = "foreground";
                }
                contentTypeToColorTokenName.put(contentType, colorName);
            }
        }

        // Some details: when dealing with sub-partitions for different languages, we may want
        // to access the current language. For that, sub-tokens should refer to "this" as the
        // sub-language. The process below fixes our contentTypeToColorTokenName so that those
        // colors can be accessed later on (and so that these generated partitions are valid).
        if (hasSubLanguages) {
            Map<String, String> thisVariation = new HashMap<String, String>();
            Set<Entry<String, String>> entrySet = contentTypeToColorTokenName.entrySet();
            for (Entry<String, String> entry : entrySet) {
                String key = entry.getKey();
                if (!SwitchLanguageToken.isSubLanguagePartition(key)) {
                    String subLanguageContentType = SwitchLanguageToken.createSubLanguageContentType("this", key);
                    if (!thisVariation.containsKey(subLanguageContentType)) {
                        thisVariation.put(subLanguageContentType, entry.getValue());
                    }
                }
            }
            contentTypeToColorTokenName.putAll(thisVariation);
        }

        // We have to add the content types of the sub-languages in our current language
        // (otherwise the content types are not considered valid).
        for (Tuple<ILiClipsePredicateRule, LiClipseLanguage> subLanguage : subLanguages) {
            Map<String, String> subContentToColor = subLanguage.o2.contentTypeToColorTokenName;
            Set<Entry<String, String>> entrySet = subContentToColor.entrySet();
            String subLanguageName = subLanguage.o2.name;
            if (subLanguageName == null) {
                throw new RuntimeException("Error, not expecting sub-language name to be null on rule: "
                        + subLanguage.o1);
            }
            for (Entry<String, String> entry : entrySet) {
                String subLanguageContentType = SwitchLanguageToken.createSubLanguageContentType(
                        subLanguageName.toLowerCase(), entry.getKey());
                contentTypeToColorTokenName.put(subLanguageContentType, entry.getValue());
            }

            Map<String, ScopeColorScanning> subScopeToScopeColorScanning = subLanguage.o2.scopeToScopeColorScanning;
            Set<Entry<String, ScopeColorScanning>> entrySet2 = subScopeToScopeColorScanning.entrySet();
            for (Entry<String, ScopeColorScanning> entry : entrySet2) {
                String subLanguageContentType = SwitchLanguageToken.createSubLanguageContentType(
                        subLanguageName.toLowerCase(), entry.getKey());
                scopeToScopeColorScanning.put(subLanguageContentType, entry.getValue());
            }
        }
    }

    /**
     * This method will fill the contentTypes based on the given rules (along with the sub-rules).
     * @param contentTypes (out): this set will have all the content types for all the rules.
     */
    private void collect(ILiClipsePredicateRule[] rules, Set<String> contentTypes) {
        for (ILiClipsePredicateRule rule : rules) {
            IToken token = rule.getSuccessToken();
            if (token == null) {
                continue;
            }
            Object data = token.getData();
            if (data == null) {
                continue;
            }
            contentTypes.add((String) data);
            if (rule instanceof CompositeRule) {
                CompositeRule compositeRule = (CompositeRule) rule;
                ILiClipsePredicateRule[] subRules = compositeRule.getSubRules();
                collect(subRules, contentTypes);
            }
        }
    }

    /**
     * Throws an exception if something bad happens loading the information (if rethrowError == true).
     */
    public static LiClipseLanguage load(ILanguageMetadataFileInfo file, InputStream stream, boolean rethrowError)
            throws Exception {
        LiClipseLanguage ret = new LiClipseLanguage();
        ret.loadInternal(file, stream, rethrowError);
        return ret;
    }

    // ====================== Attributes

    public final List<ILiClipsePredicateRule> rules;

    public final List<ScopeSelector> injectionRules;

    public final Map<String, ILiClipsePredicateRule> ruleAliases;

    public final Map<String, String> contentTypeToColorTokenName;

    public final Map<String, ScopeColorScanning> scopeToScopeColorScanning = new OrderedMap<String, ScopeColorScanning>();

    final RulesFactory rulesFactory = new RulesFactory(this);

    final Set<String> fileExtensions = new OrderedSet<String>();

    /**
     * Holds whether this language should be treated in a case insensitive way.
     */
    public boolean caseInsensitive;

    /**
     * String with the characters to be considered separators (may be null).
     */
    public String charSeparators;

    public Set<String> getFileExtensions() {
        return fileExtensions;
    }

    final Set<String> filenames = new OrderedSet<String>();

    /**
     * The name of the language (XML, Javascript, Python, etc).
     */
    public String name;

    /**
     * A caption for the language (can be the same as the name or different -- i.e.: textmate).
     */
    public String caption;

    /**
     * The editor id to be used for this language. The default is always the default liclipse editor.
     */
    public String editorId = "com.brainwy.liclipse.editor.common.LiClipseEditor";

    /**
     * The file from where the language was loaded.
     */
    public ILanguageMetadataFileInfo file;

    public boolean useOnlyTemplatesOnCodeCompletion = false;

    /**
     * Note: metadata does not have shebang when created from this call!
     */
    public LanguageMetadata getLanguageMetadata() {
        return new LanguageMetadata(name, file, null, this.languageType, this.caption);
    }

    public LiClipseLanguage() {
        rules = new ArrayList<>();
        ruleAliases = new HashMap<>();
        injectionRules = new ArrayList<>();

        contentTypeToColorTokenName = new OrderedMap<String, String>();
        contentTypeToColorTokenName.put(ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE, "foreground");
    }

    /**
     * Ideally use connect(IDocument).
     */
    public LiClipseDocumentPartitioner createPartitioner() {
        return new LiClipseDocumentPartitioner(this);
    }

    public LiClipseDocumentPartitioner connect(IDocument document) {
        LiClipseDocumentPartitioner partitioner = new LiClipseDocumentPartitioner(this);

        try {
            partitioner.connect(document);
        } catch (Exception e) {
            Log.log("Error connecting partitioner", e);
        }
        //I.e.: LiClipseDocumentPartitioner.PARTITION_TYPE == IDocumentExtension3.DEFAULT_PARTITIONING
        document.setDocumentPartitioner(partitioner);
        return partitioner;
    }

    private void clearCaches() {
        fLegalContentTypes = null;
        if (!fSpellCheckingContentTypesManuallySpecified) {
            fSpellCheckingContentTypes = null;
        }
    }

    private String[] fLegalContentTypes;

    /**
     * @return the legal content types. Note that for sub-languages it should return something as
     * sub_language&content_type.
     *
     * i.e.: javascript&multiLineComment, javascript&__dftl_partition_content_type, etc.
     */
    public String[] getLegalContentTypes() {
        if (fLegalContentTypes == null) {
            fLegalContentTypes = contentTypeToColorTokenName.keySet().toArray(
                    new String[contentTypeToColorTokenName.size()]);
        }
        return fLegalContentTypes;

        //No longer using code below because the contentTypeToColorTokenName should be already all
        //set when we finish the load.
        //
        // List<String> subLanguages = getSubLanguageContentTypes();
        //
        // String[] ret = ArrayUtils.concatArrays(
        //         contentTypeToColorTokenName.keySet().toArray(new String[contentTypeToColorTokenName.size()]),
        //         subLanguages.toArray(new String[subLanguages.size()])
        //         );
        // return ret;

    }

    public void setSpellCheckScopes(List<String> spellCheckScopes) {
        fSpellCheckingContentTypesManuallySpecified = true;
        fSpellCheckingContentTypes = spellCheckScopes.toArray(new String[spellCheckScopes.size()]);
    }

    private String[] fSpellCheckingContentTypes = null;
    private boolean fSpellCheckingContentTypesManuallySpecified = false;

    public String[] getSpellCheckingContentTypes() {
        if (fSpellCheckingContentTypes == null) {
            ArrayList<String> ret = new ArrayList<String>();
            Set<Entry<String, String>> entrySet = contentTypeToColorTokenName.entrySet();
            for (Entry<String, String> entry : entrySet) {
                if ("string".equals(entry.getValue())) {
                    ret.add(entry.getKey());
                }
            }
            fSpellCheckingContentTypes = ret.toArray(new String[ret.size()]);
        }
        return fSpellCheckingContentTypes;
    }

    /**
     * @return the language content types for our languages with switching rules.
     */
    @SuppressWarnings("unused")
    private List<String> getSubLanguageContentTypes() {
        List<Tuple<ILiClipsePredicateRule, LiClipseLanguage>> subLanguages = getSubLanguages();
        ArrayList<String> subLanguageContentTypes = new ArrayList<String>(subLanguages.size() * 10);
        for (Tuple<ILiClipsePredicateRule, LiClipseLanguage> tuple : subLanguages) {
            String data = tuple.o2.name.toLowerCase();
            String[] legalContentTypes = tuple.o2.getLegalContentTypes();
            for (String string : legalContentTypes) {
                subLanguageContentTypes.add(SwitchLanguageToken.createSubLanguageContentType(data, string));
            }
        }

        return subLanguageContentTypes;
    }

    /**
     * @return the sub-languages for switching rules available.
     */
    private List<Tuple<ILiClipsePredicateRule, LiClipseLanguage>> getSubLanguages() {
        ArrayList<Tuple<ILiClipsePredicateRule, LiClipseLanguage>> subLanguages = new ArrayList<Tuple<ILiClipsePredicateRule, LiClipseLanguage>>();
        List<ILiClipsePredicateRule> rules = this.rules;
        Map<String, LiClipseLanguage> loaded = new HashMap<String, LiClipseLanguage>();
        for (ILiClipsePredicateRule ILiClipsePredicateRule : rules) {
            if (ILiClipsePredicateRule instanceof ISwitchLanguageRule) {
                ISwitchLanguageRule switchLanguageRule = (ISwitchLanguageRule) ILiClipsePredicateRule;
                List<LiClipseLanguage> languages = switchLanguageRule.getLanguages();
                for (LiClipseLanguage language : languages) {
                    if (!loaded.containsKey(language.name)) {
                        subLanguages.add(new Tuple<ILiClipsePredicateRule, LiClipseLanguage>(ILiClipsePredicateRule, language));
                        loaded.put(language.name, language);
                    }
                }
            }
        }
        return subLanguages;
    }

    private final LanguageNavigation languageNavigation = new LanguageNavigation(this);

    public LanguageNavigation getNavigation() {
        return this.languageNavigation;
    }

    private LanguageOutline languageOutline = new LanguageOutline(this);

    public LanguageOutline getOutline() {
        return this.languageOutline;
    }

    private LanguageLaunch languageLaunch = new LanguageLaunch(this);

    public LanguageLaunch getLaunch() {
        return this.languageLaunch;
    }

    private final LanguageAutoEdit languageAutoEdit = new LanguageAutoEdit(this);

    public LanguageAutoEdit getAutoEdit() {
        return this.languageAutoEdit;
    }

    private final LanguageIndent languageIndent = new LanguageIndent(this);

    public LanguageIndent getIndent() {
        return this.languageIndent;
    }

    private final LanguageComment languageComment = new LanguageComment(this);

    public LanguageComment getComment() {
        return this.languageComment;
    }

    private final LanguageTemplates languageTemplates = new LanguageTemplates(this);

    public LanguageTemplates getTemplates() {
        return this.languageTemplates;
    }

    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // =============================================================================================
    // Helpers for programatically setting things up (just used for tests).

    public void setupLineComment(String startOfLineComment) {
        String tokenName = "singleLineComment";
        contentTypeToColorTokenName.put(tokenName, tokenName);
        rules.add(rulesFactory.createEndOfLineRule(startOfLineComment, tokenName));
        clearCaches();
    }

    public void setupBlockString(String stringStart, String stringEnd, char escapeChar) {
        String context = "multiLineString";
        context = generateContextName(context);
        contentTypeToColorTokenName.put(context, "string");
        rules.add(rulesFactory.createMultiLineRule(stringStart, stringEnd, context, escapeChar));
        clearCaches();
    }

    public void setupBlockComment(String start, String end, char escapeChar) {
        String context = "multiLineComment";
        context = generateContextName(context);
        contentTypeToColorTokenName.put(context, context);
        rules.add(rulesFactory.createMultiLineRule(start, end, context, escapeChar));
        clearCaches();
    }

    private ScopeColorScanning getScopeColorScanning() {
        ScopeColorScanning scopeColoringScanning = scopeToScopeColorScanning
                .get(ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE);
        if (scopeColoringScanning == null) {
            scopeColoringScanning = new ScopeColorScanning(this.caseInsensitive, this);
            scopeToScopeColorScanning.put(ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE, scopeColoringScanning);
        }
        return scopeColoringScanning;
    }

    public void setupKeywords(List<String> keywords) {
        ScopeColorScanning scopeColoringScanning = getScopeColorScanning();
        scopeColoringScanning.setKeywords(keywords);
    }

    public void setupBrackets(List<String> brackets) {
        ScopeColorScanning scopeColoringScanning = getScopeColorScanning();
        scopeColoringScanning.setBrackets(brackets);
    }

    public void setupOperators(List<String> operators) {
        ScopeColorScanning scopeColoringScanning = getScopeColorScanning();
        scopeColoringScanning.setOperators(operators);
    }

    public void addFileExtension(String fileExtension) {
        fileExtensions.add(fileExtension);
    }

    public void addFileName(String filename) {
        filenames.add(filename);
    }

    public void setupSingleLineStringChar(String c) {
        boolean escapeContinuesLine = true;
        String context;
        if (c.charAt(0) == '\'') {
            context = "singleQuotedString";
        } else if (c.charAt(0) == '"') {
            context = "doubleQuotedString";
        } else {
            context = "string";
        }
        context = generateContextName(context);
        contentTypeToColorTokenName.put(context, "string");

        rules.add(rulesFactory.createSingleLineRule(String.valueOf(c), context, '\\', escapeContinuesLine));
        clearCaches();
    }

    private void setupTemplates(LiClipseTemplate[] templates) {
        this.getTemplates().setTemplates(templates);
    }

    private String generateContextName(String context) {
        String initial = context;
        int i = 1;
        while (contentTypeToColorTokenName.containsKey(context)) {
            context = initial + i;
            i++;
        }
        return context;
    }

    @Override
    public String toString() {
        return new FastStringBuffer("LiClipseLanguage(", this.name.length() + 5)
                .append(this.name).append(")")
                .toString();
    }

    public static void main(String[] args) {
        LiClipseLanguage setup = new LiClipseLanguage();
        setup.name = "test";
        //        setup.setupOperators(StringUtils
        //                .split(""
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + " "
        //                        + "",
        //                        ' '));

        setup.setupTemplates(new LiClipseTemplate[] { new LiClipseTemplate("function", "function(){\n}",
                LiClipseTemplateContextType.LICLIPSE_TEMPLATES_CONTEXT_TYPE_ID,
                "function ${function_name} (${arguments}) {\n\t${cursor}\n}", false, null),
                new LiClipseTemplate("function", "function(){\n}",
                        LiClipseTemplateContextType.LICLIPSE_TEMPLATES_CONTEXT_TYPE_ID,
                        "function ${function_name} (${arguments}) {\n\t${cursor}\n}", false, null) });
        Writer writer = new StringWriter();
        setup.dump(writer);
        String string = writer.toString();
        System.out.println(string);
    }

    public IScopesParser createScopesParser() {
        return new ScopesParser(this);

    }

    private Set<Character> separatorsCharsInWord = null;

    private CustomTextAttributeTokenCreator defaultTokenCreator;
    private final Object defaultTokenCreatorLock = new Object();

    public LanguageType languageType = LanguageType.LICLIPSE; //default

    // Only available if languageType == LanguageType.TEXT_MATE (in the case that this is a .liclipse language with a .tmLanguage file).
    public File tmLanguageFile;

    public Set<Character> getSeparatorChars() {
        if (separatorsCharsInWord == null) {
            HashSet<Character> temp = new HashSet<Character>();
            //Default (to be overridden by language).
            String defaultSeparators = "./\\()\"'-:,.;<>~!@#$%^&*|+=[]{}`~?";
            if (this.charSeparators != null) {
                defaultSeparators = this.charSeparators;
            }
            for (Character character : defaultSeparators.toCharArray()) {
                temp.add(character);
            }
            separatorsCharsInWord = temp;
        }
        return separatorsCharsInWord;

    }

    public CustomTextAttributeTokenCreator getDefaultTokenCreator() {
        if (defaultTokenCreator == null) {
            synchronized (defaultTokenCreatorLock) {
                if (defaultTokenCreator == null) {
                    boolean pluginStarted = LiClipseTextEditorPlugin.PLUGIN_STARTED;

                    //If we don't have the plugin, use a dummy color cache;
                    IColorCache colorManager = pluginStarted ? LiClipseTextEditorPlugin.getDefault()
                            .getColorManager()
                            : new DummyColorCache();
                    defaultTokenCreator = new CustomTextAttributeTokenCreator(
                            colorManager, this);
                }
            }
        }
        return defaultTokenCreator;
    }

    public void printRules() {
        for (ILiClipsePredicateRule ILiClipsePredicateRule : this.rules) {
            if (ILiClipsePredicateRule instanceof IPrintableRule) {
                System.out.println(((IPrintableRule) ILiClipsePredicateRule).toTmYaml());
            } else {
                System.out.println(ILiClipsePredicateRule);
            }
        }

    }

    public void printRuleAliases() {
        Set<Entry<String, ILiClipsePredicateRule>> entrySet = this.ruleAliases.entrySet();
        for (Entry<String, ILiClipsePredicateRule> entry : entrySet) {
            ILiClipsePredicateRule ILiClipsePredicateRule = entry.getValue();
            String s;
            if (ILiClipsePredicateRule instanceof IPrintableRule) {
                s = ((IPrintableRule) ILiClipsePredicateRule).toTmYaml(1);
            } else {
                s = ILiClipsePredicateRule.toString();
            }
            System.out.println(StringUtils.format("{ %s: %s\n}", entry.getKey(), s));
        }
    }

}
