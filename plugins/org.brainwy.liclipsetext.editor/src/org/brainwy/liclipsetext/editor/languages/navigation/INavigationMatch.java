/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.navigation;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public interface INavigationMatch {

    IRegion find(boolean forward, IDocument document, int offset);

    /**
     * Should only be called if {@link INavigationMatch2} is not available.
     */
    List<IRegion> findAll(IDocument document);

    /**
     * Should only be called if {@link INavigationMatch2} is not available.
     */
    String getIcon();

}
