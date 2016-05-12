package org.brainwy.liclipsetext.editor.error_handling;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brainwy.liclipsetext.editor.handlers.extensions.BaseLiClipseHandler;
import org.brainwy.liclipsetext.editor.handlers.extensions.ILiClipseErrorCollector;
import org.brainwy.liclipsetext.editor.languages.ILanguageMetadataFileInfo;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadataInMemoryFileInfo;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IEditorInput;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;

public class LiClipseLanguageErrorCollector extends BaseLiClipseHandler implements ILiClipseErrorCollector {

    public LiClipseLanguageErrorCollector() {
        super("liclipse");
    }

    public List<Map<String, Object>> collectErrors(IEditorInput iEditorInput, File editorFile, String docContents) {
        ArrayList<Map<String, Object>> ret = new ArrayList<Map<String, Object>>(1);
        //size == 1 (that's all we collect in this validation).
        try {
            // After doing the save, check for errors in the file.
            try {
                ILanguageMetadataFileInfo file = new LanguageMetadataInMemoryFileInfo(docContents);
                file.loadLanguage(true);

            } catch (Throwable e) {
                //System.out.println(e.getClass());
                //e.printStackTrace();
                String msg = "" + e.getMessage(); //""+ to make sure it's not null!
                int line = 1;
                if (e instanceof MarkedYAMLException) {
                    MarkedYAMLException parserException = (MarkedYAMLException) e;
                    Mark problemMark = parserException.getProblemMark();
                    line = problemMark.getLine() + 1;
                } else {
                    while (e.getCause() != null) {
                        e = e.getCause();
                        msg += "\n" + e.getMessage();
                    }
                }
                Map<String, Object> map = new HashMap<String, Object>();

                map.put(IMarker.MESSAGE, msg);
                map.put(IMarker.SOURCE_ID, "liclipse_marker");
                map.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                map.put(IMarker.LINE_NUMBER, line);
                map.put(IMarker.TRANSIENT, true);
                ret.add(map);
            }
        } catch (Exception e) {
            Log.log(e);
        }
        return ret;
    }

}
