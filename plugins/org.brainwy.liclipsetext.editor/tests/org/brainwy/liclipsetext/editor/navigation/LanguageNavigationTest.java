package org.brainwy.liclipsetext.editor.navigation;

import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.navigation.LanguageNavigation.MatcherAndRegions;
import org.eclipse.core.internal.filebuffers.SynchronizableDocument;
import org.eclipse.jface.text.IRegion;

import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class LanguageNavigationTest extends TestCase {

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

    public void testLanguageNavigation() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("xml.liclipse");

        String txt = "<xml>\n"
                + "    <test></test>\n"
                + "</xml>\n";

        SynchronizableDocument document = new SynchronizableDocument();
        document.set(txt);
        Object lockObject = document.getLockObject();
        if (lockObject == null) {
            document.setLockObject(new Object());
        }

        language.connect(document);

        List<MatcherAndRegions> findAll = language.getNavigation().findAll(document);
        assertEquals(1, findAll.size());
        MatcherAndRegions matcherAndRegions = findAll.get(0);
        assertEquals(2, matcherAndRegions.regions.size());
        assertEquals(TestUtils.listToExpected("RegionAndText[ text: xml region: offset: 1, length: 3 kind: null ]",
                "RegionAndText[ text: test region: offset: 11, length: 4 kind: null ]"),
                (TestUtils.listToExpected(TestUtils.toStringList(matcherAndRegions.regions))));
    }

    public void testLanguageNavigationRegexp() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("markdown.tmbundle",
                "markdown.tmbundle-master/Syntaxes/Markdown.tmLanguage");

        String txt = "test\n---------\n\nfoo\n--------------\n\n";

        SynchronizableDocument document = new SynchronizableDocument();
        document.set(txt);
        Object lockObject = document.getLockObject();
        if (lockObject == null) {
            document.setLockObject(new Object());
        }

        language.connect(document);

        List<MatcherAndRegions> findAll = language.getNavigation().findAll(document);
        assertEquals(1, findAll.size());
        MatcherAndRegions matcherAndRegions = findAll.get(0);
        assertEquals(2, matcherAndRegions.regions.size());
        assertEquals(TestUtils.listToExpected("RegionAndText[ text: test region: offset: 0, length: 4 kind: null ]",
                "RegionAndText[ text: foo region: offset: 16, length: 3 kind: null ]"),
                (TestUtils.listToExpected(TestUtils.toStringList(matcherAndRegions.regions))));

        IRegion find = language.getNavigation().find(true, document, -1);
        assertEquals(0, find.getOffset());
        assertEquals(4, find.getLength());

        find = language.getNavigation().find(true, document, 0);
        assertEquals(16, find.getOffset());
        assertEquals(3, find.getLength());

        find = language.getNavigation().find(true, document, 1);
        assertEquals(16, find.getOffset());
        assertEquals(3, find.getLength());

        find = language.getNavigation().find(false, document, 20);
        assertEquals(16, find.getOffset());
        assertEquals(3, find.getLength());

        find = language.getNavigation().find(false, document, 17);
        assertEquals(0, find.getOffset());
        assertEquals(4, find.getLength());
    }

    public void testLanguageNavigationRegexp2() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("markdown.tmbundle",
                "markdown.tmbundle-master/Syntaxes/Markdown.tmLanguage");

        String txt = " # test\n\n\n## foo\n";

        SynchronizableDocument document = new SynchronizableDocument();
        document.set(txt);
        Object lockObject = document.getLockObject();
        if (lockObject == null) {
            document.setLockObject(new Object());
        }

        language.connect(document);

        List<MatcherAndRegions> findAll = language.getNavigation().findAll(document);
        assertEquals(1, findAll.size());
        MatcherAndRegions matcherAndRegions = findAll.get(0);
        assertEquals(2, matcherAndRegions.regions.size());
        assertEquals(TestUtils.listToExpected("RegionAndText[ text: test region: offset: 3, length: 4 kind: null ]",
                "RegionAndText[ text: foo region: offset: 13, length: 3 kind: null ]"),
                (TestUtils.listToExpected(TestUtils.toStringList(matcherAndRegions.regions))));

        IRegion find = language.getNavigation().find(true, document, 0);
        assertEquals(3, find.getOffset());
        assertEquals(4, find.getLength());

        find = language.getNavigation().find(true, document, 3);
        assertEquals(13, find.getOffset());
        assertEquals(3, find.getLength());
    }
}
