/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.BaseLiClipseEditor;
import org.brainwy.liclipsetext.editor.common.LiClipseDocumentProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseDocumentPartitioner;
import org.brainwy.liclipsetext.editor.languages.CreateNewLanguageHelper;
import org.brainwy.liclipsetext.editor.languages.LanguageMetadata;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.languages.LiClipseLanguage;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_ui.SharedUiPlugin;
import org.brainwy.liclipsetext.shared_ui.UIConstants;
import org.brainwy.liclipsetext.shared_ui.dialogs.DialogHelpers;
import org.brainwy.liclipsetext.shared_ui.dialogs.SelectElementDialog;

public class LanguagesMenu extends CompoundContributionItem {

    @Override
    protected IContributionItem[] getContributionItems() {
        ContributionItem contributionItem = new ContributionItem() {
            @Override
            public void fill(Menu menu, int index) {
                IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                IWorkbenchPage activePage = workbenchWindow.getActivePage();
                IEditorPart editorPart = activePage.getActiveEditor();
                if (editorPart instanceof BaseLiClipseEditor) {
                    final BaseLiClipseEditor liClipseEditor = (BaseLiClipseEditor) editorPart;

                    final IDocument document = liClipseEditor.getDocument();
                    if (document == null) {
                        return;
                    }
                    IDocumentPartitioner documentPartitioner = document.getDocumentPartitioner();
                    if (documentPartitioner == null) {
                        return;
                    }

                    if (!(documentPartitioner instanceof LiClipseDocumentPartitioner)) {
                        return;
                    }
                    LiClipseDocumentPartitioner liClipseDocumentPartitioner = (LiClipseDocumentPartitioner) documentPartitioner;
                    LanguageMetadata currentLanguageMetadata = liClipseDocumentPartitioner.getLanguageMetadata();

                    LanguagesManager languagesManager = LiClipseTextEditorPlugin.getLanguagesManager();
                    List<LanguageMetadata> languagesMetadata = languagesManager.getLanguagesMetadata();
                    Collections.sort(languagesMetadata);
                    for (final LanguageMetadata languageMetadata : languagesMetadata) {
                        MenuItem root = new MenuItem(menu, SWT.RADIO, index++);
                        root.setSelection(languageMetadata.file.equals(currentLanguageMetadata.file));
                        String languageCaption = languageMetadata.languageCaption;
                        if (languageCaption == null) {
                            languageCaption = languageMetadata.languageName;
                        } else {
                            if (!languageCaption.equals(languageMetadata.languageName)) {
                                languageCaption += " (" + languageMetadata.languageName + ")";
                            }
                        }
                        root.setText(languageCaption);

                        root.addSelectionListener(new SelectionAdapter() {
                            @Override
                            public void widgetSelected(SelectionEvent e) {
                                boolean selected = ((MenuItem) e.widget).getSelection();
                                if (selected) {
                                    //Will reopen the editor
                                    IEditorInput editorInput = liClipseEditor.getEditorInput();
                                    IWorkbenchPage page = liClipseEditor.getSite().getPage();
                                    page.closeEditor(liClipseEditor, true);

                                    LiClipseLanguage language = LiClipseTextEditorPlugin.getLanguagesManager()
                                            .getLanguageFromMetadata(languageMetadata);
                                    try {
                                        LiClipseDocumentProvider.pushForceLanguageOnce(editorInput, language);
                                        page.openEditor(editorInput, language.editorId);
                                    } catch (PartInitException e1) {
                                        Log.log(e1);
                                    }
                                }
                            }
                        });
                    }

                    new MenuItem(menu, SWT.SEPARATOR, index++);

                    MenuItem editLanguage = new MenuItem(menu, SWT.PUSH, index++);
                    editLanguage.setSelection(false);
                    editLanguage.setText("Edit current language definition");
                    editLanguage.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            try {
                                LiClipseLanguage language = liClipseEditor.getLanguage();
                                language.file.openEditor();
                            } catch (Exception e1) {
                                Log.log(e1);
                            }
                        };
                    });

                    MenuItem root = new MenuItem(menu, SWT.PUSH, index++);
                    root.setSelection(false);
                    root.setText("Create new language");
                    root.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            try {
                                LanguagesManager manager = LiClipseTextEditorPlugin.getLanguagesManager();
                                File[] languagesDirectories = manager.getLanguagesDirectories();
                                List<String> lst = new ArrayList<String>(languagesDirectories.length + 1);
                                for (File f : languagesDirectories) {
                                    if (f.isDirectory()) {
                                        lst.add(f.getAbsolutePath());
                                    }
                                }
                                final String createInNewDir = "Create in new directory...";
                                lst.add(createInNewDir);

                                String selected = SelectElementDialog.selectOne(
                                        lst,
                                        new LabelProvider() {
                                    @Override
                                    public org.eclipse.swt.graphics.Image getImage(Object element) {
                                        if (createInNewDir.equals(element)) {
                                            return SharedUiPlugin.getImageCache().get(
                                                    UIConstants.ASSIST_NEW_GENERIC);
                                        }
                                        return SharedUiPlugin.getImageCache().get(UIConstants.FOLDER_ICON);
                                    };
                                },
                                        "Choose directory to create language file\n"
                                                + "(if new dir, it'll be added in preferences to\n"
                                                + "be scanned for language files).");

                                if (selected != null && selected.length() > 0) {
                                    if (createInNewDir.equals(selected)) {
                                        DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent()
                                                .getActiveShell());
                                        selected = dialog.open();
                                    }
                                    if (selected != null) {
                                        File dir = new File(selected);
                                        if (!dir.exists()) {
                                            DialogHelpers.openCritical("Directory does not exist",
                                                    "Unable to create language file at dir:\n" + dir
                                                            + "\nbecause it does not exist.");
                                            return;
                                        }
                                        manager.addDirectoryToBeTracked(dir);

                                        CreateNewLanguageHelper.createNewLanguage(dir);
                                    }
                                }
                            } catch (Exception e1) {
                                Log.log(e1);
                            }
                        };
                    });
                }
            }
        };
        return new IContributionItem[] { contributionItem };
    }

}
