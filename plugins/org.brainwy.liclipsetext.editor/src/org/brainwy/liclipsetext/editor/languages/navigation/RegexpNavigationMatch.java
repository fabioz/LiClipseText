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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class RegexpNavigationMatch extends ScopeNavigationMatch {

    private final String regexp;
    private final Pattern pattern;
    private final int group;

    public RegexpNavigationMatch(Map m) {
        super(m);
        this.regexp = (String) m.remove("regexp");
        this.pattern = Pattern.compile(regexp);
        Object group = m.remove("group");
        if (group != null) {
            this.group = (Integer) group;
        } else {
            this.group = 0;
        }
    }

    @Override
    protected IRegion[] checkMatch(IDocument document, int offset, int length, int initialOffset, boolean forward) {
        IRegion[] region = super.checkMatch(document, offset, length, initialOffset, forward);
        List<IRegion> foundRegions = new ArrayList<IRegion>();
        if (region != null) {
            for (int i = 0; i < region.length; i++) {
                IRegion iRegion = region[i];
                String contents;
                if (iRegion.getOffset() > document.getLength()) {
                    continue;
                }
                try {
                    int len = iRegion.getLength();
                    if (iRegion.getOffset() + len > document.getLength()) {
                        len = document.getLength() - iRegion.getOffset();
                    }
                    contents = document.get(iRegion.getOffset(), len);
                } catch (BadLocationException e) {
                    Log.log(e);
                    continue;
                }
                Matcher matcher = pattern.matcher(contents);
                while (true) {
                    //find will always get the next subsequence
                    boolean found = matcher.find();
                    if (!found) {
                        break;
                    }
                    int start = matcher.start(group);
                    foundRegions.add(new Region(iRegion.getOffset() + start, matcher.end(group) - start));
                }
            }
        }
        if (foundRegions.size() == 0) {
            return null;
        }
        return foundRegions.toArray(new IRegion[foundRegions.size()]);
    }
}
