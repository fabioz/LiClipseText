package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.brainwy.liclipsetext.editor.languages.LanguageMetadataTmBundleZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PartitioningJsonTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.configLanguagesManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtils.clearLanguagesManager();
    }

    public void testJson() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document("{\"widget\": {\n"
                + "    \"window\": {\n"
                + "        \"name\": \"main_window\",\n"
                + "        \"width\": 500,\n"
                + "        \"height\": 500\n"
                + "    },\n"
                + "    \"image\": { \n"
                + "        \"src\": \"Images/Sun.png\",\n"
                + "        \"name\": \"sun1\",\n"
                + "        \"hOffset\": 250,\n"
                + "        \"vOffset\": 250,\n"
                + "        \"alignment\": \"center\"\n"
                + "    },\n"
                + "}}   ");

        String partition = connectAndPartition(language, document);
        assertEquals(TestUtils.listToExpected("source.json:0:"), partition);

        List<String> asList = TestUtils.partitionAsList(document);
        asList = Arrays.asList(asList.get(asList.size() - 1));
        String last = TestUtils.scanAll(language, document, asList);
        assertEquals(TestUtils.listToExpected("punctuation.definition.dictionary.begin.json:0:1",
                "punctuation.definition.string.begin.json:1:1",
                "string.quoted.double.json:2:6",
                "punctuation.definition.string.end.json:8:1",
                "punctuation.separator.dictionary.key-value.json:9:1",
                "meta.structure.dictionary.value.json:10:1",
                "punctuation.definition.dictionary.begin.json:11:2",
                "meta.structure.dictionary.json:13:4",
                "punctuation.definition.string.begin.json:17:1",
                "string.quoted.double.json:18:6",
                "punctuation.definition.string.end.json:24:1",
                "punctuation.separator.dictionary.key-value.json:25:1",
                "meta.structure.dictionary.value.json:26:1",
                "punctuation.definition.dictionary.begin.json:27:2",
                "meta.structure.dictionary.json:29:8",
                "punctuation.definition.string.begin.json:37:1",
                "string.quoted.double.json:38:4",
                "punctuation.definition.string.end.json:42:1",
                "punctuation.separator.dictionary.key-value.json:43:1",
                "meta.structure.dictionary.value.json:44:1",
                "punctuation.definition.string.begin.json:45:1",
                "string.quoted.double.json:46:11",
                "punctuation.definition.string.end.json:57:1",
                "punctuation.separator.dictionary.pair.json:58:2",
                "meta.structure.dictionary.json:60:8",
                "punctuation.definition.string.begin.json:68:1",
                "string.quoted.double.json:69:5",
                "punctuation.definition.string.end.json:74:1",
                "punctuation.separator.dictionary.key-value.json:75:1",
                "meta.structure.dictionary.value.json:76:1",
                "constant.numeric.json:77:3",
                "punctuation.separator.dictionary.pair.json:80:2",
                "meta.structure.dictionary.json:82:8",
                "punctuation.definition.string.begin.json:90:1",
                "string.quoted.double.json:91:6",
                "punctuation.definition.string.end.json:97:1",
                "punctuation.separator.dictionary.key-value.json:98:1",
                "meta.structure.dictionary.value.json:99:1",
                "constant.numeric.json:100:4",
                "meta.structure.dictionary.value.json:104:4",
                "punctuation.definition.dictionary.end.json:108:1",
                "punctuation.separator.dictionary.pair.json:109:2",
                "meta.structure.dictionary.json:111:4",
                "punctuation.definition.string.begin.json:115:1",
                "string.quoted.double.json:116:5",
                "punctuation.definition.string.end.json:121:1",
                "punctuation.separator.dictionary.key-value.json:122:1",
                "meta.structure.dictionary.value.json:123:1",
                "punctuation.definition.dictionary.begin.json:124:1",
                "meta.structure.dictionary.json:125:2",
                "meta.structure.dictionary.json:127:8",
                "punctuation.definition.string.begin.json:135:1",
                "string.quoted.double.json:136:3",
                "punctuation.definition.string.end.json:139:1",
                "punctuation.separator.dictionary.key-value.json:140:1",
                "meta.structure.dictionary.value.json:141:1",
                "punctuation.definition.string.begin.json:142:1",
                "string.quoted.double.json:143:14",
                "punctuation.definition.string.end.json:157:1",
                "punctuation.separator.dictionary.pair.json:158:2",
                "meta.structure.dictionary.json:160:8",
                "punctuation.definition.string.begin.json:168:1",
                "string.quoted.double.json:169:4",
                "punctuation.definition.string.end.json:173:1",
                "punctuation.separator.dictionary.key-value.json:174:1",
                "meta.structure.dictionary.value.json:175:1",
                "punctuation.definition.string.begin.json:176:1",
                "string.quoted.double.json:177:4",
                "punctuation.definition.string.end.json:181:1",
                "punctuation.separator.dictionary.pair.json:182:2",
                "meta.structure.dictionary.json:184:8",
                "punctuation.definition.string.begin.json:192:1",
                "string.quoted.double.json:193:7",
                "punctuation.definition.string.end.json:200:1",
                "punctuation.separator.dictionary.key-value.json:201:1",
                "meta.structure.dictionary.value.json:202:1",
                "constant.numeric.json:203:3",
                "punctuation.separator.dictionary.pair.json:206:2",
                "meta.structure.dictionary.json:208:8",
                "punctuation.definition.string.begin.json:216:1",
                "string.quoted.double.json:217:7",
                "punctuation.definition.string.end.json:224:1",
                "punctuation.separator.dictionary.key-value.json:225:1",
                "meta.structure.dictionary.value.json:226:1",
                "constant.numeric.json:227:3",
                "punctuation.separator.dictionary.pair.json:230:2",
                "meta.structure.dictionary.json:232:8",
                "punctuation.definition.string.begin.json:240:1",
                "string.quoted.double.json:241:9",
                "punctuation.definition.string.end.json:250:1",
                "punctuation.separator.dictionary.key-value.json:251:1",
                "meta.structure.dictionary.value.json:252:1",
                "punctuation.definition.string.begin.json:253:1",
                "string.quoted.double.json:254:6",
                "punctuation.definition.string.end.json:260:2",
                "meta.structure.dictionary.value.json:262:4",
                "punctuation.definition.dictionary.end.json:266:1",
                "punctuation.separator.dictionary.pair.json:267:2",
                "punctuation.definition.dictionary.end.json:269:1",
                "punctuation.definition.dictionary.end.json:270:1",
                "source.json:271:3"), last);

    }

    private LiClipseLanguage setupLanguage() throws Exception {
        LanguageMetadataTmBundleZipFileInfo metadata = setup();

        LiClipseLanguage language = metadata.loadLanguage(true);
        return language;
    }

    private String connectAndPartition(LiClipseLanguage language, Document document) throws Exception {
        language.connect(document);

        List<String> asList = TestUtils.partitionAsList(document);
        String partition = TestUtils.listToExpected(asList);
        return partition;
    }

    private LanguageMetadataTmBundleZipFileInfo setup() {
        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "json.tmbundle"),
                "json.tmbundle-master/Syntaxes/JSON.tmLanguage");
        return metadata;
    }

}
