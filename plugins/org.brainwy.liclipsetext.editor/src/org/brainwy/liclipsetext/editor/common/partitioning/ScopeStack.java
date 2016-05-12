/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

import org.brainwy.liclipsetext.shared_core.structure.FastStack;

public class ScopeStack extends FastStack<String> {

    public ScopeStack(int initialCapacity) {
        super(initialCapacity);
    }

}
