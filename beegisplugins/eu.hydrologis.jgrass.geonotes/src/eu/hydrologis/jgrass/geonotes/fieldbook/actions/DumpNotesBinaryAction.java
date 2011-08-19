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

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;

import eu.hydrologis.jgrass.beegisutils.jgrassported.CompressionUtilities;
import eu.hydrologis.jgrass.beegisutils.jgrassported.FileUtilities;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.fieldbook.GeonotesListViewer;

/**
 * Action to dumb {@link GeonotesUI}s binary.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DumpNotesBinaryAction extends Action {

    private final GeonotesListViewer geonotesViewer;

    public DumpNotesBinaryAction( GeonotesListViewer geonotesViewer ) {
        super("Dump geonotes binary");
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {
        List<GeonotesHandler> currentGeonotesSelection = geonotesViewer.getCurrentGeonotesSelection();

        DirectoryDialog directoryDialog = new DirectoryDialog(geonotesViewer.getTable().getShell(),
                SWT.OPEN);
        String path = directoryDialog.open();

        if (path != null && path.length() > 0) {
            File dirFile = new File(path);
            if (dirFile.exists()) {
                try {

                    String ffName = GeonoteConstants.GEONOTES;
                    File geonotesDirFile = new File(dirFile, ffName);
                    int i = 1;
                    while( geonotesDirFile.exists() ) {
                        ffName = "geonotes_" + i;
                        geonotesDirFile = new File(geonotesDirFile.getParentFile(), ffName);
                        i++;
                    }
                    if (!geonotesDirFile.mkdir())
                        throw new IOException("Cannot create folder: "
                                + geonotesDirFile.getAbsolutePath());

                    int index = 0;
                    for( GeonotesHandler geoNote : currentGeonotesSelection ) {
                        File dir = new File(geonotesDirFile.getAbsolutePath() + File.separator
                                + String.valueOf(index));
                        index++;
                        if (dir.mkdir())
                            geoNote.dumpBinaryNote(dir.getAbsolutePath());
                    }

                    // zip the folder
                    File zipFile = new File(geonotesDirFile.getParentFile(), ffName + ".zip");
                    CompressionUtilities.zipFolder(geonotesDirFile.getAbsolutePath(), zipFile
                            .getAbsolutePath(), false);

                    if (!FileUtilities.deleteFileOrDir(geonotesDirFile)) {
                        FileUtilities.deleteFileOrDirOnExit(geonotesDirFile);
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                    String message = "An error occurred while dumping geonotes to disk.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            GeonotesPlugin.PLUGIN_ID, e1);
                }
            } else {
                MessageDialog.openError(geonotesViewer.getTable().getShell(), "Error",
                        "The folder in, which to dump notes could not be accessed: " + path);

            }
        }

    }
}
