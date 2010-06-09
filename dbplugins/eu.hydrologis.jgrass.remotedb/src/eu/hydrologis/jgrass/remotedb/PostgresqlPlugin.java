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
package eu.hydrologis.jgrass.remotedb;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.hibernate.MappingException;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import eu.hydrologis.jgrass.embeddeddb.earlystartup.AnnotatedClassesCollector;
import eu.hydrologis.jgrass.embeddeddb.interfaces.IDbPlugin;
import eu.hydrologis.jgrass.embeddeddb.interfaces.Utils;

/**
 * The class that deals with everything related to postgres connections.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PostgresqlPlugin implements IDbPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.remote.PostgresqlPlugin"; //$NON-NLS-1$

    // The shared instance
    private static PostgresqlPlugin plugin;

    private String dbHost = null;

    public static final String DRIVER = "org.postgresql.Driver"; //$NON-NLS-1$

    public static String DBNAME = null;

    private int port = 9001;

    private String user = null;

    private String passwd = null;

    private String dbName = null;

    private String connectionString = null;

    private AnnotationConfiguration annotationConfiguration;

    private SessionFactory sessionFactory;

    /**
     * The constructor
     */
    private PostgresqlPlugin() {
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static PostgresqlPlugin getDefault() {
        if (plugin == null) {
            plugin = new PostgresqlPlugin();
        }
        return plugin;
    }

    public String getDbDriverString() {
        return DRIVER;
    }

    public String getDbName() {
        if (DBNAME == null) {
            DBNAME = dbName;
        }
        return DBNAME;
    }

    public void setConnectionParams( String user, String passwd, String dbName, int port, String dbHost ) {
        this.user = user;
        this.passwd = passwd;
        this.dbHost = dbHost;
        this.dbName = dbName;
        this.port = port;
        connectionString = "jdbc:postgresql://" + dbHost + ":" + port + "/" + dbName;
    }

    @SuppressWarnings("nls")
    public AnnotationConfiguration getConfiguration() throws Exception {
        if (annotationConfiguration == null) {
            Properties dbProps = new Properties();
            dbProps.put(Environment.DRIVER, DRIVER);
            dbProps.put(Environment.URL, connectionString);
            dbProps.put(Environment.USER, user);
            dbProps.put(Environment.PASS, passwd);
            dbProps.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
            dbProps.put(Environment.SHOW_SQL, "true");
            dbProps.put(Environment.FORMAT_SQL, "true");

            annotationConfiguration = new AnnotationConfiguration();

            File configFile = Utils.generateConfigFile();
            annotationConfiguration = annotationConfiguration.configure(configFile);
            annotationConfiguration.setProperties(dbProps);

            List<String> annotatedClassesList = AnnotatedClassesCollector.getAnnotatedClassesList();

            for( String annotatedClassString : annotatedClassesList ) {
                annotationConfiguration.addAnnotatedClass(Class.forName(annotatedClassString));
            }
        }
        return annotationConfiguration;
    }

    public SessionFactory getSessionFactory() throws Exception {
        if (sessionFactory == null) {
            sessionFactory = getConfiguration().buildSessionFactory();
        }
        return sessionFactory;
    }

    public void closeSessionFactory() throws Exception {
        // Close caches and connection pools
        getSessionFactory().close();
    }

    public void createSchema( boolean doUpdate ) throws Exception {
        getSessionFactory();
        if (doUpdate) {
            SchemaUpdate schemaUpdate = new SchemaUpdate(getConfiguration());
            schemaUpdate.execute(false, true);
        } else {
            SchemaExport schemaExport = new SchemaExport(getConfiguration());
            schemaExport.create(false, true);
        }
    }

    public boolean isDbConnected() {
        return sessionFactory != null;
    }

    public void backUpTo( String filePath ) throws IOException {
        throw new IOException("Backing up it not implemented yet for postgresql.");
    }

    public void restoreFrom( String filePath ) throws IOException {
        throw new IOException("Restoring backups it not implemented yet for postgresql.");
    }

    public boolean checkTables( String... tables ) throws Exception {
        StringBuilder sB = new StringBuilder();
        sB.append("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE ");
        for( int i = 0; i < tables.length; i++ ) {
            String tableName = tables[i];
            if (i == 0) {
                sB.append("UPPER(TABLE_NAME) = UPPER('").append(tableName).append("')");
            } else {
                sB.append(" OR UPPER(TABLE_NAME) = UPPER('").append(tableName).append("')");
            }
        }

        SessionFactory hibernateSessionFactory = getSessionFactory();
        Session session = hibernateSessionFactory.openSession();
        SQLQuery sqlQuery = session.createSQLQuery(sB.toString());
        Number foundNum = (Number) sqlQuery.list().get(0);

        session.close();

        if (tables.length == foundNum.intValue()) {
            return true;
        }
        return false;
    }

}
