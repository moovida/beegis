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

import java.util.HashMap;

import eu.hydrologis.jgrass.database.core.h2.H2ConnectionFactory;
import eu.hydrologis.jgrass.database.core.h2.H2DatabaseConnection;
import eu.hydrologis.jgrass.database.core.postgres.PostgresConnectionFactory;
import eu.hydrologis.jgrass.database.core.postgres.PostgresDatabaseConnection;

/**
 * Class taking care to find the proper {@link IDatabaseConnection}.
 * 
 * <p><b>New databases have to be registered here.</b></p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ConnectionManager {

    private static HashMap<String, IConnectionFactory> databaseDriver2ConnectionFactory = null;

    static {
        databaseDriver2ConnectionFactory = new HashMap<String, IConnectionFactory>();

        // new databases have to be registered here
        databaseDriver2ConnectionFactory.put(H2DatabaseConnection.DRIVER, new H2ConnectionFactory());
        databaseDriver2ConnectionFactory.put(PostgresDatabaseConnection.DRIVER, new PostgresConnectionFactory());
    }

    /**
     * Creates a {@link IDatabaseConnection} for the given properties.
     * 
     * @param connectionProperties
     * @return
     */
    public static synchronized IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties ) {
        String databaseDriver = connectionProperties.getDatabaseDriver();
        IConnectionFactory iConnectionFactory = databaseDriver2ConnectionFactory.get(databaseDriver);

        return iConnectionFactory.createDatabaseConnection(connectionProperties);
    }

    /**
     * Checks if the connection is local or remote.
     * 
     * @param connectionProperties the properties to check for.
     * @return true if the connection is local, false if remote.
     */
    public static boolean isLocal( DatabaseConnectionProperties connectionProperties ) {
        String databaseDriver = connectionProperties.getDatabaseDriver();
        if (databaseDriver.equals(H2DatabaseConnection.DRIVER)) {
            return true;
        } else if (databaseDriver.equals(PostgresDatabaseConnection.DRIVER)) {
            return false;
        }else{
            throw new IllegalArgumentException("Unknown database type.");
        }

    }
}
