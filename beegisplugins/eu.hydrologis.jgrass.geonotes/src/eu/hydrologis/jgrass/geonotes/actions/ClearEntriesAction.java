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
package eu.hydrologis.jgrass.geonotes.actions;

import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;

import eu.hydrologis.jgrass.geonotes.DndFile;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;

/**
 * Clear all media from mediabox and database.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ClearEntriesAction extends Action {
    private final List<DndFile> dndfiles;
    private final TableViewer lv;
    public ClearEntriesAction( String title, TableViewer lv, List<DndFile> dndfiles ) {
        super(title);
        this.lv = lv;
        this.dndfiles = dndfiles;
    }
    public void run() {
        if (dndfiles.size() == 0)
            return;
        // remove from database
        try {
            GeonotesHandler geonotesHandler = dndfiles.get(0).getGeonotesHandler();
            for( DndFile dndFile : dndfiles ) {
                geonotesHandler.deleteMedia(dndFile);
            }
            // remove from table
            dndfiles.removeAll(dndfiles);
            lv.setInput(null);
        } catch (Exception e) {
            String message = "An error occurred while removing medias.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    GeonotesPlugin.PLUGIN_ID, e);
            e.printStackTrace();
        }
    }
}
