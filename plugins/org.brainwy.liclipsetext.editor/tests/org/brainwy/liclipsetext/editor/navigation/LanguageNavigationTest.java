package org.brainwy.liclipsetext.editor.navigation;

import java.util.List;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.navigation.LanguageNavigation.MatcherAndRegions;
import org.eclipse.core.internal.filebuffers.SynchronizableDocument;

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
}
