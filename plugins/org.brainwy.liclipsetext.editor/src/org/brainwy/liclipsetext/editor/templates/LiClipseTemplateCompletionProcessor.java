/**
 * =Copyright (c) 2013-2016 by Brainwy Software Ltda, and others.
 * All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader;
import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.editor.languages.GlobalLanguageTemplates;
import org.brainwy.liclipsetext.editor.languages.LanguageTemplates;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.editor.rules.TypedRegionWithSubTokens;
import org.brainwy.liclipsetext.shared_core.document.DocumentTimeStampChangedException;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.ImmutableTuple;
import org.brainwy.liclipsetext.shared_core.structure.LowMemoryArrayList;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_core.utils.ArrayUtils;
import org.brainwy.liclipsetext.shared_ui.ImageCache;
import org.brainwy.liclipsetext.shared_ui.SharedUiPlugin;
import org.brainwy.liclipsetext.shared_ui.UIConstants;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public final class LiClipseTemplateCompletionProcessor {// implements IContentAssistProcessor {

    private final LiClipseTemplateContextType liClipseTemplateContextType;
    private LiClipseLanguage liClipseLanguage;
    private String contentType;

    public LiClipseTemplateCompletionProcessor(LiClipseLanguage liClipseLanguage, String contentType) {
        super();
        liClipseTemplateContextType = new LiClipseTemplateContextType(liClipseLanguage);
        this.liClipseLanguage = liClipseLanguage;
        this.contentType = contentType;
    }

    protected Template[] getTemplates(String contextTypeId, List<String> typesAtOffset) {
        LanguageTemplates templates = liClipseLanguage.getTemplates();
        LiClipseTemplate[] ret = templates.getTemplates(contextTypeId, contentType);
        GlobalLanguageTemplates instance = GlobalLanguageTemplates.getInstance();

        typesAtOffset = new ArrayList<>(typesAtOffset); // Create a copy as we'll mutate it.

        // TODO: We should scope things better
        typesAtOffset.removeAll(Arrays.asList(this.contentType));

        String[] typesAtOffsetArray = typesAtOffset.toArray(new String[0]);
        LiClipseTemplate[] templates2 = instance.getTemplates(contextTypeId, liClipseLanguage,
                typesAtOffsetArray);
        if (templates2 == null) {
            return ret;
        }
        return ArrayUtils.concatArrays(ret, templates2);
    }

    private static final class ProposalComparator implements
            Comparator<LiClipseTemplateProposal> {
        public int compare(LiClipseTemplateProposal o1, LiClipseTemplateProposal o2) {
            return o2.getRelevance() - o1.getRelevance();
        }
    }

    private static final ProposalComparator proposalComparator = new ProposalComparator();

    private ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset, boolean textSelected) {

        ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();

        // adjust offset to end of normalized selection
        if (selection.getOffset() == offset) {
            offset = selection.getOffset() + selection.getLength();
        }

        ImmutableTuple<String, String> prefixes = extractPrefix(viewer, offset);

        String prefix = prefixes.o1;
        Region region = new Region(offset - prefix.length(), prefix.length());

        Region regionWithSeparators = null;
        String prefixWithSeparators = prefixes.o2;
        if (prefixWithSeparators != null) {
            regionWithSeparators = new Region(offset - prefixWithSeparators.length(), prefixWithSeparators.length());
        }

        //Note: we have to create 2 contexts, one with the separator chars and one without the separator chars!
        LiClipseDocumentTemplateContext context = createContext(viewer, region);
        if (context == null) {
            return new ICompletionProposal[0];
        }
        LiClipseDocumentTemplateContext contextWithSeparators = null;
        if (regionWithSeparators != null) {
            contextWithSeparators = createContext(viewer, regionWithSeparators);
            if (contextWithSeparators == null) {
                return new ICompletionProposal[0];
            }
        }
        context.setPrefixes(prefixes.o1, prefixes.o2);

        //The target list should be the same (with or without separators).
        List<String> typesAtOffset = new LowMemoryArrayList<>();
        ITypedRegion partition;
        try {
            partition = viewer.getDocument().getPartition(offset);
            if (partition instanceof TypedRegionWithSubTokens) {
                TypedRegionWithSubTokens typedRegionWithSubTokens = (TypedRegionWithSubTokens) partition;
                SubRuleToken subRuleToken = typedRegionWithSubTokens.getSubRuleToken();
                if (subRuleToken != null) {
                    typesAtOffset = typedRegionWithSubTokens.getTypesAtOffset(offset);
                }
            }
        } catch (Exception e1) {
            Log.log(e1);
        }
        String contextTypeId = context.getContextType().getId();
        Template[] templates = getTemplates(contextTypeId, typesAtOffset);

        List<LiClipseTemplateProposal> matches = new ArrayList<LiClipseTemplateProposal>();
        if (textSelected) {
            for (int j = 0; j < templates.length; j++) {
                Template template = templates[j];
                String pattern = template.getPattern();
                if (pattern.indexOf("${selection}") != -1 || pattern.indexOf("${indented_block}") != -1) {
                    matches.add(new LiClipseTemplateProposal((LiClipseTemplate) template, context, region,
                            getImage(template), 90));
                }
            }
        } else {
            //Ignore case for matching
            prefix = prefix.toLowerCase();
            if (prefixWithSeparators != null) {
                prefixWithSeparators = prefixWithSeparators.toLowerCase();
            }

            boolean addOnlyWithSeparators = false;
            for (int i = 0; i < templates.length; i++) {
                Template template = templates[i];
                String pattern = template.getPattern();

                if (pattern.indexOf("${selection}") != -1 || pattern.indexOf("${indented_block}") != -1) {
                    continue;
                }

                String name = template.getName();
                try {
                    context.getContextType().validate(pattern);
                } catch (TemplateException e) {
                    Log.log("Error: the pattern: " + pattern + " is not valid (template: " + name + ")");
                    continue;
                }

                //Ignore case for matching
                name = name.toLowerCase();

                //Important: match first with separators (i.e.: if we have a '.' and for the other we have empty, we should match on the separator version).
                if (prefixWithSeparators != null && name.startsWith(prefixWithSeparators)) {
                    if (template.matches(prefixWithSeparators, contextTypeId)) {
                        if (!addOnlyWithSeparators) {
                            addOnlyWithSeparators = true; //if one with separators is found, ignore 'regular' ones (clear list and only let separator versions in).
                            matches.clear();
                        }
                        int relevance = name.startsWith(prefixWithSeparators) ? 90 : 0;
                        matches.add(new LiClipseTemplateProposal((LiClipseTemplate) template, contextWithSeparators,
                                regionWithSeparators, getImage(template), relevance));
                    }
                } else if (!addOnlyWithSeparators && (name.startsWith(prefix) || name.isEmpty())) {
                    if (template.matches(prefix, contextTypeId)) {
                        int relevance = name.startsWith(prefix) ? 90 : 0;
                        matches.add(new LiClipseTemplateProposal((LiClipseTemplate) template, context, region,
                                getImage(template), relevance));
                    }
                }
            }
        }

        Collections.sort(matches, proposalComparator);

        return matches.toArray(new ICompletionProposal[matches.size()]);
    }

    protected Image getImage(Template template) {
        LiClipseTemplate t = (LiClipseTemplate) template;
        if (t.icon != null) {
            return t.icon;
        }
        ImageCache imageCache = SharedUiPlugin.getImageCache();
        if (imageCache == null) {
            return null;
        }
        return imageCache.get(UIConstants.COMPLETION_TEMPLATE);
    }

    /**
     * This is the actual public API to get the needed information on the completions and fill the list of proposals.
     */
    public void collectTemplateProposals(ITextViewer viewer, int documentOffset, List<ICompletionProposal> proposals) {
        Point sel = viewer.getSelectedRange();
        boolean textSelected = sel.y > 0;

        ICompletionProposal[] templateProposals = computeCompletionProposals(viewer, documentOffset, textSelected);

        if (textSelected) {
            for (int j = 0; j < templateProposals.length; j++) {
                ICompletionProposal proposal = templateProposals[j];
                if (proposal instanceof LiClipseTemplateProposal) {
                    //Should be filtered already
                    proposals.add(proposal);
                } else {
                    Log.log("Expecting LiClipseTemplateProposal, found: " + proposal);
                }
            }

        } else {

            Map<String, Tuple<String, TypedPart>> partitionToValue = new HashMap<String, Tuple<String, TypedPart>>();
            ITypedRegion currentContentType = null;

            IDocument doc = viewer.getDocument();
            for (int j = 0; j < templateProposals.length; j++) {
                ICompletionProposal proposal = templateProposals[j];
                if (proposal instanceof LiClipseTemplateProposal) {
                    LiClipseTemplateProposal liClipseTemplateProposal = (LiClipseTemplateProposal) proposal;
                    LiClipseTemplate templateUsed = liClipseTemplateProposal.getTemplateUsed();
                    if (templateUsed.matchPreviousSubScope == null && templateUsed.matchCurrentSubScope == null) {
                        proposals.add(proposal);
                    } else {
                        boolean add = true;
                        if (templateUsed.matchPreviousSubScope != null) {
                            add = false;
                            //We have to match it.
                            Tuple<String, TypedPart> val = findPreviousPartitionOfType(documentOffset,
                                    partitionToValue, doc,
                                    templateUsed.matchPreviousSubScope);

                            //val cannot be null at this point
                            if (templateUsed.matchPreviousSubScope.o2.equals(val.o1)) {
                                add = true;
                            }
                        }
                        if (add) {
                            if (templateUsed.matchCurrentSubScope != null) {
                                add = false;
                                try {
                                    if (currentContentType == null) {
                                        currentContentType = doc.getPartition(documentOffset);
                                    }
                                } catch (BadLocationException e) {
                                    Log.log(e);
                                }

                                if (currentContentType != null) {
                                    String type = currentContentType.getType();
                                    if (type == null || type.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                                        type = "default";
                                    }

                                    String matchCurrentSubScope = templateUsed.matchCurrentSubScope.o1;
                                    String matchCurrentSubStr = templateUsed.matchCurrentSubScope.o2;

                                    if (matchCurrentSubScope.startsWith(type + ".")
                                            || matchCurrentSubScope.equals(currentContentType
                                                    .getType())) {
                                        //We have to match it.
                                        Tuple<String, TypedPart> val = findPreviousPartitionOfType(documentOffset,
                                                partitionToValue,
                                                doc,
                                                templateUsed.matchCurrentSubScope);

                                        boolean considerEmpty = val.o2 == null
                                                || currentContentType.getOffset() > val.o2.offset
                                                || val.o2.offset + val.o2.length >= documentOffset
                                                || val.o1.length() == 0;

                                        if (considerEmpty) {
                                            if (matchCurrentSubStr.length() == 0) {
                                                add = true;
                                            }
                                        } else {
                                            if (matchCurrentSubStr.equals("*notempty*")
                                                    || matchCurrentSubStr.length() > 0
                                                            && matchCurrentSubStr.equals(val.o1)) {
                                                add = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (add) {
                            proposals.add(proposal);
                        }
                    }
                } else {
                    proposals.add(proposal);
                }
            }
        }
    }

    private Tuple<String, TypedPart> findPreviousPartitionOfType(int documentOffset,
            Map<String, Tuple<String, TypedPart>> partitionToValue, IDocument doc,
            Tuple<String, String> matchSub) {
        Tuple<String, TypedPart> tup = partitionToValue.get(matchSub.o1);
        if (tup == null) {
            SubPartitionCodeReader reader = new SubPartitionCodeReader();

            reader.configurePartitions(false, doc, documentOffset, matchSub.o1);
            TypedPart read;
            try {
                read = reader.read();
            } catch (DocumentTimeStampChangedException e1) {
                Log.log(e1);
                read = null;
            }
            if (read != null) {
                try {
                    String val = doc.get(read.offset, read.length);
                    tup = new Tuple<String, TypedPart>(val, read);
                } catch (BadLocationException e) {
                    Log.log(e);
                }
            }
            if (tup == null) {
                tup = new Tuple<String, SubPartitionCodeReader.TypedPart>("", null);
            }
            partitionToValue.put(matchSub.o1, tup);
        }
        return tup;
    }

    private static final ImmutableTuple<String, String> emptyReturn = new ImmutableTuple<String, String>("", null);

    /**
     * Return a tuple with a buffer with the non-separator chars and then the separator chars before the given offset.
     *
     * The first element (non-separator chars) is always required, whereas the second may be null.
     */
    protected ImmutableTuple<String, String> extractPrefix(ITextViewer viewer, int offset) {
        Point sel = viewer.getSelectedRange();
        IDocument document = viewer.getDocument();
        if (sel.y > 0) {
            //We have some text selected: no prefix in this case
            if (offset > document.getLength()) {
                return emptyReturn;
            }

            try {
                return new ImmutableTuple<String, String>(document.get(sel.x, sel.y), null);
            } catch (BadLocationException e) {
                return emptyReturn;
            }
        }

        //This would be the super call, but we want to use our language to say which are the separators.
        Set<Character> separatorChars = liClipseLanguage.getSeparatorChars();

        int i = offset;
        if (i > document.getLength()) {
            return null;
        }

        FastStringBuffer bufNonSeparators = new FastStringBuffer();
        try {
            while (i > 0) {
                char ch = document.getChar(i - 1);
                if (Character.isWhitespace(ch) || separatorChars.contains(ch)) {
                    break;
                }
                bufNonSeparators.append(ch);
                i--;
            }
            FastStringBuffer bufSeparators = new FastStringBuffer(bufNonSeparators.length() + 5);
            bufSeparators.append(bufNonSeparators);

            while (i > 0) {
                char ch = document.getChar(i - 1);
                if (Character.isWhitespace(ch) || !separatorChars.contains(ch)) {
                    break;
                }
                bufSeparators.append(ch);
                i--;
            }

            bufNonSeparators.reverse();
            bufSeparators.reverse();

            return new ImmutableTuple<String, String>(bufNonSeparators.toString(),
                    //if they're the same thing, say that the separator version is null
                    bufNonSeparators.length() == bufSeparators.length() ? null : bufSeparators.toString());
        } catch (BadLocationException e) {
            return emptyReturn;
        }
    }

    protected LiClipseDocumentTemplateContext createContext(final ITextViewer viewer, final IRegion region) {
        Assert.isNotNull(liClipseTemplateContextType);

        IDocument document = viewer.getDocument();
        TextSelectionUtils ts = new TextSelectionUtils(document, viewer.getSelectedRange().x);
        String indent = ts.getIndentationFromLine();
        return new LiClipseDocumentTemplateContext(liClipseTemplateContextType, document, region.getOffset(),
                region.getLength(), indent, this.liClipseLanguage);
    }

}
