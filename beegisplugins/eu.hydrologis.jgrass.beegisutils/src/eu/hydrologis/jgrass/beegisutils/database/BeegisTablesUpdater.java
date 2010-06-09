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
package eu.hydrologis.jgrass.beegisutils.database;

import javax.persistence.Table;

import org.eclipse.ui.IStartup;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.AnnotationsTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesDrawareaTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesMediaboxBlobsTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesMediaboxTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTextareaTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GpsLogTable;
import eu.hydrologis.jgrass.embeddeddb.EmbeddedDbPlugin;

/**
 * Creates the tables if they do not exist.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class BeegisTablesUpdater implements IStartup {

    public void earlyStartup() {
        // update beegis tables if necessary

        // get the table names
        String annotationsName = AnnotationsTable.class.getAnnotation(Table.class).name();
        String geonotesName = GeonotesTable.class.getAnnotation(Table.class).name();
        String geonotesdrawrareaName = GeonotesDrawareaTable.class.getAnnotation(Table.class)
                .name();
        String geonotesTextareaName = GeonotesTextareaTable.class.getAnnotation(Table.class).name();
        String geonotesMediaboxName = GeonotesMediaboxTable.class.getAnnotation(Table.class).name();
        String geonotesMediaboxBlobsName = GeonotesMediaboxBlobsTable.class.getAnnotation(Table.class)
                .name();
        String gpslogName = GpsLogTable.class.getAnnotation(Table.class).name();

        // EmbeddedDbPlugin.getDefault().createHibernateSchema(false);

        // check if tables exist
        try {
            if (!EmbeddedDbPlugin.getDefault().checkTables(annotationsName, geonotesName,
                    geonotesdrawrareaName, geonotesTextareaName, geonotesMediaboxName,
                    geonotesMediaboxBlobsName, gpslogName)) {
                EmbeddedDbPlugin.getDefault().createSchema(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    public synchronized static void checkSchema(){
        new BeegisTablesUpdater().earlyStartup();
    }
}
