package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.editor.partitioning.IPartitionCodeReaderInScannerHelper;
import org.brainwy.liclipsetext.editor.partitioning.PartitionCodeReaderInScannerHelper;
import org.brainwy.liclipsetext.editor.partitioning.Utf8WithCharLen;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class PartitionCodeReaderInScannerHelperTest extends TestCase {

    public void testPartitionCodeReaderInScannerHelper() throws Exception {
        IPartitionCodeReaderInScannerHelper helper = new PartitionCodeReaderInScannerHelper();
        IDocument document = new Document("aa\nbb\ncc\n");
        helper.setDocument(document);

        Tuple<Utf8WithCharLen, Integer> lineFromOffsetAsBytes;

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(7);
        assertEquals("cc\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(1, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(0);
        assertEquals("aa\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(0, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(5);
        assertEquals("bb\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(2, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(1);
        assertEquals("aa\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(1, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(2);
        assertEquals("aa\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(2, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(3);
        assertEquals("bb\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(0, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(4);
        assertEquals("bb\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(1, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(6);
        assertEquals("cc\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(0, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(8);
        assertEquals("cc\n", new String(lineFromOffsetAsBytes.o1.getBytes()));
        assertEquals(2, (int) lineFromOffsetAsBytes.o2);

        lineFromOffsetAsBytes = helper.getLineFromOffsetAsBytes(9);
        assertNull(lineFromOffsetAsBytes);
    }
}
