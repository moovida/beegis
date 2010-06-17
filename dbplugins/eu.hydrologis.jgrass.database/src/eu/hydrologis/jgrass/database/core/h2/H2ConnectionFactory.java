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
package eu.hydrologis.jgrass.database.core.h2;

import i18n.Messages;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.IConnectionFactory;
import eu.hydrologis.jgrass.database.core.IDatabaseConnection;

/**
 * A connection factory for H2 databases.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class H2ConnectionFactory implements IConnectionFactory {

    @Override
    public IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties ) {
        H2DatabaseConnection connection = new H2DatabaseConnection();
        connection.setConnectionParameters(connectionProperties);
        return connection;
    }

    /**
     * This creates {@link DatabaseConnectionProperties connection properties} based on a local db file.
     * 
     * <p>The type and some of the fields are guessed.</p>
     * 
     * @param dbFile the file representing a local database. 
     * @return best guessed connection properties.
     * @throws IOException 
     */
    public static DatabaseConnectionProperties createProperties( File dbFile ) throws IOException {
        if (!dbFile.exists()) {
            throw new IOException(Messages.H2ConnectionFactory__db_doesnt_exist);
        }
        if (!dbFile.isDirectory()) {
            return null;
        }

        File[] files = dbFile.listFiles(new FileFilter(){
            public boolean accept( File pathname ) {
                return pathname.getName().endsWith(".data.db"); //$NON-NLS-1$
            }
        });

        if (files.length == 0) {
            return null;
        }

        String dbName = files[0].getName().replaceFirst("\\.data\\.db", ""); //$NON-NLS-1$ //$NON-NLS-2$

        // curently only H2 is supported
        DatabaseConnectionProperties props = new DatabaseConnectionProperties();
        props.put("TYPE", H2DatabaseConnection.TYPE); //$NON-NLS-1$
        props.put("ISACTIVE", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        props.put("TITLE", dbName); //$NON-NLS-1$
        props.put("DESCRIPTION", dbName); //$NON-NLS-1$
        props.put("DRIVER", H2DatabaseConnection.DRIVER); //$NON-NLS-1$
        props.put("DATABASE", dbName); //$NON-NLS-1$
        props.put("PORT", "9092"); //$NON-NLS-1$ //$NON-NLS-2$
        props.put("USER", "sa"); //$NON-NLS-1$ //$NON-NLS-2$
        props.put("PASS", ""); //$NON-NLS-1$ //$NON-NLS-2$
        props.put("PATH", dbFile.getAbsolutePath()); //$NON-NLS-1$

        return props;

    }

}
