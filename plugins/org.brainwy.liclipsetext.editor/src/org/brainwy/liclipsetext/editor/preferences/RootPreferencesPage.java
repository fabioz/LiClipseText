/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.preferences;

import java.io.File;
import java.util.Collections;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.languages.CreateNewLanguageHelper;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.dialogs.DialogHelpers;
import org.brainwy.liclipsetext.shared_ui.field_editors.LinkFieldEditor;
import org.brainwy.liclipsetext.shared_ui.utils.UIUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.handlers.ShowInSystemExplorerHandler;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public class RootPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private final class SelectPageSelectionAdapter extends SelectionAdapter {
        private String id;

        public SelectPageSelectionAdapter(String id) {
            this.id = id;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            IWorkbenchPreferenceContainer workbenchPreferenceContainer = ((IWorkbenchPreferenceContainer) getContainer());
            workbenchPreferenceContainer.openPage(id, null);
        }
    }

    private void createLink(Composite composite, String msg, String id) {
        LinkFieldEditor link = new LinkFieldEditor("UNUSED", msg, composite, new SelectPageSelectionAdapter(id));
        link.getLinkControl(composite);
    }

    public RootPreferencesPage() {
        //        setDescription("LiClipse");
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);

        createLink(composite,
                "\n<a>Color theme</a>: Change the theme for liclipse editors, all editors or the whole IDE.",
                "com.github.eclipsecolortheme.preferences.ColorThemePreferencePage");

        createLink(
                composite,
                "\n<a>License</a>: If you have a license, please add it to mark your version of LiClipse as registered.",
                "liclipse.license");

        Label label = new Label(composite, SWT.NONE);
        label.setText("\nDirectory containing language definiton:\n");

        final Composite dirs = new Composite(composite, SWT.NONE);

        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
        File[] languagesDirectories = languagesManager.getLanguagesDirectories();
        for (final File dir : languagesDirectories) {
            createNewLink(dirs, dir);
        }

        LinkFieldEditor link = new LinkFieldEditor("UNUSED",
                "\n<a>Track new directory...\n</a>", composite,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent()
                                .getActiveShell());
                        String selected = dialog.open();
                        if (selected != null) {
                            LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                            File dir = new File(selected);
                            if (languagesManager.addDirectoryToBeTracked(dir)) {
                                createNewLink(dirs, dir);
                            }
                        }
                    }
                });
        link.getLinkControl(composite);

        return parent;
    }

    private void createNewLink(Composite dirs, final File dir) {
        LinkFieldEditor link = new LinkFieldEditor("UNUSED",
                "<a>" + dir.toString() + "</a>", dirs,
                new SelectionAdapter() {
                    @SuppressWarnings("restriction")
					@Override
                    public void widgetSelected(SelectionEvent e) {
						try {
							ECommandService commandService = (ECommandService) PlatformUI.getWorkbench().getService(ECommandService.class);
							EHandlerService handlerService = (EHandlerService) PlatformUI.getWorkbench().getService(EHandlerService.class);

							Command command = commandService.getCommand(ShowInSystemExplorerHandler.ID);
							if (command.isDefined()) {
								ParameterizedCommand parameterizedCommand = commandService
										.createCommand(ShowInSystemExplorerHandler.ID, Collections.singletonMap(
												ShowInSystemExplorerHandler.RESOURCE_PATH_PARAMETER, dir.toString()));
								if (handlerService.canExecute(parameterizedCommand)) {
									handlerService.executeHandler(parameterizedCommand);
								}
							}
						} catch (Throwable e1) {
							Log.log(e1);
						}

                    }
                });
        Link linkControl = link.getLinkControl(dirs);
        updateLinkControlActions(linkControl, dir);
        try {
            dirs.pack(true);
            dirs.getParent().pack(true);
            dirs.getParent().getParent().pack(true);
        } catch (Exception e1) {
            Log.log(e1);
        }
    }

    private void updateLinkControlActions(final Link linkControl, final File dir) {
        MenuDetectListener listener = new MenuDetectListener() {

            @Override
            public void menuDetected(MenuDetectEvent event) {
                Display display = Display.getCurrent();

                Menu menu = new Menu(UIUtils.getActiveShell(), SWT.POP_UP);
                MenuItem item = new MenuItem(menu, SWT.PUSH);
                item.setText("Remove directory (" + dir + ")");
                item.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event e) {
                        LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                        if (languagesManager.stopTrackingDirectory(dir)) {
                            Composite parent = linkControl.getParent();
                            linkControl.dispose();
                            parent.pack(true);
                        } else {
                            DialogHelpers.openWarning("Unable to remove",
                                    "Could not remove directory (builtin directories are always tracked)");
                        }
                    }
                });

                item = new MenuItem(menu, SWT.PUSH);
                item.setText("Create new language at directory");
                item.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event e) {
                        CreateNewLanguageHelper.createNewLanguage(dir);
                    }
                });
                menu.setLocation(event.x, event.y);
                menu.setVisible(true);
                while (!menu.isDisposed() && menu.isVisible()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
                menu.dispose();

            }
        };
        linkControl.addMenuDetectListener(listener);
        linkControl.setToolTipText("Use context menu to create language on dir / remove directory from tracked dirs.");
    }

    public void init(IWorkbench workbench) {
    }
}
