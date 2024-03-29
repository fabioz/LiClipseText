/**
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.shared_core.resource_stubs;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;

public class AbstractIFileStub extends AbstractIResourceStub implements IFile {

    @Override
    public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor)
            throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void create(InputStream source, boolean force, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void create(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void createLink(URI location, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getCharset() throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getCharset(boolean checkImplicit) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getCharsetFor(Reader reader) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public IContentDescription getContentDescription() throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public InputStream getContents() throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public InputStream getContents(boolean force) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getEncoding() throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException {
        return new IFileState[0];
    }

    @Override
    public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor)
            throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setCharset(String newCharset) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setCharset(String newCharset, IProgressMonitor monitor) throws CoreException {
        //no-op
    }

    @Override
    public void setContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor)
            throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor)
            throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getType() {
        return IResource.FILE;
    }
}
