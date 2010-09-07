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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import eu.hydrologis.jgrass.geonotes.DndFile;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;

/**
 * Clear selected media from mediabox and database.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ClearSelectedEntriesAction extends Action {
    private final List<DndFile> dndfiles;
    private final TableViewer lv;
    public ClearSelectedEntriesAction( String title, TableViewer lv, List<DndFile> dndfiles ) {
        super(title);
        this.lv = lv;
        this.dndfiles = dndfiles;
    }

    public void run() {
        IStructuredSelection selection = (IStructuredSelection) lv.getSelection();
        DndFile sel = (DndFile) selection.getFirstElement();
        // remove from table
        dndfiles.remove(sel);
        lv.setInput(dndfiles);

        // remove also from the database
        try {
            sel.getGeonotesHandler().deleteMedia(sel);
        } catch (Exception e) {
            String message = "An error occurred while removing the selected media.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    GeonotesPlugin.PLUGIN_ID, e);
            e.printStackTrace();
        }
    }
}
