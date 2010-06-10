package eu.hydrologis.jgrass.database.core.h2;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.IConnectionFactory;
import eu.hydrologis.jgrass.database.core.IDatabaseConnection;

public class H2ConnectionFactory implements IConnectionFactory {

    @Override
    public IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties ) {
        H2DatabaseConnection connection = new H2DatabaseConnection();
        connection.setConnectionParameters(connectionProperties);
        return connection;
    }

}
