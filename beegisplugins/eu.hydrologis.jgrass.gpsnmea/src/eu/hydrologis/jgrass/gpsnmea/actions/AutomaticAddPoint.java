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

import java.awt.Toolkit;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;
import eu.hydrologis.jgrass.gpsnmea.layerinteraction.LayerHandler;
import eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants;

/**
 * Action to automatically start to add points from gps.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class AutomaticAddPoint extends Action
        implements
            IWorkbenchWindowActionDelegate,
            IActionDelegate2 {

    public static final String ID = "automatic.add.id"; //$NON-NLS-1$

    private IWorkbenchWindow window;
    private IPreferenceStore prefs;
    private GpsPoint priorGpsPoint = null;
    private boolean runIsActive = false;
    private boolean isOn = false;
    private IAction action;

    public AutomaticAddPoint() {
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID,
                "icons/automatic_continue16.png")); //$NON-NLS-1$
        setDisabledImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                GpsActivator.PLUGIN_ID, "icons/automatic_continue16_disabled.png")); //$NON-NLS-1$
        setText("Automatic point add");
        setToolTipText("Automatic addition of a point from the gps");
        setId(ID);

        prefs = GpsActivator.getDefault().getPreferenceStore();
    }

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
        // add action to the registry
        GpsActivator.getDefault().registerAction(this);
    }

    public void run( IAction action ) {
        if (!isOn) {
            isOn = !isOn;
            runThread();
        } else {
            isOn = !isOn;
            GpsActivator.getDefault().setInAutomaticMode(false);
        }

    }

    private void runThread() {
        final ILayer selectedLayer = ApplicationGIS.getActiveMap().getEditManager()
                .getSelectedLayer();

        Thread t = new Thread(){
            public void run() {
                if (!GpsActivator.getDefault().isGpsLogging()) {
                    GpsActivator.getDefault().setInAutomaticMode(false);
                    Display.getDefault().asyncExec(new Runnable(){
                        public void run() {
                            MessageBox msgBox = new MessageBox(window.getShell(), SWT.ICON_QUESTION
                                    | SWT.OK);
                            msgBox
                                    .setMessage("The gps is currently not logging. You have to first enable logging and then restart the automatic acquisition.");
                            msgBox.open();
                        }
                    });
                    return;
                }

                runIsActive = true;
                GpsActivator.getDefault().setInAutomaticMode(true);
                try {
                    while( isOn && GpsActivator.getDefault().isGpsLogging()
                            && GpsActivator.getDefault().isGpsConnected() ) {
                        final int milliSeconds = prefs.getInt(PreferenceConstants.INTERVAL_SECONDS) * 1000;
                        final double distanceUnits = prefs.getDouble(PreferenceConstants.DISTANCE_THRESHOLD);

                        if (LayerHandler.getInstance().initLayer(selectedLayer)) {
                            GpsPoint nextGpsPoint = GpsActivator.getDefault().getNextGpsPoint();
                            if (nextGpsPoint == null)
                                continue;
                            if (priorGpsPoint == null
                                    || nextGpsPoint.isAtLeastAtDistanceOf(priorGpsPoint, distanceUnits)) {
                                LayerHandler.getInstance().addGpsPointToLayer(selectedLayer,
                                        nextGpsPoint);
                                priorGpsPoint = nextGpsPoint;
                            }
                        }

                        Thread.sleep(milliSeconds);
                    }
                } catch (final Exception e) {
                    // emit sounds when exiting automatc mode because of problems
                    for( int i = 0; i < 4; i++ ) {
                        Toolkit.getDefaultToolkit().beep();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                        }
                    }
                    GpsActivator.getDefault().setInAutomaticMode(false);
                    String message = "An error occurred while adding points from gps in automatic mode.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            GpsActivator.PLUGIN_ID, e);
                    e.printStackTrace();
                } finally {
                    runIsActive = false;
                }
            }
        };
        t.start();
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
