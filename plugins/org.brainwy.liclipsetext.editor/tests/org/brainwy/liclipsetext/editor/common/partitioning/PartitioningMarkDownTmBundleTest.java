package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PartitioningMarkDownTmBundleTest extends TestCase {

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

    public void testMarkDownPartitioningHeader() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document(""
                + "head\n"
                + "-----");
        String partition = connectAndPartition(language, document);
        assertEquals(TestUtils.listToExpected("text.html.markdown.include:0:"), partition);

        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                "text.html.markdown.include", contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("meta.paragraph.markdown:0:5",
                "markup.heading.setext.2.markdown:5:5"), scan);
    }

    public void testMarkDownPartitioningHeader0a() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document(""
                + "head\r\n"
                + "-----\r\n"
                + "bar");
        String partition = connectAndPartition(language, document);
        assertEquals(TestUtils.listToExpected("text.html.markdown.include:0:"), partition);

        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                "text.html.markdown.include", contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("meta.paragraph.markdown:0:6",
                "markup.heading.setext.2.markdown:6:7",
                "meta.paragraph.markdown:13:3"), scan);
    }

    /**
     * http://blog.macromates.com/2011/format-strings/
     *
     * The Case for Replacements
     *
     * Another form of Format Strings allows you to change the case of a variable reference.
     * If you combine that with the fact that scope name elements in TextMate 2's Language
     * Grammars are now Format Strings, this allows for nice dynamic scoping tricks.
     *
     * For example, say we rework Ruby's "here-document" (heredoc) syntax rule to look
     * something like this (simplified) example:
     *
     * begin = '<<-?(\w+)';
     * name  = 'string.unquoted.${1:/downcase}.ruby';
     * ...
     * That would mean that a heredoc definition like this:
     *
     * <<-CSS
     * body {
     * margin: 10px auto;
     * }
     * CSS
     *
     * would be scoped as meta.here-doc.css.ruby. This allows the injection of the CSS
     * Grammar and automations into the scope meta.here-doc.css. That would mean that
     * you get the proper syntax highlighting of these heredocs and you could trigger
     * CSS Bundle commands while editing them.
     *
     * As I write this, the ruby grammar still has a dozen rules with a dozen different
     * tokens that include a dozen different languages, so the above is a planned
     * simplification that would allow us to avoid having to upgrade the ruby grammar
     * for new potential here-doc tokens.
     *
     * The supported case transformations are /upcase, /downcase, and /capitalize.
     * There's also a special /asciify transformation that will transliterate things
     * like "æ" and "ø" into "ae" and "o" respectively as well as remove any remaining
     * non-ASCII characters. The latter is particularly useful when deriving an
     * identifier or label, that needs to be accepted by a language parser,
     * from a heading or comment, which is free-form prose.
     * These transformations can be combined.
     *
     * @throws Exception
     */
    public void testMarkDownPartitioningNumberHeaders() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document(""
                + "# head 1\n"
                + "## head 2\n");
        String partition = connectAndPartition(language, document);
        assertEquals(TestUtils.listToExpected("text.html.markdown.include:0:"), partition);

        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                "text.html.markdown.include", contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("punctuation.definition.heading.markdown:0:1",
        		"markup.heading.${1/(#)(#)?(#)?(#)?(#)?(#)?/${6:?6:${5:?5:${4:?4:${3:?3:${2:?2:1}}}}}/}.markdown:1:1",
        		"entity.name.section.markdown:2:7",
        		"punctuation.definition.heading.markdown:9:2",
        		"markup.heading.${1/(#)(#)?(#)?(#)?(#)?(#)?/${6:?6:${5:?5:${4:?4:${3:?3:${2:?2:1}}}}}/}.markdown:11:1",
        		"entity.name.section.markdown:12:7"),
                scan);

// tm4e currently doesn't resolve variables... (see: org.brainwy.liclipsetext.editor.regexp.TmFormatStringTest)
//        assertEquals(TestUtils.listToExpected("punctuation.definition.heading.markdown:0:1",
//        		"markup.heading.1.markdown:1:1",
//        		"entity.name.section.markdown:2:6",
//        		"markup.heading..markdown:8:1",
//        		"punctuation.definition.heading.markdown:9:2",
//        		"markup.heading.2.markdown:11:1",
//        		"entity.name.section.markdown:12:6",
//        		"markup.heading..markdown:18:1"),
//        		scan);

        // See test: TmFormatStringTest
    }

    private LiClipseLanguage setupLanguage() throws Exception {
        LanguageMetadataZipFileInfo metadata = setup();

        LiClipseLanguage language = metadata.loadLanguage(true);
        return language;
    }

    public void testMarkDownPartitioningItalic() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document(""
                + "a *it* a");
        String partition = connectAndPartition(language, document);
        assertEquals(TestUtils.listToExpected("text.html.markdown.include:0:"), partition);

        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                "text.html.markdown.include", contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("meta.paragraph.markdown:0:2",
                "punctuation.definition.italic.markdown:2:1",
                "markup.italic.markdown:3:2",
                "punctuation.definition.italic.markdown:5:1",
                "meta.paragraph.markdown:6:2"), scan);

    }

    private String connectAndPartition(LiClipseLanguage language, Document document) throws Exception {
        language.connect(document);

        List<String> asList = TestUtils.partitionAsList(document);
        String partition = TestUtils.listToExpected(asList);
        return partition;
    }

    private LanguageMetadataZipFileInfo setup() {
        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "markdown.tmbundle"),
                "markdown.tmbundle-master/Syntaxes/Markdown.tmLanguage");
        return metadata;
    }

    public void testMarkDownPartitioning() throws Exception {
        LiClipseLanguage language = setupLanguage();
        Document document = new Document(""
                + "Header 1\n" +
                "=========\n" +
                "\n" +
                "Header 2\n" +
                "---------\n" +
                "\n" +
                "Foo\n" +
                "\n" +
                "### Header 3\n" +
                "\n" +
                "> This \n" +
                "> \n" +
                "> is a blockqote\n");
        language.connect(document);

        List<String> asList = TestUtils.partitionAsList(document);
        String partition = TestUtils.listToExpected(asList);
        assertEquals(TestUtils.listToExpected("text.html.markdown.include:0:19",
                "__dftl_partition_content_type:19:20",
                "text.html.markdown.include:20:39",
                "__dftl_partition_content_type:39:40",
                "text.html.markdown.include:40:44",
                "__dftl_partition_content_type:44:45",
                "text.html.markdown.include:45:58",
                "__dftl_partition_content_type:58:59",
                "text.html.markdown.include:59:"), partition);

        asList = Arrays.asList(asList.get(asList.size() - 1));
        String last = TestUtils.scanAll(language, document, asList);

        assertEquals(TestUtils.listToExpected("punctuation.definition.quote.markdown:59:1",
                "markup.quote.markdown:60:1",
                "meta.paragraph.markdown:61:6",
                "punctuation.definition.quote.markdown:67:1",
                "markup.quote.markdown:68:2",
                "punctuation.definition.quote.markdown:70:1",
                "markup.quote.markdown:71:1",
                "meta.paragraph.markdown:72:15"), last);
    }

}
