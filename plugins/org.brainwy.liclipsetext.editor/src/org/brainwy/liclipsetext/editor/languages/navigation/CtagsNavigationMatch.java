package org.brainwy.liclipsetext.editor.languages.navigation;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.images.LiClipseImageProvider;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.cache.LRUMap;
import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.process.ProcessUtils;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.LowMemoryArrayList;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.brainwy.liclipsetext.shared_core.structure.Tuple3;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class CtagsNavigationMatch implements INavigationMatch, INavigationMatch2 {

    private String ext;

    private Tuple3<IDocument, Long, List<RegionAndText>> lastResult;

    public CtagsNavigationMatch(WeakReference<LiClipseLanguage> liClipseLanguage, Map m) {
        LiClipseLanguage language = liClipseLanguage.get();
        if (language != null) {
            Set<String> fileExtensions = language.getFileExtensions();
            if (fileExtensions.size() > 0) {
                this.ext = fileExtensions.iterator().next();
            }
        }
    }

    public IRegion find(boolean forward, IDocument document, int offset) {
        List<RegionAndText> findAll = findAllEnhanced(document);
        if (forward) {
            int size = findAll.size();
            for (int i = 0; i < size; i++) {
                IRegion region = findAll.get(i).region;
                if (region.getOffset() > offset) {
                    return region;
                }
            }
        } else {
            int size = findAll.size();
            if (size == 0) {
                return null;
            }
            IRegion last = findAll.get(0).region;
            if (last.getOffset() > offset) {
                return null;
            }

            for (int i = 1; i < size; i++) {
                IRegion region = findAll.get(i).region;
                if (region.getOffset() >= offset) {
                    return last;
                }
                last = region;
            }
            if (last != null && last.getOffset() < offset) {
                return last;
            }

        }
        return null;
    }

    public List<IRegion> findAll(IDocument document) {
        throw new AssertionFailedException("Use findAllEnhanced instead.");
    }

    public List<RegionAndText> findAllEnhanced(IDocument document) {
        IDocumentExtension4 ext = (IDocumentExtension4) document;
        if (lastResult != null) {
            if (lastResult.o1 == document) {
                if (lastResult.o2 == ext.getModificationStamp()) {
                    return lastResult.o3;
                }
            }
        }
        File tempFile = getTempFile();
        FileUtils.writeBytesToFile(document.get().getBytes(), tempFile);
        String[] cmdarray = new String[] {
                LiClipseTextEditorPlugin.getCtagsExecutable(),
                //"-e", //etags format 
                "--excmd=number", //provide line numbers and not regexps
                "-f", "-", //force stdout output
                tempFile.getAbsolutePath()
        };
        Tuple<String, String> output;
        try {
            output = ProcessUtils.runAndGetOutput(cmdarray, null, new File("."),
                    new NullProgressMonitor(), "utf-8");
        } catch (Exception e) {
            NotifyCtagsErrorDialog.notifyError(e);
            return new ArrayList<RegionAndText>(0);
        }

        List<RegionAndText> ret = parseCtags(document, output.o1);
        lastResult = new Tuple3<IDocument, Long, List<RegionAndText>>(document, ext.getModificationStamp(), ret);
        return ret;
    }

    /**
     * Format is:
     * CRectangle  X:\liclipse\plugins\org.brainwy.liclipsetext.editor\.\ctags_generation_temp_5.cpp   5;" c   file:
     * area    X:\liclipse\plugins\org.brainwy.liclipsetext.editor\.\ctags_generation_temp_5.cpp   9;" f   class:CRectangle
     * 
     * We return List<IRegion> or List<RegionAndText>
     */
    private List<RegionAndText> parseCtags(IDocument doc, String o1) {
        //        System.out.println(o1);
        List<RegionAndText> regions = new ArrayList<RegionAndText>();

        if (o1 != null && o1.length() > 0) {
            int currTabPos = 0;

            String found = null;
            int lineFound = -1;

            int last = 0;

            String kind = null;

            char[] charArray = o1.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                switch (c) {
                    case '\t':
                        //System.out.println(currTabPos + ":" + new String(charArray, last, i - last));
                        switch (currTabPos) {
                            case 0:
                                //Found text
                                kind = null;
                                found = new String(charArray, last, i - last);
                                break;
                            case 2:
                                //Line
                                int less = 0;
                                char oneL = charArray[i - 1];
                                char twoL = charArray[i - 2];
                                if (oneL == ';' || oneL == '"') {
                                    less++;
                                }
                                if (twoL == ';' || twoL == '"') {
                                    less++;
                                }
                                try {
                                    lineFound = Integer.parseInt(new String(charArray, last, i - last - less));
                                } catch (NumberFormatException e) {
                                    Log.log(e);
                                    lineFound = -1;
                                }
                                break;
                            case 3:
                                //Kind
                                kind = getKind(charArray[i - 1]);

                            default:
                                break;
                        }
                        last = i + 1;
                        currTabPos += 1;

                        break;
                    case '\r':
                    case '\n':
                        if (lineFound != -1 && found != null) {
                            if (kind == null) {
                                //It can be null if we found a new line without finding a tab first.
                                if (i > 0) {
                                    //only \r or only \n
                                    kind = getKind(charArray[i - 1]);
                                }
                                if (kind == null && i > 1) {
                                    //\r\n
                                    kind = getKind(charArray[i - 1]);
                                }
                                if (kind == null) {
                                    int start = i - 256;
                                    if (start < 0) {
                                        start = 0;
                                    }
                                    Log.log("Unable to get type from CTAGS. Last contents: "
                                            + new String(charArray, 0, i));
                                    kind = LiClipseImageProvider.ATTRIBUTE_KIND;
                                }
                            }
                            String initial = found;

                            //Handle "operator []"
                            int spaceI = found.indexOf(' ');
                            if (spaceI != -1) {
                                found = found.substring(0, spaceI);
                            }
                            String line = TextSelectionUtils.getLine(doc, lineFound - 1);
                            Pattern compile = getCompiled(found);
                            int col = 0;
                            if (compile != null) {
                                Matcher matcher = compile.matcher(line);
                                if (matcher.find()) {
                                    col = matcher.start();
                                }
                            }
                            int offset = TextSelectionUtils.getAbsoluteCursorOffset(doc, lineFound - 1, col);

                            regions.add(new RegionAndText(new Region(offset, found.length()), initial,
                                    lineFound - 1, col,
                                    kind));
                        }

                        found = null;
                        last = i + 1;
                        currTabPos = 0;
                        lineFound = -1;
                        break;

                    default:
                        break;
                }
            }
        }
        Collections.sort(regions, new Comparator<RegionAndText>() {

            public int compare(RegionAndText o1, RegionAndText o2) {
                int x = o1.region.getOffset();
                int y = o2.region.getOffset();
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        });
        return regions;
    }

    public String getKind(char curr) {
        String kind;
        switch (curr) {
            case 'm': //method
            case 'n': //namespace
            case 'f': //function definition
                kind = LiClipseImageProvider.METHOD_KIND;
                break;
            case 'c': //class
            case 's': //struct
            case 'u': //union
                kind = LiClipseImageProvider.CLASS_KIND;
                break;
            default:
                kind = LiClipseImageProvider.ATTRIBUTE_KIND;
                break;
        }
        return kind;
    }

    private final LRUMap<String, Pattern> lru = new LRUMap<>(10);

    public Pattern getCompiled(String found) {
        Pattern pattern = lru.get(found);
        if (pattern == null) {
            try {
                pattern = Pattern.compile("\\b" + found + "\\b");
            } catch (Exception e) {
                Log.log(e);
                return null;
            }
            lru.put(found, pattern);
        }
        return pattern;
    }

    private final static char startChar = (char) 127;

    private final static char endChar = (char) 1;

    /**
     * Unused: etags (emacs) format processing
     * @param doc
     * @param o1
     * @return
     */
    private List<IRegion> parseEtags(IDocument doc, String o1) {
        List<IRegion> regions = new LowMemoryArrayList<IRegion>();

        if (o1 != null && o1.length() > 0) {
            int start = -1;

            //In etags format, we find: line, (127)occurrence(0)len,offset
            char[] charArray = o1.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (c == startChar) {
                    start = i + 1;
                }
                if (c == endChar) {
                    if (start != -1) {
                        String found = new String(charArray, start, i - start);
                        //System.out.println("Found: " + found);
                        i++;
                        start = i;
                        int lineFound = -1;
                        try {
                            for (; i < charArray.length; i++) {
                                c = charArray[i];
                                if (c == ',') {
                                    //System.out.println("Len:" + o1.substring(start, i));
                                    lineFound = Integer.parseInt(o1.substring(start, i));
                                    start = i + 1; //just skip the len and the comma 
                                }
                                if (c == '\n' || c == '\r') {
                                    if (lineFound != -1) {
                                        int offset = Integer.parseInt(o1.substring(start, i));
                                        String line = TextSelectionUtils.getLine(doc, lineFound - 1);
                                        Pattern compile = getCompiled(found);
                                        Matcher matcher = compile.matcher(line);
                                        boolean find = matcher.find();
                                        if (find) {
                                            regions.add(new Region(offset + matcher.start(), found.length()));
                                        }
                                        //System.out.println("Offset: " + offset);
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Log.log(e);
                        }
                    }
                }
            }
        }
        return regions;
    }

    private static int genFiles = 0;

    private File getTempFile() {
        LiClipseTextEditorPlugin plugin = LiClipseTextEditorPlugin.getDefault();
        File location = new File(".");
        if (plugin != null) {
            IPath stateLocation = plugin.getStateLocation();
            location = stateLocation.toFile();
        }
        File file = new File(location, "ctags_gen");
        if (!file.exists()) {
            file.mkdirs();
        }
        location = file;

        String prefix = "ctags_generation_temp_";
        File tempFileAt = FileUtils.getTempFileAt(location, prefix, "." + ext);
        genFiles++;
        if (genFiles >= 15) {
            FileUtils.clearTempFilesAt(location, prefix);
        }
        return tempFileAt;
    }

    public String getIcon() {
        throw new AssertionFailedException("As the info is returned enhanced, this should not be called.");
    }

}
