/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.database.core;

import java.util.Properties;

/**
 * Database connection properties.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseConnectionProperties extends Properties {
    private static final long serialVersionUID = 1L;

    public static final String TITLE = "TITLE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String DRIVER = "DRIVER";
    public static final String DATABASE = "DATABASE";
    public static final String PORT = "PORT";
    public static final String USER = "USER";
    public static final String PASS = "PASS";
    public static final String HOST = "HOST";
    public static final String PATH = "PATH";

    public static final String SHOW_SQL = "SHOW_SQL";
    public static final String FORMAT_SQL = "FORMAT_SQL";

    /**
     * Wrapps a {@link Properties} object into a {@link DatabaseConnectionProperties}.
     * 
     * @param properties the properties object.
     */
    public DatabaseConnectionProperties( Properties properties ) {
        super(properties);
    }

    public String getTitle() {
        return getProperty(TITLE);
    }

    public String getDescription() {
        return getProperty(DESCRIPTION);
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
