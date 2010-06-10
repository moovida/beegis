package eu.hydrologis.jgrass.database.core;

import java.util.Properties;

public class DatabaseConnectionProperties extends Properties {
    private static final long serialVersionUID = 1L;

    public static final String POSTGRESQL = "POSTGRESQL";
    public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    public static final String POSTGRESQL_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";

    public static final String DRIVER = "DRIVER";
    public static final String DATABASE = "DATABASE";
    public static final String PORT = "PORT";
    public static final String USER = "USER";
    public static final String PASS = "PASS";
    public static final String HOST = "HOST";
    public static final String PATH = "PATH";

    public static final String SHOW_SQL = "SHOW_SQL";
    public static final String FORMAT_SQL = "FORMAT_SQL";

    public DatabaseConnectionProperties( Properties properties ) {
        super(properties);
    }

    public String getDatabaseDriver() {
        return getProperty(DRIVER);
    }

    public String getDatabaseName() {
        return getProperty(DATABASE);
    }

    public String getUser() {
        return getProperty(USER);
    }

    public String getPassword() {
        return getProperty(PASS);
    }

    public String getPath() {
        return getProperty(PATH);
    }

    public String getHost() {
        return getProperty(HOST);
    }

    public String getPort() {
        return getProperty(PORT);
    }

    public boolean doLogSql() {
        String doLog = getProperty(SHOW_SQL);
        return Boolean.parseBoolean(doLog);
    }

}
