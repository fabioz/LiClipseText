package org.brainwy.liclipsetext.editor.languages;

import java.io.InputStream;

import org.brainwy.liclipsetext.editor.languages.LanguageLoaderUew;
import org.brainwy.liclipsetext.shared_core.io.FileUtils;

import junit.framework.TestCase;

public class LanguageLoaderTest extends TestCase {

    public void testLoadLanguage() throws Exception {
        InputStream resourceAsStream = LanguageLoaderTest.class.getClassLoader().getResourceAsStream("yaml.uew");
        String streamContents = FileUtils.getStreamContents(resourceAsStream, "utf-8", null);
        LanguageLoaderUew languageLoaderUew = new LanguageLoaderUew();
        languageLoaderUew.setThrowErrors(true);
        languageLoaderUew.load("yaml.uew", streamContents);

        // File dir = new File("C:/Users/Fabio/Downloads/wordfiles");
        // File[] listFiles = dir.listFiles();
        // for (File file : listFiles) {
        //     if (file.getName().endsWith(".uew")) {
        //         languageLoaderUew = new LanguageLoaderUew();
        //         languageLoaderUew.setThrowErrors(true);
        //         languageLoaderUew.load(file.getName(), FileUtils.getFileContents(file));
        //     }
        // }
    }
}
