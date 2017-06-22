package org.brainwy.liclipsetext.editor.common.partitioning;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataFileInfo;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataInMemoryFileInfo;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataTmBundleZipFileInfo;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.editor.partitioning.ScannerRange;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple3;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.custom.StyleRange;

public class TestUtils {

    public static File getLanguagesDir() {
        URL url = PartitioningSetupIOTest.class.getClassLoader()
                .getResource("org/brainwy/liclipsetext/editor/common/partitioning/PartitioningSetupIOTest.class");
        File path = new File(url.getPath());
        String fullPath = path.toString();
        int i = fullPath.lastIndexOf("org.brainwy.liclipsetext.editor");
        IPath p = Path.fromOSString(fullPath.substring(0, i));
        IPath languagesDir = p.append("org.brainwy.liclipsetext.editor").append("languages");
        File languagesDirFile = languagesDir.toFile();
        Assert.isTrue(languagesDirFile.exists());
        return languagesDirFile;
    }

    public static File getTestLanguagesDir() {
        URL url = PartitioningSetupIOTest.class.getClassLoader()
                .getResource("org/brainwy/liclipsetext/editor/common/partitioning/PartitioningSetupIOTest.class");
        File path = new File(url.getPath());
        String fullPath = path.toString();
        int i = fullPath.lastIndexOf("org.brainwy.liclipsetext.editor");
        IPath p = Path.fromOSString(fullPath.substring(0, i));
        IPath languagesDir = p.append("org.brainwy.liclipsetext.editor").append("tests").append("languages");
        File languagesDirFile = languagesDir.toFile();
        Assert.isTrue(languagesDirFile.exists());
        return languagesDirFile;
    }

    public static File getTestCtagsDir() {
        URL url = PartitioningSetupIOTest.class.getClassLoader()
                .getResource("org/brainwy/liclipsetext/editor/common/partitioning/PartitioningSetupIOTest.class");
        File path = new File(url.getPath());
        String fullPath = path.toString();
        int i = fullPath.lastIndexOf("org.brainwy.liclipsetext.editor");
        IPath p = Path.fromOSString(fullPath.substring(0, i));
        IPath languagesDir = p.append("org.brainwy.liclipsetext.editor").append("tests").append("test_ctags");
        File languagesDirFile = languagesDir.toFile();
        Assert.isTrue(languagesDirFile.exists());
        return languagesDirFile;
    }

    public static File getTestSwiftUnicodeDir() {
        URL url = PartitioningSetupIOTest.class.getClassLoader()
                .getResource("org/brainwy/liclipsetext/editor/common/partitioning/PartitioningSetupIOTest.class");
        File path = new File(url.getPath());
        String fullPath = path.toString();
        int i = fullPath.lastIndexOf("org.brainwy.liclipsetext.editor");
        IPath p = Path.fromOSString(fullPath.substring(0, i));
        IPath languagesDir = p.append("org.brainwy.liclipsetext.editor").append("tests").append("test_swift_unicode");
        File languagesDirFile = languagesDir.toFile();
        Assert.isTrue(languagesDirFile.exists());
        return languagesDirFile;
    }

    public static LiClipseLanguage loadLanguageFile(String string) throws Exception {
        return loadLanguageFile(string, null);
    }

    /**
     * Loads a language file and returns it the loaded setup for it.
     *
     * I.e.: "xml.liclipse", "python.liclipse"
     */
    public static LiClipseLanguage loadLanguageFile(String string, String zipPath) throws Exception {
        File file = new File(getLanguagesDir(), string);
        if (!file.exists()) {
            file = new File(getTestLanguagesDir(), string);
        }
        if (zipPath != null) {
            return new LanguageMetadataTmBundleZipFileInfo(file, zipPath).loadLanguage(true);
        }
        return new LanguageMetadataFileInfo(file, (File) null).loadLanguage(true);
    }

    public static LiClipseLanguage loadLanguageFromContents(String contents) throws Exception {
        return new LanguageMetadataInMemoryFileInfo(contents).loadLanguage(true);
    }

