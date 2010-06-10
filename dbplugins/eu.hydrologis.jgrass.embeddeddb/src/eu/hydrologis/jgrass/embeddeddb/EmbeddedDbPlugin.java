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
package eu.hydrologis.jgrass.embeddeddb;

import java.io.File;
import java.net.URL;

import net.refractions.udig.catalog.URLUtils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.database.interfaces.IDatabaseFactoryProvider;
import eu.hydrologis.jgrass.database.interfaces.IDbPlugin;
import eu.hydrologis.jgrass.embeddeddb.preferences.DbParams;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class EmbeddedDbPlugin extends AbstractUIPlugin implements IDatabaseFactoryProvider {

    public static final String H2 = "H2 database"; //$NON-NLS-1$

    public static final String LOGHIBERNATESQL = "true"; //$NON-NLS-1$

    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.embeddeddb"; //$NON-NLS-1$

    private static EmbeddedDbPlugin plugin;

    private IDbPlugin dbPlugin = null;

    public EmbeddedDbPlugin() {
    }

    @SuppressWarnings({"nls"})
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;

        ScopedPreferenceStore store = (ScopedPreferenceStore) plugin.getPreferenceStore();
        final String dbType = store.getString(DbParams.DATABASETYPE);

        /*
         * it is not required that the plugins are there, so try to load it through reflection in
         * order to not create dependencies on non existing classes
         */
        if (dbType.equals(H2)) {
            dbPlugin = H2Plugin.getDefault();
        }

        if (dbPlugin == null) {
            Display.getDefault().asyncExec(new Runnable(){
                public void run() {
                    Shell shell = new Shell(Display.getDefault());
                    MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
                    msgBox
                            .setMessage(dbType
                                    + ": The database plugin for the choosen database could not be loaded. Probably the plugin is not installed. The database can't be used! Try to check your settings and restart the application.");
                    msgBox.open();
                }
            });
        }

        int port = store.getInt(DbParams.PORT);
        String user = store.getString(DbParams.USER);
        String passwd = store.getString(DbParams.PASSWD);
        String dbRoot = store.getString(DbParams.FOLDER);

        /*
         * if a different path was supplied, this will force the db
         */
        String tmpFilePath = System.getProperty("jgrass.db.dir"); //$NON-NLS-1$
        if (tmpFilePath != null) {
            if (tmpFilePath.startsWith("IP:")) { //$NON-NLS-1$
                // it is a relative path inside the install folder
                URL pluginInternalURL = Platform.getInstallLocation().getURL();
                File urlToFile = URLUtils.urlToFile(pluginInternalURL);
                tmpFilePath = urlToFile.getAbsolutePath() + File.separator
                        + tmpFilePath.split("IP:")[1]; //$NON-NLS-1$
            } else {

                // it is a absolute path
                store.setValue(DbParams.FOLDER, tmpFilePath);
                dbRoot = tmpFilePath;
            }
        }

        dbPlugin.setConnectionParams(user, passwd, dbRoot, port, null);
        
        // TODO this put here triggers the sessionfactory even if unused,
        //      check if there is a reason.
        // dbPlugin.getSessionFactory();
    }

    public void stop( BundleContext context ) throws Exception {
        dbPlugin.closeSessionFactory();
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static EmbeddedDbPlugin getDefault() {
        return plugin;
    }

    /* (non-Javadoc)
     * @see eu.hydrologis.jgrass.embeddeddb.DatabaseFactoryProvider#getSessionFactory()
     */
    public SessionFactory getSessionFactory() throws Exception {
        return dbPlugin.getSessionFactory();
    }

    /* (non-Javadoc)
     * @see eu.hydrologis.jgrass.embeddeddb.DatabaseFactoryProvider#closeSessionFactory()
     */
    public void closeSessionFactory() throws Exception {
        dbPlugin.closeSessionFactory();
    }

    public AnnotationConfiguration getConfiguration() throws Exception {
        return dbPlugin.getConfiguration();
    }

    /**
     * Creates or updates the database schema from the hibernate mapped classes.
     * 
     * @param doUpdate if true, and update occurs. Instead if false is used, 
     *                  tables are dropped and recreated. 
     * @throws Exception 
     */
    public void createSchema( boolean doUpdate ) throws Exception {
        dbPlugin.createSchema(doUpdate);
    }

    public String getDbDriverString() {
        return dbPlugin.getDbDriverString();
    }

    public String getDbName() {
        return dbPlugin.getDbName();
    }

    public void backUpTo( String filePath ) throws Exception {
        dbPlugin.backUpTo(filePath);
    }

    public void restoreFrom( String filePath ) throws Exception {
        dbPlugin.restoreFrom(filePath);
    }

    public boolean checkTables( String... tables ) throws Exception {
        return dbPlugin.checkTables(tables);
    }

    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR iff:
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

}
