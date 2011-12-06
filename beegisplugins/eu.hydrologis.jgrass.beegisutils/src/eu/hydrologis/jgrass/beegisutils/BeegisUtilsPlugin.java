/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.beegisutils;

import java.io.File;

import net.refractions.udig.core.AbstractUdigUIPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.beegisutils.utils.ImageCache;

/**
 * The activator class controls the plug-in life cycle
 */
public class BeegisUtilsPlugin extends AbstractUdigUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.beegisutils";

    static final String ICONS_PATH = "icons/"; //$NON-NLS-1$

    /**
     * Global formatter for joda datetime (yyyy-MM-dd HH:mm:ss).
     */
    public static DateTimeFormatter dateTimeFormatterYYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

    /**
     * Global formatter for joda datetime (yyyy-MM-dd HH:mm).
     */
    public static DateTimeFormatter dateTimeFormatterYYYYMMDDHHMM = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"); //$NON-NLS-1$

    // The shared instance
    private static BeegisUtilsPlugin plugin;

    /**
     * The constructor
     */
    public BeegisUtilsPlugin() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        ImageCache.getInstance().dispose();
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static BeegisUtilsPlugin getDefault() {
        return plugin;
    }

    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR if:
     * <ul>
     * <li>t is an Exception we are assuming it is human readable or if a message is provided
     */
    public static void log( String message2, Throwable t ) {
        if (getDefault() == null) {
            t.printStackTrace();
            return;
        }
        String message = message2;
        if (message == null)
            message = ""; //$NON-NLS-1$
        int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        getDefault().getLog().log(new Status(status, PLUGIN_ID, IStatus.OK, message, t));
    }

    public static final String BEEGIS_LAST_CHOSEN_FOLDER = "beegis_last_chosen_folder";
    /**
     * Utility method for file dialogs to retrieve the last folder.
     * 
     * @return the path to the last folder chosen or the home folder.
     */
    public String getLastFolderChosen() {
        IPreferenceStore store = getPreferenceStore();
        String lastFolder = store.getString(BEEGIS_LAST_CHOSEN_FOLDER);

        if (lastFolder != null) {
            File f = new File(lastFolder);
            if (f.exists() && f.isDirectory()) {
                return lastFolder;
            }
            if (f.exists() && f.isFile()) {
                return f.getParent();
            }
        }

        return new File(System.getProperty("user.home")).getAbsolutePath();
    }

    /**
     * Utility method for file dialogs to set the last folder.
     * 
     * @param folderPath the folder path. If the path is a file path, the parent folder is saved.
     */
    public void setLastFolderChosen( String folderPath ) {
        File fiel = new File(folderPath);
        if (!fiel.isDirectory()) {
            folderPath = fiel.getParent();
        }
        IPreferenceStore store = getPreferenceStore();
        store.putValue(BEEGIS_LAST_CHOSEN_FOLDER, folderPath);
    }

    public IPath getIconPath() {
        return new Path(ICONS_PATH);
    }

}
