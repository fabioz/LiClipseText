/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.brainwy.liclipsetext.editor.images.LiClipseImageProvider;
import org.brainwy.liclipsetext.editor.languages.LanguageConfig;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.languages.navigation.LanguageNavigation;
import org.brainwy.liclipsetext.editor.languages.navigation.RegionAndText;
import org.brainwy.liclipsetext.editor.languages.navigation.LanguageNavigation.MatcherAndRegions;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.model.ISimpleNode;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.structure.TreeNode;
import org.brainwy.liclipsetext.shared_ui.UIConstants;

public class LanguageOutline extends LanguageConfig {

    public static class OutlineData {

        public final String caption;
        public final IRegion region;
        /**
         * beginLine 1-based
         */
        public final int beginLine;
        public final String icon;
        public final int beginCol;

        /**
         * @param beginLine 1-based
         */
        public OutlineData(String caption, IRegion iRegion, int beginLine, int beginCol, String icon) {
            this.caption = caption;
            this.region = iRegion;
            this.beginLine = beginLine;
            this.beginCol = beginCol;
            if (icon == null) {
                icon = UIConstants.PUBLIC_ATTR_ICON;
            }
            this.icon = icon;
        }

        @Override
        public String toString() {
            return new FastStringBuffer(caption, 25)
                    .append(" offset:")
                    .append(region.getOffset())
                    .append(" len:")
                    .append(region.getLength())
                    .append(" beginLine:")
                    .append(beginLine)
                    .append(" icon:")
                    .append(icon)
                    .toString();
        }

        public Image getImage() {
            return LiClipseImageProvider.getImage(this.icon, UIConstants.PUBLIC_ATTR_ICON);
        }

    }

    public static class LiClipseNode extends TreeNode<OutlineData> implements ISimpleNode {

        /**
         * The level may differ the meaning depending on the indentation mode.
         *
         * For flat, it should be always -1
         * For indent-based, it's the column
         * For brace-based, it's the level of braces found.
         */
        public final int level;
        public final boolean definesNewScope;

        public LiClipseNode(LiClipseNode parent, OutlineData data, int level, boolean definesNewScope) {
            super(parent, data);
            this.level = level;
            this.definesNewScope = definesNewScope;
        }

        /*default*/void sortChildren() {
            Comparator<? super TreeNode/*OutlineData*/> comparator = new Comparator<TreeNode/*OutlineData*/>() {

                public int compare(TreeNode/*OutlineData*/o1, TreeNode/*OutlineData*/o2) {
                    return ((OutlineData) o1.getData()).region.getOffset()
                            - ((OutlineData) o2.getData()).region.getOffset();
                }
            };
            this.children.sortAndTrim(comparator);
        }

        @Override
        public LiClipseNode getParent() {
            return (LiClipseNode) super.getParent();
        }

        public void trim() {
            this.children.trim();
            int size = this.children.size();
            if (size > 0) {
                Object[] internalArray = this.children.internalArray();
                for (int i = 0; i < size; i++) {
                    ((LiClipseNode) internalArray[i]).trim();
                }
            }
        }

    }

    public LanguageOutline(LiClipseLanguage liClipseLanguage) {
        super(liClipseLanguage);
    }

    /**
     * @return the root node of the tree.
     */
    public LiClipseNode createOutline(IDocument document) {
        //        Timer timer = new Timer();
        LiClipseLanguage language = this.liClipseLanguage.get();
        if (language == null) {
            Log.log("Language not expected to be null!");
            return null;
        }
        LanguageNavigation navigation = language.getNavigation();
        List<MatcherAndRegions> findAll = navigation.findAll(document);

        List<OutlineData> outlineData = new ArrayList<LanguageOutline.OutlineData>();
        for (MatcherAndRegions matcherAndRegions : findAll) {
            List<RegionAndText> regions = matcherAndRegions.regions;
            for (RegionAndText iRegion : regions) {
                String caption = iRegion.text;
                int beginLine = iRegion.beginLine + 1;
                String icon = iRegion.kind;
                if (iRegion.kind == null) {
                    icon = matcherAndRegions.navigationMatcher.getIcon();
                }
                outlineData.add(new OutlineData(caption, iRegion.region, beginLine, iRegion.beginCol + 1, icon));
            }
        }
        Collections.sort(outlineData, new Comparator<OutlineData>() {

            public int compare(OutlineData o1, OutlineData o2) {
                return o1.region.getOffset() - o2.region.getOffset();
            }
        });
        LiClipseNode root = language.getIndent().calculateOutline(document, outlineData);

        //Put things in order for the outline!
        root.trim();
        //        timer.printDiff("Total time to create outline.");
        return root;
    }
}
