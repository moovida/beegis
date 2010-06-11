package eu.hydrologis.jgrass.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
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

    private List<DatabaseConnectionProperties> availableDatabaseConnectionProperties;

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
            loadSavedDatabaseConnections();
        }
        if (activeDatabaseConnection == null) {
            // TODO create a default one
        }
        return activeDatabaseConnection;
    }
    /**
     * Disconnects the active database connection.
     * 
     * @return true if the database was disconnected properly.
     */
    public boolean disconnectActiveDatabaseConnection() {
        try {
            activeDatabaseConnection.closeSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setActiveDatabaseConnection( IDatabaseConnection databaseConnection ) throws Exception {
        if (activeDatabaseConnection != null) {
            disconnectActiveDatabaseConnection();
        }
        activeDatabaseConnection = databaseConnection;

    }
    
    private IDatabaseConnection activateDatabaseConnection( DatabaseConnectionProperties newCP ) {
        // TODO Auto-generated method stub
        return null;
    }
    

    public void loadSavedDatabaseConnections() throws IOException, Exception {
        FileReader reader = null;
        try {
            reader = new FileReader(getConfigurationsFile());
            loadSavedDatabaseConnections(XMLMemento.createReadRoot(reader));
        } catch (FileNotFoundException e) {
            // ignore, file does not exist yet
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void loadSavedDatabaseConnections( XMLMemento memento ) {
        String[] possibleTags = DatabaseConnectionProperties.POSSIBLETAGS;
        IMemento[] children = memento.getChildren(DatabaseConnectionProperties.DATABASES_XML);
        availableDatabaseConnectionProperties = new ArrayList<DatabaseConnectionProperties>();
        for( int i = 0; i < children.length; i++ ) {
            DatabaseConnectionProperties newCP = new DatabaseConnectionProperties();
            for( String tag : possibleTags ) {
                String value = children[i].getString(tag);
                if (value != null && value.length() > 0) {
                    newCP.put(tag, value);
                    availableDatabaseConnectionProperties.add(newCP);
                    if (tag.equals(DatabaseConnectionProperties.ISACTIVE)) {
                        if (activeDatabaseConnection != null) {
                            throw new RuntimeException("No connection should be active at this time.");
                        }
                        activeDatabaseConnection = activateDatabaseConnection(newCP);
                    }
                }
            }
        }
    }

    public void savedDatabaseConnections() throws IOException {
        XMLMemento memento = XMLMemento.createWriteRoot(DatabaseConnectionProperties.DATABASES_XML);
        savedDatabaseConnections(memento);

        FileWriter writer = null;
        try {
            writer = new FileWriter(getConfigurationsFile());
            memento.save(writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    private void savedDatabaseConnections( XMLMemento memento ) {
        for( DatabaseConnectionProperties dcP : availableDatabaseConnectionProperties ) {
            IMemento child = memento.createChild(DatabaseConnectionProperties.DATABASE_XML);
            Set<Entry<Object, Object>> entries = dcP.entrySet();
            for( Entry<Object, Object> entry : entries ) {
                child.putString(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    public File getConfigurationsFile() {
        return getStateLocation().append("databases.xml").toFile();
    }

}
