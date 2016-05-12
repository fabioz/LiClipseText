/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.navigation;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.EditorUtils;
import org.brainwy.liclipsetext.shared_ui.utils.RunInUiThread;

public class NotifyCtagsErrorDialog {

    public final static int INTERPRETER_NOTIFY_AGAIN = 0;
    public final static int INTERPRETER_IGNORE = 1;
    public final static int INTERPRETER_CANCEL_CONFIG = -1;

    private static final String DONT_ASK_AGAIN_PREFERENCE_VALUE = "DONT_ASK";

    private final static String key = "CTAGS_ERROR_MSG";

    public static void notifyError(Exception e) {
        Log.log(e);

        LiClipseTextEditorPlugin plugin = LiClipseTextEditorPlugin.getDefault();
        if (plugin == null) {
            return;
        }
        final String errorMsg = e.getMessage();
        final IPreferenceStore store = plugin.getPreferenceStore();
        String val = store.getString(key);

        if (!DONT_ASK_AGAIN_PREFERENCE_VALUE.equals(val)) {
            RunInUiThread.async(new Runnable() {

                public void run() {
                    String title = "Error running CTAGS";
                    String message = "Some error occurred running CTAGS.\n\n"
                            + "Please make sure it's properly installed on your system and accessible through your\n"
                            + "PATH environment variable (you may have to restart Eclipse after adding it to your PATH).\n\n"

                            + "CTAGS is used for the outline or navigation, so, not having it may prevent some features\n"
                            + "from working properly for some languages.\n\n"

                            + "Error: " + errorMsg
                            + "\n\nSee the error log for more details.\n"
                            + "";

                    Shell shell = EditorUtils.getShell();
                    MessageDialog dialog = new MessageDialog(shell, title, null, message, MessageDialog.QUESTION,
                            new String[] {
                                    "Notify about error again",
                                    "Don't show this error again" }, 0);
                    int open = dialog.open();
                    switch (open) {
                        case 0:
                            break;

                        case 1:
                            store.putValue(key, DONT_ASK_AGAIN_PREFERENCE_VALUE);
                            break;
                    }
                }
            });
        }
    }

}
