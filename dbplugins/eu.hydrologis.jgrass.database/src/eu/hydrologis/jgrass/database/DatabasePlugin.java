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
package eu.hydrologis.jgrass.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import net.refractions.udig.project.IProject;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.hibernate.mapping.Table;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.IDatabaseConnection;
import eu.hydrologis.jgrass.database.core.h2.H2DatabaseConnection;
import eu.hydrologis.jgrass.database.utils.ImageCache;

/**
 * The activator class controls the plug-in life cycle
 */
public class DatabasePlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.database";

    // The shared instance
    private static DatabasePlugin plugin;

    private IDatabaseConnection activeDatabaseConnection;
    private DatabaseConnectionProperties activeDatabaseConnectionProperties;

    private List<DatabaseConnectionProperties> availableDatabaseConnectionProperties = new ArrayList<DatabaseConnectionProperties>();

    /**
     * The constructor
     */
    public DatabasePlugin() {
    }

    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;
    }

    public void stop( BundleContext context ) throws Exception {
        try {
            saveDatabaseConnections();
            disconnectActiveDatabaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        plugin = null;

        ImageCache.getInstance().dispose();

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
            try {
                loadSavedDatabaseConnections();
                activateDatabaseConnectionFromSaved();
            } catch (Exception e) {
                String message = "An error occurred while connecting to the database. Connecting to the default database.";
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
                // fold back on the default database
            }
        }
        if (activeDatabaseConnection == null) {
            createDefaultDatabase();
        }
        return activeDatabaseConnection;
    }

    /**
     * Returns the properties of the current active database connection.
     * 
     * <p><b>This should be called after {@link #getActiveDatabaseConnection()}, to ensure consistency.</b></p>
     * 
     * @return teh propeties of the active database connection.
     */
    public DatabaseConnectionProperties getActiveDatabaseConnectionProperties() {
        return activeDatabaseConnectionProperties;
    }

    private void createDefaultDatabase() throws Exception {
        // create an embedded database inside the project folder
        IProject activeProject = ApplicationGIS.getActiveProject();
        URI id = activeProject.getID();
        String projectPath = id.toFileString();
        File projectFile = new File(projectPath);
        if (!projectFile.exists()) {
            String tempdir = System.getProperty("java.io.tmpdir");
            projectFile = new File(tempdir);
        } else {
            projectFile = projectFile.getParentFile().getParentFile();
        }
        File databaseFolder = new File(projectFile, "databases/defaultdatabase");
        boolean madeDirs = databaseFolder.mkdirs();
        if (!madeDirs && databaseFolder.exists()) {
            DatabaseConnectionProperties props = new DatabaseConnectionProperties();
            props.put("TYPE", H2DatabaseConnection.TYPE);
            props.put("ISACTIVE", "true");
            props.put("TITLE", "Default Database");
            props.put("DESCRIPTION", "Default Database");
            props.put("DRIVER", H2DatabaseConnection.DRIVER);
            props.put("DATABASE", "database");
            props.put("PORT", "9093");
            props.put("USER", "sa");
            props.put("PASS", "");
            props.put("PATH", databaseFolder.getAbsolutePath());

            activateDatabaseConnection(props);
            if (!availableDatabaseConnectionProperties.contains(props)) {
                availableDatabaseConnectionProperties.add(props);
            }
        } else {
            throw new IOException("An error occurred while creating the default database.");
        }
    }

    /**
     * Disconnects the active database connection.
     * 
     * @return true if the database was disconnected properly.
     */
    public boolean disconnectActiveDatabaseConnection() {
        try {
            activeDatabaseConnection.closeSessionFactory();
            activeDatabaseConnection = null;
            activeDatabaseConnectionProperties.setActive(false);
            activeDatabaseConnectionProperties = null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Creates a new database connection and activates it. 
     * 
     * @param newCP connection properties.
     * @throws Exception
     */
    public void activateDatabaseConnection( DatabaseConnectionProperties newCP ) throws Exception {
        // if another is active, first disconnect it
        if (activeDatabaseConnection != null) {
            disconnectActiveDatabaseConnection();
        }
        // create the new one
        activeDatabaseConnection = ConnectionManager.createDatabaseConnection(newCP);
        activeDatabaseConnectionProperties = newCP;
        // make connection
        activeDatabaseConnection.getSessionFactory();
        checkTableExistence();
        activeDatabaseConnectionProperties.setActive(true);
        if (!availableDatabaseConnectionProperties.contains(newCP)) {
            availableDatabaseConnectionProperties.add(newCP);
        }
        for( DatabaseConnectionProperties properties : availableDatabaseConnectionProperties ) {
            if (!properties.equals(activeDatabaseConnectionProperties)) {
                properties.setActive(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkTableExistence() throws Exception {
        Iterator tableMappings = activeDatabaseConnection.getAnnotationConfiguration().getTableMappings();
        List<String> tableList = new ArrayList<String>();
        while( tableMappings.hasNext() ) {
            Object next = tableMappings.next();
            if (next instanceof Table) {
                Table mappedTable = (Table) next;
                String name = mappedTable.getName();
                tableList.add(name);
            }
        }
        boolean checkTables = activeDatabaseConnection.checkTables((String[]) tableList.toArray(new String[tableList.size()]));
        if (!checkTables) {
            activeDatabaseConnection.createSchemas(true);
        }
    }

    /**
     * Loads the saved database connection properties.
     * 
     * 
     * @throws IOException
     * @throws Exception
     */
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

    // /home/moovida/rcpdevelopment/WORKSPACES/runtime-New_configuration/.metadata/.plugins/eu.hydrologis.jgrass.database/databases.xml
    private void loadSavedDatabaseConnections( XMLMemento memento ) throws Exception {
        String[] possibleTags = DatabaseConnectionProperties.POSSIBLETAGS;
        IMemento[] children = memento.getChildren(DatabaseConnectionProperties.DATABASE_XML);
        for( int i = 0; i < children.length; i++ ) {
            DatabaseConnectionProperties newCP = new DatabaseConnectionProperties();
            for( String tag : possibleTags ) {
                String value = children[i].getString(tag);
                if (value != null && value.length() > 0) {
                    newCP.put(tag, value);
                }
            }
            availableDatabaseConnectionProperties.add(newCP);
        }
    }

    private void activateDatabaseConnectionFromSaved() throws Exception {
        for( DatabaseConnectionProperties properties : availableDatabaseConnectionProperties ) {
            if (properties.isActive()) {
                activateDatabaseConnection(properties);
                if (!availableDatabaseConnectionProperties.contains(properties)) {
                    availableDatabaseConnectionProperties.add(properties);
                }
                break;
            }
        }
    }

    /**
     * Saves collected database connections in the configuration area.
     * 
     * @throws IOException
     */
    public void saveDatabaseConnections() throws IOException {
        XMLMemento memento = XMLMemento.createWriteRoot(DatabaseConnectionProperties.DATABASES_XML);
        saveDatabaseConnections(memento);

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

    private void saveDatabaseConnections( XMLMemento memento ) {
        for( DatabaseConnectionProperties dcP : availableDatabaseConnectionProperties ) {
            IMemento child = memento.createChild(DatabaseConnectionProperties.DATABASE_XML);
            Set<Entry<Object, Object>> entries = dcP.entrySet();
            for( Entry<Object, Object> entry : entries ) {
                child.putString(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    private File getConfigurationsFile() {
        return getStateLocation().append("databases.xml").toFile();
    }

    public List<DatabaseConnectionProperties> getAvailableDatabaseConnectionProperties() {
        return availableDatabaseConnectionProperties;
    }
}
