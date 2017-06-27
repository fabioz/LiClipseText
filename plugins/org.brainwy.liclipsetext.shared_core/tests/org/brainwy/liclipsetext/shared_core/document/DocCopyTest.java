package org.brainwy.liclipsetext.shared_core.document;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;

import junit.framework.TestCase;

public class DocCopyTest extends TestCase {

    public void testDocCopy() throws BadLocationException {
        Document doc = new Document("abc");
        DocCopy copy = new DocCopy(doc);
        assertEquals(doc.get(0, 3), copy.get(0, 3));

        try {
            copy.get(0, 4);
            fail("Expected BadLocationException.");
        } catch (BadLocationException e) {
            assertEquals("Bad location. Start Offset: 0 Final offset: 4 len: 4 doc len: 3", e.getMessage());
        }
    }

    public void testDocCopyPositionBinarySearch() throws Exception {
        Position[] pos = new Position[] { new Position(0, 1), new Position(1, 1), new Position(2, 1) };
        assertEquals(-1, DocCopy.binarySearch(pos, -1));
        assertEquals(0, DocCopy.binarySearch(pos, 0));
        assertEquals(1, DocCopy.binarySearch(pos, 1));
        assertEquals(2, DocCopy.binarySearch(pos, 2));
        assertEquals(-1, DocCopy.binarySearch(pos, 3));

    }

    public void testDocCopyPositionBinarySearch2() throws Exception {
        Position[] pos = new Position[] { new Position(0, 2), new Position(2, 1), new Position(3, 2) };
        assertEquals(-1, DocCopy.binarySearch(pos, -1));
        assertEquals(0, DocCopy.binarySearch(pos, 0));
        assertEquals(0, DocCopy.binarySearch(pos, 1));

        assertEquals(1, DocCopy.binarySearch(pos, 2));

        assertEquals(2, DocCopy.binarySearch(pos, 3));
        assertEquals(2, DocCopy.binarySearch(pos, 4));

        assertEquals(-1, DocCopy.binarySearch(pos, 5));
    }

    public void testDocCopyPositionBinarySearchHoles() throws Exception {
        Position[] pos = new Position[] { new Position(0, 2), new Position(3, 1), new Position(5, 2) };
        assertEquals(-1, DocCopy.binarySearch(pos, -1));

        assertEquals(0, DocCopy.binarySearch(pos, 0));
        assertEquals(0, DocCopy.binarySearch(pos, 1));

        assertEquals(-1, DocCopy.binarySearch(pos, 2));

        assertEquals(1, DocCopy.binarySearch(pos, 3));

        assertEquals(-1, DocCopy.binarySearch(pos, 4));

        assertEquals(2, DocCopy.binarySearch(pos, 5));
        assertEquals(2, DocCopy.binarySearch(pos, 6));

        assertEquals(-1, DocCopy.binarySearch(pos, 7));
    }
}
