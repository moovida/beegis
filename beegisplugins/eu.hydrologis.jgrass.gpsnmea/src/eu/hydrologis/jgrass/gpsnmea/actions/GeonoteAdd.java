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
package eu.hydrologis.jgrass.gpsnmea.actions;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Coordinate;

import eu.hydrologis.jgrass.geonotes.GeonoteConstants;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;
import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;

/**
 * Action to add a Geonote from gps.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeonoteAdd extends Action implements IWorkbenchWindowActionDelegate, IActionDelegate2 {
    public static final String ID = "geonote.add.id";

    private IAction action;

    private IWorkbenchWindow window;

    public GeonoteAdd() {
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID, "icons/geonote.png"));
        setText("Add geonote");
        setToolTipText("Add geonote from gps point");
        setId(ID);
    }

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
        // add action to the registry
        GpsActivator.getDefault().registerAction(this);
    }

    public void run( IAction action ) {
        if (GpsActivator.getDefault().isGpsLogging() && GpsActivator.getDefault().isGpsConnected()) {

            try {
                GpsPoint nextGpsPoint = GpsActivator.getDefault().getNextGpsPoint();
                if (nextGpsPoint == null) {
                    MessageDialog.openWarning(window.getShell(), "Gps warning",
                            "the Gps didn't return a valid point. Please try again.");
                    return;
                }

                String projectName = ApplicationGIS.getActiveProject().getName();
                IMap activeMap = ApplicationGIS.getActiveMap();
                String mapName = activeMap.getName();
                String crsWkt = activeMap.getViewportModel().getCRS().toWKT();
                Coordinate reprojected = nextGpsPoint.reproject(null);
                GeonotesHandler geonotesHandler = new GeonotesHandler(reprojected.x, reprojected.y,
                        mapName + " - " + projectName, null, GeonoteConstants.GPS, new DateTime(), crsWkt, null, null, null, null);

                GeonotesPlugin.getDefault().getGeonotesLayer();

                GeonotesUI geoNote = new GeonotesUI(geonotesHandler);
                geoNote.openInShell(null);

                FieldbookView fieldBookView = GeonotesPlugin.getDefault().getFieldbookView();
                if (fieldBookView != null) {
                    geonotesHandler.addObserver(fieldBookView);
                }

            } catch (Exception e) {
                String message = "An error occurred while adding the geonote.";
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e);
                e.printStackTrace();
            }
        }
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

    public void setEnabled( boolean enabled ) {
        action.setEnabled(enabled);
    }

    public void init( IAction action ) {
        this.action = action;
        setEnabled(false);
    }

    public void runWithEvent( IAction action, Event event ) {
        run(action);
    }

}
