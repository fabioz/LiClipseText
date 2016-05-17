/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.launch;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.LanguageConfig;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.SharedCorePlugin;
import org.brainwy.liclipsetext.shared_core.locator.BaseItemPointer;
import org.brainwy.liclipsetext.shared_core.locator.GetFiles;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.FastStringBuffer;
import org.brainwy.liclipsetext.shared_core.structure.Location;
import org.brainwy.liclipsetext.shared_core.utils.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.ui.console.IHyperlink;

public class LanguageLaunch extends LanguageConfig {

    // Settings related to hyperlinking
    private static class HyperlinkInfo {
        private String regexp; //may be null (in which case we can't match hyperlinks).
        private int filenameGroup;
        private Integer lineGroup; //may be null
        private Integer startGroup; //may be null
        private Integer endGroup; //may be null
        private Pattern linePattern;
    }

    HyperlinkInfo[] hyperlinkInfos = new HyperlinkInfo[0];

    // Settings related to creating a launch configuration (settings should never be null, but may be empty)
    private String command = "";
    private String vmArgs = "";
    private final Map<String, String> environment = new HashMap<>();

    private final GetFiles getFiles = new GetFiles();

    public LanguageLaunch(LiClipseLanguage liClipseLanguage) {
        super(liClipseLanguage);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void load(Map<String, Object> launch, List<IStatus> errorList) {
        if (launch == null) {
            return; //Nothing to load!
        }
        Object command = launch.remove("command");
        if (command == null) {
            command = "";
        }
        this.command = command.toString().trim();

        Object vmArgs = launch.remove("vm_args");
        if (vmArgs == null) {
            vmArgs = "";
        }
        this.vmArgs = vmArgs.toString().trim();

        Object environment = launch.remove("environment");
        if (!(environment instanceof Map)) {
            LiClipseTextEditorPlugin.createWarning("Expected 'environment' to be a map: "
                    + environment, errorList);
        } else {
            this.environment.clear();
            Map<Object, Object> map = (Map) environment;
            Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
            for (Map.Entry<Object, Object> entry : entrySet) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key == null) {
                    key = "null";
                }
                if (value == null) {
                    value = "null";
                }
                this.environment.put(key.toString(), value.toString());
            }
        }

