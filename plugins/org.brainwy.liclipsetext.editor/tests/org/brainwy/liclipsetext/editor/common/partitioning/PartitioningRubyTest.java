package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;
import java.util.Arrays;

import org.brainwy.liclipsetext.editor.languages.LanguageMetadataTmBundleZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
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
        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"), "ruby.tmbundle-master/Syntaxes/Ruby.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);
        Document document = new Document(""
                + ""
                + "class Name\n"
                + "  # comment\n"
                + "end");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("source.ruby:0:"), partition);

        assertEquals(TestUtils.listToExpected("keyword.control.class.ruby:0:5",
                "meta.class.ruby:5:1",
                "entity.name.type.class.ruby:6:5",
                "punctuation.whitespace.comment.leading.ruby:11:2",
                "punctuation.definition.comment.ruby:13:1",
                "comment.line.number-sign.ruby:14:9",
                "keyword.control.ruby:23:3"), TestUtils.scanAll(language, document, Arrays.asList("source.ruby:0:")));
    }

    public void testRubyPartitioningUnicode() throws Exception {
        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
                new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"), "ruby.tmbundle-master/Syntaxes/Ruby.plist");

        LiClipseLanguage language = metadata.loadLanguage(true);
        Document document = new Document(""
                + "# áé\n" +
                "'á'#f\n" +
                "");
        language.connect(document);
        String partition = TestUtils.partition(document);
        assertEquals(TestUtils.listToExpected("source.ruby:0:"), partition);

        // assertEquals(TestUtils.listToExpected("punctuation.definition.comment.ruby:0:1",
        // 		"comment.line.number-sign.ruby:1:4",
        // 		"punctuation.definition.string.begin.ruby:5:1",
        // 		"string.quoted.single.ruby:6:1",
        // 		"punctuation.definition.string.end.ruby:7:1",
        // 		"punctuation.definition.comment.ruby:8:1",
        // 		"comment.line.number-sign.ruby:9:2"),
        // 		TestUtils.scanAll(language, document, Arrays.asList("source.ruby:0:")));

        assertEquals(TestUtils.listToExpected("punctuation.definition.comment.ruby:0:1",
                "comment.line.number-sign.ruby:1:3", // Difference: was comment.line.number-sign.ruby:1:4
                "comment.line.number-sign.ruby:4:1", // Didn't exist without unicode chars
                "punctuation.definition.string.begin.ruby:5:1",
                "string.quoted.single.ruby:6:1",
                "punctuation.definition.string.end.ruby:7:1",
                "punctuation.definition.comment.ruby:8:1",
                "comment.line.number-sign.ruby:9:1", // Difference: was comment.line.number-sign.ruby:9:2
                "comment.line.number-sign.ruby:10:1"), // Didn't exist without unicode chars
                TestUtils.scanAll(language, document, Arrays.asList("source.ruby:0:")));
    }

    public void testRubyPartitioning2() throws Exception {
        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
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
        assertEquals(TestUtils.listToExpected("source.ruby:0:"), partition);

        assertEquals(TestUtils.listToExpected("keyword.control.def.ruby:0:3",
                "meta.function.method.with-arguments.ruby:3:1",
                "entity.name.function.ruby:4:10",
                "punctuation.definition.parameters.ruby:14:1",
                "variable.parameter.function.ruby:15:1",
                "punctuation.definition.parameters.ruby:16:2",
                "keyword.control.ruby:18:4",
                "source.ruby:22:1",
                "source.ruby:23:5",
                "keyword.operator.assignment.ruby:28:1",
                "support.class.ruby:29:8",
                "punctuation.separator.method.ruby:37:1",
                "keyword.other.special-method.ruby:38:3",
                "punctuation.section.function.ruby:41:1",
                "punctuation.section.function.ruby:42:2"),
                TestUtils.scanAll(language, document, Arrays.asList("source.ruby:0:")));

    }

    public void testRubyPartitioning3() throws Exception {
        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
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
        assertEquals(TestUtils.listToExpected("source.ruby:0:"), partition);

        assertEquals(TestUtils.listToExpected("keyword.control.def.ruby:0:3",
                "meta.function.method.with-arguments.ruby:3:1",
                "entity.name.function.ruby:4:10",
                "punctuation.definition.parameters.ruby:14:1",
                "variable.parameter.function.ruby:15:1",
                "punctuation.definition.parameters.ruby:16:2",
                "keyword.control.ruby:18:4",
                "source.ruby:22:1",
                "source.ruby:23:5",
                "keyword.operator.assignment.ruby:28:1",
                "support.class.ruby:29:8",
                "punctuation.separator.method.ruby:37:1",
                "keyword.other.special-method.ruby:38:3",
                "punctuation.section.function.ruby:41:1",
                "source.ruby:42:1",
                "punctuation.separator.object.ruby:43:1",
                "source.ruby:44:2",
                "punctuation.section.function.ruby:46:2",
                "source.ruby:48:1",
                "keyword.control.def.ruby:49:3",
                "meta.function.method.with-arguments.ruby:52:1",
                "entity.name.function.ruby:53:4",
                "punctuation.definition.parameters.ruby:57:1",
                "variable.parameter.function.ruby:58:1",
                "keyword.operator.assignment.ruby:59:1",
                "constant.numeric.integer.ruby:60:1",
                "meta.function.method.with-arguments.ruby:61:1",
                "variable.parameter.function.ruby:62:1",
                "keyword.operator.assignment.ruby:63:1",
                "constant.numeric.integer.ruby:64:1",
                "meta.function.method.with-arguments.ruby:65:1",
                "storage.type.variable.ruby:66:1",
                "variable.parameter.function.ruby:67:1",
                "punctuation.definition.parameters.ruby:68:2",
                "source.ruby:70:2",
                "support.function.kernel.ruby:72:4",
                "source.ruby:76:1",
                "punctuation.definition.string.begin.ruby:77:1",
                "punctuation.section.embedded.begin.ruby:78:2",
                "source.ruby:80:1",
                "source.ruby:81:1",
                "string.quoted.double.ruby:82:1",
                "punctuation.section.embedded.begin.ruby:83:2",
                "source.ruby:85:1",
                "source.ruby:86:1",
                "punctuation.definition.string.end.ruby:87:2",
                "source.ruby:89:3",
                "punctuation.separator.method.ruby:92:1",
                "source.ruby:93:4",
                "punctuation.section.scope.begin.ruby:97:1",
                "punctuation.separator.arguments.ruby:98:1",
                "variable.other.block.ruby:99:1",
                "punctuation.separator.arguments.ruby:100:1",
                "source.ruby:101:1",
                "support.function.kernel.ruby:102:5",
                "source.ruby:107:1",
                "punctuation.definition.string.begin.ruby:108:1",
                "string.quoted.double.ruby:109:1",
                "punctuation.section.embedded.begin.ruby:110:2",
                "source.ruby:112:1",
                "source.ruby:113:1",
                "string.quoted.double.ruby:114:2",
                "punctuation.definition.string.end.ruby:116:1",
                "punctuation.section.scope.end.ruby:117:1",
                "source.ruby:118:2",
                "punctuation.definition.comment.ruby:120:1",
                "comment.line.number-sign.ruby:121:51",
                "keyword.control.ruby:172:4",
                "support.function.kernel.ruby:176:4",
                "source.ruby:180:1",
                "constant.numeric.integer.ruby:181:1",
                "punctuation.separator.object.ruby:182:1",
                "source.ruby:183:1",
                "constant.numeric.integer.ruby:184:1",
                "punctuation.separator.object.ruby:185:1",
                "source.ruby:186:1",
                "constant.numeric.integer.ruby:187:1",
                "punctuation.separator.object.ruby:188:1",
                "source.ruby:189:1",
                "constant.numeric.integer.ruby:190:2",
                "punctuation.separator.object.ruby:192:1",
                "source.ruby:193:1",
                "constant.numeric.integer.ruby:194:2",
                "punctuation.separator.object.ruby:196:1",
                "source.ruby:197:1",
                "constant.numeric.integer.ruby:198:3",
                "source.ruby:201:1",
                "source.ruby:202:1",
                "source.ruby:203:24"),
                TestUtils.scanAll(language, document, Arrays.asList("source.ruby:0:")));
    }

    public void testRubyPartitioning4() throws Exception {
        LanguageMetadataTmBundleZipFileInfo metadata = new LanguageMetadataTmBundleZipFileInfo(
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
        assertEquals(TestUtils.listToExpected("source.ruby:0:"), partition);

        assertEquals(TestUtils.listToExpected("punctuation.definition.comment.ruby:0:1",
                "comment.line.number-sign.ruby:1:50",
                "punctuation.definition.comment.ruby:51:1",
                "comment.line.number-sign.ruby:52:11",
                "punctuation.definition.comment.ruby:63:1",
                "comment.line.number-sign.ruby:64:7",
                "punctuation.definition.comment.ruby:71:2",
                "punctuation.definition.comment.ruby:73:1",
                "comment.line.number-sign.ruby:74:14",
                "punctuation.definition.comment.ruby:88:1",
                "comment.line.number-sign.ruby:89:7",
                "punctuation.definition.comment.ruby:96:1",
                "comment.line.number-sign.ruby:97:5",
                "source.ruby:102:1",
                "keyword.control.class.ruby:103:5",
                "meta.class.ruby:108:1",
                "entity.name.type.class.ruby:109:18",
                "meta.class.ruby:127:1",
                "keyword.operator.other.ruby:128:1",
                "meta.class.ruby:129:1",
                "entity.other.inherited-class.ruby:130:22",
                "source.ruby:152:2",
                "keyword.control.def.ruby:154:3",
                "meta.function.method.without-arguments.ruby:157:1",
                "entity.name.function.ruby:158:4",
                "source.ruby:162:2",
                "keyword.control.ruby:164:4",
                "source.ruby:168:1",
                "source.ruby:169:2",
                "keyword.control.def.ruby:171:3",
                "meta.function.method.without-arguments.ruby:174:1",
                "entity.name.function.ruby:175:7",
                "source.ruby:182:2",
                "keyword.control.ruby:184:4",
                "keyword.control.ruby:188:4",
                "keyword.control.class.ruby:192:5",
                "meta.class.ruby:197:1",
                "entity.name.type.class.ruby:198:18",
                "meta.class.ruby:216:1",
                "keyword.operator.other.ruby:217:1",
                "meta.class.ruby:218:1",
                "entity.other.inherited-class.ruby:219:22",
                "source.ruby:241:2",
                "keyword.control.def.ruby:243:3",
                "meta.function.method.without-arguments.ruby:246:1",
                "entity.name.function.ruby:247:4",
                "source.ruby:251:2",
                "keyword.control.ruby:253:4",
                "source.ruby:257:1",
                "source.ruby:258:2",
                "keyword.control.def.ruby:260:3",
                "meta.function.method.without-arguments.ruby:263:1",
                "entity.name.function.ruby:264:7",
                "source.ruby:271:2",
                "keyword.control.ruby:273:4",
                "keyword.control.ruby:277:4"),
                TestUtils.scanAll(language, document, Arrays.asList("source.ruby:0:")));

    }

}
