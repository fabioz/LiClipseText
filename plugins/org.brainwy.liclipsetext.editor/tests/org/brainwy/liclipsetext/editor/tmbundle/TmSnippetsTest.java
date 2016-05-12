package org.brainwy.liclipsetext.editor.tmbundle;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.tmbundle.ITmLanguagePart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmLanguagePart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmSnippetPart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.Node;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.RootNode;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.SnippetToTemplateCtx;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.StringScanner;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.TmSnippetParser;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.TmSnippetParserV2;

import junit.framework.TestCase;

public class TmSnippetsTest extends TestCase {

    public void testSnippets() throws Exception {
        try (ZipFile z = new ZipFile(new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"))) {
            InputStream inputStream = z
                    .getInputStream(z.getEntry("ruby.tmbundle-master/Snippets/assert_in_delta(..)  (asid).plist"));

            ITmLanguagePart create = TmLanguagePart.create(inputStream);
            assertTrue(create instanceof TmSnippetPart);
            TmSnippetPart part = (TmSnippetPart) create;
            assertEquals("assert_in_delta(${expected_float}, ${actual_float}, ${220}${cursor})",
                    TmSnippetPart.fixPattern(part.getContent()));
        }
    }

    public void testSnippets2() throws Exception {
        try (ZipFile z = new ZipFile(new File(TestUtils.getLanguagesDir(), "ruby.tmbundle"))) {
            // Enumeration<? extends ZipEntry> entries = z.entries();
            // while (entries.hasMoreElements()) {
            //     System.out.println(entries.nextElement());
            // }
            InputStream inputStream = z
                    .getInputStream(z.getEntry("ruby.tmbundle-master/Snippets/Array.new(10) { |i| .. }  (Arr).plist"));

            ITmLanguagePart create = TmLanguagePart.create(inputStream);
            assertTrue(create instanceof TmSnippetPart);
            TmSnippetPart part = (TmSnippetPart) create;

            //Note that we remove the empty ones before and after ${i} because they'd conflict.
            assertEquals("Array.new(${10}) { ${i}${cursor} }",
                    TmSnippetPart.fixPattern(part.getContent()));
        }
    }

    public void testSnippetsPattern() throws Exception {
        assertEquals("Test${empty}", TmSnippetPart.fixPattern("Test${2:${1/([\\w&&[^_]]+)|./u$1/g}}"));
    }

    public void testSnippetsPatternReplace() throws Exception {
        check("${empty}", "$1");
    }

    public void testSnippetsPatternReplace1() throws Exception {
        check("${cursor}", "$0");
    }

    public void testSnippetsPatternReplace2() throws Exception {
        check("${cursor}", "${0}");
    }

    public void testSnippetsPatternReplace3() throws Exception {
        check("${empty}", "${1}");
    }

    public void testSnippetsPatternReplace4() throws Exception {
        check("${bar}", "${1:bar}");
    }

    public void testSnippetsPatternReplace5() throws Exception {
        check("${a}", "${1:a${3:x}}");
    }

    public void testSnippetsPatternReplace6() throws Exception {
        check("${ab}", "${1:a\\}b}"); //Escape the '\}'
    }

    public void testSnippetsPatternReplace7() throws Exception {
        check("${empty}", "${1/reg/fmt/gm}");
    }

    private void check(String expected, String pattern) {
        SnippetToTemplateCtx parser;
        parser = TmSnippetParser.createReplacement(pattern, new SnippetToTemplateCtx());
        assertEquals(expected, parser.getReplaced());
    }

    public void testSnippetsPatternReplace7a() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(new StringScanner("${1/reg/fmt/ig}"), root);
        assertEquals(
                "RootNode[[PlaceholderNode[1 regexp: reg fmt: FmtNode[[TextNode[fmt]]] opts: ig]]]",
                root.toString());
    }