        // Hyperlinking
        Object hyperlink = launch.remove("hyperlink");
        if (hyperlink != null) {
            if (hyperlink instanceof List) {
                List list = (List) hyperlink;
                for (Object object : list) {
                    checkCreateHyperlinkFromObject(errorList, object);
                }
            } else if (hyperlink instanceof Map[]) {
                Map[] array = (Map[]) hyperlink;
                for (Object object : array) {
                    checkCreateHyperlinkFromObject(errorList, object);
                }
            } else {
                checkCreateHyperlinkFromObject(errorList, hyperlink);
            }
        }

    }

    @SuppressWarnings("rawtypes")
    private void checkCreateHyperlinkFromObject(List<IStatus> errorList, Object hyperlink) {
        if (hyperlink instanceof Map) {
            createHyperlinkInfoFromMap(errorList, (Map) hyperlink);
        } else {
            LiClipseTextEditorPlugin.createWarning("Expected 'hyperlink' to be a map: "
                    + hyperlink, errorList);
        }
    }

    @SuppressWarnings("rawtypes")
    private void createHyperlinkInfoFromMap(List<IStatus> errorList, Map map) {
        HyperlinkInfo hyperlinkInfo = new HyperlinkInfo();
        Object regexp = map.remove("regexp");
        if (regexp != null) {
            if (regexp instanceof String) {
                hyperlinkInfo.regexp = (String) regexp;

                Object obj = map.remove("filename");
                Integer filenameGroup = asInt(obj, null, errorList);
                if (filenameGroup == null) {
                    LiClipseTextEditorPlugin.createWarning("Expected 'filename' to be an integer: "
                            + obj, errorList);
                    hyperlinkInfo.regexp = null; //at least the filename and the regexp must be available!
                } else {
                    hyperlinkInfo.filenameGroup = filenameGroup;
                }

                hyperlinkInfo.lineGroup = asInt(map.remove("line"), null, errorList);
                hyperlinkInfo.startGroup = asInt(map.remove("start"), null, errorList);
                hyperlinkInfo.endGroup = asInt(map.remove("end"), null, errorList);

                hyperlinkInfo.linePattern = null;
                if (hyperlinkInfo.regexp != null) {
                    try {
                        hyperlinkInfo.linePattern = Pattern.compile(hyperlinkInfo.regexp);
                        this.hyperlinkInfos = ArrayUtils.concatArrays(this.hyperlinkInfos,
                                new HyperlinkInfo[] { hyperlinkInfo });
                    } catch (Exception e) {
                        LiClipseTextEditorPlugin.createWarning(
                                "Error compiling regexp: >>" + regexp + "<<: " + e.getMessage(),
                                errorList);
                    }
                }
            } else {
                LiClipseTextEditorPlugin.createWarning("Expected 'regexp' to be a string: "
                        + regexp, errorList);

            }
        }
    }

    public String linkToString() {
        FastStringBuffer buf = new FastStringBuffer();
        for (HyperlinkInfo info : hyperlinkInfos) {
            buf.append("Regexp: ").appendObject(info.regexp).
                    append("\nFilename group:").append(info.filenameGroup).
                    append("\nLine group:").appendObject(info.lineGroup).
                    append("\nStart group:").appendObject(info.startGroup).
                    append("\nEnd group:").appendObject(info.endGroup).append('\n').
                    toString();
        }
        return buf.toString();
    }

    /**
     * Creates the links for the console.
     */
    public void createLinks(int lineOffset, int lineLength, String lineText, ILinkCreator linkCreator) {
        for (HyperlinkInfo info : hyperlinkInfos) {
            if (info.linePattern == null) {
                continue;
            }
            Matcher m = info.linePattern.matcher(lineText);
            String fileName = null;
            String lineNumber = null;
            int fileStart = -1;
            int end = -1;
            // match
            if (m.matches()) {
                fileName = m.group(info.filenameGroup);
                lineNumber = m.group(info.lineGroup);
                if (info.startGroup == null) {
                    fileStart = m.start(0);

                } else {
                    fileStart = m.start(info.startGroup); // The beginning of the line, "File  "

                }
                if (info.endGroup != null) {
                    end = m.end(info.endGroup);
                }
            }
            // hyperlink if we found something
            if (fileName != null) {
                IHyperlink link = null;
                int num = -1;
                try {
                    num = lineNumber != null ? Integer.parseInt(lineNumber) : 0;
                } catch (NumberFormatException e) {
                    num = 0;
                }
                IFile file;
                IProject project = null;
                if (!SharedCorePlugin.inTestMode()) {
                    IProcess process = DebugUITools.getCurrentProcess();
                    if (process != null) {
                        ILaunchConfiguration lc = process.getLaunch().getLaunchConfiguration();
                        try {
                            project = lc.getMappedResources()[0].getProject();
                        } catch (NullPointerException e) {
                            //Ignore if we don't have lc or mapped resources.
                        } catch (CoreException e) {
                            Log.log("Error accessing launched resources.", e);
                        }
                    }
                }

                file = getFiles.getFileForLocation(Path.fromOSString(fileName), project);

                if (file != null && file.exists()) {
                    link = new FileLink(file, null, -1, -1, num);
                }
                else {
                    // files outside of the workspace
                    File realFile = new File(fileName);
                    BaseItemPointer p = new BaseItemPointer(realFile, new Location(num - 1, 0), null);
                    link = new ConsoleLink(p);
                }
                if (link != null) {
                    if (end > fileStart) {
                        linkCreator.addLink(link, lineOffset + fileStart, end - fileStart);
                    } else {
                        linkCreator.addLink(link, lineOffset + fileStart, lineLength - fileStart);
                    }
                    //bail out on first match in line
                    break;
                }
            }
        }
    }

    /**
     * @return true if the language had the needed settings set and false if a dialog must be opened
     * for the user to fill things in.
     */
    public boolean fillDefaults(ILaunchConfigurationWorkingCopy workingCopy) {
        workingCopy.setAttribute(IExternalToolConstants.ATTR_EXECUTABLE, this.command);
        workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_VM_ARGUMENTS, this.vmArgs);
        workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<>(this.environment));
        workingCopy.setAttribute(IExternalToolConstants.ATTR_LICLIPSE_LANGUAGE, this.liClipseLanguage.get().name);

        return this.command.length() > 0;

    }
}
