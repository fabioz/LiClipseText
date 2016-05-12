/**
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
/*
 * Created on Apr 30, 2006
 */
package org.brainwy.liclipsetext.shared_ui.editor;

import org.brainwy.liclipsetext.shared_core.string.TextSelectionUtils;

/**
 * Used if the interface also wants to be notified of when the cursor position is changed.
 * 
 * This is an extension to the IEditListener
 */
public interface IEditListener2 {

    /**
     * Called when the cursor position changes.
     * 
     * Note: the listeners of this method should be very efficient, as in any change, it will be called.
     * 
     * @param edit the editor that had its cursor position changed.
     * @param ps the new selection (after the cursor changed its position)
     */
    void handleCursorPositionChanged(BaseEditor edit, TextSelectionUtils ps);
}
