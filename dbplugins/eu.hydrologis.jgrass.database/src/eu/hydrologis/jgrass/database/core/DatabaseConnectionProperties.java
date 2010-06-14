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
import java.util.Set;
import java.util.Map.Entry;

/**
 * Database connection properties.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseConnectionProperties extends Properties {
    private static final long serialVersionUID = 1L;

    public static final String DATABASES_XML = "DATABASES";
    public static final String DATABASE_XML = "DATABASE";

    public static final String ISACTIVE = "ISACTIVE";
    public static final String TITLE = "TITLE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String DRIVER = "DRIVER";
    public static final String DATABASE = "DATABASE";
    public static final String PORT = "PORT";
    public static final String USER = "USER";
    public static final String PASS = "PASS";
    public static final String HOST = "HOST";
    public static final String PATH = "PATH";

    public static String[] POSSIBLETAGS = {ISACTIVE, TITLE, DESCRIPTION, DRIVER, DATABASE, PORT, USER, PASS, HOST, PATH};

    public static final String SHOW_SQL = "SHOW_SQL";
    public static final String FORMAT_SQL = "FORMAT_SQL";

    public DatabaseConnectionProperties() {
    }

    /**
     * Wrapps a {@link Properties} object into a {@link DatabaseConnectionProperties}.
     * 
     * @param properties the properties object.
     */
    public DatabaseConnectionProperties( Properties properties ) {
        Set<Entry<Object, Object>> entries = properties.entrySet();
        for( Entry<Object, Object> entry : entries ) {
            put(entry.getKey(), entry.getValue());
        }
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
        String password = getProperty(PASS);
        if (password == null) {
            password = "";
        }
        return password;
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

    public boolean isActive() {
        String isActive = getProperty(ISACTIVE);
        return Boolean.parseBoolean(isActive);
    }

    @Override
    public synchronized String toString() {
        StringBuilder sB = new StringBuilder();
        Set<Entry<Object, Object>> entries = entrySet();
        for( Entry<Object, Object> entry : entries ) {
            sB.append(entry.getKey().toString());
            sB.append("=");
            sB.append(entry.getValue().toString());
            sB.append("\n");
        }
        return sB.toString();
    }

    /**
     * Populates the properties from a text of properties.
     * 
     * @param propertiesString
     */
    public void fromString( String propertiesString ) {
        String[] linesSplit = propertiesString.split("\n|\r");
        for( String line : linesSplit ) {
            if (line.contains("=")) {
                String[] split = line.trim().split("=");
                put(split[0], split[1]);
            }
        }
    }

}
