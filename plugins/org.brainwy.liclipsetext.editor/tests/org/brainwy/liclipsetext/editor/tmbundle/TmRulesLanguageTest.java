package org.brainwy.liclipsetext.editor.tmbundle;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.ScopeSelector;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class TmRulesLanguageTest extends TestCase {

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

    public void testLiClipseTmRulesPartitioning() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test_tm_rules.liclipse");
        String string = ""
                + "In [20]: foo\n"
                + "In [20]: foo\n"
                + "";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("support.ipython.in.python:0:8",
                "__dftl_partition_content_type:8:13",
                "support.ipython.in.python:13:21",
                "__dftl_partition_content_type:21:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test_tm_rules.liclipse",
                "support.ipython.in.python");

        TestUtils.checkScan(new Document(string), scanner, "support.ipython.in.python:0:4",
                "support.ipython.cell-number.python:4:2",
                "support.ipython.in.python:6:2",
                "null:8:5",
                "support.ipython.in.python:13:4",
                "support.ipython.cell-number.python:17:2",
                "support.ipython.in.python:19:2",
                "null:21:5");
    }

    public void testLiClipseTmRulesPartitioning3() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test_tm_rules.liclipse");
        String string = ""
                + "In [20]: rara\r\n"
                + "In [30]: rara\r\n"
                + "In [40]: rara\r\n"
                + "";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("support.ipython.in.python:0:8",
                "__dftl_partition_content_type:8:15",
                "support.ipython.in.python:15:23",
                "__dftl_partition_content_type:23:30",
                "support.ipython.in.python:30:38",
                "__dftl_partition_content_type:38:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test_tm_rules.liclipse",
                "support.ipython.in.python");

        TestUtils.checkScan(new Document(string), scanner, "support.ipython.in.python:0:4",
                "support.ipython.cell-number.python:4:2",
                "support.ipython.in.python:6:2",
                "null:8:7",
                "support.ipython.in.python:15:4",
                "support.ipython.cell-number.python:19:2",
                "support.ipython.in.python:21:2",
                "null:23:7",
                "support.ipython.in.python:30:4",
                "support.ipython.cell-number.python:34:2",
                "support.ipython.in.python:36:2",
                "null:38:7");
    }

    public void testLiClipseTmRulesPartitioning2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test_tm_rules3.liclipse");
        String string = "class MyClass:\n  pass";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("meta.class.old-style.python:0:14",
                "__dftl_partition_content_type:14:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test_tm_rules3.liclipse",
                "meta.class.old-style.python");

        TestUtils.checkScan(new Document(string), scanner, "storage.type.class.python:0:5",
                "meta.class.old-style.python:5:1",
                "meta.class.old-style.python:6:7",
                "punctuation.section.class.begin.python:13:1",
                "null:14:7");
    }

    public void testLiClipseTmRulesPartitioning2a() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test_tm_rules3.liclipse");
        String string = "  class MyClass:\n  pass";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:2",
                "meta.class.old-style.python:2:16",
                "__dftl_partition_content_type:16:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test_tm_rules3.liclipse",
                "meta.class.old-style.python");

        TestUtils.checkScan(new Document(string), scanner, "null:0:2",
                "storage.type.class.python:2:5",
                "meta.class.old-style.python:7:1",
                "meta.class.old-style.python:8:7",
                "punctuation.section.class.begin.python:15:1",
                "null:16:7");
    }

    public void testLiClipseTmRulesPartitioning4() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test_tm_rules.liclipse");
        String string = " <<'FOO'\nbar\ntxt\nFOO\n";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:1",
                "string.unquoted:1:20",
                "__dftl_partition_content_type:20:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test_tm_rules.liclipse",
                "string.unquoted");

        TestUtils.checkScan(new Document(string.substring(1)), scanner, "string.unquoted:0:7",
                "string.unquoted.scope:7:9",
                "string.unquoted:16:3",
                "null:19:1");
    }

    public void testLiClipseTmRulesPartitioning5() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("language_test_tm_rules2.liclipse");
        String string = "anchor test end test";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("invalid.error.test:0:6",
                "invalid.error.test2:6:11",
                "__dftl_partition_content_type:11:"), partition);

        LiClipsePartitionScanner scanner = TestUtils.createScanner("language_test_tm_rules.liclipse",
                "invalid.error.test");

        TestUtils.checkScan(new Document(string.substring(1)), scanner, "null:0:19");
    }

    public void testLiClipseTmRulesPartitioning6() throws Exception {
        //Test injections
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        LiClipseLanguage language = languagesManager.getLanguageFromName("source.testing");
        LiClipseLanguage language2 = languagesManager.getLanguageFromName("source.testing2");
        List<ScopeSelector> injectionRules = language2.injectionRules;
        assertEquals(1, injectionRules.size());
        assertNotNull(language);
        assertNotNull(language2);
        String string = "aa bb cc";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("constant.a:0:2",
                "__dftl_partition_content_type:2:3",
                "source.testing.include:3:5",
                "__dftl_partition_content_type:5:"), partition);

        ScopeColorScanning scopeColoringScanning = language.scopeToScopeColorScanning.get("source.testing.include");
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner(scopeColoringScanning, language);

        TestUtils.checkScan(new Document(string), scanner, "null:0:3",
                "constant.b:3:1",
                "constant.b:4:1",
                "null:5:3");

    }

    public void testLiClipseTmRulesPartitioning6a() throws Exception {
        //Test injections
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        LiClipseLanguage language = languagesManager.getLanguageFromName("source.testing2");
        List<ScopeSelector> injectionRules = language.injectionRules;
        assertEquals(1, injectionRules.size());
        assertNotNull(language);
        String string = "aa bb cc";
        Document document = new Document(string);
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:3",
                "constant.b:3:5",
                "__dftl_partition_content_type:5:6",
                "constant.c:6:"), partition);
    }

    public void testLiClipseTmRulesPartitioning6b() throws Exception {
        //Test injections
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        LiClipseLanguage language = languagesManager.getLanguageFromName("text.html.erb");
        List<ScopeSelector> injectionRules = language.injectionRules;
        assertEquals(1, injectionRules.size());
        ScopeSelector scopeSelector = injectionRules.get(0);
        assertEquals(scopeSelector.getScopeStr(),
                "text.html.erb - (meta.embedded.block.erb | meta.embedded.line.erb | comment)");
        assertNotNull(language);
        String string = "<html>\n" +
                "<%= @name %>\n" +
                "</html>";
        Document document = new Document(string);
        language.connect(document);
        List<String> partition = TestUtils.partitionAsList(document);
        List<String> newPartition = fixList(partition);
        if (newPartition.equals(partition)) {
            throw new AssertionError("Expected the includes to be renumbered.");
        }
        assertEquals(TestUtils.listToExpected(newPartition),
                TestUtils.listToExpected("text.html.erb.include:0:6",
                        "__dftl_partition_content_type:6:7",
                        "text.html.erb.include:7:19",
                        "__dftl_partition_content_type:19:20",
                        "text.html.erb.include:20:"));

        String key = partition.get(2);
        key = key.substring(0, key.indexOf(":"));
        ScopeColorScanning scopeColoringScanning = language.scopeToScopeColorScanning.get(key);
        assertNotNull("Unable to find scanning for: " + key, scopeColoringScanning);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner(scopeColoringScanning, language);

        TestUtils.checkScan(new Document(string.substring(7, 19)), scanner,
                "punctuation.section.embedded.begin.erb:0:3",
                "source.ruby:3:1",
                "punctuation.definition.variable.ruby:4:1",
                "variable.other.readwrite.instance.ruby:5:4",
                "source.ruby:9:1",
                "source.ruby:10:1",
                "punctuation.section.embedded.end.erb:11:1");

        key = partition.get(0);
        key = key.substring(0, key.indexOf(":"));
        scopeColoringScanning = language.scopeToScopeColorScanning.get(key);
        assertNotNull("Unable to find scanning for: " + key, scopeColoringScanning);
        scanner = new LiClipsePartitionScanner(scopeColoringScanning, language);

        TestUtils.checkScan(new Document(string.substring(0, 6)), scanner, "punctuation.definition.tag.html:0:1",
                "entity.name.tag.structure.any.html:1:4",
                "punctuation.definition.tag.html:5:1");

    }

    public void testLiClipseTmRulesPartitioningBeginWhile() throws Exception {
        //Test begin/while rules
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        LiClipseLanguage language = languagesManager.getLanguageFromName("source.beginwhile");
        String string = ">some\n"
                + "> what\n"
                + "> another\n"
                + "stop";
        Document document = new Document(string);
        language.connect(document);
        List<String> partition = TestUtils.partitionAsList(document);
        assertEquals(TestUtils.listToExpected(partition),
                TestUtils.listToExpected("constant.character:0:23",
                        "__dftl_partition_content_type:23:"));

        ScopeColorScanning scopeColoringScanning = language.scopeToScopeColorScanning.get("constant.character");
        assertNotNull("Unable to find scanning for: constant.character", scopeColoringScanning);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner(scopeColoringScanning, language);

        TestUtils.checkScan(new Document(string.substring(0, 23)), scanner, "punctuation.definition.quote.markdown:0:1",
                "constant.character:1:5",
                "punctuation.definition.quote.markdown:6:1",
                "constant.character:7:3",
                "comment.a:10:1",
                "constant.character:11:2",
                "punctuation.definition.quote.markdown:13:1",
                "constant.character:14:1",
                "comment.a:15:1",
                "constant.character:16:7");
    }

    private List<String> fixList(List<String> lst) {
        ArrayList<String> ret = new ArrayList<>();
        for (String string : lst) {
            String newString = string.replaceFirst("\\.include\\.(\\d+)", ".include"); //Pattern.compile
            ret.add(newString);
        }
        return ret;
    }
}
