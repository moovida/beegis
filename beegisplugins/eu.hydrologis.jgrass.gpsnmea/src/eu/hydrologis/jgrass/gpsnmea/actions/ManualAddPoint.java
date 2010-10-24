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

import net.refractions.udig.project.ILayer;
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

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;
import eu.hydrologis.jgrass.gpsnmea.layerinteraction.LayerHandler;

/**
 * Action to manually add a point from gps.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ManualAddPoint extends Action
        implements
            IWorkbenchWindowActionDelegate,
            IActionDelegate2 {
    public static final String ID = "manual.add.id"; //$NON-NLS-1$

    private IWorkbenchWindow window;
    private IAction action;

    public ManualAddPoint() {
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID,
                "icons/manual_add16.png"));
        setText("Manual point add");
        setToolTipText("Manual addition of a point from the gps");
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
                ILayer selectedLayer = ApplicationGIS.getActiveMap().getEditManager()
                        .getSelectedLayer();
                if (LayerHandler.getInstance().initLayer(selectedLayer)) {
                    GpsPoint nextGpsPoint = GpsActivator.getDefault().getNextGpsPoint();
                    if (nextGpsPoint == null) {
                        MessageDialog.openWarning(window.getShell(), "Gps warning",
                                "the Gps didn't return a valid point. Please try again.");
                        return;
                    }
                    LayerHandler.getInstance().addGpsPointToLayer(selectedLayer, nextGpsPoint);
                }
            } catch (Exception e) {
                String message = "An error occurred while manually adding a new point from gps.";
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                        GpsActivator.PLUGIN_ID, e);
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