    public static String partition(IDocument document) throws Exception {
        return org.brainwy.liclipsetext.shared_core.testutils.TestUtils.getContentTypesAsStr(document);
    }

    public static List<String> partitionAsList(IDocument document) throws Exception {
        return org.brainwy.liclipsetext.shared_core.testutils.TestUtils.getContentTypesAsList(document);
    }

    public static String listToExpected(List<String> expected) {
        return listToExpected(expected.toArray(new String[expected.size()]));
    }

    public static String listToExpected(String... expected) {
        return org.brainwy.liclipsetext.shared_core.testutils.TestUtils.listToExpected(expected);
    }

    @SuppressWarnings("rawtypes")
    public static List<String> toStringList(List lst) {
        ArrayList<String> newLst = new ArrayList<>(lst.size());
        for (Object o : lst) {
            newLst.add(String.valueOf(o));
        }
        return newLst;
    }

    //    public static String scan(Object scanner, IDocument document) {
    //        return scan((ITokenScanner) scanner, document);
    //    }

    public static String scan(ITokenScanner scanner, IDocument document) {
        scanner.setRange(document, 0, document.getLength());
        return scan(scanner);
    }

    public static String scan(ITokenScanner scanner) {
        return scan(scanner, true);
    }

    public static String scan(ITokenScanner scanner, boolean join) {
        if (!join) {
            return scanNoJoin(scanner);
        }
        ArrayList<String> found = new ArrayList<String>();
        FastStringBuffer buf = new FastStringBuffer();
        IToken token = scanner.nextToken();
        Tuple3<IToken, Integer, Integer> tup = new Tuple3<>(token, scanner.getTokenOffset(),
                scanner.getTokenLength());

        while (tup != null && !tup.o1.isEOF()) {
            Tuple3<IToken, Integer, Integer> lookaheadTup = null;

            while (true) {
                token = scanner.nextToken();
                lookaheadTup = new Tuple3<>(token, scanner.getTokenOffset(), scanner.getTokenLength());
                if (lookaheadTup.o1.isEOF()) {
                    break;
                }
                String contentTypeFromTokenLookahead = getTokenRep(lookaheadTup.o1);
                String contentTypeFromToken = getTokenRep(tup.o1);
                //only null can be joined!
                if ("null".equals(contentTypeFromToken) && contentTypeFromTokenLookahead.equals(contentTypeFromToken)
                        && tup.o2 + tup.o3 == lookaheadTup.o2) {
                    tup.o3 += lookaheadTup.o3;
                } else {
                    break;
                }
            }

            buf.clear();
            buf.append(getTokenRep(tup.o1));
            buf.append(":");
            buf.append(tup.o2).append(":");
            buf.append(tup.o3);
            found.add(buf.toString());
            tup = lookaheadTup;
        }
        return listToExpected(found);
    }

    private static String scanNoJoin(ITokenScanner scanner) {
        ArrayList<String> found = new ArrayList<String>();
        FastStringBuffer buf = new FastStringBuffer();
        IToken token = scanner.nextToken();
        while (!token.isEOF()) {
            Object data = token.getData();
            int tokenLength = scanner.getTokenLength();
            if (tokenLength != 0) {
                if (data != null) {
                    buf.clear();
                    String contentTypeFromToken = LiClipseTextAttribute.getContentTypeFromToken(token);
                    buf.append(contentTypeFromToken).append(":");
                    buf.append(scanner.getTokenOffset()).append(":");
                    buf.append(tokenLength);
                    found.add(buf.toString());
                } else {
                    buf.clear();
                    buf.append("null").append(":");
                    buf.append(scanner.getTokenOffset()).append(":");
                    buf.append(tokenLength);
                    found.add(buf.toString());
                }
            }
            token = scanner.nextToken();
        }
        return listToExpected(found);
    }

    private static String getTokenRep(IToken o1) {
        if (o1.getData() == null) {
            return "null";
        }
        return LiClipseTextAttribute.getContentTypeFromToken(o1);
    }

    public static class TextAttributeWithData extends TextAttribute {

        public String data;

