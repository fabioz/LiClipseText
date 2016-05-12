/**
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.shared_ui.editor;

import java.lang.ref.WeakReference;
import java.util.ListResourceBundle;

import org.brainwy.liclipsetext.shared_core.log.Log;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;

/**
 * Helper to give notifications for the listeners of the editor.
 * 
 * @author Fabio
 */
public class EditNotifier {

    private WeakReference<BaseEditor> fEdit;

    public static interface INotifierRunnable {
        public void run(IProgressMonitor monitor);
    }

    public EditNotifier(BaseEditor edit) {
        this.fEdit = new WeakReference<BaseEditor>(edit);
    }

    /**
     * Notifies listeners that the actions have just been created in the editor.
     */
    public void notifyOnCreateActions(final ListResourceBundle resources) {
        final BaseEditor edit = fEdit.get();
        if (edit == null) {
            return;
        }
        INotifierRunnable runnable = new INotifierRunnable() {
            @Override
            public void run(final IProgressMonitor monitor) {
                for (IEditListener listener : edit.getAllListeners()) {
                    try {
                        if (!monitor.isCanceled()) {
                            listener.onCreateActions(resources, edit, monitor);
                        }
                    } catch (Exception e) {
                        //must not fail
                        Log.log(e);
                    }
                }
            }
        };
        runIt(runnable);
    }

    /**
     * Notifies listeners that the editor has just been saved
     */
    public void notifyOnSave() {
        final BaseEditor edit = fEdit.get();
        if (edit == null) {
            return;
        }
        INotifierRunnable runnable = new INotifierRunnable() {
            @Override
            public void run(IProgressMonitor monitor) {
                for (IEditListener listener : edit.getAllListeners()) {
                    try {
                        if (!monitor.isCanceled()) {
                            listener.onSave(edit, monitor);
                        }
                    } catch (Throwable e) {
                        //must not fail
                        Log.log(e);
                    }
                }
            }
        };
        runIt(runnable);

    }

    /**
     * Helper function to run the notifications of the editor in a job.
     * 
     * @param runnable the runnable to be run.
     */
    private void runIt(final INotifierRunnable runnable) {
        Job job = new Job("EditNotifier") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                runnable.run(monitor);
                return Status.OK_STATUS;
            }

        };
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
    }

    /**
     * Notifies listeners that the editor has just been disposed
     */
    public void notifyOnDispose() {
        final BaseEditor edit = fEdit.get();
        if (edit == null) {
            return;
        }

        INotifierRunnable runnable = new INotifierRunnable() {
            @Override
            public void run(IProgressMonitor monitor) {
                for (IEditListener listener : edit.getAllListeners(false)) {
                    try {
                        if (!monitor.isCanceled()) {
                            listener.onDispose(edit, monitor);
                        }
                    } catch (Throwable e) {
                        //no need to worry... as we're disposing, in shutdown, we may not have access to some classes anymore
                    }
                }
            }
        };
        runIt(runnable);
    }

    /**
     * @param document the document just set
     */
    public void notifyOnSetDocument(final IDocument document) {
        final BaseEditor edit = fEdit.get();
        if (edit == null) {
            return;
        }
        INotifierRunnable runnable = new INotifierRunnable() {
            @Override
            public void run(IProgressMonitor monitor) {
                for (IEditListener listener : edit.getAllListeners()) {
                    try {
                        if (!monitor.isCanceled()) {
                            listener.onSetDocument(document, edit, monitor);
                        }
                    } catch (Exception e) {
                        //must not fail
                        Log.log(e);
                    }
                }
            }
        };
        runIt(runnable);
    }

    /**
     * Notifies the available listeners that the input has changed for the editor.
     * 
     * @param oldInput the old input of the editor
     * @param input the new input of the editor
     */
    public void notifyInputChanged(final IEditorInput oldInput, final IEditorInput input) {
        final BaseEditor edit = fEdit.get();
        if (edit == null) {
            return;
        }
        INotifierRunnable runnable = new INotifierRunnable() {
            @Override
            public void run(IProgressMonitor monitor) {
                for (IEditListener listener : edit.getAllListeners()) {
                    if (listener instanceof IEditListener3) {
                        IEditListener3 pyEditListener3 = (IEditListener3) listener;
                        try {
                            if (!monitor.isCanceled()) {
                                pyEditListener3.onInputChanged(edit, oldInput, input, monitor);
                            }
                        } catch (Exception e) {
                            //must not fail
                            Log.log(e);
                        }
                    }
                }
            }
        };
        runIt(runnable);
    }

    public void notifyEditorCreated() {
        //Note that it's not done on a Job as in the other cases!
        final BaseEditor edit = fEdit.get();
        if (edit == null) {
            return;
        }
        for (IEditListener listener : edit.getAllListeners(false)) {
            if (listener instanceof IEditListener4) {
                IEditListener4 pyEditListener4 = (IEditListener4) listener;
                try {
                    pyEditListener4.onEditorCreated(edit);
                } catch (Exception e) {
                    //must not fail
                    Log.log(e);
                }
            }
        }
    }

}
