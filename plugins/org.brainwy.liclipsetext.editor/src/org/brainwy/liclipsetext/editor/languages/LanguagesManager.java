/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadata.LanguageType;
import org.brainwy.liclipsetext.editor.languages.LanguageTemplates.LiClipseVariableResolver;
import org.brainwy.liclipsetext.editor.languages.tmbundle.ITmLanguagePart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmCommentPart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmGrammarPart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmIndentPart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmLanguagePart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.TmSnippetPart;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.SnippetToTemplateCtx;
import org.brainwy.liclipsetext.editor.languages.tmbundle.parsing.TmSnippetParser;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplate;
import org.brainwy.liclipsetext.editor.templates.LiClipseTemplateContextType;
import org.brainwy.liclipsetext.shared_core.callbacks.CallbackWithListeners;
import org.brainwy.liclipsetext.shared_core.callbacks.ICallback;
import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.brainwy.liclipsetext.shared_core.io.FileUtils.ReadLines;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.path_watch.IFilesystemChangesListener;
import org.brainwy.liclipsetext.shared_core.path_watch.IPathWatch;
import org.brainwy.liclipsetext.shared_core.path_watch.PathWatch;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;
import org.brainwy.liclipsetext.shared_core.structure.OrderedSet;
import org.brainwy.liclipsetext.shared_core.structure.TreeNode;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.core.runtime.content.IContentTypeManager.IContentTypeChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.Registry;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class LanguagesManager {

    protected static final long JOB_TIMEOUT = 50;

    private static final String ADDITIONAL_TRACKED_DIRS = "ADDITIONAL_TRACKED_DIRS";

    private Map<String, LanguageMetadata> filenames = null;
    private Map<String, LanguageMetadata> fileExtensions = null;
    private Map<String, List<File>> extendLanguages = new HashMap<>();
    private final Map<String, LanguageMetadata> languageNameToMetadata = new HashMap<String, LanguageMetadata>();
    private File[] languagesDir;
    private IPathWatch pathWatch;

    private final Registry fRegistry = new Registry();

    public final CallbackWithListeners<LanguagesManager> onReload = new CallbackWithListeners<>();

    private final Job reloadJob = new Job("Reload Languages") {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            reloadAll();
            onReload.call(LanguagesManager.this);
            return Status.OK_STATUS;
        }

    };
    private IFilesystemChangesListener listener = new IFilesystemChangesListener() {

        public void removed(File file) {
            reloadJob.schedule(JOB_TIMEOUT);
        }

        public void added(File file) {
            reloadJob.schedule(JOB_TIMEOUT);
        }
    };

    /**
     * @param languagesDir these are the directories from where we'll load the available languages
     * we have. If no directory is passed, we'll use the default directory(ies).
     */
    public LanguagesManager(File... languagesDir) {
        try {
            if (languagesDir == null || languagesDir.length == 0) {
                //Only start path-watching if we're not passed any parameters.

                this.pathWatch = new PathWatch();
                FileFilter dirsFilter = new FileFilter() {

                    public boolean accept(File pathname) {
                        return false; //don't track dirs inside the tracked dir
                    }
                };
                FileFilter fileFilter = new FileFilter() {

                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        return name.endsWith(".liclipse") || name.endsWith(".tmbundle");
                    }
                };
                this.pathWatch.setDirectoryFileFilter(fileFilter, dirsFilter);
                updateTrackedDirs();
            } else {
                this.languagesDir = languagesDir;
            }
            reloadAll();
        } catch (Exception e) {
            Log.log("Error loading languages!", e);
        }
    }

    // Call with synchronized(lock) -- or in constructor.
    private void updateTrackedDirs() {
        //if not passed, use default dir: we should also add the user home directory here.
        Set<File> set = new OrderedSet<File>();

        //Default paths always there!
        set.add(LiClipseTextEditorPlugin.getFile(new Path("/languages")));
        try {
            File f = LiClipseTextEditorPlugin.getLiClipseUserDir();
            if (f != null) {
                f = new File(f, "languages");
                if (!f.exists()) {
                    f.mkdir();
                }
                if (f.exists()) {
                    set.add(f);
                }

            }
        } catch (Exception e1) {
            Log.log(e1);
        }

        //Load additional tracked dirs
        IPreferenceStore preferenceStore = LiClipseTextEditorPlugin.getDefault().getPreferenceStore();
        String string = preferenceStore.getString(ADDITIONAL_TRACKED_DIRS);
        for (String s : StringUtils.split(string, '|')) {
            set.add(new File(s));
        }
        languagesDir = set.toArray(new File[set.size()]);
    }

    private final Object lock = new Object();

    public void dispose() {
        synchronized (lock) {
            clearAllInternalInfo();

            if (pathWatch != null) {
                pathWatch.dispose();
                pathWatch = null;
            }
        }
    }

    // Helper to keep tracked dirs.
    private final List<File> trackedDirs = new ArrayList<File>();

    /**
     * Returns a list with the extensions (may return null).
     */
    public List<File> getExtensionsFor(String languageName) {
        List<File> lst = this.extendLanguages.get(languageName);
        return lst;
    }

    public final Map<String, TmIndentPart> scopeToTmIndent = new HashMap<String, TmIndentPart>();
    public final Map<String, List<TmCommentPart>> scopeToTmComment = new HashMap<String, List<TmCommentPart>>();

    private void clearAllInternalInfo() {
        filenames = null;
        fileExtensions = null;
        languageNameToMetadata.clear();
        GlobalLanguageTemplates.getInstance().clear();
        extendLanguages.clear();
        scopeToTmIndent.clear();
        scopeToTmComment.clear();
    }

    private void reloadAll() {
        synchronized (lock) {
            clearAllInternalInfo();
            for (File f : trackedDirs) {
                if (pathWatch != null) {
                    pathWatch.stopTrack(f, listener);
                }
            }
            trackedDirs.clear();

            for (File dir : languagesDir) {
                if (dir.isDirectory()) {
                    if (pathWatch != null) {
                        pathWatch.track(dir, listener);
                    }
                    trackedDirs.add(dir);
                }
            }

            //Load all extends
            for (File dir : languagesDir) {
                File[] listFiles = dir.listFiles();
                if (listFiles != null) {
                    for (File file : listFiles) {
                        String name = file.getName();
                        if (name.toLowerCase().endsWith(".extend.liclipse")) {
                            try (InputStream stream = new FileInputStream(file)) {
                                Map<String, Object> loadedData = new LiClipseLanguageIO(null)
                                        .loadDataFromContents(stream);
                                Object extendLanguage = loadedData.get(LiClipseLanguage.EXTEND);
                                if (extendLanguage != null) {
                                    Object patch = loadedData.get(LiClipseLanguage.PATCH);
                                    if (patch != null) {
                                        List<File> lst = extendLanguages.get(extendLanguage.toString());
                                        if (lst == null) {
                                            lst = new ArrayList<File>();
                                            extendLanguages.put(extendLanguage.toString(), lst);
                                        }
                                        lst.add(file);

                                    } else {
                                        Log.log("File: " + file + " extends: " + extendLanguage
                                                + " but does not declare a 'patch' section.");

                                    }
                                } else {
                                    Log.log("Languages with .extend.liclipse must declare which language they extend.");
                                }
                            } catch (Exception e) {
                                Log.log(e);
                            }
                        }
                    }
                }
            }

            for (File dir : languagesDir) {
                File[] listFiles = dir.listFiles();
                if (listFiles != null) {
                    for (File file : listFiles) {
                        String name = file.getName();
                        String lowerName = name.toLowerCase();
                        if (lowerName.endsWith(".tmbundle")) {
                            loadTmBundle(file);
                        } else if (lowerName.endsWith(".liclipse") && !lowerName.endsWith(".extend.liclipse")) {
                            try {
                                loadFile(file);
                            } catch (Exception e) {
                                Log.log(e);
                            }
                        }
                    }
                } else {
                    Log.log("Unable to list files in dir: " + dir);
                }
            }

        }
    }

    private void loadTmBundle(final File file) {
        synchronized (lock) {
            if (file.isFile()) {
                //Let's see if we're dealing with a zipped file
                try {
                    try (ZipFile zipFile = new ZipFile(file)) {
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry element = entries.nextElement();
                            String elementName = element.getName();
                            if (considerTmBundleZipName(elementName)) {
                                ITmLanguagePart part = TmLanguagePart.create(file, zipFile, element);
                                if (part instanceof TmGrammarPart) {
                                    ILanguageMetadataFileInfo fileInfo = new LanguageMetadataTmBundleZipFileInfo(file,
                                            elementName);
                                    onGrammarFound(file, part, fileInfo);
                                } else if (part instanceof TmSnippetPart) {
                                    try {
                                        onSnippetFound((TmSnippetPart) part);
                                    } catch (Exception e) {
                                        Log.log(e);
                                    }
                                } else if (part instanceof TmIndentPart) {
                                    onIndentFound((TmIndentPart) part);
                                } else if (part instanceof TmCommentPart) {
                                    onCommentFound((TmCommentPart) part);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.log("Error in file: " + file, e);
                }
            } else if (file.isDirectory()) {
                ICallback<Boolean, java.nio.file.Path> callback = new ICallback<Boolean, java.nio.file.Path>() {

                    @Override
                    public Boolean call(java.nio.file.Path path) {
                        String string = path.toString();
                        if (considerTmBundleZipName(string)) {
                            ITmLanguagePart part = TmLanguagePart.create(path);
                            if (part instanceof TmGrammarPart) {
                                ILanguageMetadataFileInfo fileInfo = new LanguageMetadataFileInfo(path.toFile(),
                                        path.toFile());
                                onGrammarFound(file, part, fileInfo); //Note: the file is expected to be the tmbundle dir.
                            } else if (part instanceof TmSnippetPart) {
                                onSnippetFound((TmSnippetPart) part);
                            } else if (part instanceof TmIndentPart) {
                                onIndentFound((TmIndentPart) part);
                            } else if (part instanceof TmCommentPart) {
                                onCommentFound((TmCommentPart) part);
                            }
                        }

                        return true;
                    }
                };
                //I.e.: search beneath the directory structure
                try {
                    FileUtils.visitDirectory(file, true, callback);
                } catch (IOException e) {
                    Log.log(e);
                }
            }
        }
    }

    protected void onIndentFound(TmIndentPart part) {
        scopeToTmIndent.put(part.scope, part);
    }

    protected void onCommentFound(TmCommentPart part) {
        List<TmCommentPart> list = scopeToTmComment.get(part.scope);
        if (list == null) {
            list = new ArrayList<>();
            scopeToTmComment.put(part.scope, list);
        }
        list.add(part);
    }

    private void onSnippetFound(TmSnippetPart part) {

        final GlobalLanguageTemplates instance = GlobalLanguageTemplates.getInstance();
        SnippetToTemplateCtx ctx = new SnippetToTemplateCtx() {
            @Override
            public LiClipseVariableResolver obtainValidTemplateVariableName(String sub) {
                //When a resolver is requested, it's created at the global templates.
                return instance.obtainValidTemplateVariableName(sub);
            }
        };

        try {
            TmSnippetParser.createReplacement(part.getContent(), ctx);
            String pattern = ctx.getReplaced();
            instance.addTemplate(new LiClipseTemplate(part.getTabTrigger(), pattern,
                    LiClipseTemplateContextType.LICLIPSE_TEMPLATES_CONTEXT_TYPE_ID, pattern, true,
                    part.getScope()));
        } catch (Exception e) {
            String msg = "Unable to handle snippet in scope: " + part.getScope() + " name: " + part.getName()
                    + " tab trigger: " + part.getTabTrigger() + " pattern: "
                    + part.getContent();
            System.err.println(msg);
            Log.log(msg, e);
        }

    }

    private void onGrammarFound(File file, ITmLanguagePart part, ILanguageMetadataFileInfo fileInfo) {
        TmGrammarPart tmGrammarPart = (TmGrammarPart) part;
        String name = tmGrammarPart.getName();
        String caption = tmGrammarPart.getCaption();
        String shebangStr = tmGrammarPart.getShebang();

        ArrayList<Pattern> shebang = new ArrayList<>(1);
        if (shebangStr != null) {
            try {
                Pattern p = Pattern.compile(shebangStr);
                shebang.add(p);
            } catch (Exception e) {
                Log.log("Unable to compile regexp: " + shebangStr, e);
            }
        }

        LanguageMetadata languageMetadata = new LanguageMetadata(name,
                fileInfo,
                shebang == null ? null
                        : shebang.toArray(new Pattern[shebang.size()]),
                LanguageType.TEXT_MATE, caption);
        languageNameToMetadata.put(name, languageMetadata);

        List<String> fileTypes = tmGrammarPart.getFileTypes();
        registerFileExtensions(file, languageMetadata, fileTypes);
        registerFilenames(file, languageMetadata, fileTypes);
    }

    public static boolean considerTmBundleZipName(String elementName) {
        String elementNameLower = elementName.toLowerCase();
        return elementNameLower.endsWith(".plist") || elementNameLower.endsWith(".tmpreferences")
                || elementNameLower.endsWith(".tmpreference")
                || elementNameLower.endsWith(".tmlanguage") || elementNameLower.endsWith(".tmsnippet");
    }

    @SuppressWarnings("rawtypes")
    private void loadFile(File file) throws Exception {
        synchronized (lock) {

            //Note: we could probably do things faster: i.e.: instead of loading with Yaml, just search
            //for a regexp (but let's leave that as an optimization for later on).
            Yaml yaml = new Yaml();
            Object load = yaml.load(FileUtils.getFileContents(file));
            if (!(load instanceof Map)) {
                if (load == null) {
                    throw new RuntimeException("Error, the file: " + file + " is not a valid yaml file.");
                }
                throw new RuntimeException("Expected map. found: " + load.getClass());
            }
            Map data = (Map) load;
            String name = (String) data.get(LiClipseLanguage.NAME);
            String[] s = asStringArrayOrNull(data.get(LiClipseLanguage.SHEBANG));
            ArrayList<Pattern> shebang = null;
            if (s != null) {
                shebang = new ArrayList<Pattern>(s.length);
                for (String string : s) {
                    try {
                        Pattern p = Pattern.compile(string);
                        shebang.add(p);
                    } catch (Exception e) {
                        Log.log("Unable to compile regexp: " + string, e);
                    }
                }
            }
            LanguageMetadata languageMetadata = new LanguageMetadata(name, new LanguageMetadataFileInfo(file, data),
                    shebang == null ? null
                            : shebang.toArray(new Pattern[shebang.size()]),
                    LanguageType.LICLIPSE, name);
            languageNameToMetadata.put(name, languageMetadata);

            List<String> list = (List) data.get(LiClipseLanguage.FILE_EXTENSION);
            registerFileExtensions(file, languageMetadata, list);

            list = (List) data.get(LiClipseLanguage.FILENAME);
            registerFilenames(file, languageMetadata, list);
        }
    }

    private void registerFilenames(File file, LanguageMetadata languageMetadata, List<String> list) {
        if (list != null) {
            if (filenames == null) {
                filenames = new HashMap<String, LanguageMetadata>();
            }
            fill(file, filenames, list, languageMetadata);
        }
    }

    private void registerFileExtensions(File file, LanguageMetadata languageMetadata, List<String> list) {
        if (list != null) {
            if (fileExtensions == null) {
                fileExtensions = new HashMap<String, LanguageMetadata>();
            }
            fill(file, fileExtensions, list, languageMetadata);
        }
    }

    private String[] asStringArrayOrNull(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return new String[] { object.toString() };
        }
        if (object instanceof String[]) {
            return (String[]) object;
        }
        Log.log("Ignoring shebang (expecting string or array of strings). Found: " + object.getClass() + " (" + object
                + ")");
        return null;
    }

    private void fill(File file, Map<String, LanguageMetadata> map, List<String> list, LanguageMetadata metadata) {
        for (String s : list) {
            if (map.containsKey(s)) {
                LanguageMetadata old = map.get(s);
                File parentFolder = old.file.getParentFolder();
                if (parentFolder != null) {
                    File parentFile = file.getParentFile();
                    if (parentFolder.equals(parentFile)) {
                        //I.e.: both files are located at the same directory.
                        //If that's the case, textmate bundles should not override liclipse bundles.
                        if (old.getType() == LanguageType.LICLIPSE && metadata.getType() == LanguageType.TEXT_MATE) {
                            continue;
                        }
                    }
                }
                String msg = "Warning: overriding default language definition for file extension: %s\nOld file: %s\nNew file: %s";
                Log.logInfo(StringUtils.format(msg, s, old, file));
            }

            // Add regular and lowercase (prefer lowercase)
            map.put(s, metadata);
            final String lower = s.toLowerCase();
            if (!map.containsKey(lower)) {
                // Only put the lower if it's still not there
                map.put(lower, metadata);
            }
        }
    }

    private final Map<ILanguageMetadataFileInfo, Tuple<LiClipseLanguage, Long>> cacheSetup = new HashMap<ILanguageMetadataFileInfo, Tuple<LiClipseLanguage, Long>>();
    private final Map<String, Set<Long>> cacheExtensions = new HashMap<>(); // Language name -> modified times of dependencies

    /**
     * @param filename: just the name of the file (not complete path). I.e.: Scons, foo.py
     * @return the language to be used for the given filename.
     * @note: may return null if we did not have a match.
     * @see {@link #getLanguage(String, IDocument)}: this is the preferred way to get a language.
     */
    public LiClipseLanguage getLanguageForFilename(String filename) {
        LanguageMetadata metadata = this.getMetadataForFilename(filename);
        if (metadata != null) {
            return getLanguageFromMetadata(metadata);
        }
        return new LiClipseLanguage();
    }

    /**
     * A class that identifies a language or if the language is not provided, its binary status.
     * @author Fabio
     */
    public static final class LanguageOrBinary {

        /**
         * The language to which a file is related.
         */
        public final LiClipseLanguage language;

        /**
         * Only actually read if language == null (otherwise, if language is provided, always considered non-binary).
         */
        public final boolean isBinary;

        public LanguageOrBinary(LiClipseLanguage language, boolean isBinary) {
            this.language = language;
            this.isBinary = isBinary;
        }
    }

    /**
     * Gets a language based on the given file.
     */
    public LanguageOrBinary getLanguageFromFileContents(File file) {
        synchronized (lock) {
            ReadLines read = FileUtils.readLines(file);
            if (read == null || read.size() == 0) {
                return new LanguageOrBinary(null, read.isBinary());
            }
            LiClipseLanguage languageFromContents = getLanguageFromContents(read.lines);
            if (languageFromContents != null) {
                return new LanguageOrBinary(languageFromContents, false);
            }
            return new LanguageOrBinary(null, read.isBinary());
        }
    }

    public LiClipseLanguage getLanguageFromMetadata(LanguageMetadata metadata) {
        synchronized (lock) {
            Tuple<LiClipseLanguage, Long> cached = cacheSetup.get(metadata.file);
            Set<Long> modifiedTimesExtensions = new HashSet<>();
            List<File> lst = this.extendLanguages.get(metadata.languageName);
            if (lst != null) {
                for (File file : lst) {
                    modifiedTimesExtensions.add(FileUtils.lastModified(file));
                }
            }

            if (cached != null) {
                long modified = metadata.file.lastModified();
                if (modified == cached.o2) {

                    Set<Long> cachedExtensionsTime = cacheExtensions.get(metadata.languageName);
                    boolean equals = false;
                    if (cachedExtensionsTime == null) {
                        if (modifiedTimesExtensions.size() == 0) {
                            equals = true;
                        }
                    } else {
                        equals = cachedExtensionsTime.equals(modifiedTimesExtensions);
                    }
                    if (equals) {
                        // Cache hit: file time (and dependencies time) did not change!
                        return cached.o1;
                    }
                }
            }
            try {
                LiClipseLanguage ret = metadata.file.loadLanguage(false);
                // Cache it based on the modified time.
                cacheSetup.put(metadata.file,
                        new Tuple<LiClipseLanguage, Long>(ret, metadata.file.lastModified()));
                cacheExtensions.put(metadata.languageName, modifiedTimesExtensions);
                return ret;
            } catch (Throwable e) {
                Log.log(e);
            }
            return new LiClipseLanguage();
        }
    }

    /**
     * null is expected if there's no registry for the given filename.
     */
    public LanguageMetadata getMetadataForFilename(String filename) {
        filename = filename.toLowerCase();
        synchronized (lock) {

            if (this.filenames != null) {
                LanguageMetadata metadata = this.filenames.get(filename);
                if (metadata != null) {
                    return metadata;
                }
            }

            if (this.fileExtensions != null) {
                int i = filename.lastIndexOf('.');
                if (i != -1) {
                    String substring = filename.substring(i + 1);
                    LanguageMetadata metadata = this.fileExtensions.get(substring);
                    if (metadata != null) {
                        return metadata;
                    }
                } else {
                    // Note: if the file has no extension consider the whole filename
                    LanguageMetadata metadata = this.fileExtensions.get(filename);
                    if (metadata != null) {
                        return metadata;
                    }
                }
            }
            return null;
        }
    }

    /**
     * This is the preferred way for getting a language. It's based on the filename and
     * the actual document (so, we may base it on the file shebang).
     *
     * @return the setup to be used. Never returns null (returns at least the setup for a regular
     * .txt file).
     */
    public LiClipseLanguage getLanguage(String filename, IDocument document) {
        synchronized (lock) {

            LiClipseLanguage setup = getLanguageForFilename(filename);
            if (setup == null || setup.name == null) {
                if (document.getLength() > 0) {
                    setup = getLanguageFromContents(Arrays.asList(TextSelectionUtils.getLine(document, 0),
                            TextSelectionUtils.getLine(document, 1)));
                }
            }
            if (setup == null) {
                setup = new LiClipseLanguage();
            }
            return setup;
        }
    }

    public List<LanguageMetadata> getLanguagesMetadata() {
        synchronized (lock) {

            ArrayList<LanguageMetadata> lst = new ArrayList<LanguageMetadata>();
            lst.addAll(this.languageNameToMetadata.values());
            return lst;
        }
    }

    public LiClipseLanguage getLanguageFromContents(List<String> lines) {
        synchronized (lock) {

            if (lines == null || lines.size() == 0) {
                return null;
            }
            Collection<LanguageMetadata> values = languageNameToMetadata.values();
            for (LanguageMetadata languageMetadata : values) {
                if (languageMetadata.shebang != null) {
                    Pattern[] s = languageMetadata.shebang;
                    for (Pattern p : s) {
                        for (String line : lines) {
                            if (line.length() == 0) {
                                continue;
                            }
                            if (p.matcher(line).find()) {
                                return getLanguageFromMetadata(languageMetadata);
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    public LiClipseLanguage getLanguageFromName(String languageName) {
        synchronized (lock) {

            List<LanguageMetadata> languagesMetadata = this.getLanguagesMetadata();
            for (LanguageMetadata languageMetadata : languagesMetadata) {
                if (languageName.equalsIgnoreCase(languageMetadata.languageName)) {
                    return getLanguageFromMetadata(languageMetadata);
                }
            }
            Log.log("Unable to find language named: " + languageName);
            return null;
        }
    }

    /**
     * Use with care: will load all the languages!
     *
     * Returns a language for the given editor id or null if there's no default language for the
     * given editor.
     */
    public LiClipseLanguage getLanguageFromEditorId(String editorId) {
        synchronized (lock) {

            List<LanguageMetadata> languagesMetadata = this.getLanguagesMetadata();
            for (LanguageMetadata languageMetadata : languagesMetadata) {
                LiClipseLanguage languageFromMetadata = getLanguageFromMetadata(languageMetadata);
                if (editorId.equals(languageFromMetadata.editorId)) {
                    return languageFromMetadata;
                }
            }
            return null;
        }
    }

    public File[] getLanguagesDirectories() {
        synchronized (lock) {

            return this.languagesDir;
        }
    }

    private boolean changeTrackedDir(File file, boolean add) {
        boolean changed = false;
        synchronized (lock) {
            IPreferenceStore preferenceStore = LiClipseTextEditorPlugin.getDefault().getPreferenceStore();
            String string = preferenceStore.getString(ADDITIONAL_TRACKED_DIRS);
            Set<String> split = new OrderedSet<>(StringUtils.split(string, '|'));
            if (add) {
                changed = split.add(file.getAbsolutePath());
            } else {
                //remove
                changed = split.remove(file.getAbsolutePath());
            }
            if (changed) {
                preferenceStore.setValue(ADDITIONAL_TRACKED_DIRS,
                        StringUtils.join("|", split.toArray(new String[split.size()])));
                if (preferenceStore instanceof IPersistentPreferenceStore) {
                    IPersistentPreferenceStore iPersistentPreferenceStore = (IPersistentPreferenceStore) preferenceStore;
                    try {
                        iPersistentPreferenceStore.save();
                    } catch (Exception e) {
                        Log.log(e);
                    }
                }
                updateTrackedDirs();
            }
        }
        if (changed) {
            reloadJob.schedule(JOB_TIMEOUT);
        }
        return changed;

    }

    public boolean addDirectoryToBeTracked(File dir) {
        return changeTrackedDir(dir, true);
    }

    public boolean stopTrackingDirectory(File dir) {
        return changeTrackedDir(dir, false);
    }

    public boolean isValidTextFile(File file) {
        final String name = file.getName().toLowerCase();
        LanguageMetadata metadata = this.getMetadataForFilename(name);
        boolean isValidTextFile = metadata != null;

        // System.out.println("LiClipse Extensions:");
        // System.out.println(StringUtils.join("\n", fileExtensions.keySet()));
        //
        // System.out.println("LiClipse Filenames:");
        // System.out.println(StringUtils.join("\n", filenames.keySet()));
        //
        // System.out.println("Extensions:");
        // System.out.println(StringUtils.join("\n", textExtensions.o1));
        //
        // System.out.println("Filenames:");
        // System.out.println(StringUtils.join("\n", textExtensions.o2));

        if (!isValidTextFile) {
            Tuple<Set<String>, Set<String>> platformTextExtensions = getPlatformTextExtensionsLower();

            String ext = StringUtils.getFileExtension(name);
            if (ext == null || ext.isEmpty()) {
                // If it doesn't have an extension, check in both
                if (platformTextExtensions.o2.contains(name) || platformTextExtensions.o1.contains(name)) {
                    isValidTextFile = true;
                } else {
                    // For files without a known extension, we check their contents.
                    LanguageOrBinary languageForFile = this.getLanguageFromFileContents(file);
                    if (languageForFile != null && !languageForFile.isBinary) {
                        // index if not binary.
                        isValidTextFile = true;
                    }
                }

            } else {
                if (platformTextExtensions.o1.contains(ext)) {
                    isValidTextFile = true;
                }
            }
        }
        return isValidTextFile;
    }

    /**
     * @return the extensions and filenames for text-files (creates internal cache to make it faster later on).
     */
    private Tuple<Set<String>, Set<String>> getPlatformTextExtensionsLower() {
        Tuple<Set<String>, Set<String>> ret = contentTypeTextExtensionsLower;
        if (ret == null) {
            synchronized (contentTypeTextExtensionsLock) {
                if (contentTypeTextExtensionsLower == null) {
                    contentTypeTextExtensionsLower = computePlatformLowerCaseTextExtensions();

                    IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
                    // Clear the cache if it changes later on (calling multiple times is Ok).
                    contentTypeManager.addContentTypeChangeListener(contentTypeChangeListenerCacheClearer);
                }
                ret = contentTypeTextExtensionsLower;
            }
        }
        return ret;
    }

    /**
     * When set the first element has the extensions and the second the filenames.
     */
    Tuple<Set<String>, Set<String>> contentTypeTextExtensionsLower;

    private final Object contentTypeTextExtensionsLock = new Object();

    private final IContentTypeChangeListener contentTypeChangeListenerCacheClearer = new IContentTypeChangeListener() {

        @Override
        public void contentTypeChanged(ContentTypeChangeEvent event) {
            synchronized (contentTypeTextExtensionsLock) {
                //Clear our caches
                contentTypeTextExtensionsLower = null;
            }
        }
    };

    /**
     * Only meant to be called from getTextExtensions.
     * @return the id mapping to the subtree for that id.
     */
    private Map<String, TreeNode<IContentType>> buildContentTypeTree() {
        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        IContentType[] allContentTypes = contentTypeManager.getAllContentTypes();

        TreeNode<IContentType> root = new TreeNode<>(null, null);
        Map<String, TreeNode<IContentType>> idToContentTypeNode = new HashMap<>();

        for (IContentType contentType : allContentTypes) {
            idToContentTypeNode.put(contentType.getId(), new TreeNode<IContentType>(null, contentType));
        }
        idToContentTypeNode.put("", root); // Root has empty id

        Set<Entry<String, TreeNode<IContentType>>> entrySet = idToContentTypeNode.entrySet();
        for (Entry<String, TreeNode<IContentType>> entry : entrySet) {
            TreeNode<IContentType> node = entry.getValue();
            if (node.data == null) {
                continue; // root.
            }
            IContentType baseType = node.data.getBaseType();
            if (baseType == null) {
                node.setParent(root);
            } else {
                node.setParent(idToContentTypeNode.get(baseType.getId()));
            }
        }
        return idToContentTypeNode;
    }

    /**
     * Only meant to be called from getTextExtensions.
     */
    private Tuple<Set<String>, Set<String>> computePlatformLowerCaseTextExtensions() {
        Map<String, TreeNode<IContentType>> buildContentTypeTree = this.buildContentTypeTree();
        Set<String> extensions = new HashSet<>();
        Set<String> filenamesRegistered = new HashSet<>();

        TreeNode<IContentType> textNode = buildContentTypeTree.get("org.eclipse.core.runtime.text");
        if (textNode != null) {
            fillLowerCaseSets(textNode, extensions, filenamesRegistered);
        } else {
            Log.log("Could not find org.eclipse.core.runtime.text content type.");
        }

        Tuple<Set<String>, Set<String>> tup = new Tuple<Set<String>, Set<String>>(extensions, filenamesRegistered);
        return tup;
    }

    @SuppressWarnings("rawtypes")
    private void fillLowerCaseSets(TreeNode<IContentType> textNode, Set<String> extensions,
            Set<String> filenamesRegistered) {
        IContentType contentType = textNode.data;
        if (contentType != null) {
            String[] exts = contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
            for (String string : exts) {
                extensions.add(string.toLowerCase());
            }
            String[] filenames = contentType.getFileSpecs(IContentType.FILE_NAME_SPEC);
            for (String string : filenames) {
                filenamesRegistered.add(string.toLowerCase());
            }
        }

        List<TreeNode> children = textNode.getChildren();
        if (children != null) {
            for (TreeNode<IContentType> treeNode : children) {
                fillLowerCaseSets(treeNode, extensions, filenamesRegistered);
            }
        }
    }

    public IGrammar getTm4EGrammar(LiClipseLanguage language) throws Exception {
        IStreamProvider streamProvider = language.file.getTmLanguageStreamProvider();

        IGrammar grammar = fRegistry.grammarForScopeName(language.name);
        if (grammar == null) {
            grammar = fRegistry.loadGrammarFromPathSync(".tmLanguage",
                    streamProvider.getStream());
        }
        return grammar;
    }

}
