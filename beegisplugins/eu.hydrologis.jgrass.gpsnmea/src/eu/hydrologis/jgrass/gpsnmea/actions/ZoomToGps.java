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
import net.refractions.udig.project.command.factory.NavigationCommandFactory;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;

/**
 * Action to toggle the zoom to gps.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ZoomToGps extends Action implements IWorkbenchWindowActionDelegate, IActionDelegate2 {
    public static final String ID = "center.zoom.id"; //$NON-NLS-1$

    public static final double BORDERPERCENT = 0.2;

    boolean isOn = false;
    private boolean runIsActive = false;
    private IAction action;

    public ZoomToGps() {
        setId(ID);
    }

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        // add action to the registry
        GpsActivator.getDefault().registerAction(this);
    }

    public void run( IAction action ) {
        if (!isOn) {
            isOn = !isOn;
            runThread();
        } else {
            isOn = !isOn;
        }
    }

    private void runThread() {
        Runnable runnable = new Runnable(){
            public void run() {
                runIsActive = true;
                while( isOn ) {
                    if (GpsActivator.getDefault() == null) {
                        break;
                    } else if (GpsActivator.getDefault().isGpsLogging()) {
                        GpsPoint nextGpsPoint = null;
                        try {
                            nextGpsPoint = GpsActivator.getDefault().getNextGpsPoint();
                            if (nextGpsPoint == null)
                                continue;
                            Coordinate reprojected = nextGpsPoint.reproject(null);
                            double longitude = reprojected.x;
                            double latitude = reprojected.y;

                            final IMap map = ApplicationGIS.getActiveMap();
                            IViewportModel viewportModel = map.getViewportModel();
                            Envelope bbox = viewportModel.getBounds();

                            /*
                             * make the zoom start if we are reaching 10% from the
                             * border
                             */
                            double width = bbox.getWidth();
                            double height = bbox.getHeight();
                            double insetX = width * BORDERPERCENT / 2.0;
                            double insetY = height * BORDERPERCENT / 2.0;

                            double x1 = bbox.getMinX() + insetX;
                            double x2 = bbox.getMaxX() - insetX;
                            double y1 = bbox.getMinY() + insetY;
                            double y2 = bbox.getMaxY() - insetY;
                            Envelope newEnv = new Envelope(x1, x2, y1, y2);

                            if (!newEnv.contains(longitude, latitude)) {

                                Envelope bounds = new Envelope();
                                bounds.setToNull();
                                bounds.init(longitude - bbox.getWidth() / 2.0, longitude + bbox.getWidth() / 2.0, latitude
                                        - bbox.getHeight() / 2.0, latitude + bbox.getHeight() / 2.0);

                                if (!bounds.isNull()) {
                                    map.sendCommandASync(NavigationCommandFactory.getInstance().createSetViewportBBoxCommand(
                                            bounds));
                                }
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            String message = "An error occurred while retriving the GPS position.";
                            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID, e1);
                            break;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                runIsActive = false;
            }
        };
        new Thread(runnable).start();
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

    public void runIfOn() {
        // case in which logging was stopped without disabling the button. Has to be changed to
        // something better
        if (!runIsActive && isOn) {
            runThread();
        }

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
