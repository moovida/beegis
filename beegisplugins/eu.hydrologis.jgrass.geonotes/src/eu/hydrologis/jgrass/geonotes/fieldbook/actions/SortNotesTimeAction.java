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
import eu.hydrologis.jgrass.geonotes.util.GeonotesNameSorter;
import eu.hydrologis.jgrass.geonotes.util.GeonotesTimeSorter;

/**
 * Action to dumb {@link GeonotesUI}s binary.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SortNotesTimeAction extends Action {

    private final GeonotesListViewer geonotesViewer;

    public SortNotesTimeAction( GeonotesListViewer geonotesViewer ) {
        super("Sort notes by time");
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {
        geonotesViewer.setSorter(new GeonotesTimeSorter());
    
    }
}
