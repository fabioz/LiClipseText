/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.brainwy.liclipsetext.shared_core.io.FileUtils;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.preferences.ScopedPreferences;
import org.brainwy.liclipsetext.shared_core.structure.Tuple;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.yaml.snakeyaml.Yaml;

import com.eclipsesource.json.JsonObject;

public class ConfigFile {

    /**
     * Returns the contents of the configuration file to be used or null.
     */
    public static Tuple<String, IPath> getConfigFileContents(IEditorInput iEditorInput, File editorFile,
            String filename) {
        IFile foundFile = null;
        try {
            if (iEditorInput != null) {
                IFile iFile = iEditorInput.getAdapter(IFile.class);
                if (iFile != null) {
                    IProject project = iFile.getProject();
                    if (project != null && project.exists()) {
                        foundFile = project.getFile(new Path(".settings").append(filename));
                        if (foundFile.exists()) {
                            IDocument docFromResource = FileUtils.getDocFromResource(foundFile);
                            return new Tuple<String, IPath>(docFromResource.get(), foundFile.getFullPath());
                        }

                        foundFile = project.getFile(new Path(filename));
                        if (foundFile.exists()) {
                            IDocument docFromResource = FileUtils.getDocFromResource(foundFile);
                            return new Tuple<String, IPath>(docFromResource.get(), foundFile.getFullPath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.log(e);
        }
        return null;
    }

    /**
     * Will return the options to pass to jshint. If null jshint should not be executed.
     * Default .javascript.liclipseprefs file:

     # Option determining whether JSHint should be run
     enable_jshint: true


     # Json contents to be passed to JSHint
     jshint_opts: |
         {
             "undef": true,
             "browser": true,
             "jquery": true
         }

     */
    @SuppressWarnings("unchecked")
    public static JsonObject getJsHintConfigOpts(IEditorInput iEditorInput, File editorFile) {
        //More info on: http://www.jshint.com/docs/options/
        //http://www.jshint.com/docs/
        final String DEFAULT_OPTS = ""
                + "{\n"
                + "  \"undef\": true,\n"
                + "  \"browser\": true,\n"
                + "  \"jquery\": true\n"
                + "}\n"
                + "\n"
                + "";

        String opts = DEFAULT_OPTS;

        Tuple<String, IPath> found = ConfigFile.getConfigFileContents(iEditorInput, editorFile,
                ".javascript.liclipseprefs");
        try {
            if (found != null) {
                if (found.o1 != null) {
                    Yaml yaml = new Yaml();
                    Object load = yaml.load(found.o1);
                    if (!(load instanceof Map)) {
                        if (load == null) {
                            throw new RuntimeException("Expected top-level element to be a map. Found: null");
                        }
                        throw new RuntimeException("Expected top-level element to be a map. Found: " + load.getClass());
                    }
                    Map<String, Object> config = (Map<String, Object>) load;
                    Object enable = config.get("enable_jshint");
                    if (enable != null) {
                        if (!isTrue(config, "enable_jshint")) {
                            return null;
                        }
                    }
                    Object jsHint = config.get("jshint_opts");
                    if (jsHint != null) {
                        opts = jsHint.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.log("Error loading: " + found, e);
        }
        try {
            return JsonObject.readFrom(opts);
        } catch (Exception e) {
            Log.log("Error loading json: " + opts, e);
        }
        return JsonObject.readFrom(DEFAULT_OPTS);
    }

    public static JsonObject getJsBeautifyConfigOpts(IEditorInput iEditorInput, File editorFile) {
        final String DEFAULT_OPTS = ""
                + "{\n"
                + "	\"indent_size\": 4,\n"
                + "	\"indent_char\": \" \",\n"
                + "	\"preserve_newlines\": true,\n"
                + "	\"max_preserve_newlines\": 2147483647,\n"
                + "	\"jslint_happy\": false,\n"
                + "	\"space_after_anon_function\": false,\n"
                + "	\"brace_style\":  \"collapse\",\n"
                + "	\"space_before_conditional\": true,\n"
                + "	\"unescape_strings\": false,\n"
                + "	\"wrap_line_length\": 2147483647,\n"
                + "	\"end_with_newline\":false\n"
                + "}\n"
                + "\n"
                + "";
        String opts = DEFAULT_OPTS;

        Tuple<String, IPath> found = ConfigFile.getConfigFileContents(iEditorInput, editorFile,
                ".javascript.liclipseprefs");
        try {
            if (found != null) {
                if (found.o1 != null) {
                    Yaml yaml = new Yaml();
                    Object load = yaml.load(found.o1);
                    if (!(load instanceof Map)) {
                        if (load == null) {
                            throw new RuntimeException("Expected top-level element to be a map. Found: null");
                        }
                        throw new RuntimeException("Expected top-level element to be a map. Found: " + load.getClass());
                    }
                    Map<String, Object> config = (Map<String, Object>) load;
                    Object enable = config.get("enable_beautify_js");
                    if (enable != null) {
                        if (!isTrue(config, "enable_beautify_js")) {
                            return null;
                        }
                    }
                    Object jsHint = config.get("beautify_js_opts");
                    if (jsHint != null) {
                        opts = jsHint.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.log("Error loading: " + found, e);
        }
        try {
            return JsonObject.readFrom(opts);
        } catch (Exception e) {
            Log.log("Error loading json: " + opts, e);
        }
        return JsonObject.readFrom(DEFAULT_OPTS);
    }

    /** Default .html.liclipseprefs if not provided would be:

    # If true, we'll show error markers for the file, otherwise no errors will be collected nor shown.
    collect_errors: true

    # If true, code formatting will be enabled, if false it'll be disabled.
    enable_code_format: true


    # If true, the html will be fixed to be conformant by using: http://about.validator.nu/htmlparser/
    fix_html: true

    # The flags below represent properties set for the jericho html parser.
    # More details at:
    # http://jericho.htmlparser.net/docs/javadoc/net/htmlparser/jericho/SourceFormatter.html

    # The indent is formed by writing n repetitions of the string specified in the indent_string property,
    # where n is the depth of the indentation.
    indent_string: '    '

    # If the tidy_tags property is set to true, every tag in the document is replaced with the output from its
    # Tag.tidy() method. If this property is set to false, the tag from the original text is used, including
    # all white space, but with any new lines indented at a depth one greater than that of the element.
    tidy_tags: false


    # If the collapse_whitespace property is set to true, every string of one or more white space characters located
    # outside of a tag is replaced with a single space in the output. White space located adjacent to a
    # non-inline-level element tag (except server tags) may be removed.
    collapse_whitespace: false

    # If the indent_all_elements property is set to true, every element appears indented on a new line, including
    # inline-level elements. This generates output that is a good representation of the actual document element
    # hierarchy, but is very likely to introduce white space that compromises the functional equivalency of the document.
    indent_all_elements: false
     */
    public static Map<String, Object> getHtmlConfig(IEditorInput iEditorInput, File editorFile) {
        HashMap<String, Object> defaultConfig = new HashMap<String, Object>();
        defaultConfig.put("collect_errors", true);
        defaultConfig.put("enable_code_format", true);
        defaultConfig.put("fix_html", true);
        defaultConfig.put("indent_string", "    ");
        defaultConfig.put("tidy_tags", false);
        defaultConfig.put("collapse_whitespace", false);
        defaultConfig.put("indent_all_elements", false);
        String opts = null;
        String configFilename = ".html.liclipseprefs";

        Map<String, Object> config = patchConfigOptions(iEditorInput, editorFile, defaultConfig, opts, configFilename);
        return config;
    }

    /** Default .yaml.liclipseprefs if not provided would be:

    # If true, we'll show error markers for the file, otherwise no errors will be collected nor shown.
    collect_errors: true
     */
    public static Map<String, Object> getYamlConfig(IEditorInput iEditorInput, File editorFile) {
        HashMap<String, Object> defaultConfig = new HashMap<String, Object>();
        defaultConfig.put("collect_errors", true);
        String opts = null;
        String configFilename = ".yaml.liclipseprefs";

        Map<String, Object> config = patchConfigOptions(iEditorInput, editorFile, defaultConfig, opts, configFilename);
        return config;

    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> patchConfigOptions(IEditorInput iEditorInput, File editorFile,
            HashMap<String, Object> defaultConfig, String opts, String configFilename) {
        Tuple<String, IPath> configFileContents = ConfigFile.getConfigFileContents(iEditorInput, editorFile,
                configFilename);
        if (configFileContents != null) {
            opts = configFileContents.o1;
        }

        Map<String, Object> config = defaultConfig;
        if (opts != null && opts.length() > 0) {
            try {
                Yaml yaml = new Yaml();
                Object load = yaml.load(opts);
                if (!(load instanceof Map)) {
                    if (load == null) {
                        throw new RuntimeException("Expected top-level element to be a map. Found: null");
                    }
                    throw new RuntimeException("Expected top-level element to be a map. Found: " + load.getClass());
                }

                config = (Map<String, Object>) load;

                //If something wasn't set, use the default for it.
                Set<Entry<String, Object>> entrySet = defaultConfig.entrySet();
                for (Entry<String, Object> entry : entrySet) {
                    if (config.get(entry.getKey()) == null) {
                        config.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception e) {
                Log.log("Error loading: " + configFileContents + " falling back to default.", e);
            }
        }
        return config;
    }

    public static boolean isTrue(Map<String, Object> config, String string) {
        Object found = config.get(string);
        return ScopedPreferences.toBoolean(found);
    }

}
