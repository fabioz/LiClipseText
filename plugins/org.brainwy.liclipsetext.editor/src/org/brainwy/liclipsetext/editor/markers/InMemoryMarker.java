/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class InMemoryMarker extends PlatformObject implements IMarker {

    private Map<String, Object> attributes;
    private long creationTime;
    private long id;

    private static long next = 0;
    private static final Object lock = new Object();

    public InMemoryMarker(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.creationTime = System.currentTimeMillis();
        synchronized (lock) {
            next++;
            this.id = next;
        }
    }

    public void delete() throws CoreException {
        //Do nothing at this point
    }

    public boolean exists() {
        return true;
    }

    public Object getAttribute(String attributeName) throws CoreException {
        return attributes.get(attributeName);
    }

    public int getAttribute(String attributeName, int defaultValue) {
        Object object = attributes.get(attributeName);
        if (object instanceof Integer) {
            return ((Integer) object).intValue();
        }
        return defaultValue;
    }

    public String getAttribute(String attributeName, String defaultValue) {
        Object object = attributes.get(attributeName);
        if (object instanceof String) {
            return (String) object;
        }
        return defaultValue;
    }

    public boolean getAttribute(String attributeName, boolean defaultValue) {
        Object object = attributes.get(attributeName);
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        return defaultValue;
    }

    public Map<String, Object> getAttributes() throws CoreException {
        return new HashMap<String, Object>(attributes);
    }

    public Object[] getAttributes(String[] attributeNames) throws CoreException {
        return this.attributes.values().toArray(new Object[this.attributes.size()]);
    }

    public long getCreationTime() throws CoreException {

        return this.creationTime;
    }

    public long getId() {

        return this.id;
    }

    public IResource getResource() {
        return getWorkspace().getRoot();
    }

    public String getType() throws CoreException {
        return IMarker.PROBLEM;
    }

    public boolean isSubtypeOf(String superType) throws CoreException {
        return getWorkspace().getMarkerManager().isSubtype(getType(), superType);
    }

    private Workspace getWorkspace() {
        return (Workspace) ResourcesPlugin.getWorkspace();
    }

    public void setAttribute(String attributeName, int value) throws CoreException {
        this.attributes.put(attributeName, value);
    }

    public void setAttribute(String attributeName, Object value) throws CoreException {
        this.attributes.put(attributeName, value);

    }

    public void setAttribute(String attributeName, boolean value) throws CoreException {
        this.attributes.put(attributeName, value);

    }

    public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {
        this.attributes.clear();
        for (int i = 0; i < attributeNames.length; i++) {
            this.attributes.put(attributeNames[i], values[i]);
        }
    }

    public void setAttributes(Map<String, ? extends Object> attributes) throws CoreException {
        this.attributes.clear();
        this.attributes.putAll(attributes);

    }

}
