/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.brainwy.liclipsetext.editor.common.partitioning.IColorCache;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCacheProvider;
import org.brainwy.liclipsetext.editor.common.partitioning.LiClipseColorCache;
import org.brainwy.liclipsetext.editor.languages.LanguagesManager;
import org.brainwy.liclipsetext.editor.preferences.LiClipseTextPreferences;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.utils.BaseExtensionHelper;
import org.brainwy.liclipsetext.shared_core.utils.PlatformUtils;
import org.brainwy.liclipsetext.shared_ui.ImageCache;
import org.brainwy.liclipsetext.shared_ui.bundle.BundleInfo;
import org.brainwy.liclipsetext.shared_ui.bundle.IBundleInfo;
import org.brainwy.liclipsetext.shared_ui.utils.UIUtils;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LiClipseTextEditorPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.brainwy.liclipsetext.editor"; //$NON-NLS-1$

    // The shared instance
    private static LiClipseTextEditorPlugin plugin;



    // ----------------- SINGLETON THINGS -----------------------------
    public static IBundleInfo info;

    public static IBundleInfo getBundleInfo() {
        if (LiClipseTextEditorPlugin.info == null) {
            LiClipseTextEditorPlugin.info = new BundleInfo(LiClipseTextEditorPlugin.getDefault().getBundle());
        }
        return LiClipseTextEditorPlugin.info;
    }

    public static void setBundleInfo(IBundleInfo b) {
        LiClipseTextEditorPlugin.info = b;
    }

    /**
     * The constructor
     */
    public LiClipseTextEditorPlugin() {
    }

    private static LanguagesManager languagesManager;

    private static final Object lock = new Object();

    private File[] languagesDir;

    public static boolean PLUGIN_STARTED = false;

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        PLUGIN_STARTED = true;
        startPlugin();
    }

    public void startPlugin(File... languagesDir) {
        plugin = this;
        this.languagesDir = languagesDir;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        stopPlugin();
        super.stop(context);
    }

    public void stopPlugin() {
        if (listener != null) {
            try {
                IPreferenceStore prefs = LiClipseTextPreferences.getChainedPreferenceStore();
                prefs.removePropertyChangeListener(listener);
                listener = null;
            } catch (Exception e) {
                Log.log(e);
            }
        }
        if (colorManager != null) {
            colorManager.dispose();
            colorManager = null;
        }
        plugin = null;
        if (languagesManager != null) {
            languagesManager.dispose();
        }
        languagesManager = null;
        PLUGIN_STARTED = false;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static LiClipseTextEditorPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    private ImageCache imageCache;

    public ImageCache getImageCache() {
        if (imageCache == null) {
            imageCache = new ImageCache(getBundle().getEntry("/icons/"));
        }
        return imageCache;
    }

    public static Image getIcon(String icon) {
        LiClipseTextEditorPlugin plugin = getDefault();
        ImageCache imageCache = plugin.getImageCache();
        return imageCache.get(icon);
    }

    public static File getFile(IPath path) {
        try {
            Bundle bundle = getDefault().getBundle();
            URL url = FileLocator.find(bundle, path, null);
            return new File(FileLocator.toFileURL(url).getPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to get path: " + path);
        }
    }

    public static void setLanguagesManager(LanguagesManager languagesManager) {
        LiClipseTextEditorPlugin.languagesManager = languagesManager;
    }

    /**
     * @return the languages manager. Could be null if the plugin is already stopped.
     */
    public static LanguagesManager getLanguagesManager() {
        if (languagesManager == null) {
            synchronized (lock) {
                if (plugin == null) {
                    return null;
                }

                //initialize lazily
                if (languagesManager == null) {
                    languagesManager = new LanguagesManager(plugin.languagesDir);
                }
            }
        }
        return languagesManager;
    }

    private IPropertyChangeListener listener;
    private IColorCache colorManager;

    public IColorCache getColorManager() {
        if (colorManager == null) {
            IPreferenceStore prefs = LiClipseTextPreferences.getChainedPreferenceStore();
            try {
            	IColorCacheProvider provider = (IColorCacheProvider) BaseExtensionHelper.getParticipant(
            			"org.brainwy.liclipsetext.editor.liclipse_color_cache_provider", false);
            	if(provider != null){
            		colorManager = provider.createColorCache(prefs);
            	}
			} catch (Exception e) {
				Log.log(e);
			}
            if(colorManager == null){
            	colorManager = new LiClipseColorCache(prefs);
            }
            listener = createColorUpdatePrefChangeListener(colorManager);
            prefs.addPropertyChangeListener(listener);
        }
        return colorManager;
    }

    /**
     * Create a change listener that notifies the color manager to check for a color reload.
     */
    private static IPropertyChangeListener createColorUpdatePrefChangeListener(final IColorCache colorManager) {
        return new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                try {
                    String property = event.getProperty();
                    colorManager.checkReloadProperty(property);
                } catch (Exception e) {
                    Log.log(e);
                }
            }
        };
    }

    private static IStatus createWarning(String message) {
        return new Status(IStatus.WARNING, PLUGIN_ID, message);
    }

    public static void createWarning(String message, List<IStatus> errorList) {
        errorList.add(createWarning(message));
    }

    private static IStatus createError(String message) {
        return new Status(IStatus.ERROR, PLUGIN_ID, message);
    }

    public static void createError(String message, List<IStatus> errorList) {
        errorList.add(createError(message));
    }

    @SuppressWarnings("restriction")
    public static void setCssId(Object control, String id, boolean applyToChildren) {
        try {
            IStylingEngine engine = UIUtils.getActiveWorkbenchWindow()
                    .getService(IStylingEngine.class);
            if (engine != null) {
                engine.setId(control, id);
                IThemeEngine themeEngine = (IThemeEngine) Display.getDefault().getData(
                        "org.eclipse.e4.ui.css.swt.theme");
                themeEngine.applyStyles(control, applyToChildren);
            }
        } catch (Throwable e) {
            //Ignore: older versions of Eclipse won't have it!
            // e.printStackTrace();
        }
    }

    public static String getCtagsExecutable() {
        if (PlatformUtils.isWindowsPlatform()) {
            if (plugin == null) {
                URL url = LiClipseTextEditorPlugin.class.getClassLoader()
                        .getResource("org/brainwy/liclipsetext/editor/LiClipseTextEditorPlugin.class");
                File path = new File(url.getPath());
                String fullPath = path.toString();
                int i = fullPath.lastIndexOf("org.brainwy.liclipsetext.editor");
                IPath p = Path.fromOSString(fullPath.substring(0, i));
                IPath executable = p.append("org.brainwy.liclipsetext.editor").append("libs").append("ctags.exe");
                File file = executable.toFile();
                if (!file.exists()) {
                    throw new AssertionFailedException("Expecting file: " + file + " to exist.");
                }
                return file.getAbsolutePath();
            }

            IPath relative = new Path("libs").addTrailingSeparator().append("ctags.exe");
            try {
                return getBundleInfo().getRelativePath(relative).getAbsolutePath();
            } catch (CoreException e) {
                Log.log(e);
                throw new RuntimeException(e);
            }
        } else {
            return "ctags"; //mac, linux, etc.
        }
    }

    public static File getLiClipseUserDir() {
        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            try {
                File f = new File(userHome);
                if (f.isDirectory()) {
                    f = new File(f, ".liclipse");
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    return f;
                }
            } catch (Throwable e) {
                Log.log(e);
            }
        }
        return null;
    }
}
