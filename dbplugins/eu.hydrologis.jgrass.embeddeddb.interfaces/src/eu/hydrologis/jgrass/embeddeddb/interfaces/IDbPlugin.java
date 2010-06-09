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
package eu.hydrologis.jgrass.embeddeddb.interfaces;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public interface IDbPlugin {

    /**
     * Set the database parameters and resets the connection.
     * 
     * @param user
     * @param passwd
     * @param dbRoot the database name for remote and the database folder for embedded databases
     * @param port
     * @param dbHost
     */
    public void setConnectionParams( String user, String passwd, String dbRoot, int port,
            String dbHost );

    /**
     * @return the name of the database file, if needed to be added to the rootpath (as for hsqldb)
     */
    public String getDbName();

    /**
     * @return the database url string
     */
    public String getDbDriverString();

    /**
     * @param filePath file to which to backup the database to
     * @throws Exception 
     */
    public void backUpTo( String filePath ) throws IOException, Exception;

    /**
     * @param filePath file from which to restore the database
     * @throws Exception 
     */
    public void restoreFrom( String filePath ) throws IOException, Exception;

    /**
     * Method to check the existence of a certain set of tables
     * 
     * @param tables the tables that should be found to return true
     * @return true if all tables were found
     * @throws Exception 
     */
    public boolean checkTables( String... tables ) throws Exception;

    public SessionFactory getSessionFactory() throws Exception;

    public void closeSessionFactory() throws Exception;
    
    public AnnotationConfiguration getConfiguration() throws Exception;

    public void createSchema( boolean doUpdate ) throws Exception;

    public boolean isDbConnected();
}
