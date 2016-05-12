package org.brainwy.liclipsetext.editor.regexp;

import junit.framework.TestCase;

import org.brainwy.liclipsetext.editor.regexp.QuoteRegexp;
import org.jcodings.specific.UTF8Encoding;

public class QuoteRegexpTest extends TestCase {

    public void testQuoteRegexp() throws Exception {
        assertEquals("", new String(QuoteRegexp.quote(new byte[] {}, UTF8Encoding.INSTANCE)));
        assertEquals("\\t", new String(QuoteRegexp.quote(new byte[] { '\t' }, UTF8Encoding.INSTANCE)));
        assertEquals("a\\t", new String(QuoteRegexp.quote(new byte[] { 'a', '\t' }, UTF8Encoding.INSTANCE)));
        assertEquals("a\\t\\r\\n\\\\1",
                new String(QuoteRegexp.quote(new byte[] { 'a', '\t', '\r', '\n', '\\', '1' }, UTF8Encoding.INSTANCE)));
        assertEquals("ab",
                new String(QuoteRegexp.quote(new byte[] { 'a', 'b' }, UTF8Encoding.INSTANCE)));
        assertEquals("aç\\n",
                new String(QuoteRegexp.quote("aç\n".getBytes("utf-8"), UTF8Encoding.INSTANCE)));
    }
}
