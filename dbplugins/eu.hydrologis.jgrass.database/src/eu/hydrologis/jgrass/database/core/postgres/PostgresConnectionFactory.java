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
package eu.hydrologis.jgrass.database.core.postgres;

import i18n.Messages;

import java.io.File;
import java.io.IOException;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.IConnectionFactory;
import eu.hydrologis.jgrass.database.core.IDatabaseConnection;

/**
 * A connection factory for Postgresql databases.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PostgresConnectionFactory implements IConnectionFactory {

    @Override
    public IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties ) {
        PostgresDatabaseConnection connection = new PostgresDatabaseConnection();
        connection.setConnectionParameters(connectionProperties);
        return connection;
    }

    @Override
    public DatabaseConnectionProperties createProperties( File dbFile ) throws IOException {
        // postgres is not file based
        return null;
    }
    
    @Override
    public DatabaseConnectionProperties createDefaultProperties() {
        DatabaseConnectionProperties props = new DatabaseConnectionProperties();
        props.put(DatabaseConnectionProperties.TYPE, PostgresDatabaseConnection.TYPE);
        props.put(DatabaseConnectionProperties.ISACTIVE, "false"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.TITLE, Messages.H2ConnectionFactory__default_db);
        props.put(DatabaseConnectionProperties.DESCRIPTION, Messages.H2ConnectionFactory__default_db);
        props.put(DatabaseConnectionProperties.DRIVER, PostgresDatabaseConnection.DRIVER);
        props.put(DatabaseConnectionProperties.DATABASE, "database"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.PORT, "9093"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.USER, "sa"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.PASS, ""); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.HOST, "localhost"); //$NON-NLS-1$
        return props;
    }

}
