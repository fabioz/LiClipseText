package org.brainwy.liclipsetext.editor.common;

import java.util.Arrays;

import org.brainwy.liclipsetext.shared_core.testutils.TestUtils;
import org.brainwy.liclipsetext.shared_ui.editor.BaseSourceViewer.KeepRangesUpdated;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyleRange;

import junit.framework.TestCase;

public class LiClipseSourceViewerTest extends TestCase {

    private String rangesToStr(StyleRange[] ranges) {
        String[] s = new String[ranges.length];
        for (int i = 0; i < s.length; i++) {
            s[i] = "" + ranges[i].start + ":" + ranges[i].length;
        }
        return TestUtils.listToExpected(Arrays.asList(s));
    }

    public void testKeepRangesUpdated() throws Exception {
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(2, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(2, 0, 2); // Add 2 chars to the end of the first partition
        assertEquals(TestUtils.listToExpected(
                "0:4",
                "4:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(0, 0, 2); // Add 2 chars to the start of the first partition
        assertEquals(TestUtils.listToExpected(
                "0:6",
                "6:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(2, 0, 2); // Add 2 chars to the middle of the first partition
        assertEquals(TestUtils.listToExpected(
                "0:8",
                "8:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(8, 0, 2); // Add 2 chars to the end of the first partition
        assertEquals(TestUtils.listToExpected(
                "0:10",
                "10:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(11, 0, 2); // Add 2 chars to the middle of the second partition
        assertEquals(TestUtils.listToExpected(
                "0:10",
                "10:4"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(14, 0, 2); // Add 2 chars to the end of the second partition
        assertEquals(TestUtils.listToExpected(
                "0:10",
                "10:6"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(17, 0, 2); // Add 2 chars out of range (no change)
        assertEquals(TestUtils.listToExpected(
                "0:10",
                "10:6"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(-1, 0, 2); // Out of range (no change)
        assertEquals(TestUtils.listToExpected(
                "0:10",
                "10:6"), rangesToStr(ranges));
    }

    public void testKeepRangesUpdated2() throws Exception {
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(2, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(2, 2, 0); // Remove the chars from the second partition
        assertEquals(TestUtils.listToExpected(
                "0:2",
                "2:0"), rangesToStr(ranges));
    }

    public void testKeepRangesUpdated3() throws Exception {
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(2, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(1, 1, 0); // Remove a single char from the first partition
        assertEquals(TestUtils.listToExpected(
                "0:1",
                "1:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(0, 1, 0); // Remove a single char from the first partition
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(0, 1, 0); // Remove a single char from the last partition
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:1"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(0, 1, 0); // Remove a single char from the last partition
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:0"), rangesToStr(ranges));
    }

    public void testKeepRangesUpdated4() throws Exception {
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(2, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(1, 2, 0); // Remove a char from the first and another from the second.
        assertEquals(TestUtils.listToExpected(
                "0:1",
                "1:1"), rangesToStr(ranges));
    }

    public void testKeepRangesUpdated5() throws Exception {
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(2, 2, null, null),
                new StyleRange(4, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(1, 2, 0); // Remove a char from the first and another from the second.
        assertEquals(TestUtils.listToExpected(
                "0:1",
                "1:1",
                "2:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(0, 1, 0); // Remove a char from the first
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:1",
                "1:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(2, 1, 0); // Remove a char from the last
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:1",
                "1:1"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(1, 1, 0); // Remove a char from the last
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:1",
                "1:0"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(0, 1, 0); // Remove a char from the middle
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:0",
                "0:0"), rangesToStr(ranges));
    }

    public void testKeepRangesUpdated6() throws Exception {
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(2, 2, null, null),
                new StyleRange(4, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(1, 4, 0); // Remove a char from the first, the whole second and another from the last.
        assertEquals(TestUtils.listToExpected(
                "0:1",
                "1:0",
                "1:1"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(1, 1, 0); // Remove a char from the last
        assertEquals(TestUtils.listToExpected(
                "0:1",
                "1:0",
                "1:0"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(0, 1, 0); // Remove a char from the first
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:0",
                "0:0"), rangesToStr(ranges));
    }

    public void testKeepRangesUpdated7() throws Exception {
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(2, 2, null, null),
                new StyleRange(4, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(1, 1, 1); // Remove and add a char to the middle partition
        assertEquals(TestUtils.listToExpected(
                "0:2",
                "2:2",
                "4:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(2, 2, 1); // Remove the middle partition and add a char to the first
        assertEquals(TestUtils.listToExpected(
                "0:3",
                "3:0",
                "3:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(0, 3, 1); // Remove the first partition and add a char to the last
        assertEquals(TestUtils.listToExpected(
                "0:0",
                "0:0",
                "0:3"), rangesToStr(ranges));

    }

    public void testKeepRangesUpdated8() throws Exception {
        // check with gaps
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(4, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(1, 1, 1); // Remove and add a char to the first partition
        assertEquals(TestUtils.listToExpected(
                "0:2",
                "4:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(3, 1, 1); // Remove and add a char from a gap
        assertEquals(TestUtils.listToExpected(
                "0:2",
                "4:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(2, 1, 1); // Remove from a gap and extend the first
        assertEquals(TestUtils.listToExpected(
                "0:3",
                "4:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(4, 0, 1); // Adding to gap
        assertEquals(TestUtils.listToExpected(
                "0:3",
                "5:2"), rangesToStr(ranges));

    }

    public void testKeepRangesUpdated9() throws Exception {
        // check with gaps
        StyleRange[] ranges = new StyleRange[] {
                new StyleRange(0, 2, null, null),
                new StyleRange(6, 2, null, null),
        };
        IDocument document = new Document();
        KeepRangesUpdated keepRangesUpdated = new LiClipseSourceViewer.KeepRangesUpdated(ranges, document);

        keepRangesUpdated.updateRanges(5, 0, 2); // Add to gap
        assertEquals(TestUtils.listToExpected(
                "0:2",
                "8:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(2, 5, 2); // Remove 5 from gap, add 2 to prev
        assertEquals(TestUtils.listToExpected(
                "0:4",
                "5:2"), rangesToStr(ranges));

        keepRangesUpdated.updateRanges(4, 2, 1); // Remove 2 (one from gap and another from last) and add 1 to prev
        assertEquals(TestUtils.listToExpected(
                "0:5",
                "6:1"), rangesToStr(ranges));

    }

}