        public TextAttributeWithData(String data) {
            super(null);
            if (data == null) {
                throw new AssertionError("Data may not be null.");
            }
            this.data = data;
        }
    }

    public static String textPresentationToExpected(TextPresentation textPresentation) {
        List<String> lst = TestUtils.textPresentationToLst(textPresentation);
        return TestUtils.listToExpected(lst);
    }

    public static List<String> textPresentationToLst(TextPresentation textPresentation) {
        Iterator<StyleRange> allStyleRangeIterator = textPresentation
                .getAllStyleRangeIterator();
        List<String> lst = new ArrayList<>();
        FastStringBuffer buf = new FastStringBuffer();
        while (allStyleRangeIterator.hasNext()) {
            StyleRange styleRange = allStyleRangeIterator.next();
            buf.clear();
            Token token = (Token) styleRange.data;
            lst.add(buf
                    .appendObject(token.getData())
                    .append(':')
                    .append(styleRange.start)
                    .append(':')
                    .append(styleRange.length)
                    .toString());
        }
        return lst;
    }

    public static LiClipseLanguage connectDocumentToLanguage(IDocument doc, String loadLanguage) throws Exception {
        return connectDocumentToLanguage(doc, loadLanguage, null);
    }

    public static LiClipseLanguage connectDocumentToLanguage(IDocument doc, String loadLanguage, String zipPath)
            throws Exception {
        LiClipseLanguage language = loadLanguageFile(loadLanguage, zipPath);
        language.connect(doc);
        return language;
    }

    public static void startEditorPlugin() {
        if (LiClipseTextEditorPlugin.getDefault() != null) {
            throw new AssertionError("Plugin seems to be started already.");
        }
        LiClipseTextEditorPlugin plugin = new LiClipseTextEditorPlugin();
        plugin.startPlugin(TestUtils.getLanguagesDir(), TestUtils.getTestLanguagesDir());
    }

    public static void stopEditorPlugin() {
        LiClipseTextEditorPlugin.getDefault().stopPlugin();

    }

    public static LiClipsePartitionScanner createScanner(String language) throws Exception {
        return createScanner(language, IDocument.DEFAULT_CONTENT_TYPE);
    }

    /**
     * @param language python.liclipse, xml.liclipse, etc.
     */
    public static LiClipsePartitionScanner createScanner(String language, String contentType) throws Exception {
        LiClipseLanguage partitioningSetup = TestUtils.loadLanguageFile(language);
        ScopeColorScanning scopeColoringScanning = partitioningSetup.scopeToScopeColorScanning
                .get(contentType);
        LiClipsePartitionScanner scanner = new LiClipsePartitionScanner(scopeColoringScanning, partitioningSetup);
        return scanner;
    }

    public static void checkScan(IDocument document, LiClipsePartitionScanner scanner, String... expected)
            throws Exception {
        String found = TestUtils.scan(scanner, document);
        org.junit.Assert.assertEquals(TestUtils.listToExpected(expected), found);
    }

    public static void checkPartitions(IDocument document, String... expected) throws Exception {
        String found = TestUtils.partition(document);
        org.junit.Assert.assertEquals(TestUtils.listToExpected(expected), found);
    }

    public static File configLanguagesManager() {
        //Note: optimization: if the environment is set, don't set it up again.
        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        File languagesDirFile = TestUtils.getLanguagesDir();
        if (languagesManager == null) {
            LanguagesManager manager = new LanguagesManager(languagesDirFile);
            LiClipseTextEditorPlugin.setLanguagesManager(manager);
        }
        return languagesDirFile;
    }

    public static void clearLanguagesManager() {
        //Note: optimization: don't clear the environment afterwards!
        //LiClipseTextEditorPlugin.setLanguagesManager(null);
    }

    private static IColorCache colorManager = new DummyColorCache(false);

