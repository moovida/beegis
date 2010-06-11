package eu.hydrologis.jgrass.database;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.database.core.IDatabaseConnection;

/**
 * The activator class controls the plug-in life cycle
 */
public class DatabasePlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.database";

    // The shared instance
    private static DatabasePlugin plugin;

    private IDatabaseConnection activeDatabaseConnection;

    /**
     * The constructor
     */
    public DatabasePlugin() {
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
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static DatabasePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the active database connection. 
     * 
     * <p>
     * If no database connection is active, this triggers the create database connection wizard.
     * If the preferences have no database connection info to connect to, create a default one.
     * </p> 
     * <p>
     * <b>This assures to return a database connection or else to throw an Exception.</b>
     * </p>
     * 
     * @return the {@link IDatabaseConnection database connection}.
     */
    public synchronized IDatabaseConnection getActiveDatabaseConnection() throws Exception {
        if (activeDatabaseConnection == null) {
            // TODO setup a database connection
        }

        if (activeDatabaseConnection == null) {
            throw new RuntimeException("Unable to create a database connection");
        }
        return activeDatabaseConnection;
    }

    /**
     * 
     * 
     * @throws Exception
     */
    public void disconnectActiveDatabaseConnection() throws Exception {
        activeDatabaseConnection.closeSessionFactory();
    }

    public void setActiveDatabaseConnection( IDatabaseConnection databaseConnection ) throws Exception {
        if (activeDatabaseConnection != null) {
            disconnectActiveDatabaseConnection();
        }
        activeDatabaseConnection = databaseConnection;
    }

}
