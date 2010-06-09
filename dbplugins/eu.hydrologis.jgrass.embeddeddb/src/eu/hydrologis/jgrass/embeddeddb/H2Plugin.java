package eu.hydrologis.jgrass.embeddeddb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.h2.tools.Server;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.osgi.framework.Bundle;

import eu.hydrologis.jgrass.embeddeddb.earlystartup.AnnotatedClassesCollector;
import eu.hydrologis.jgrass.embeddeddb.interfaces.IDbPlugin;
import eu.hydrologis.jgrass.embeddeddb.interfaces.Utils;

/**
 * The class that deals with everything related to H2 connections.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class H2Plugin implements IDbPlugin {

    private static H2Plugin plugin;

    public static final String DRIVER = "org.h2.Driver"; //$NON-NLS-1$

    public static final String DBNAME = "database"; //$NON-NLS-1$

    private int port = 9001;

    private String user = null;

    private String passwd = null;

    private boolean dbIsAlive = false;

    private Server tcpServer = null;
    private Server webServer = null;

    private AnnotationConfiguration annotationConfiguration;

    private SessionFactory sessionFactory;

    private String connectionString;

    private List<String> annotatedClassesList = new ArrayList<String>();

    private H2Plugin() {
    }

    public static H2Plugin getDefault() {
        if (plugin == null) {
            plugin = new H2Plugin();
        }
        return plugin;
    }

    public String getDbDriverString() {
        return DRIVER;
    }

    public String getDbName() {
        return DBNAME;
    }

    public void setConnectionParams( String user, String passwd, String dbRoot, int port, String dbHost ) {
        this.user = user;
        this.passwd = passwd;
        this.port = port;
        final String database = dbRoot + File.separator + DBNAME;
        connectionString = "jdbc:h2:tcp://localhost:" + port + "/" + database; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public AnnotationConfiguration getConfiguration() throws Exception {
        if (annotationConfiguration == null) {
            Properties dbProps = new Properties();
            dbProps.put(Environment.DRIVER, DRIVER);
            dbProps.put(Environment.URL, connectionString);
            dbProps.put(Environment.USER, user);
            dbProps.put(Environment.PASS, passwd);
            dbProps.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect"); //$NON-NLS-1$
            dbProps.put(Environment.SHOW_SQL, EmbeddedDbPlugin.LOGHIBERNATESQL);
            dbProps.put(Environment.FORMAT_SQL, EmbeddedDbPlugin.LOGHIBERNATESQL);

            annotationConfiguration = new AnnotationConfiguration();

            File configFile = Utils.generateConfigFile();
            annotationConfiguration = annotationConfiguration.configure(configFile);
            annotatedClassesList.addAll(AnnotatedClassesCollector.getAnnotatedClassesList());
            annotationConfiguration.setProperties(dbProps);

            for( String annotatedClassString : getAnnotatedClassesList() ) {
                annotationConfiguration.addAnnotatedClass(Class.forName(annotatedClassString));
            }
        }
        return annotationConfiguration;
    }

    public synchronized SessionFactory getSessionFactory() throws Exception {
        if (sessionFactory == null) {
            startWebserver();
            int timeout = 0;
            while( !dbIsAlive ) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (timeout++ > 50) {
                    throw new RuntimeException("An error occurred while starting the embedded database."); //$NON-NLS-1$
                }
            }
            sessionFactory = getConfiguration().buildSessionFactory();
        }
        return sessionFactory;
    }

    public void closeSessionFactory() throws Exception {
        if (sessionFactory == null) {
            return;
        }
        getSessionFactory().close();
        if (tcpServer != null) {
            tcpServer.stop();
        }
        if (webServer != null) {
            webServer.stop();
        }
    }

    public void createSchema( boolean doUpdate ) throws Exception {
        getSessionFactory();
        if (doUpdate) {
            SchemaUpdate schemaUpdate = new SchemaUpdate(getConfiguration());
            schemaUpdate.execute(true, true);
        } else {
            SchemaExport schemaExport = new SchemaExport(getConfiguration());
            schemaExport.create(true, true);
        }
    }

    public boolean isDbConnected() {
        return sessionFactory != null;
    }

    /**
     * start the database instance
     */
    private void startWebserver() {
        Thread h2WebserverThread = new Thread(){

            @SuppressWarnings("nls")
            public void run() {
                try {
                    if (!dbIsAlive) {
                        String[] args = {"-tcp", "-tcpPort", String.valueOf(port)};
                        tcpServer = Server.createTcpServer(args).start();
                        args = new String[]{"-web", "-webPort", String.valueOf(port + 1)};
                        webServer = Server.createWebServer(args).start();
                        dbIsAlive = true;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        h2WebserverThread.start();
    }

    @SuppressWarnings("nls")
    public void backUpTo( String filePath ) throws Exception {
        Session session = getSessionFactory().openSession();
        String sql = "SCRIPT TO '" + filePath + "' compression zip";
        SQLQuery sqlQuery = session.createSQLQuery(sql);
        sqlQuery.executeUpdate();
        session.close();
    }

    @SuppressWarnings("nls")
    public void restoreFrom( String filePath ) throws Exception {
        Session session = getSessionFactory().openSession();
        String sql = "RUNSCRIPT FROM '" + filePath + "' compression zip";
        SQLQuery sqlQuery = session.createSQLQuery(sql);
        sqlQuery.executeUpdate();
        session.close();
    }

    @SuppressWarnings("nls")
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

    /**
     * Getter for the annotated classe, that have to be defined if the plugin
     * system is not up (console mode). 
     * 
     * @return the mutuable list of annotataed classes names.
     */
    public List<String> getAnnotatedClassesList() {
        return annotatedClassesList;
    }

}
