package org.brainwy.liclipsetext.editor.languages;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.TestUtils;
import org.brainwy.liclipsetext.editor.partitioning.ICustomPartitionTokenScanner;
import org.brainwy.liclipsetext.shared_core.partitioner.PartitionMerger;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.partitioner.TypedPositionWithSubTokens;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.Token;

import junit.framework.TestCase;

public class ScopeNavigationMatchTest extends TestCase {

    public void testSortAndMerge() throws Exception {
        Position[] positions = new Position[] { new TypedPosition(5, 5, "tag"), new TypedPosition(12, 2, "tag") };
        List<TypedPosition> merged = PartitionMerger.sortAndMergePositions(positions, 15);
        ArrayList<String> found = new ArrayList<String>();
        for (TypedPosition typedPosition : merged) {
            found.add(typedPosition.getType() + ":" + typedPosition.getOffset() + ":" + typedPosition.getLength());
        }
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:5",
                "tag:5:5",
                "__dftl_partition_content_type:10:2",
                "tag:12:2",
                "__dftl_partition_content_type:14:1"), TestUtils.listToExpected(found));
    }

    public void testSortAndMerge2() throws Exception {
        Position[] positions = new Position[] {
                new TypedPosition(0, 1, ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE),
                new TypedPosition(1, 2, ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE),
                new TypedPosition(3, 2, "tag"),
                new TypedPosition(10, 1, ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE) };
        List<TypedPosition> merged = PartitionMerger.sortAndMergePositions(positions, 15);
        ArrayList<String> found = new ArrayList<String>();
        for (TypedPosition typedPosition : merged) {
            found.add(typedPosition.getType() + ":" + typedPosition.getOffset() + ":" + typedPosition.getLength());
        }
        assertEquals(TestUtils.listToExpected("__dftl_partition_content_type:0:3",
                "tag:3:2",
                "__dftl_partition_content_type:5:10"), TestUtils.listToExpected(found));
    }

    public void testSortAndMergeWithSubTokens() throws Exception {
        Position[] positions = new Position[] {
                new TypedPositionWithSubTokens(0, 1, ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE,
                        new SubRuleToken(new Token("sub1"), 0, 1)),

                new TypedPositionWithSubTokens(1, 2, ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE,
                        new SubRuleToken(new Token("sub2"), 1, 2)),

                new TypedPositionWithSubTokens(3, 2, "tag", new SubRuleToken(new Token("sub3"), 3, 2)),

                new TypedPositionWithSubTokens(10, 1, ICustomPartitionTokenScanner.DEFAULT_CONTENT_TYPE,
                        new SubRuleToken(new Token("sub4"), 10, 1)),

                new TypedPositionWithSubTokens(11, 1, "t2",
                        new SubRuleToken(new Token("sub5"), 11, 1)),

                new TypedPositionWithSubTokens(12, 1, "t2",
                        new SubRuleToken(new Token("sub6"), 12, 1)),

                new TypedPositionWithSubTokens(13, 1, "t2",
                        new SubRuleToken(new Token("sub7"), 13, 1)),

                new TypedPositionWithSubTokens(14, 1, "t3",
                        new SubRuleToken(new Token("sub8"), 14, 1)),

                new TypedPositionWithSubTokens(15, 1, "t3",
                        new SubRuleToken(new Token("sub8"), 15, 1)),
        };
        List<TypedPosition> merged = PartitionMerger.sortAndMergePositions(positions, 19);
        ArrayList<String> found = new ArrayList<String>();
        for (TypedPosition typedPosition : merged) {
            found.add(((TypedPositionWithSubTokens) typedPosition).toStringTest());
        }
        assertEquals("\"__dftl_partition_content_type:0:3 [\n" +
                " SubRuleToken: null offset: 0 len: 3 children:[\n" +
                "   SubRuleToken: sub1 offset: 0 len: 1\n" +
                "   SubRuleToken: sub2 offset: 1 len: 2\n" +
                "]\n" +
                "]\",\n" +
                "\"tag:3:2 [\n" +
                " SubRuleToken: sub3 offset: 0 len: 2\n" +
                "]\",\n" +
                "\"__dftl_partition_content_type:5:6 [\n" +
                " SubRuleToken: sub4 offset: 0 len: 1\n" +
                "]\",\n" +
                "\"t2:11:3 [\n" +
                " SubRuleToken: null offset: 0 len: 3 children:[\n" +
                "   SubRuleToken: sub5 offset: 0 len: 1\n" +
                "   SubRuleToken: sub6 offset: 1 len: 1\n" +
                "   SubRuleToken: sub7 offset: 2 len: 1\n" +
                "]\n" +
                "]\",\n" +
                "\"t3:14:2 [\n" +
                " SubRuleToken: sub8 offset: 0 len: 2\n" +
                "]\",\n" +
                "\"__dftl_partition_content_type:16:3 [\n" +
                " null\n" +
                "]\"", TestUtils.listToExpected(found));
    }
}
