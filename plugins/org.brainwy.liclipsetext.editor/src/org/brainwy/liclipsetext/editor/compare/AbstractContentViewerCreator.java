/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.LiClipseSourceViewerConfiguration;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class AbstractContentViewerCreator implements IViewerCreator {

    private final String type;

    public AbstractContentViewerCreator(String type) {
        this.type = type;
    }

    @Override
    public Viewer createViewer(Composite parent, CompareConfiguration config) {
        return new ContentViewer(parent, type);
    }

    static class ContentViewer extends Viewer {

        private SourceViewer fSourceViewer;
        private Object fInput;
        private final String type;

        ContentViewer(Composite parent, String type) {
            fSourceViewer = new SourceViewer(parent, null, SWT.LEFT_TO_RIGHT | SWT.H_SCROLL | SWT.V_SCROLL);
            this.type = type;

            fSourceViewer.configure(new LiClipseSourceViewerConfiguration(LiClipseTextEditorPlugin.getDefault()
                    .getColorManager(), LiClipseTextPreferences.getChainedPreferenceStore()));

            fSourceViewer.setEditable(false);
        }

        @Override
        public Control getControl() {
            return fSourceViewer.getControl();
        }

        @Override
        public void setInput(Object input) {
            if (input instanceof IStreamContentAccessor) {
                Document document = new Document(getString(input));
                LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                LiClipseLanguage language = languagesManager.getLanguageFromName(this.type);
                language.connect(document);
            }
            fInput = input;
        }

        @Override
        public Object getInput() {
            return fInput;
        }

        @Override
        public ISelection getSelection() {
            return null;
        }

        @Override
        public void setSelection(ISelection s, boolean reveal) {
        }

        @Override
        public void refresh() {
        }

        /**
         * A helper method to retrieve the contents of the given object
         * if it implements the IStreamContentAccessor interface.
         */
        private static String getString(Object input) {

            if (input instanceof IStreamContentAccessor) {
                IStreamContentAccessor sca = (IStreamContentAccessor) input;
                try {
                    return readString(sca);
                } catch (CoreException ex) {
                    Log.log(ex);
                }
            }
            return ""; //$NON-NLS-1$
        }

        public static String readString(IStreamContentAccessor sa) throws CoreException {
            InputStream is = sa.getContents();
            if (is != null) {
                String encoding = null;
                if (sa instanceof IEncodedStreamContentAccessor) {
                    try {
                        encoding = ((IEncodedStreamContentAccessor) sa).getCharset();
                    } catch (Exception e) {
                    }
                }
                if (encoding == null) {
                    encoding = ResourcesPlugin.getEncoding();
                }
                return readString(is, encoding);
            }
            return null;
        }

        /**
         * Reads the contents of the given input stream into a string.
         * The function assumes that the input stream uses the platform's default encoding
         * (<code>ResourcesPlugin.getEncoding()</code>).
         * Returns null if an error occurred.
         */
        private static String readString(InputStream is, String encoding) {
            if (is == null) {
                return null;
            }
            BufferedReader reader = null;
            try {
                StringBuffer buffer = new StringBuffer();
                char[] part = new char[2048];
                int read = 0;
                reader = new BufferedReader(new InputStreamReader(is, encoding));

                while ((read = reader.read(part)) != -1) {
                    buffer.append(part, 0, read);
                }

                return buffer.toString();

            } catch (IOException ex) {
                // NeedWork
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        // silently ignored
                    }
                }
            }
            return null;
        }
    }

}
