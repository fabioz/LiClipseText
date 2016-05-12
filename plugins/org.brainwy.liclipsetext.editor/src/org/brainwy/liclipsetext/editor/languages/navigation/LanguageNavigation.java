/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.LanguageConfig;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallback;
import org.brainwy.liclipsetext.shared_core.document.DocumentSync;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class LanguageNavigation extends LanguageConfig {

    public LanguageNavigation(LiClipseLanguage liClipseLanguage) {
        super(liClipseLanguage);
    }

    private final List<INavigationMatch> matchers = new ArrayList<INavigationMatch>();

    public IRegion find(final boolean forward, IDocument document, final int offset) {
        //No 2 places can do a find at the same time.
        return (IRegion) runWithDocumentSynched(document, new ICallback<Object, IDocument>() {

            public Object call(IDocument document) {
                IRegion last = null;
                for (INavigationMatch m : matchers) {
                    IRegion region = m.find(forward, document, offset);
                    if (region != null) {
                        if (forward) {
                            //get the lowest match
                            if (last == null || region.getOffset() < last.getOffset()) {
                                last = region;
                            }
                        } else {
                            //get the highest match
                            if (last == null || region.getOffset() > last.getOffset()) {
                                last = region;
                            }
                        }
                    }
                }
                if (last != null) {
                    return last;
                }

                if (forward) {
                    // Last: select document end
                    return new Region(document.getLength(), 0);
                } else {
                    // Last: select start
                    return new Region(0, 0);
                }
            }
        });
    }

    @SuppressWarnings({ "rawtypes" })
    public void load(List<Map<String, Object>> navigation, List<IStatus> errorList) {
        if (navigation == null) {
            return; //Nothing to load!
        }
        for (Map m : navigation) {
            String type = (String) m.remove("type");
            if (type.toLowerCase().equals("regexp")) {
                matchers.add(new RegexpNavigationMatch(m));

            } else if (type.toLowerCase().equals("scope")) {
                matchers.add(new ScopeNavigationMatch(m));

            } else if (type.toLowerCase().equals("ctags")) {
                matchers.add(new CtagsNavigationMatch(liClipseLanguage, m));

            } else {
                LiClipseTextEditorPlugin.createError("Unknown outline type: " + type, errorList);
                continue;
            }

            if (!m.isEmpty()) {
                LiClipseTextEditorPlugin.createWarning("Fields not treated in outline: "
                        + StringUtils.join(", ", m.keySet()), errorList);
            }
        }
    }

    public static class MatcherAndRegions {

        public final List<RegionAndText> regions;
        public final INavigationMatch navigationMatcher;

        public MatcherAndRegions(List<RegionAndText> regions, INavigationMatch navigationMatcher) {
            this.regions = regions;
            this.navigationMatcher = navigationMatcher;
        }

    }

    /**
     * @return all matched regions. may return overlapping areas!
     */
    @SuppressWarnings("unchecked")
    public List<MatcherAndRegions> findAll(IDocument document) {
        return (List<MatcherAndRegions>) runWithDocumentSynched(document, new ICallback<Object, IDocument>() {

            public Object call(IDocument document) {
                List<MatcherAndRegions> ret = new ArrayList<MatcherAndRegions>(matchers.size());
                return findAllInternal(document, ret);
            }
        });
    }

    private final Object lock = new Object();

    private Object runWithDocumentSynched(IDocument document, ICallback<Object, IDocument> iCallback) {
        synchronized (lock) { //Synchronize within the language navigation too as matchers are not thread-safe.
            return DocumentSync.runWithDocumentSynched(document, iCallback, true);
        }
    }

    /**
     * Must be already synchronized.
     */
    private List<MatcherAndRegions> findAllInternal(IDocument document, List<MatcherAndRegions> ret) {
        for (INavigationMatch m : matchers) {
            if (m instanceof INavigationMatch2) {
                INavigationMatch2 match2 = (INavigationMatch2) m;
                List<RegionAndText> enhanced = match2.findAllEnhanced(document);
                ret.add(new MatcherAndRegions(enhanced, m));
            } else {
                List<IRegion> regions = m.findAll(document);
                if (regions != null && regions.size() > 0) {
                    ArrayList<RegionAndText> arrayList = new ArrayList<RegionAndText>(regions.size());
                    for (IRegion r : regions) {
                        try {
                            int offset = r.getOffset();
                            int beginLine = document.getLineOfOffset(offset);
                            arrayList.add(new RegionAndText(r, document.get(offset, r.getLength()),
                                    beginLine, offset - document.getLineOffset(beginLine)));
                        } catch (BadLocationException e) {
                            Log.log(e);
                        }
                    }
                    ret.add(new MatcherAndRegions(arrayList, m));
                }
            }

        }
        return ret;
    }
}
