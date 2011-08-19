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
import net.refractions.udig.project.command.MapCommand;
import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.layerinteraction.LayerHandler;

/**
 * Action to toggle the gps logging.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ToggleLoggingGps extends Action
        implements
            IWorkbenchWindowActionDelegate,
            IActionDelegate2 {

    public static final String ID = "startgps.id"; //$NON-NLS-1$

    private boolean isOn = false;

    private IWorkbenchWindow window;

    private IAction action;

    public ToggleLoggingGps() {
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
        isOn = !isOn;
        LayerHandler layerHandler = LayerHandler.getInstance();
        if (isOn) {
            if (!GpsActivator.getDefault().isGpsConnected()) {
                openGpsPreferences();
            }

            startLog(action);
        } else {
            stopLog(action);
            layerHandler.cleanLayerMap();
            IMap map = ApplicationGIS.getActiveMap();
            MapCommand commitCmd = EditCommandFactory.getInstance().createCommitCommand();
            map.sendCommandASync(commitCmd);
        }

    }

    private void openGpsPreferences() {
        Shell shell = new Shell(window.getShell(), SWT.SHELL_TRIM | SWT.PRIMARY_MODAL);
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(500, 500);
        Point cursorLocation = window.getShell().getDisplay().getCursorLocation();
        shell.setLocation(cursorLocation);
        new GpsSettingsComposite(shell);
        shell.open();

        while( !shell.isDisposed() ) {
            if (!shell.getDisplay().readAndDispatch())
                shell.getDisplay().sleep();
        }
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

    private void startLog( IAction action ) {
        action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                GpsActivator.PLUGIN_ID, "icons/toggleloggingon16.png"));
        action.setToolTipText("Stop Gps data logging");
        GpsActivator.getDefault().startGpsLogging();

        // start automatic if it was left selected
        AutomaticAddPoint automaticAddPointAction = (AutomaticAddPoint) GpsActivator.getDefault()
                .getAutomaticAddPointAction();
        automaticAddPointAction.setEnabled(true);
        automaticAddPointAction.runIfOn();
        // start automatic if it was left selected
        ZoomToGps zoomToGpsAction = (ZoomToGps) GpsActivator.getDefault().getZoomToGpsAction();
        zoomToGpsAction.setEnabled(true);
        zoomToGpsAction.runIfOn();

        GpsActivator.getDefault().getGeonoteAddAction().setEnabled(true);
        GpsActivator.getDefault().getManualAddPointAction().setEnabled(true);

    }

    /**
     * @param action
     */
    private void stopLog( IAction action ) {
        action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                GpsActivator.PLUGIN_ID, "icons/togglelogging16.png"));
        action.setToolTipText("Start Gps data logging");
        GpsActivator.getDefault().stopGpsLogging();
        // disable actions
        GpsActivator.getDefault().getAutomaticAddPointAction().setEnabled(false);
        GpsActivator.getDefault().getGeonoteAddAction().setEnabled(false);
        GpsActivator.getDefault().getManualAddPointAction().setEnabled(false);
        GpsActivator.getDefault().getZoomToGpsAction().setEnabled(false);
    }

    public void setEnabled( boolean enabled ) {
        action.setEnabled(enabled);
    }

    public void init( IAction action ) {
        this.action = action;
    }

    public void runWithEvent( IAction action, Event event ) {
        run(action);
    }

}
