package org.brainwy.liclipsetext.shared_core.document;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

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

}