    public void testSnippetsStr() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(new StringScanner("$10"), root);
        assertEquals("RootNode[[PlaceholderNode[10]]]", root.toString());
    }

    public void testSnippetsFormatStr() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseFormatString(new StringScanner("$10"), root);
        assertEquals("RootNode[[VariableNode[10]]]", root.toString());
    }

    public void testSnippetsFormatStr1() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseFormatString(new StringScanner("$var"), root);
        assertEquals("RootNode[[VariableNode[var]]]", root.toString());
    }

    public void testSnippetsFormatStr2() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseFormatString(new StringScanner("${var}"), root);
        assertEquals("RootNode[[VariableNode[var]]]", root.toString());
    }

    public void testSnippetsFormatStr3() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseFormatString(new StringScanner("${var/reg/fmt/ig}"), root);
        assertEquals(
                "RootNode[[VariableNode[var regexp: reg fmt: FmtNode[[TextNode[fmt]]] opts: ig]]]",
                root.toString());
    }

    public void testSnippetsFormatStr4() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseFormatString(new StringScanner("${var/reg/f\\x{12}mt/ig}"), root);
        assertEquals(
                "RootNode[[VariableNode[var regexp: reg fmt: FmtNode[[TextNode[f\\x{12}mt]]] opts: ig]]]",
                root.toString());
    }

    public void testSnippetsFormatStr4a() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseFormatString(new StringScanner("${var/reg/f\\x{12}m\\t\\nt/ig}"), root);
        assertEquals(
                "RootNode[[VariableNode[var regexp: reg fmt: FmtNode[[TextNode[f\\x{12}m\\t\\nt]]] opts: ig]]]",
                root.toString());
    }

    public void testSnippetsFormatStr5() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseFormatString(new StringScanner("(?3:if)"), root);
        assertEquals(
                "RootNode[[ConditionNode[3 children:[IfNode[ children:[TextNode[if]]]]]]]",
                root.toString());
    }

    public void testSnippetsFormatStr6() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseFormatString(new StringScanner("(?3:if:else)"), root);
        assertEquals(
                "RootNode[[ConditionNode[3 children:[IfNode[ ElseNode[else children:[TextNode[else]]]]]]]]",
                root.toString());
    }

    //${var:?if:else}
    public void testSnippetsFormatStr7() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("${var:?if:else}"), root);
        assertEquals(
                "RootNode[[VariableNode[var IfNode[ ElseNode[ children:[TextNode[else]]]]]]]",
                root.toString());
    }

    public void testSnippetsFormatStr8() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("${1/reg/${2:?3:4}/}"), root);
        assertEquals(
                "RootNode[[PlaceholderNode[1 regexp: reg fmt: FmtNode[[VariableNode[2 IfNode[ ElseNode[ children:[TextNode[4]]]]]]] opts: ]]]",
                root.toString());
    }

    public void testSnippetsFormatStr9() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("${1/(#)(#)?(#)?(#)?(#)?(#)?/${6:?6:${5:?5:${4:?4:${3:?3:${2:?2:1}}}}}/}"), root);
        assertEquals(
                "RootNode[[PlaceholderNode[1 regexp: (#)(#)?(#)?(#)?(#)?(#)? fmt: FmtNode[[VariableNode[6 IfNode[ ElseNode[ children:[VariableNode[5 IfNode[ ElseNode[ children:[VariableNode[4 IfNode[ ElseNode[ children:[VariableNode[3 IfNode[ ElseNode[ children:[VariableNode[2 IfNode[ ElseNode[ children:[TextNode[1]]]]]]]]]]]]]]]]]]]]]]] opts: ]]]",
                root.toString());
    }

    //${var:+if}
    public void testSnippetsFormatStr10() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("${var:+if}"), root);
        assertEquals(
                "RootNode[[VariableNode[var IfNode[ children:[TextNode[if]]]]]]",
                root.toString());
    }

    //${var:-else}
    //${var:else}
    public void testSnippetsFormatStr11() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("${var:-else}"), root);
        assertEquals(
                "RootNode[[VariableNode[var IfNode[NOT children:[TextNode[else]]]]]]",
                root.toString());
    }

    //${var:-else}
    //${var:else}
    public void testSnippetsFormatStr12() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("${var:else}"), root);
        assertEquals(
                "RootNode[[VariableNode[var IfNode[NOT children:[TextNode[else]]]]]]",
                root.toString());
    }

    //${var:[/upcase][/downcase][/capitalize][/asciify]}
    public void testSnippetsFormatStr13() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("${a:/upcase/downcase}"), root);
        assertEquals(
                "RootNode[[VariableNode[a convert:[ConvertNode[upcase], ConvertNode[downcase]]]]]",
                root.toString());
    }

    //`code`
    public void testSnippetsFormatStr14() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("`code`"), root);
        assertEquals(
                "RootNode[[CodeNode[code]]]",
                root.toString());
    }

    //${int|choice 1,...,choice n|}
    public void testSnippetsFormatStr16() throws Exception {
        Node root = new RootNode();
        TmSnippetParserV2.parseSnippet(
                new StringScanner("${1|bar,foo|}"), root);
        assertEquals(
                "RootNode[[PlaceholderNode[1 choiceNodes:[ChoiceNode[ children:[TextNode[bar]]], ChoiceNode[ children:[TextNode[foo]]]]]]]",
                root.toString());
    }

    public void testLetterOrDigit() {
        assertTrue(Character.isLetterOrDigit('a'));
        assertTrue(Character.isLetterOrDigit('A'));
        assertTrue(Character.isLetterOrDigit('2'));
        assertTrue(Character.isLetterOrDigit('á'));
        assertTrue(Character.isLetterOrDigit('á'));
    }

}

