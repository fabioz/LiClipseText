/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.handlers.extensions;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IEditorInput;

public interface ILiClipseErrorCollector extends ILiClipseHandler {

    List<Map<String, Object>> collectErrors(IEditorInput iEditorInput, File file, String docContents) throws Exception;

}
