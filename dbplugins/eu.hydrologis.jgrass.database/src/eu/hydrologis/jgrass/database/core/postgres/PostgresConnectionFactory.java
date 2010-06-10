package eu.hydrologis.jgrass.database.core.postgres;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.IConnectionFactory;
import eu.hydrologis.jgrass.database.core.IDatabaseConnection;

public class PostgresConnectionFactory implements IConnectionFactory {

    @Override
    public IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties ) {
        PostgresDatabaseConnection connection = new PostgresDatabaseConnection();
        connection.setConnectionParameters(connectionProperties);
        return connection;
    }



}
