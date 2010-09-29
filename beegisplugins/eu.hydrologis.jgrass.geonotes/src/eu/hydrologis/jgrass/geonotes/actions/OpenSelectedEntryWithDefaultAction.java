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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.program.Program;

import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedStroke;
import eu.hydrologis.jgrass.geonotes.DndFile;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.MediaBoxUI;

/**
 * Action to open media from the {@link MediaBoxUI media area}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class OpenSelectedEntryWithDefaultAction extends Action {
    private final TableViewer lv;

    public OpenSelectedEntryWithDefaultAction( String title, TableViewer lv ) {
        super(title);
        this.lv = lv;
    }

    public void run() {
        try {
            IStructuredSelection selection = (IStructuredSelection) lv.getSelection();
            DndFile sel = (DndFile) selection.getFirstElement();
            if (sel.file != null && sel.file.exists()) {
                Program.launch(sel.file.getAbsolutePath());
            } else {
                // need to extract the file to temporary folder
                final String name = sel.name;
                final List<DressedStroke> drawingList = new ArrayList<DressedStroke>();
                File file = sel.getGeonotesHandler().extractMediaToPath(null, name, drawingList);
                if (file.exists()) {
                    // if it is an image use the own editor
                    Program.launch(file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            String message = "An error occurred while opening the selected media.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    GeonotesPlugin.PLUGIN_ID, e);
            e.printStackTrace();
        }
    }

}
