package eu.hydrologis.jgrass.database.core;

public interface IConnectionFactory {

    public IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties );

}
