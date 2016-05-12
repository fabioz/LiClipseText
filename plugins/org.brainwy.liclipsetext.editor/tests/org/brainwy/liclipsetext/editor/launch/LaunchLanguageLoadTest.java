package org.brainwy.liclipsetext.editor.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.launch.ConsoleLink;
import org.brainwy.liclipsetext.editor.languages.launch.ILinkCreator;
import org.brainwy.liclipsetext.editor.languages.launch.LanguageLaunch;
import org.brainwy.liclipsetext.shared_core.locator.BaseItemPointer;
import org.eclipse.ui.console.IHyperlink;

public class LaunchLanguageLoadTest extends TestCase {

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

    public void testPythonLaunchSettings() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("python.liclipse");
        LanguageLaunch launch = language.getLaunch();
        assertEquals(
                "Regexp: .*(File) \\\"([^\\\"]*)\\\", line (\\d*).*\n" +
                        "Filename group:2\n" +
                        "Line group:3\n" +
                        "Start group:1\n"
                        + "End group:null\n"
                , launch.linkToString());

        final List<BaseItemPointer> lst = new ArrayList<>();
        ILinkCreator linkCreator = new ILinkCreator() {

            @Override
            public void addLink(IHyperlink link, int offset, int length) {
                if (link instanceof ConsoleLink) {
                    ConsoleLink consoleLink = (ConsoleLink) link;
                    lst.add(consoleLink.pointer);
                }

            }
        };
        launch.createLinks(0, 50, "  File \"W:/bbn.py\", line 5, in m1", linkCreator);

        assertEquals(1, lst.size());
        BaseItemPointer pointer = lst.get(0);
        assertEquals(pointer.file, new File("W:/bbn.py"));

    }

    public void testJuliaLaunchSettings() throws Exception {
        LiClipseLanguage language = TestUtils.loadLanguageFile("julia.liclipse");
        LanguageLaunch launch = language.getLaunch();
        assertEquals(
                ""
                        + "Regexp: .*(at) ([^\\\"]*):(\\d*).*\n"
                        + "Filename group:2\n"
                        + "Line group:3\n"
                        + "Start group:1\n"
                        + "End group:null\n"
                        + "Regexp: .*(while loading) ([^\\\"]*), in expression starting on line (\\d*).*\n"
                        + "Filename group:2\n"
                        + "Line group:3\n"
                        + "Start group:1\n"
                        + "End group:null\n"
                , launch.linkToString());

        final List<BaseItemPointer> lst = new ArrayList<>();
        ILinkCreator linkCreator = new ILinkCreator() {

            @Override
            public void addLink(IHyperlink link, int offset, int length) {
                if (link instanceof ConsoleLink) {
                    ConsoleLink consoleLink = (ConsoleLink) link;
                    lst.add(consoleLink.pointer);
                }

            }
        };
        launch.createLinks(0, 50, " at x.jl:5", linkCreator);

        assertEquals(1, lst.size());
        BaseItemPointer pointer = lst.get(0);
        assertEquals(pointer.file, new File("x.jl"));

    }
}
