package org.brainwy.liclipsetext.editor.languages;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadata;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataFileInfo;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
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
        assertEquals(22, listFiles.length);
        for (File file : listFiles) {
            try {
                LanguageMetadataFileInfo fileInfo = new LanguageMetadataFileInfo(file);
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
        assertEquals(37, languagesMetadata.size());
        for (LanguageMetadata languageMetadata : languagesMetadata) {
        	LiClipseLanguage language = languageMetadata.file.loadLanguage(true);
        	assertNotNull(language);
		}

    }
}
