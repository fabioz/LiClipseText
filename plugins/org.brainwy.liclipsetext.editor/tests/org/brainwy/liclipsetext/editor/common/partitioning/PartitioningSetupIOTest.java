package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.languages.LanguageMetadataFileInfo;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataInMemoryFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.yaml.snakeyaml.Yaml;

public class PartitioningSetupIOTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.startEditorPlugin();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtils.stopEditorPlugin();
    }

    public void testWords() throws Exception {
        Yaml yaml = new Yaml();
        String dump = yaml
                .dump(StringUtils
                        .split(""
                                + ""
                                + "above absolute absolute-colorimetric accent adjacent afar after-edge ahead alias all-scroll alphabetic "
                                + "alternate always amharic amharic-abegede arabic-indic armenian ascent asterisks attr auto avoid "
                                + "back backwards baseline before-edge behind below bengali bidi-override binary blink block block-axis "
                                + "block-inside block-line-height block-within-page bold bolder border-box border-edge both bottom "
                                + "bounding-box box break-all break-strict break-word "
                                + "cambodian cap-height capitalize caps-height cell center center-left center-right centerline central check "
                                + "child circle circled-decimal circled-lower-latin circled-upper-latin cjk-earthly-branch cjk-heavenly-stem "
                                + "cjk-ideographic close-quote col-resize collapse compact condensed consider-shifts content-box "
                                + "content-edge context-menu continuous copy counter cross crosshair current cursive "
                                + "dashed decimal decimal-leading-zero default definition-src descent devanagari diamond digits disc "
                                + "discard disregard-shifts distribute distribute-letter distribute-space dot dot-dash dot-dot-dash "
                                + "dotted dotted-decimal double double-circled-decimal down "
                                + "e-resize each-box embed emboss end end-edge engrave ethiopic ethiopic-abegede ethiopic-abegede-am-et "
                                + "ethiopic-abegede-gez ethiopic-abegede-ti-er ethiopic-abegede-ti-et ethiopic-halehame-aa-er "
                                + "ethiopic-halehame-aa-et ethiopic-halehame-am-et ethiopic-halehame-gez ethiopic-halehame-om-et "
                                + "ethiopic-halehame-sid-et ethiopic-halehame-so-et ethiopic-halehame-ti-er ethiopic-halehame-ti-et "
                                + "ethiopic-halehame-tig ethiopic-numeric ew-resize exclude-ruby expanded extra-condensed extra-expanded "
                                + "fantasy far-left far-right fast faster female fill filled-circled-decimal fixed footnotes forwards "
                                + "front georgian grid-height groove gujarati gurmukhi "
                                + "hanging hangul hangul-consonant hebrew help here hidden hide high higher hiragana hiragana-iroha "
                                + "horizontal hyphen "
                                + "icon ideographic images include-ruby increment indent inherit initial ink inline inline-axis "
                                + "inline-block inline-inside inline-line-height inline-table inset inset-rect inside inter-cluster "
                                + "inter-ideograph inter-word invert italic "
                                + "japanese-formal japanese-informal justify "
                                + "kannada kashida katakana katakana-iroha keep-all khmer "
                                + "landscape lao large larger last left left-side leftwards level lighter line line-edge line-through "
                                + "list-item literal-punctuation local loose loud low lower lower-alpha lower-armenian lower-greek "
                                + "lower-hexadecimal lower-latin lower-norwegian lower-roman lowercase ltr "
                                + "malayalam male manual margin-box margin-edge marker marker-offset marks mathematical mathline max-size "
                                + "medium meet menu message-box middle mix modal moderate mongolian monospace move multiple myanmar "
                                + "n-resize narrower ne-resize nesw-resize never new no-change no-close-quote no-drop no-limit no-open-quote "
                                + "no-punctuation no-repeat none normal not-allowed nowrap ns-resize nw-resize nwse-resize "
                                + "oblique octal old once open-quote oriya oromo outset outside overline "
                                + "padding-box padding-edge panose-1 parent parenthesised-decimal parenthesised-lower-latin "
                                + "perceptual persian pointer portrait pre-line pre-wrap preserve preserve-breaks progress "
                                + "rect reduced relative relative-colorimetric repeat repeat-x repeat-y reset-size reverse rgb ridge right "
                                + "right-side rightwards round row-resize rtl ruby ruby-base ruby-base-group ruby-text ruby-text-group run-in "
                                + "s-resize same sans-serif saturation scroll se-resize self semi-condensed semi-expanded separate serif "
                                + "show sidama silent simp-chinese-formal simp-chinese-informal single size slice slide slope slow slower "
                                + "small-caps small-caption smaller soft solid somali spaces spell-out square src sRGB start static "
                                + "status-bar stemh stemv stretch super suppress sw-resize syriac "
                                + "tab table-caption table-cell table-column table-column-group table-footer-group table-header-group "
                                + "table-row table-row-group tamil telugu text text-after-edge text-before-edge text-bottom text-shadow "
                                + "text-size text-top thai thick thin tibetan tigre tigrinya-er tigrinya-er-abegede tigrinya-et "
                                + "tigrinya-et-abegede top topline trad-chinese-formal trad-chinese-informal transparent "
                                + "ultra-condensed ultra-expanded underline unicode-range units-per-em unrestricted up upper-alpha "
                                + "upper-armenian upper-greek upper-hexadecimal upper-latin upper-norwegian upper-roman uppercase "
                                + "urdu url use-script "
                                + "vertical vertical-text visible "
                                + "w-resize wait wave weak wider window "
                                + "x-fast x-height x-high x-large x-loud x-low x-slow x-small x-soft x-strong x-weak xx-large xx-small "
                                + "young "
                                + " "
                                + "",
                                ' '));
        //System.out.println(dump);
    }

    public void testDumpLoad() throws Exception {
        LiClipseLanguage setup = new LiClipseLanguage();
        setup.setupLineComment("//");
        setup.setupBlockComment("/*", "*/", '\0');
        setup.setupSingleLineStringChar("'");
        setup.setupSingleLineStringChar("\"");
        setup.name = "test";
        setup.setupKeywords(StringUtils
                .split("abstract break byte case catch char class const continue default delete do double else extends false final finally float for function goto if implements import in instanceof int interface long native null package private protected public reset return short static super switch synchronized this throw transient true try var void while with",
                        ' '));
        setup.setupBrackets(StringUtils.split("( { [ ] } )", ' '));
        setup.setupOperators(StringUtils.split("! $ % & * + - // / : < = > ? ^ | ~ is new sizeof typeof unchecked , ;",
                ' '));
        setup.addFileExtension("JS");
        setup.addFileName("SpecialName");

        Writer writer = new StringWriter();
        setup.dump(writer);
        String string = writer.toString();

        LiClipseLanguage load = LiClipseLanguage.load(null, new ByteArrayInputStream(string.getBytes("utf-8")), true);
        writer = new StringWriter();
        load.dump(writer);
        assertEquals(string, writer.toString());
    }

    public void testDumpLoad2() throws Exception {
        LiClipseLanguage setup = new LiClipseLanguage();
        setup.setupLineComment("#");
        setup.setupBlockString("'''", "'''", '\0');
        setup.setupSingleLineStringChar("'");
        setup.setupSingleLineStringChar("\"");
        setup.setupBrackets(StringUtils.split("( { [ ] } )", ' '));
        setup.setupKeywords(StringUtils.split(""
                + "and as assert break continue "
                + "del elif else except exec finally for from global if import in is "
                + "lambda nonlocal not or pass print raise return self try while with "
                + "yield False None True"
                + "", ' '));

        setup.setupOperators(StringUtils.split(""
                + "< "
                + "> "
                + "= "
                + "+ "
                + "- "
                + "/ "
                + "* "
                + "! "
                + "& "
                + "| "
                + "% "
                + "~ "
                + "^ "
                + ", ",
                ' '));
        setup.addFileExtension("PY");
        setup.name = "Python";

        Writer writer = new StringWriter();
        setup.dump(writer);
        String string = writer.toString();

        LiClipseLanguage load = LiClipseLanguage.load(null, new ByteArrayInputStream(string.getBytes("utf-8")), true);
        writer = new StringWriter();
        load.dump(writer);
        //Not a requirement anymore (dump isn't really used).
        //assertEquals(string, writer.toString());
    }

    public void testContentTypes() throws Exception {
        String str = ""
                + "scope_to_color_name: {}\n" //default: foreground there by default!
                + "scope_definition_rules:\n"
                + "- {type: MultiLineRule, scope: dict, start: '{', end: '}', escapeCharacter: \"'\"}\n"
                + "- {type: MultiLineRule, scope: list, start: '[', end: ']', escapeCharacter: \"'\"}\n"
                + "scope:\n"
                + "  default:\n"
                + "    class: []\n"
                + "        \n"
                + "file_extensions: [foo]\n"
                + "filename: []\n"
                + "name: Foo\n"
                + "";
        LiClipseLanguage load = LiClipseLanguage.load(null, new ByteArrayInputStream(str.getBytes("utf-8")), true);
        String[] legalContentTypes = load.getLegalContentTypes();
        Arrays.sort(legalContentTypes);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type",
                "dict",
                "list"), TestUtils.listToExpected(legalContentTypes));
    }

    public void testSpellCheckingContentTypes() throws Exception {
        String str = ""
                + "scope_to_color_name: {dict: string}\n"
                + "scope_definition_rules:\n"
                + "- {type: MultiLineRule, scope: dict, start: '{', end: '}', escapeCharacter: \"'\"}\n"
                + "- {type: MultiLineRule, scope: list, start: '[', end: ']', escapeCharacter: \"'\"}\n"
                + "scope:\n"
                + "  default:\n"
                + "    class: []\n"
                + "        \n"
                + "file_extensions: [foo]\n"
                + "filename: []\n"
                + "name: Foo\n"
                + "";
        LiClipseLanguage load = LiClipseLanguage.load(null, new ByteArrayInputStream(str.getBytes("utf-8")), true);
        String[] legalContentTypes = load.getSpellCheckingContentTypes();
        Arrays.sort(legalContentTypes);
        assertEquals(TestUtils.listToExpected("dict"), TestUtils.listToExpected(legalContentTypes));
    }

    public void testLoadBase() throws Exception {
        File languagesDirFile = TestUtils.getLanguagesDir();
        File file = new File(languagesDirFile, "base.liclipse");
        assertNotNull(new LanguageMetadataFileInfo(file).loadLanguage(true));
    }

    public void testInheritOnDjango() throws Exception {
        File languagesDirFile = TestUtils.configLanguagesManager();
        try {
            File file = new File(languagesDirFile, "django.liclipse");
            LiClipseLanguage load = new LanguageMetadataFileInfo(file).loadLanguage(true);
            assertEquals(load.name, "Django");
        } finally {
            TestUtils.clearLanguagesManager();
        }

    }

    public void testInheritOnDjangoLevel2() throws Exception {
        File languagesDirFile = TestUtils.configLanguagesManager();
        try {
            LiClipseLanguage load = new LanguageMetadataInMemoryFileInfo(""
                    + "inherit: Django\n" //Same thing as django as we have nothing else.
                    + "").loadLanguage(true);
            assertEquals(load.name, "Django");
        } finally {
            TestUtils.clearLanguagesManager();
        }

    }
}