    public static void connectPresentationReconciler(DummyTextViewer dummyTextViewer) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        IDocument document = dummyTextViewer.getDocument();
        LiClipseDocumentPartitioner partitioner = (LiClipseDocumentPartitioner) document.getDocumentPartitioner();
        IPresentationReconciler presentationReconciler = partitioner
                .getPresentationReconciler(colorManager);
        presentationReconciler.install(dummyTextViewer);
    }

    public static String scanAll(LiClipseLanguage language, IDocument document, List<String> asList) {
        String last = null;
        for (String p : asList) {
            List<String> split = StringUtils.split(p, ':');
            LiClipseDocumentPartitioner documentPartitioner = (LiClipseDocumentPartitioner) document
                    .getDocumentPartitioner();
            Map<String, ICustomPartitionTokenScanner> contentTypeToScanner = new HashMap<>();
            ICustomPartitionTokenScanner scannerForContentType = documentPartitioner.obtainTokenScannerForContentType(
                    split.get(0), contentTypeToScanner, language);

            int start = Integer.parseInt(split.get(1));
            int len;
            if (split.size() == 3) {
                len = Integer.parseInt(split.get(2)) - start;
            } else {
                len = document.getLength() - start;
            }

            ScannerRange range = scannerForContentType.createScannerRange(document, start, len);
            scannerForContentType.clearCache(document, start);
            String scan = TestUtils.scan(scannerForContentType, range, false);
            last = scan;
        }
        return last;
    }

    public static String scan(ICustomPartitionTokenScanner scanner, ScannerRange range, boolean join) {
        if (!join) {
            return scanNoJoin(scanner, range);
        }
        ArrayList<String> found = new ArrayList<String>();
        FastStringBuffer buf = new FastStringBuffer();
        range.nextToken(scanner);
        Tuple3<IToken, Integer, Integer> tup = new Tuple3<>(range.getToken(), range.getTokenOffset(),
                range.getTokenLength());

        while (tup != null && !tup.o1.isEOF()) {
            Tuple3<IToken, Integer, Integer> lookaheadTup = null;

            while (true) {
                range.nextToken(scanner);
                lookaheadTup = new Tuple3<>(range.getToken(), range.getTokenOffset(), range.getTokenLength());
                if (lookaheadTup.o1.isEOF()) {
                    break;
                }
                String contentTypeFromTokenLookahead = getTokenRep(lookaheadTup.o1);
                String contentTypeFromToken = getTokenRep(tup.o1);
                //only null can be joined!
                if ("null".equals(contentTypeFromToken) && contentTypeFromTokenLookahead.equals(contentTypeFromToken)
                        && tup.o2 + tup.o3 == lookaheadTup.o2) {
                    tup.o3 += lookaheadTup.o3;
                } else {
                    break;
                }
            }

            buf.clear();
            buf.append(getTokenRep(tup.o1));
            buf.append(":");
            buf.append(tup.o2).append(":");
            buf.append(tup.o3);
            found.add(buf.toString());
            tup = lookaheadTup;
        }
        return listToExpected(found);
    }

    private static String scanNoJoin(ICustomPartitionTokenScanner scanner, ScannerRange range) {
        ArrayList<String> found = new ArrayList<String>();
        FastStringBuffer buf = new FastStringBuffer();
        range.nextToken(scanner);
        while (!range.getToken().isEOF()) {
            Object data = range.getToken().getData();
            int tokenLength = range.getTokenLength();
            if (tokenLength != 0) {
                if (data != null) {
                    buf.clear();
                    String contentTypeFromToken = LiClipseTextAttribute.getContentTypeFromToken(range.getToken());
                    buf.append(contentTypeFromToken).append(":");
                    buf.append(range.getTokenOffset()).append(":");
                    buf.append(tokenLength);
                    found.add(buf.toString());
                } else {
                    buf.clear();
                    buf.append("null").append(":");
                    buf.append(range.getTokenOffset()).append(":");
                    buf.append(tokenLength);
                    found.add(buf.toString());
                }
            }
            range.nextToken(scanner);
        }
        return listToExpected(found);
    }

    public static String scan(ICustomPartitionTokenScanner scannerForContentType, ScannerRange range) {
        return scan(scannerForContentType, range, true);
    }

    public static String scan(ICustomPartitionTokenScanner scanner, IDocument document) {
        return scan(scanner, scanner.createScannerRange(document, 0, document.getLength()));
    }

}