//Unable to handle snippet in scope: text.html - B:meta.tag name: Embed QT Movie tab trigger: movie pattern: <object width="$2" height="$3" classid="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B" codebase="http://www.apple.com/qtactivex/qtplugin.cab"><param name="src" value="$1"${TM_XHTML}><param name="controller" value="$4"${TM_XHTML}><param name="autoplay" value="$5"${TM_XHTML}><embed src="${1:movie.mov}"
//        width="${2:320}" height="${3:240}"
//        controller="${4:true}" autoplay="${5:true}"
//        scale="tofit" cache="true"
//        pluginspage="http://www.apple.com/quicktime/download/"
//    ${TM_XHTML}></object>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Input tab trigger: input pattern: <input type="${1|text,submit,hidden,button|}" name="${2:some_name}" value="$3"${4: id="${5:$2}"}${TM_XHTML}>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Mail Anchor tab trigger: mailto pattern: <a href="mailto:${1:joe@example.com}?subject=${2:feedback}">${3:email me}</a>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Base tab trigger: base pattern: <base href="$1"${2: target="$3"}${TM_XHTML}>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Body tab trigger: body pattern: <body id="${1:${TM_FILENAME/(.*)\..*/\L$1/}}"${2: onload="$3"}>
//    {cursor}
//</body>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Div tab trigger: div pattern: <div${1: id="${2:name}"}>
//    ${0:$TM_SELECTED_TEXT}
//</div>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Form tab trigger: form pattern: <form action="${1:${TM_FILENAME/(.*?)\..*/$1_submit/}}" method="${2:get}" accept-charset="utf-8">
//    {cursor}
//
//    <p><input type="submit" value="Continue &rarr;"${TM_XHTML}></p></form>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Heading tab trigger: h1 pattern: <h1 id="${1/[[:alpha:]]+|( )/(?1:_:\L{cursor})/g}">${1:$TM_SELECTED_TEXT}</h1>
//Unable to handle snippet in scope: text.html - B:meta.tag - text.html source name: Head tab trigger: head pattern: <head><meta http-equiv="Content-type" content="text/html; charset=utf-8"${TM_XHTML}><title>${1:${TM_FILENAME/((.+)\..*)?/(?2:$2:Page Title)/}}</title>
//    {cursor}
//</head>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Link tab trigger: link pattern: <link rel="${1:stylesheet}" href="${2:/css/master.css}" type="text/css" media="${3|screen,all,braille,embossed,handheld,print,projection,speech,tty,tv|}"${TM_XHTML}>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Script With External Source tab trigger: scriptsrc pattern: <script src="$1" type="text/javascript" charset="${3:utf-8}"></script>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Table tab trigger: table pattern: <table border="${1:0}"${2: cellspacing="${3:5}" cellpadding="${4:5}"}><tr><th>${5:Header}</th></tr><tr><td>${0:Data}</td></tr></table>
//Unable to handle snippet in scope: text.html - B:meta.tag name: Title tab trigger: title pattern: <title>${1:${TM_FILENAME/((.+)\..*)?/(?2:$2:Page Title)/}}</title>
//Unable to handle snippet in scope: source.ruby name: if ... else ... end tab trigger: ife pattern: if ${1:condition}
//    $2
//else
//    $3
//end
//Unable to handle snippet in scope: source.ruby name: Array.new(10) { |i| .. } tab trigger: Array pattern: Array.new(${1:10}) { ${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${2:i}${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: Dir.glob("..") { |file| .. } tab trigger: Dir pattern: Dir.glob(${1:"${2:dir/glob/*}"}) { |${3:file}| {cursor} }
//Unable to handle snippet in scope: source.ruby name: File.foreach ("..") { |line| .. } tab trigger: File pattern: File.foreach(${1:"${2:path/to/file}"}) { |${3:line}| {cursor} }
//Unable to handle snippet in scope: source.ruby name: Marshal.dump(.., file) tab trigger: Md pattern: File.open(${1:"${2:path/to/file}.dump"}, "wb") { |${3:file}| Marshal.dump(${4:obj}, ${3:file}) }
//Unable to handle snippet in scope: source.ruby name: Marshal.load(obj) tab trigger: Ml pattern: File.open(${1:"${2:path/to/file}.dump"}, "rb") { |${3:file}| Marshal.load(${3:file}) }
//Unable to handle snippet in scope: source.ruby - comment name: begin ... rescue ... end tab trigger: begin pattern: ${TM_SELECTED_TEXT/([\t ]*).*/$1/m}begin
//    ${3:${TM_SELECTED_TEXT/(\A.*)|(.+)|\n\z/(?1:{cursor}:(?2:\t{cursor}))/g}}
//${TM_SELECTED_TEXT/([\t ]*).*/$1/m}rescue ${1:Exception}${2/.+/ => /}${2:e}
//${TM_SELECTED_TEXT/([\t ]*).*/$1/m} {cursor}
//${TM_SELECTED_TEXT/([\t ]*).*/$1/m}end
//
//Unable to handle snippet in scope: source.ruby name: YAML.dump(.., file) tab trigger: Yd- pattern: File.open(${1:"${2:path/to/file}.yaml"}, "w") { |${3:file}| YAML.dump(${4:obj}, ${3:file}) }
//Unable to handle snippet in scope: source.ruby name: YAML.load(file) tab trigger: Yl- pattern: File.open(${1:"${2:path/to/file}.yaml"}) { |${3:file}| YAML.load(${3:file}) }
//Unable to handle snippet in scope: source.ruby name: application { .. } tab trigger: app pattern: if __FILE__ == \$PROGRAM_NAME
//    {cursor}
//end
//Unable to handle snippet in scope: source.ruby name: assert(..) tab trigger: as pattern: assert(${1:test}, "${0:Failure message.}")
//Unable to handle snippet in scope: source.ruby name: assert_in_delta(..) tab trigger: asid pattern: assert_in_delta(${1:expected_float}, ${2:actual_float}, ${0:2 ** -20})
//Unable to handle snippet in scope: source.ruby name: class .. < DelegateClass .. initialize .. end tab trigger: cla- pattern: class ${1:${TM_FILENAME/(?:\A|_)([A-Za-z0-9]+)(?:\.rb)?/(?2::u$1)/g}} < DelegateClass(${2:ParentClass})
//    def initialize${3/(^.*?\S.*)|.*/(?1:\()/}${3:args}${3/(^.*?\S.*)|.*/(?1:\))/}
//        super(${4:del_obj})
//        
//        {cursor}
//    end
//end
//Unable to handle snippet in scope: source.ruby name: class .. < ParentClass .. initialize .. end tab trigger: cla pattern: class ${1:${TM_FILENAME/(?:\A|_)([A-Za-z0-9]+)(?:\.rb)?/(?2::u$1)/g}} < ${2:ParentClass}
//    def initialize${3/(^.*?\S.*)|.*/(?1:\()/}${3:args}${3/(^.*?\S.*)|.*/(?1:\))/}
//        {cursor}
//    end
//end
//Unable to handle snippet in scope: source.ruby name: ClassName = Struct .. do .. end tab trigger: cla pattern: ${1:${TM_FILENAME/(?:\A|_)([A-Za-z0-9]+)(?:\.rb)?/(?2::u$1)/g}} = Struct.new(:${2:attr_names}) do
//    def ${3:method_name}
//        {cursor}
//    end
//    
//    
//end
//Unable to handle snippet in scope: source.ruby name: class .. < Test::Unit::TestCase .. end tab trigger: tc pattern: require "test/unit"
//
//require "${1:library_file_name}"
//
//class Test${2:${1/([\w&&[^_]]+)|./u$1/g}} < Test::Unit::TestCase
//    def test_${3:case_name}
//        {cursor}
//    end
//end
//Unable to handle snippet in scope: source.ruby name: class .. end tab trigger: cla pattern: class ${1:${TM_FILENAME/(?:\A|_)([A-Za-z0-9]+)(?:\.rb)?/(?2::u$1)/g}}
//    {cursor}
//end
//Unable to handle snippet in scope: source.ruby name: class .. initialize .. end tab trigger: cla pattern: class ${1:${TM_FILENAME/(?:\A|_)([A-Za-z0-9]+)(?:\.rb)?/(?2::u$1)/g}}
//    def initialize${2/(^.*?\S.*)|.*/(?1:\()/}${2:args}${2/(^.*?\S.*)|.*/(?1:\))/}
//        {cursor}
//    end
//    
//    
//end
//Unable to handle snippet in scope: source.ruby name: class BlankSlate .. initialize .. end tab trigger: cla pattern: class ${1:BlankSlate}
//    instance_methods.each { |meth| undef_method(meth) unless meth =~ /\A__/ }
//    
//    def initialize${2/(^.*?\S.*)|.*/(?1:\()/}${2:args}${2/(^.*?\S.*)|.*/(?1:\))/}
//        @${3:delegate} = ${4:delegate_object}
//        
//        {cursor}
//    end
//    
//    def method_missing(meth, *args, &block)
//        @${3:delegate}.send(meth, *args, &block)
//    end
//end
//Unable to handle snippet in scope: source.ruby name: def_delegator .. tab trigger: defd pattern: def_delegator :${1:@del_obj}, :${2:del_meth}, :${3:new_name}
//Unable to handle snippet in scope: source.ruby name: def_delegators .. tab trigger: defds pattern: def_delegators :${1:@del_obj}, :${0:del_methods}
//Unable to handle snippet in scope: source.ruby name: Insert do |variable| ... end tab trigger: do pattern: do${1/(^(?<var>\s*[a-z_][a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1: |)/}${1:variable}${1/(^(?<var>\s*[a-z_][a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}
//    {cursor}
//end
//Unable to handle snippet in scope: source.ruby name: downto(0) { |n| .. } tab trigger: dow pattern: downto(${1:0}) { ${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${2:n}${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: each_line { |line| .. } tab trigger: eal pattern: each_line$1 { |${2:line}| {cursor} }
//Unable to handle snippet in scope: source.ruby name: fetch(name) { |key| .. } tab trigger: fet pattern: fetch(${1:name}) { ${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${2:key}${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: fill(range) { |i| .. } tab trigger: fil pattern: fill(${1:range}) { ${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${2:i}${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: flunk(..) tab trigger: flu pattern: flunk("${1:Failure message.}"){cursor}
//Unable to handle snippet in scope: source.ruby name: grep(/pattern/) { |match| .. } tab trigger: gre pattern: grep(${1:/${2:pattern}/}) { |${3:match}| {cursor} }
//Unable to handle snippet in scope: source.ruby name: gsub(/../) { |match| .. } tab trigger: gsu pattern: gsub(/${1:pattern}/) { ${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${2:match}${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: Hash Pair — :key => "value" tab trigger: : pattern: :${1:key} => ${2:"${3:value}"}${4:, }
//Unable to handle snippet in scope: source.ruby name: inject(init) { |mem, var| .. } tab trigger: inj pattern: inject${1/.+/(/}${1:init}${1/.+/)/} { |${2:mem}, ${3:var}| {cursor} }
//Unable to handle snippet in scope: source.ruby name: lambda { |args| .. } tab trigger: lam pattern: lambda { ${1/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${1:args}${1/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: module .. ClassMethods .. end tab trigger: mod pattern: module ${1:${TM_FILENAME/(?:\A|_)([A-Za-z0-9]+)(?:\.rb)?/(?2::u$1)/g}}
//    module ClassMethods
//        {cursor}
//    end
//    
//    module InstanceMethods
//        
//    end
//    
//    def self.included(receiver)
//        receiver.extend         ClassMethods
//        receiver.send :include, InstanceMethods
//    end
//end
//Unable to handle snippet in scope: source.ruby name: module .. end tab trigger: mod pattern: module ${1:${TM_FILENAME/(?:\A|_)([A-Za-z0-9]+)(?:\.rb)?/(?2::u$1)/g}}
//    {cursor}
//end
//Unable to handle snippet in scope: source.ruby name: module .. module_function .. end tab trigger: mod pattern: module ${1:${TM_FILENAME/(?:\A|_)([A-Za-z0-9]+)(?:\.rb)?/(?2::u$1)/g}}
//    module_function
//    
//    {cursor}
//end
//Unable to handle snippet in scope: source.ruby - string - comment name: Insert { |variable| ... } tab trigger: { pattern: { ${1/(^(?<var>\s*[a-z_][a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${1:variable}${1/(^(?<var>\s*[a-z_][a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}${2:$TM_SELECTED_TEXT} 
//Unable to handle snippet in scope: source.ruby name: open("path/or/url", "w") { |io| .. } tab trigger: ope pattern: open(${1:"${2:path/or/url/or/pipe}"}${3/(^[rwab+]+$)|.*/(?1:, ")/}${3:w}${3/(^[rwab+]+$)|.*/(?1:")/}) { |${4:io}| {cursor} }
//Unable to handle snippet in scope: source.ruby name: option_parse { .. } tab trigger: optp pattern: require "optparse"
//
//options = {${1::default => "args"}}
//
//ARGV.options do |opts|
//    opts.banner = "Usage:  #{File.basename(\$PROGRAM_NAME)} [OPTIONS]${2/^\s*$|(.*\S.*)/(?1: )/}${2:OTHER_ARGS}"
//    
//    opts.separator ""
//    opts.separator "Specific Options:"
//    
//    {cursor}
//    
//    opts.separator "Common Options:"
//    
//    opts.on( "-h", "--help",
//             "Show this message." ) do
//        puts opts
//        exit
//    end
//    
//    begin
//        opts.parse!
//    rescue
//        puts opts
//        exit
//    end
//end
//
//Unable to handle snippet in scope: source.ruby name: step(2) { |e| .. } tab trigger: ste pattern: step(${1:2}) { ${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${2:n}${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: sub(/../) { |match| .. } tab trigger: sub pattern: sub(/${1:pattern}/) { ${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${2:match}${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: times { |n| .. } tab trigger: tim pattern: times { ${1/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${1:n}${1/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: unix_filter { .. } tab trigger: unif pattern: ARGF.each_line$1 do |${2:line}|
//    {cursor}
//end
//Unable to handle snippet in scope: source.ruby name: option(..) tab trigger: opt pattern: opts.on( "-${1:o}", "--${2:long-option-name}"${3/^\s*$|(.*\S.*)/(?1:, )/}${3:String},
//         "${4:Option description.}" ) do |${6:opt}|
//    {cursor}
//end
//Unable to handle snippet in scope: source.ruby name: upto(1.0/0.0) { |n| .. } tab trigger: upt pattern: upto(${1:1.0/0.0}) { ${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:|)/}${2:n}${2/(^(?<var>\s*(?:\*|\*?[a-z_])[a-zA-Z0-9_]*\s*)(,\g<var>)*,?\s*$)|.*/(?1:| )/}{cursor} }
//Unable to handle snippet in scope: source.ruby name: usage_if() tab trigger: usai pattern: if ARGV.$1
//    abort "Usage:  #{\$PROGRAM_NAME} ${2:ARGS_GO_HERE}"
//end
//Unable to handle snippet in scope: source.ruby name: usage_unless() tab trigger: usau pattern: unless ARGV.$1
//    abort "Usage:  #{\$PROGRAM_NAME} ${2:ARGS_GO_HERE}"
//end
