package org.brainwy.liclipsetext.editor.languages;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.shared_core.io.FileUtils;

import junit.framework.TestCase;

public class LanguagesManagerTest extends TestCase {
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

    private static final int TIMEOUT = 50;

    public void testLanguagesManager() throws Exception {
        File d = FileUtils.getTempFileAt(new File("."), "lang", ".liclipse");

        try {
            FileUtils.writeStrToFile(getContents(), d);

            File languagesDir = TestUtils.getLanguagesDir();
            LanguagesManager languagesManager = new LanguagesManager(languagesDir, new File("."));
            LanguageMetadataFileInfo liclipseFile = (LanguageMetadataFileInfo) languagesManager
                    .getMetadataForFilename(".liclipse").file;
            assertEquals(new File(languagesDir, "liclipse.liclipse"),
                    liclipseFile.getFile());
            LanguageMetadataFileInfo fooFile = (LanguageMetadataFileInfo) languagesManager
                    .getMetadataForFilename(".foo").file;
            assertEquals(d, fooFile.getFile());
            LiClipseLanguage partitioningSetup0 = languagesManager.getLanguageForFilename("a.foo");

            Thread.sleep(TIMEOUT); //elapse some time to make certain that the change time will actually change.
            LiClipseLanguage partitioningSetup1 = languagesManager.getLanguageForFilename("a.foo");

            FileUtils.writeStrToFile(getContents(), d);
            Thread.sleep(TIMEOUT); //elapse some time to make certain that the change time will actually change.
            LiClipseLanguage partitioningSetup2 = languagesManager.getLanguageForFilename("a.foo");

            assertSame(partitioningSetup0, partitioningSetup1);
            assertNotSame(partitioningSetup1, partitioningSetup2);

        } finally {
            d.delete();
        }
    }

    public void testShebangCoffee() throws Exception {
        LanguagesManager manager = LiClipseTextEditorPlugin.getLanguagesManager();
        List<String> lines = Arrays.asList("#!test/coffee");
        LiClipseLanguage language = manager.getLanguageFromContents(lines);
        assertEquals(language.name, "CoffeeScript");
    }

    public void testShebangPython() throws Exception {
        LanguagesManager manager = LiClipseTextEditorPlugin.getLanguagesManager();
        List<String> lines = Arrays.asList("#!test/python");
        LiClipseLanguage language = manager.getLanguageFromContents(lines);
        assertEquals(language.name, "Python");
    }

    private String getContents() {
        return ""
                + "scope_to_color_name: {singleLineComment: singleLineComment, multiLineComment: multiLineComment,\n"
                + "  singleQuotedString: string, doubleQuotedString: string, default: foreground}\n"
                + "scope_definition_rules:\n"
                + "- {type: EndOfLineRule, scope: singleLineComment, start: '#'}\n"
                + "scope:\n"
                + "  default:\n"
                + "    keyword: [EndOfLineRule, MultiLineRule, PatternRule]\n"
                + "    bracket: ['{', '[', ']', '}']\n"
                + "    class: [string, singleLineComment, multiLineComment, foreground]\n"
                + "file_extensions: [foo]\n"
                + "filename: []\n"
                + "name: 'foo'";
    }

    public void testExistingLanguages() throws Exception {
        File languagesDirFile = TestUtils.getLanguagesDir();
        File[] listFiles = languagesDirFile.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if ((name.endsWith(".liclipse") && !name.endsWith(".extend.liclipse"))) {
                    return true;
                }
                return false;
            }
        });
        assertEquals(23, listFiles.length);
        for (File file : listFiles) {
            try {
                LanguageMetadataFileInfo fileInfo = new LanguageMetadataFileInfo(file, (File) null);
                LiClipseLanguage language = fileInfo.loadLanguage(true);
                assertNotNull(language);
            } catch (Exception e) {
                throw new RuntimeException("Error loading: " + file.getName(), e);
            }
        }
    }

    public void testLanguagesManagerTmBundles() throws Exception {
        LanguagesManager manager = new LanguagesManager(TestUtils.getLanguagesDir());
        List<LanguageMetadata> languagesMetadata = manager.getLanguagesMetadata();
        List<String> captions = languagesMetadata.stream().map(u -> u.languageCaption).collect(Collectors.toList());
        List<String> names = languagesMetadata.stream().map(u -> u.languageName).collect(Collectors.toList());
        Collections.sort(captions);
        Collections.sort(names);
        assertEquals(TestUtils.listToExpected(captions), TestUtils.listToExpected("BaseDefinitions",
                "Batch File",
                "CMake Cache",
                "CMake Listfile",
                "CSS",
                "CoffeeScript",
                "Cpp",
                "DXL",
                "Django",
                "Format String",
                "Gemfile",
                "Go",
                "HTML",
                "HTML", // TM Bundle
                "HTML (Derivative)", // TM Bundle
                "HTML (Ruby - ERB)", // TM Bundle
                "Java",
                "JavaScript", // TM Bundle
                "Javascript",
                "Jinja2",
                "Kivy",
                "LiClipse",
                "LiClipse Example",
                "Mako",
                "Markdown",
                "Nim",
                "PHP",
                "Perl",
                "Perl 6",
                "Properties",
                "Python",
                "RAML",
                "ReStructured Text",
                "Regular Expressions (JavaScript)",
                "Regular Expressions (Oniguruma)",
                "Ruby",
                "SCSS",
                "Shell Script (Bash)",
                "StringTemplate",
                "Swift",
                "TypeScript",
                "TypeScriptReact",
                "XML",
                "Xgui20",
                "Yaml",
                "dart",
                "julia"));

        assertEquals(TestUtils.listToExpected(names), TestUtils.listToExpected("BaseDefinitions",
                "CoffeeScript",
                "Cpp",
                "DXL",
                "Django",
                "Go",
                "HTML",
                "Java",
                "Javascript",
                "Jinja2",
                "Kivy",
                "LiClipse",
                "LiClipse Example",
                "Mako",
                "Nim",
                "Python",
                "ReStructured Text",
                "StringTemplate",
                "XML",
                "Xgui20",
                "Yaml",
                "dart",
                "julia",
                "source.cache.cmake",
                "source.cmake",
                "source.css",
                "source.dosbatch",
                "source.js",
                "source.js.regexp",
                "source.perl",
                "source.perl.6",
                "source.raml",
                "source.regexp.oniguruma",
                "source.ruby",
                "source.ruby.gemfile",
                "source.scss",
                "source.shell",
                "source.swift",
                "source.tm-properties",
                "source.ts",
                "source.tsx",
                "text.html.basic",
                "text.html.derivative",
                "text.html.erb",
                "text.html.markdown",
                "text.html.php",
                "textmate.format-string"));
        assertEquals(47, languagesMetadata.size());
        for (LanguageMetadata languageMetadata : languagesMetadata) {
            LiClipseLanguage language = languageMetadata.file.loadLanguage(true);
            assertNotNull(language);
        }

    }
}
