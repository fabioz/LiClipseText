package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PartitioningRubyTest extends TestCase {

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

    public void testRubyPartitioning() throws Exception {
        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"), "ruby.tmbundle-master/Syntaxes/Ruby.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);
        Document document = new Document(""
                + ""
                + "class Name\n"
                + "  # comment\n"
                + "end");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("meta.class.ruby:0:10",
                "__dftl_partition_content_type:10:11",
                "source.ruby:11:24",
                "__dftl_partition_content_type:24:"), partition.replaceFirst("source\\.ruby\\.(\\d+)", "source.ruby"));
    }

    public void testRubyPartitioning2() throws Exception {
        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"), "ruby.tmbundle-master/Syntaxes/Ruby.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);

        Document document = new Document(""
                + "def initialize(a)\n" +
                "end\n" +
                "\n" +
                "cust1=Customer.new()\n" +
                "");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("meta.function.method.with-arguments.ruby:0:17",
                "__dftl_partition_content_type:17:18",
                "keyword.control.ruby:18:21",
                "__dftl_partition_content_type:21:28",
                "keyword.operator.assignment.ruby:28:29",
                "support.class.ruby:29:37",
                "punctuation.separator.method.ruby:37:38",
                "keyword.other.special-method.ruby:38:41",
                "punctuation.section.function.ruby:41:43",
                "__dftl_partition_content_type:43:"), partition);
    }

    public void testRubyPartitioning3() throws Exception {
        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"), "ruby.tmbundle-master/Syntaxes/Ruby.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);

        Document document = new Document(""
                + "def initialize(a)\n" +
                "end\n" +
                "\n" +
                "cust1=Customer.new(a, b)\n" +
                "\n" +
                "def test(a=1,b=2,*c)\n" +
                "  puts \"#{a},#{b}\"\n" +
                "  c.each{|x| print \" #{x}, \"}  # We will learn about \"each\" very soon.  I promise.\n" +
                "end\n" +
                "test 3, 6, 9, 12, 15, 18\n" +
                "\n" +
                "\n" +
                "nthesonth uonth uasonth ");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("meta.function.method.with-arguments.ruby:0:17",
                "__dftl_partition_content_type:17:18",
                "keyword.control.ruby:18:21",
                "__dftl_partition_content_type:21:28",
                "keyword.operator.assignment.ruby:28:29",
                "support.class.ruby:29:37",
                "punctuation.separator.method.ruby:37:38",
                "keyword.other.special-method.ruby:38:41",
                "punctuation.section.function.ruby:41:42",
                "__dftl_partition_content_type:42:43",
                "punctuation.separator.object.ruby:43:44",
                "__dftl_partition_content_type:44:46",
                "punctuation.section.function.ruby:46:47",
                "__dftl_partition_content_type:47:49",
                "meta.function.method.with-arguments.ruby:49:69",
                "__dftl_partition_content_type:69:77",
                "string.quoted.double.ruby:77:88",
                "__dftl_partition_content_type:88:92",
                "punctuation.separator.method.ruby:92:93",
                "__dftl_partition_content_type:93:97",
                "punctuation.section.scope.begin.ruby:97:98",
                "source.ruby.23:98:101",
                "__dftl_partition_content_type:101:108",
                "string.quoted.double.ruby:108:117",
                "punctuation.section.scope.end.ruby:117:118",
                "__dftl_partition_content_type:118:120",
                "source.ruby.20:120:173",
                "__dftl_partition_content_type:173:181",
                "constant.numeric.integer.ruby:181:182",
                "punctuation.separator.object.ruby:182:183",
                "__dftl_partition_content_type:183:184",
                "constant.numeric.integer.ruby:184:185",
                "punctuation.separator.object.ruby:185:186",
                "__dftl_partition_content_type:186:187",
                "constant.numeric.integer.ruby:187:188",
                "punctuation.separator.object.ruby:188:189",
                "__dftl_partition_content_type:189:190",
                "constant.numeric.integer.ruby:190:192",
                "punctuation.separator.object.ruby:192:193",
                "__dftl_partition_content_type:193:194",
                "constant.numeric.integer.ruby:194:196",
                "punctuation.separator.object.ruby:196:197",
                "__dftl_partition_content_type:197:198",
                "constant.numeric.integer.ruby:198:200",
                "__dftl_partition_content_type:200:"), partition);
    }

    public void testRubyPartitioning4() throws Exception {
        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"), "ruby.tmbundle-master/Syntaxes/Ruby.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);

        Document document = new Document("# class ArticlesController < ApplicationController\n" +
                "#   def new\n" +
                "#   end\n" +
                "#\n" +
                "#   def create\n" +
                "#   end\n" +
                "# end\n" +
                "\n" +
                "class ArticlesController < ApplicationController\n" +
                "  def new\n" +
                "  end\n" +
                "\n" +
                "  def create\n" +
                "  end\n" +
                "end\n" +
                "class ArticlesController < ApplicationController\n" +
                "  def new\n" +
                "  end\n" +
                "\n" +
                "  def create\n" +
                "  end\n" +
                "end\n" +
                "");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("source.ruby.20:0:103",
                "meta.class.ruby:103:151",
                "__dftl_partition_content_type:151:154",
                "meta.function.method.without-arguments.ruby:154:161",
                "__dftl_partition_content_type:161:164",
                "keyword.control.ruby:164:167",
                "__dftl_partition_content_type:167:171",
                "meta.function.method.without-arguments.ruby:171:181",
                "__dftl_partition_content_type:181:184",
                "keyword.control.ruby:184:187",
                "__dftl_partition_content_type:187:188",
                "keyword.control.ruby:188:191",
                "__dftl_partition_content_type:191:192",
                "meta.class.ruby:192:240",
                "__dftl_partition_content_type:240:243",
                "meta.function.method.without-arguments.ruby:243:250",
                "__dftl_partition_content_type:250:253",
                "keyword.control.ruby:253:256",
                "__dftl_partition_content_type:256:260",
                "meta.function.method.without-arguments.ruby:260:270",
                "__dftl_partition_content_type:270:273",
                "keyword.control.ruby:273:276",
                "__dftl_partition_content_type:276:277",
                "keyword.control.ruby:277:280",
                "__dftl_partition_content_type:280:"), partition);

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                "meta.class.ruby", contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 103, 151 - 103);
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("keyword.control.class.ruby:103:5",
                "meta.class.ruby:108:1",
                "entity.name.type.class.ruby:109:18",
                "entity.other.inherited-class.ruby:127:1",
                "punctuation.separator.inheritance.ruby:128:1",
                "entity.other.inherited-class.ruby:129:22"), scan);

    }

    public void testRubyPartitioning5() throws Exception {
        LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"), "ruby.tmbundle-master/Syntaxes/Ruby.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);

        Document document = new Document(""
                + "puts \"a#{'b'}c\"" +
                "");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:5",
                "string.quoted.double.ruby:5:"), partition);

        Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
        LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                .getDocumentPartitioner();
        ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                "string.quoted.double.ruby", contentTypeToScanner, language);
        ScannerRange range = scannerForContentType.createScannerRange(document, 5, document.getLength() - 5);
        String scan = TestUtils.scan(scannerForContentType, range, false);
        assertEquals(TestUtils.listToExpected("punctuation.definition.string.begin.ruby:5:1",
                "string.quoted.double.ruby:6:1",
                "punctuation.section.embedded.begin.ruby:7:2",
                "punctuation.definition.string.begin.ruby:9:1",
                "string.quoted.single.ruby:10:1",
                "punctuation.definition.string.end.ruby:11:1",
                "source.ruby:12:1",
                "string.quoted.double.ruby:13:1",
                "punctuation.definition.string.end.ruby:14:1"), scan);

    }
    
    public void testRubyPartitioning6() throws Exception {
    	LanguageMetadataZipFileInfo metadata = new LanguageMetadataZipFileInfo(
    			new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"), "ruby.tmbundle-master/Syntaxes/Ruby.plist");
    	
    	LiClipseLanguage language = metadata.loadLanguage(true);
    	
    	Document document = new Document(""
    			+ "\"a#{'b'}c\"" +
    			"");
    	language.connect(document);
    	String partition = TestUtils.partition(document);
    	assertEquals(TestUtils.listToExpected("string.quoted.double.ruby:0:"), partition);
    	
    	Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
    	LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
    			.getDocumentPartitioner();
    	ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
    			"string.quoted.double.ruby", contentTypeToScanner, language);
    	ScannerRange range = scannerForContentType.createScannerRange(document, 0, document.getLength());
    	String scan = TestUtils.scan(scannerForContentType, range, false);
    	assertEquals(TestUtils.listToExpected("punctuation.definition.string.begin.ruby:0:1",
    			"string.quoted.double.ruby:1:1",
    			"punctuation.section.embedded.begin.ruby:2:2",
    			"punctuation.definition.string.begin.ruby:4:1",
    			"string.quoted.single.ruby:5:1",
    			"punctuation.definition.string.end.ruby:6:1",
    			"source.ruby:7:1",
    			"string.quoted.double.ruby:8:1",
    			"punctuation.definition.string.end.ruby:9:1"), scan);
    	
    }

}
