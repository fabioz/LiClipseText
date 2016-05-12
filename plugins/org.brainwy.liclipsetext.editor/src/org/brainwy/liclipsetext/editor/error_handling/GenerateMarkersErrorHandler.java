/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.error_handling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.eclipse.core.resources.IMarker;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@SuppressWarnings({ "rawtypes" })
public class GenerateMarkersErrorHandler implements ErrorHandler, ErrorListener {

    private final FastStringBuffer out;
    private final List<Map> markers = new ArrayList<Map>();

    public Map[] getMarkers() {
        return markers.toArray(new Map[markers.size()]);
    }

    public GenerateMarkersErrorHandler() {
        out = new FastStringBuffer();
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException e) throws SAXException {
        out.clear();
        out.append("Warning:\n");
        generateSaxException(e, IMarker.SEVERITY_WARNING);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e) throws SAXException {
        out.clear();
        out.append("Error:\n");
        generateSaxException(e, IMarker.SEVERITY_ERROR);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException e) throws SAXException {
        out.clear();
        out.append("Fatal Error:\n");
        generateSaxException(e, IMarker.SEVERITY_ERROR);
    }

    public void error(TransformerException e) throws TransformerException {
        out.clear();
        out.append("Error:\n");
        out.append(e.getMessage());
        generateMarkerForTransformerException(e, IMarker.SEVERITY_ERROR);

    }

    public void fatalError(TransformerException e) throws TransformerException {
        out.clear();
        out.append("Fatal Error:\n");
        out.append(e.getMessage());
        generateMarkerForTransformerException(e, IMarker.SEVERITY_ERROR);

    }

    public void warning(TransformerException e) throws TransformerException {
        out.clear();
        out.append("Warning:\n");
        out.append(e.getMessage());
        generateMarkerForTransformerException(e, IMarker.SEVERITY_WARNING);
    }

    // Exception generation ------------------------------------------------------------------------

    private void generateSaxException(SAXParseException e, int severity) {
        out.append(e.getMessage());
        out.append("\nLine: ");
        out.append(Integer.toString(e.getLineNumber()));
        out.append(" Col: ");
        out.append(Integer.toString(e.getColumnNumber()));
        out.append("\n\n");
        generateMarker(e.getLineNumber(), e.getColumnNumber(), severity);
    }

    private void generateMarkerForTransformerException(TransformerException e, int severity) {
        int lineNumber = 0;
        int columnNumber = 0;
        SourceLocator sourceLocator = e.getLocator();
        if (sourceLocator != null) {
            out.append("\nLine: ");
            lineNumber = sourceLocator.getLineNumber();
            out.append(Integer.toString(lineNumber));
            out.append(" Col: ");
            columnNumber = sourceLocator.getColumnNumber();
            out.append(Integer.toString(columnNumber));
        }
        out.append("\n\n");
        generateMarker(lineNumber, columnNumber, severity);
    }

    private void generateMarker(int line, int col, int severity) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(IMarker.MESSAGE, out.toString());
        map.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        map.put(IMarker.LINE_NUMBER, line);
        map.put(IMarker.SEVERITY, severity);
        map.put(IMarker.TRANSIENT, true);
        markers.add(map);
        out.clear();
    }

}
