package org.brainwy.liclipsetext.editor.common.partitioning;

import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipsePartitionScanner;
import org.brainwy.liclipsetext.editor.common.partitioning.ScopeColorScanning;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;

import junit.framework.TestCase;

public class PartitioningTest extends TestCase {

    public static void main(String[] args) throws Exception {
        PartitioningTest test = new PartitioningTest();
        test.testPartitioningXml();
    }

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

    public void testPartitioningOffPartition() throws Exception {
        String txt = ""
                + "a: (b='c') ->\n"
                + "";

        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = setupOffPartitionTest(document);
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:6",
                "singleQuotedMultiLineString:6:9",
                "__dftl_partition_content_type:9:");

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                IDocument.DEFAULT_CONTENT_TYPE, contentTypeToScanner, partitioningSetup);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, 6);
        String scan = TestUtils.scan(scannerForContentType, range);
        assertEquals(TestUtils.listToExpected("method:0:1",
                "operator:1:1",
                "foreground:2:1",
                "foreground:3:1",
                "foreground:4:1",
                "foreground:5:1"), scan);
    }

    public void testPartitioningOffPartition2() throws Exception {
        String txt = ""
                + "a: (b='c') \n"
                + "";

        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = setupOffPartitionTest(document);
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:6",
                "singleQuotedMultiLineString:6:9",
                "__dftl_partition_content_type:9:");

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                IDocument.DEFAULT_CONTENT_TYPE, contentTypeToScanner, partitioningSetup);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, 6);
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("foreground:0:1",
                "foreground:1:1",
                "null:2:1",
                "foreground:3:1",
                "foreground:4:1",
                "foreground:5:1"), scan);
    }

    private LiClipseLanguage setupOffPartitionTest(IDocument document) throws Exception {
        String contents = ""
                + "scope_to_color_name: {\n"
                + "singleQuotedMultiLineString: singleQuotedMultiLineString, doubleQuotedMultiLineString: doubleQuotedMultiLineString}\n"
                + "scope_definition_rules:\n"
                + "  - {type: MultiLineRule, scope: singleQuotedMultiLineString, start: \"'\", end: \"'\", escapeCharacter: \\}\n"
                + "  - {type: MultiLineRule, scope: doubleQuotedMultiLineString, start: '\"', end: '\"', escapeCharacter: \\}\n"
                + "scope:\n"
                + "  default:\n"
                + "    sub_rules: [\n"
                + "      {type: CompositeRule, sub_rules: [\n"
                + "        { type: OptionalSequenceRule, scope: field, sequence: '@' },\n"
                + "        { type: AnyWordRule, scope: method },\n"
                + "        { type: ZeroOrMoreSpacesRule, scope: default},\n"
                + "        { type: SequencesRule, scope: operator, sequences: ['=', ':']},\n"
                + "        { type: ZeroOrMoreSpacesRule, scope: default},\n"
                + "        \n"
                + "        \n"
                + "        { type: OptionalMultiLineRule, scope: OFF_PARTITION, start: '(', end: ')', escapeCharacter: \\0},\n"
                + "        { type: ZeroOrMoreSpacesRule, scope: OFF_PARTITION},\n"
                + "        { type: SequencesRule, scope: OFF_PARTITION, sequences: ['=>', '->']},\n"
                + "      ]},\n"
                + "    ]\n"
                + "\n"
                + "file_extensions: []\n"
                + "filename: []\n"
                + "name: Dummy\n"
                + "\n"
                + "";
        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFromContents(contents);
        partitioningSetup.connect(document);
        return partitioningSetup;
    }

    public void testPartitioning() throws Exception {
        String txt = ""
                + "/*test*/\n"
                + "var a = 10; //comment"
                + "";

        IDocument document = new Document(txt);
        LiClipseLanguage partitioningSetup = new LiClipseLanguage();
        partitioningSetup.connect(document);
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:");

        partitioningSetup.setupLineComment("//");
        partitioningSetup.connect(document);
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:21",
                "singleLineComment:21:");

        partitioningSetup.setupBlockComment("/*", "*/", '\0');

        partitioningSetup.connect(document);
        TestUtils.checkPartitions(document, "multiLineComment:0:8", "__dftl_partition_content_type:8:21",
                "singleLineComment:21:");
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        assertNotNull(documentPartitioner);
    }

    public void testPartitioningXml() throws Exception {
        String txt = ""
                + "<a href=\"test\"></a>\n"
                + "<a href=\"test\"></a>\n"
                + "";

        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("xml.liclipse");
        partitioningSetup.connect(document);
        TestUtils.checkPartitions(document, "tag:0:19",
                "__dftl_partition_content_type:19:20",
                "tag:20:39",
                "__dftl_partition_content_type:39:");
    }

    public void testPartitioningDjango() throws Exception {
        String txt = ""
                + "<a>{{ filter }}</a>\n"
                + "{% tag %} <br/>"
                + "";

        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("django.liclipse");
        partitioningSetup.connect(document);
        TestUtils.checkPartitions(document, "tag:0:3",
                "django_filter:3:15",
                "tag:15:19",
                "__dftl_partition_content_type:19:20",
                "django_tag:20:29",
                "__dftl_partition_content_type:29:30",
                "tag:30:");
    }

    public void testPartitioningPython() throws Exception {
        String txt = ""
                + "'\\\n"
                + "\\\n"
                + "a' b '\\\n"
                + "c\\\n"
                + "e' f'g' ''' h ''' "
                + "";

        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("python.liclipse");
        partitioningSetup.connect(document);

        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        String scan = TestUtils.scan(partitioner.getScanner(), document);
        assertEquals(TestUtils.listToExpected("singleQuotedString:0:7",
                "null:7:3",
                "singleQuotedString:10:8",
                "null:18:2",
                "singleQuotedString:20:3",
                "null:23:1",
                "singleQuotedMultiLineString:24:9",
                "null:33:1"), scan);

        TestUtils.checkPartitions(document, "singleQuotedString:0:7",
                "__dftl_partition_content_type:7:10",
                "singleQuotedString:10:18",
                "__dftl_partition_content_type:18:20",
                "singleQuotedString:20:23",
                "__dftl_partition_content_type:23:24",
                "singleQuotedMultiLineString:24:33",
                "__dftl_partition_content_type:33:");

        document.replace(txt.length() - " ''' ".length(), 0, "i");
        TestUtils.checkPartitions(document, "singleQuotedString:0:7",
                "__dftl_partition_content_type:7:10",
                "singleQuotedString:10:18",
                "__dftl_partition_content_type:18:20",
                "singleQuotedString:20:23",
                "__dftl_partition_content_type:23:24",
                "singleQuotedMultiLineString:24:34",
                "__dftl_partition_content_type:34:");

        document.replace(txt.length() - " ''' ".length() + 1, 0, "j");
        TestUtils.checkPartitions(document, "singleQuotedString:0:7",
                "__dftl_partition_content_type:7:10",
                "singleQuotedString:10:18",
                "__dftl_partition_content_type:18:20",
                "singleQuotedString:20:23",
                "__dftl_partition_content_type:23:24",
                "singleQuotedMultiLineString:24:35",
                "__dftl_partition_content_type:35:");

    }

    public void testColoringXml() throws Exception {
        LiClipsePartitionScanner scanner = createXmlScanner();

        String txt = "<a href=\"test\"></a>";

        Document document = new Document(txt);
        TestUtils.checkScan(document, scanner, "open_tag:0:1",
                "class:1:1",
                "null:2:1",
                "keyword:3:4",
                "operator:7:1",
                "doubleQuotedString:8:6",
                "bracket:14:1",
                "close_tag:15:2",
                "close_class:17:1",
                "bracket:18:1");
    }

    public void testColoringXml2() throws Exception {
        LiClipsePartitionScanner scanner = createXmlScanner();

        String txt = "<a href='test\\'></a>\n";

        Document document = new Document(txt);
        TestUtils.checkScan(document, scanner, "open_tag:0:1",
                "class:1:1",
                "null:2:1",
                "keyword:3:4",
                "operator:7:1",
                "singleQuotedString:8:7",
                "bracket:15:1",
                "close_tag:16:2",
                "close_class:18:1",
                "bracket:19:1",
                "null:20:1");
    }

    public void testColoringPython() throws Exception {
        LiClipsePartitionScanner scanner = TestUtils.createScanner("python.liclipse");

        String txt = "def m1";

        Document document = new Document(txt);
        TestUtils.checkScan(document, scanner, "keyword:0:3",
                "foreground:3:1",
                "method:4:2");
    }

    public void testColoringPythonNumber() throws Exception {
        LiClipsePartitionScanner scanner = TestUtils.createScanner("python.liclipse");

        String txt = "a = 10e10";

        Document document = new Document(txt);
        TestUtils.checkScan(document, scanner, "null:0:2",
                "operator:2:1",
                "null:3:1",
                "number:4:5");
    }

    public void testLiClipsePartitioning() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("liclipse.liclipse");
        Document document = new Document(""
                + "a: [ 'a' ]\n");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:5",
                "singleQuotedString:5:8",
                "__dftl_partition_content_type:8:"), partition);
    }

    public void testLiClipseColoringScannerWithChanges() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("liclipse.liclipse");
        Document document = new Document(""
                + "filename: []\n"
                + "#comment\n");
        language.connect(document);
        document.computePartitioning(0, document.getLength());
        document.replace(11, 1, "");
        document.computePartitioning(IDocumentExtension3.DEFAULT_PARTITIONING, 0, 11, false);
        document.replace(11, 0, "s");
        document.computePartitioning(IDocumentExtension3.DEFAULT_PARTITIONING, 0, document.getLength(), false);
    }

    public LiClipsePartitionScanner createXmlScanner() throws Exception {
        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile("xml.liclipse");
        ScopeColorScanning scopeColoringScanning = partitioningSetup.scopeToScopeColorScanning.get("tag");
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner(scopeColoringScanning, partitioningSetup);
        return scanner;
    }

    //Construct from RenPy.
    private LiClipseLanguage setupPartitioningMatchStartWithSpaces(IDocument document) throws Exception {
        String contents = ""
                + "scope_to_color_name: {\n"
                + "}\n"
                + "scope_definition_rules:\n"
                + "  - {type: EndOfLineRule, scope: singleLineComment, start: '#'}\n"
                + "\n"
                + "scope:\n"
                + "  default:\n"
                + "    sub_rules: [\n"
                + "      {type: CompositeRule, sub_rules: [\n"
                + "        { type: MatchLineStartRule, scope: default},\n"
                + "        { type: ZeroOrMoreSpacesRule, scope: default},\n"
                + "        { type: SequenceRule, scope: keyword, sequence: 'def'},\n"
                + "        { type: OneOrMoreSpacesRule, scope: default},\n"
                + "        { type: AnyWordRule, scope: method }]\n"
                + "      },\n"
                + "    ]\n"
                + "\n"
                + "file_extensions: []\n"
                + "name: Dummy\n"
                + "\n"
                + "";
        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFromContents(contents);
        partitioningSetup.connect(document);
        return partitioningSetup;
    }

    public void testPartitioningMatchStartWithSpaces() throws Exception {
        String txt = ""
                + "def bar:\n"
                + "  def foo:\r\n"
                + "    def foo:\r"
                + "    \r"
                + "  \r\n\r\n\n  "
                + "";

        IDocument document = new Document(txt);

        LiClipseLanguage partitioningSetup = setupPartitioningMatchStartWithSpaces(document);
        TestUtils.checkPartitions(document, "__dftl_partition_content_type:0:");

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                IDocument.DEFAULT_CONTENT_TYPE, contentTypeToScanner, partitioningSetup);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("keyword:0:3", //def
                "foreground:3:1",
                "method:4:3",
                "foreground:7:1",
                "null:8:1",
                "foreground:9:2",
                "keyword:11:3", //def
                "foreground:14:1",
                "method:15:3",
                "foreground:18:1",
                "null:19:2", // \r\n
                "foreground:21:4",
                "keyword:25:3", //def
                "foreground:28:1",
                "method:29:3",
                "foreground:32:1",
                "null:33:1", // \r
                "null:34:4", // spaces (4)
                "null:38:1", //\r
                "null:39:2", //spaces (2)
                "null:41:5", //new lines
                "null:46:2" //spaces (2)
        ), scan);
    }

}
