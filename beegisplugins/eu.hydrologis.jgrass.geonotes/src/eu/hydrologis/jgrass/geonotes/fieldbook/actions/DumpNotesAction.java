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
package eu.hydrologis.jgrass.geonotes.fieldbook.actions;

import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;

import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.fieldbook.GeonotesListViewer;

/**
 * Action to dump {@link GeonotesUI} to disk.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DumpNotesAction extends Action {

    private final GeonotesListViewer geonotesViewer;

    public DumpNotesAction( GeonotesListViewer geonotesViewer ) {
        super("Dump geonotes");
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {
        List<GeonotesHandler> currentGeonotesSelection = geonotesViewer.getCurrentGeonotesSelection();

        DirectoryDialog directoryDialog = new DirectoryDialog(geonotesViewer.getTable().getShell(),
                SWT.OPEN);
        String path = directoryDialog.open();

        if (path != null && path.length() > 0) {
            try {
                for( GeonotesHandler geoNote : currentGeonotesSelection ) {
                    GeonotesUI geonotesUI = GeonotesUI.guiCache.get(geoNote);
                    if (geonotesUI==null) {
                        geonotesUI = new GeonotesUI(geoNote);
                    }
                    Image drawareaImage = geonotesUI.getDrawareaImage();
                    geoNote.dumpNote(path, drawareaImage);
                }
            } catch (Exception e1) {
                String message = "An error occurred while dumping the geonotes to disk.";
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                        GeonotesPlugin.PLUGIN_ID, e1);
                e1.printStackTrace();
            }
        }

    }
}
