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

    public IDatabaseConnection getActiveDatabaseConnection() {
        return activeDatabaseConnection;
    }

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
