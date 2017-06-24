/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.brainwy.liclipsetext.editor.common.partitioning.rules.ITextMateRule;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.ScopeSelector;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.ILiClipsePredicateRule;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.FastStack;
import org.brainwy.liclipsetext.shared_core.structure.LinkedListWarningOnSlowOperations;
import org.brainwy.liclipsetext.shared_core.structure.OrderedMap;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Interesting reads:
 *
 * http://www.apeth.com/nonblog/stories/textmatebundle.html
 *
 * Notes:
 * Bear in mind, however, that because of the way the TextMate parser surveys your document,
 * all regular expressions used in grammar match rules must apply to a single line at a time.
 * A single expression cannot embrace multiple lines. Thus it is possible to write a regular
 * expression that appears to work in Rubular (or TextMate’s regex Find) but will fail as part of a grammar.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TmLanguageHandler {

    public static final String PATTERNS = "patterns";
    public static final String REPOSITORY = "repository";
    public static final String SCOPE_NAME = "scopeName";
    public static final String SHEBANG = "firstLineMatch";
    public static final String NAME = "name";
    public static final String FILE_TYPES = "fileTypes";

    private Map rootDict;

    public TmLanguageHandler() {
    }

    private XMLReader makeXMLReader() throws Exception {
        SAXParserFactory fac = SAXParserFactory.newInstance();
        fac.setNamespaceAware(false);
        fac.setValidating(false);
        fac.setFeature("http://xml.org/sax/features/namespaces", false);
        fac.setFeature("http://xml.org/sax/features/validation", false);
        fac.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        fac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        SAXParser saxParser = fac.newSAXParser();
        XMLReader reader = saxParser.getXMLReader();

        reader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String pid, String sid) throws SAXException {
                //Disabling things above should work, but just in case, let's also set it not to
                //resolve any entities.
                return new InputSource(new StringReader(""));
            }
        });
        return reader;
    }

    private static final class IntegerBuffer {

        private FastStringBuffer buf = new FastStringBuffer();

        public void append(String str) {
            buf.append(str);
        }

        public Integer toInteger() {
            return new Integer(buf.toString());
        }
    }

    private static final class TrueBuffer {

    }

    private static final class FalseBuffer {

    }

    private static final class Entry {
        @Override
        public String toString() {
            return "Entry [key=" + key + ", value=" + value + "]";
        }

        public String key;
        public Object value;
    }

    private static final class Handler extends DefaultHandler implements ContentHandler {

        private FastStack stack = new FastStack<>(0);
        private Map rootDict = null;

        private static void log(String string) {
            TmLanguageHandler.log(string);
        }

        @Override
        final public void startElement(final String namespace, final String localname, final String type,
                final Attributes attributes)
                throws SAXException {
            switch (type) {
                case "dict":
                    OrderedMap item = new OrderedMap();
                    if (rootDict == null) {
                        rootDict = item;
                    }
                    stack.push(item);
                    break;

                case "array":
                    stack.push(new ArrayList<>());
                    break;

                case "key":
                    stack.push(new Entry());
                    break;

                case "string":
                    stack.push(new FastStringBuffer());
                    break;

                case "integer":
                    stack.push(new IntegerBuffer());
                    break;

                case "true":
                    stack.push(new TrueBuffer());
                    break;

                case "false":
                    stack.push(new FalseBuffer());
                    break;

                case "plist":
                    break;

                default:
                    log("UNHANDLED startElement: " + type);
                    break;
            }
        }

        @Override
        final public void endElement(final String namespace, final String localname, final String type)
                throws SAXException {
            switch (type) {
                case "dict":
                    handleDict();
                    break;

                case "string":
                    handleString();
                    break;

                case "integer":
                    handleInteger();
                    break;

                case "true":
                    handleTrue();
                    break;

                case "false":
                    handleFalse();
                    break;

                case "key":
                    handleKey();
                    break;

                case "array":
                    handleArray();
                    break;

                case "plist":
                    break;

                default:
                    log("UNHANDLED endElement: " + type);
                    break;
            }
        }

        private void handleDict() {
            Map dict = (Map) stack.pop();
            if (!stack.empty()) {
                Object parent = stack.peek();
                if (parent instanceof Entry) {
                    stack.pop(); // Key value: pop key
                    Entry key = (Entry) parent;
                    key.value = dict;
                } else if (parent instanceof List) {
                    List parentList = (List) parent;
                    parentList.add(dict);
                } else {
                    log("Error: did not expect dict: " + dict + " at parent: " + parent.getClass());
                }
            }
        }

        private void handleArray() {
            ArrayList list = (ArrayList) stack.pop();
            list.trimToSize();
            Object parent = stack.peek();
            if (parent instanceof Entry) {
                stack.pop(); // Key value: pop key
                Entry key = (Entry) parent;
                key.value = list;
            } else if (parent instanceof List) {
                List parentList = (List) parent;
                parentList.add(list);
            } else {
                log("Error: did not expect array: " + list + " at parent: " + parent.getClass());
            }
        }

        private void handleKey() {
            Entry key = (Entry) stack.peek(); // Special case: the key remains in the stack until its value is given!
            Object parent = stack.peek(1);
            if (parent instanceof Map) {
                Map map = (Map) parent;
                map.put(key.key, key);
            } else {
                log("Error: did not expect key: " + key + " at parent: " + parent.getClass());
            }
        }

        private void handleString() {
            FastStringBuffer buf = (FastStringBuffer) stack.pop();
            Object parent = stack.peek();
            if (parent instanceof Entry) {
                stack.pop(); // Key value: pop key
                Entry key = (Entry) parent;
                key.value = buf.toString();
            } else if (parent instanceof List) {
                List list = (List) parent;
                list.add(buf.toString());
            } else {
                log("Error: did not expect string: " + buf + " at parent: " + parent.getClass());
            }
        }

        private void handleInteger() {
            IntegerBuffer buf = (IntegerBuffer) stack.pop();
            Object parent = stack.peek();
            if (parent instanceof Entry) {
                stack.pop(); // Key value: pop entry
                Entry entry = (Entry) parent;
                entry.value = buf.toInteger();
            } else {
                log("Error: did not expect integer: " + buf + " at parent: " + parent.getClass());
            }
        }

        private void handleTrue() {
            TrueBuffer buf = (TrueBuffer) stack.pop();
            Object parent = stack.peek();
            if (parent instanceof Entry) {
                stack.pop(); // Key value: pop entry
                Entry entry = (Entry) parent;
                entry.value = true;
            } else {
                log("Error: did not expect true: " + buf + " at parent: " + parent.getClass());
            }
        }

        private void handleFalse() {
            TrueBuffer buf = (TrueBuffer) stack.pop();
            Object parent = stack.peek();
            if (parent instanceof Entry) {
                stack.pop(); // Key value: pop entry
                Entry entry = (Entry) parent;
                entry.value = false;
            } else {
                log("Error: did not expect false: " + buf + " at parent: " + parent.getClass());
            }
        }

        @Override
        final public void characters(final char[] ch, final int start, final int len) {
            String str = new String(ch, start, len);
            if (str.trim().length() > 0) {
                Object peek = stack.peek();
                if (peek instanceof FastStringBuffer) {
                    FastStringBuffer buffer = (FastStringBuffer) peek;
                    buffer.append(str);

                } else if (peek instanceof IntegerBuffer) {
                    IntegerBuffer buffer = (IntegerBuffer) peek;
                    buffer.append(str);

                } else if (peek instanceof TrueBuffer || peek instanceof FalseBuffer) {

                } else if (peek instanceof Entry) {
                    Entry key = (Entry) peek;
                    key.key = str;

                } else {
                    log("Error: did not expect chars: " + str + " at: " + peek.getClass());
                }
            }
        }
    }

    public void parse(File tmFile) throws Exception {
        try (FileInputStream stream = new FileInputStream(tmFile)) {
            parse(stream);
        }
    }

    public void parse(InputStream stream) throws Exception {
        if (!(stream instanceof BufferedInputStream)) {
            stream = new BufferedInputStream(stream);
        }
        XMLReader reader = makeXMLReader();
        Handler handler = new Handler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(stream));

        Map<Object, Object> finalDict = new OrderedMap<>();
        Set<Map.Entry> entrySet = handler.rootDict.entrySet();
        for (Map.Entry object : entrySet) {
            Object value = object.getValue();
            if (value instanceof Entry) {
                value = ((Entry) value).value;
            }
            finalDict.put(object.getKey(), convert(object.getValue()));
        }

        this.rootDict = finalDict;
    }

    private Object convert(Object value) {
        if (value instanceof Entry) {
            value = ((Entry) value).value;
        }

        if (value instanceof Map) {
            Map map = (Map) value;
            Map<Object, Object> finalDict = new OrderedMap<>();
            Set<Map.Entry> entrySet = map.entrySet();

            for (Map.Entry object : entrySet) {
                finalDict.put(object.getKey(), convert(object.getValue()));
            }
            return finalDict;

        } else if (value instanceof List) {
            List list = (List) value;
            ArrayList<Object> newLst = new ArrayList<>(list.size());
            for (Object object : list) {
                newLst.add(convert(object));
            }
            return newLst;

        } else if (value instanceof String) {
            return value;

        } else if (value instanceof Integer) {
            return value;

        } else if (value instanceof Boolean) {
            return value;

        } else {
            throw new RuntimeException("Unexpected: " + value);

        }
    }

    public Object getValue(String key) {
        return this.rootDict.get(key);
    }

    public Map getMap(String path) {
        List<String> parts = StringUtils.split(path, "/");
        Map curr = this.rootDict;
        for (String part : parts) {
            Object object = curr.get(part);
            if (object instanceof Entry) {
                Entry entry = (Entry) object;
                object = entry.value;
            }
            curr = (Map) object;
        }
        return curr;
    }

    public void printContents() {
        printContents(System.out);
    }

    public void printContents(PrintStream out) {
        this.printDict(rootDict, 0, out);

    }

    private String createWithLevel(char c, int level) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < level; i++) {
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    private void printDict(Map dict, int level, PrintStream out) {
        String baseIndent = createWithLevel(' ', level * 2);

        Set<Map.Entry> entrySet = dict.entrySet();

        level += 1;
        String indent = createWithLevel(' ', level * 2).toString();

        boolean single = false;
        if (dict.size() == 1) {
            Object o = dict.values().iterator().next();
            if (o instanceof Entry) {
                o = ((Entry) o).value;
            }
            if (o instanceof String) {
                single = true;
            }
        }
        out.print("{");
        if (!single) {
            out.println();
        }

        for (Map.Entry entry : entrySet) {
            if (!single) {
                out.print(indent);
            }
            out.print(entry.getKey());
            out.print(": ");
            Object value = entry.getValue();
            if (value instanceof Entry) {
                value = ((Entry) value).value;
            }

            if (value instanceof Map) {
                printDict((Map) value, level, out);
            } else if (value instanceof List) {
                printArray((List) value, level, out);
            } else if (value instanceof String) {
                out.print(value);
            } else {
                System.err.print("Don't know what: " + value + " is.");
            }
            if (!single) {
                out.println();
            }
        }
        if (!single) {
            out.print(baseIndent);
        }
        out.print("}");

    }

    private void printArray(List lst, int level, PrintStream out) {
        String baseIndent = createWithLevel(' ', level * 2).toString();

        boolean single = false;
        if (lst.size() == 1) {
            Object o = lst.get(0);
            if (o instanceof Entry) {
                o = ((Entry) o).value;
            }
            if (o instanceof String) {
                single = true;
            }
        }
        out.print("[");
        if (!single) {
            out.println();
        }

        level += 1;
        String indent = createWithLevel(' ', level * 2).toString();
        for (Object value : lst) {
            if (!single) {
                out.print(indent);
            }
            if (value instanceof Entry) {
                value = ((Entry) value).value;
            }

            if (value instanceof Map) {
                printDict((Map) value, level, out);
            } else if (value instanceof List) {
                printArray((List) value, level, out);
            } else if (value instanceof String) {
                out.print(value);
            } else {
                System.err.print("Don't know what: " + value + " is.");
            }
            if (!single) {
                out.println();
            }
        }
        out.print(baseIndent);
        out.print("]");
    }

    public Map<String, ILiClipsePredicateRule> loadRepositoryRules(LiClipseLanguage language) {
        TmRulesConverter converter = new TmRulesConverter(language);

        Map<String, ILiClipsePredicateRule> ruleAliases = new TreeMap<>();
        Map repo = this.getMap(REPOSITORY);
        if (repo != null) {
            loadRepositoryRules(converter, ruleAliases, repo);
        }
        return ruleAliases;
    }

    private void loadRepositoryRules(TmRulesConverter converter, Map<String, ILiClipsePredicateRule> ruleAliases,
            Map repo) {
        Set<Map.Entry> entrySet = repo.entrySet();
        for (Map.Entry object : entrySet) {
            String key = object.getKey().toString();
            Object map = object.getValue();
            if (map instanceof Map) {
                Map map2 = (Map) map;
                ruleAliases.put(key, converter.convertDictToRule(map2, key));

                Object innerRepo2 = map2.get(REPOSITORY);
                if (innerRepo2 instanceof Map) {
                    Map map3 = (Map) innerRepo2;
                    loadRepositoryRules(converter, ruleAliases, map3);
                }
            } else {
                log("Expected: " + map + " to be a dict.");
            }
        }
    }

    private static void log(String msg) {
        //throw new RuntimeException(msg);
        Log.log(msg);
    }

    public LinkedList<ITextMateRule> loadRegularRules(LiClipseLanguage language) {
        TmRulesConverter converter = new TmRulesConverter(language);
        LinkedList<ITextMateRule> lst = new LinkedListWarningOnSlowOperations<>();
        List patterns = (List) this.getValue(PATTERNS);
        for (Object object : patterns) {
            ITextMateRule rule = converter.convertDictToRule((Map) object, "");
            lst.add(rule);
        }
        return lst;
    }

    public List<ScopeSelector> loadInjectionRules(LiClipseLanguage language) {
        TmRulesConverter converter = new TmRulesConverter(language);
        List<ScopeSelector> ret = new ArrayList<>();
        Map injections = (Map) this.getValue("injections");
        if (injections == null) {
            return ret;
        }

        Set<Map.Entry> entrySet = injections.entrySet();
        for (Map.Entry entry : entrySet) {
            String scope = entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map map = (Map) value;
                Object patterns = map.get("patterns");
                if (patterns instanceof List) {
                    List patternsLst = (List) patterns;
                    LinkedList<ITextMateRule> lst = new LinkedListWarningOnSlowOperations<>();
                    for (Object object : patternsLst) {
                        ITextMateRule rule = converter.convertDictToRule((Map) object, "");
                        lst.add(rule);
                    }
                    ret.add(new ScopeSelector(scope, lst));
                }
            }
        }

        return ret;
    }

}
