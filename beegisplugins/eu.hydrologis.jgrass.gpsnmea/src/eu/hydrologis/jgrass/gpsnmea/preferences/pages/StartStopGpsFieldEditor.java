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
package eu.hydrologis.jgrass.gpsnmea.preferences.pages;

import java.lang.reflect.InvocationTargetException;
import java.util.Observable;
import java.util.Observer;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.NmeaGpsImpl;
import eu.hydrologis.jgrass.gpsnmea.gps.NmeaGpsPoint;

/**
 * {@link FieldEditor} for starting and stopping gps logging and checking result.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StartStopGpsFieldEditor extends FieldEditor implements Observer {

    private Text text;

    private int columns = 2;

    private boolean gpsIsOn = false;

    private boolean isFirst = false;

    private Button startButton;

    private Button stopButton;

    public StartStopGpsFieldEditor( String preferenceName, Composite parent ) {
        init(preferenceName, ""); //$NON-NLS-1$
        createControl(parent);
    }

    protected void adjustForNumColumns( int numColumns ) {
        columns = numColumns;
    }

    protected void doFillIntoGrid( Composite parent, int numColumns ) {

        Group group = new Group(parent, SWT.None);
        group.setText("Start / Stop the Gps connection");
        group.setLayout(new GridLayout(2, true));
        GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        gridData.horizontalSpan = columns;
        group.setLayoutData(gridData);

        startButton = new Button(group, SWT.BORDER | SWT.RADIO);
        startButton.setText("Start Gps");
        startButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID,
                "icons/start16.png").createImage()); //$NON-NLS-1$
        startButton
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        startButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                try {
                    PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
                            new IRunnableWithProgress(){
                                public void run( IProgressMonitor pm )
                                        throws InvocationTargetException, InterruptedException {
                                    if (!GpsActivator.getDefault().isGpsConnected()) {
                                        pm.beginTask("Starting Gps...", IProgressMonitor.UNKNOWN);
                                        GpsActivator.getDefault().startGps();
                                        GpsActivator.getDefault().startGpsLogging();
                                        isFirst = true;
                                        gpsIsOn = true;
                                        pm.done();

                                        // add this as listener to gps
                                        GpsActivator.getDefault().addObserverToGps(
                                                StartStopGpsFieldEditor.this);
                                    }
                                }
                            });
                } catch (Exception e1) {
                    String message = "An error occurred while starting gps logging.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            GpsActivator.PLUGIN_ID, e1);
                    e1.printStackTrace();
                }
            }
        });

        stopButton = new Button(group, SWT.BORDER | SWT.RADIO);
        stopButton.setText("Stop Gps");
        stopButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID,
                "icons/stop16.png").createImage()); //$NON-NLS-1$
        stopButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        stopButton.setSelection(true);
        stopButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                try {

                    PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
                            new IRunnableWithProgress(){
                                public void run( IProgressMonitor pm )
                                        throws InvocationTargetException, InterruptedException {
                                    pm.beginTask("Stopping Gps...", IProgressMonitor.UNKNOWN);
                                    if (GpsActivator.getDefault().isGpsConnected())
                                        GpsActivator.getDefault().stopGps();
                                    gpsIsOn = false;
                                    pm.done();
                                }
                            });

                } catch (Exception e1) {
                    String message = "An error occurred while stopping gps logging.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            GpsActivator.PLUGIN_ID, e1);
                    e1.printStackTrace();
                }
            }
        });

        text = new Text(group, SWT.BORDER | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        text.setLayoutData(gd);

    }

    protected void doLoad() {
        gpsIsOn = getPreferenceStore().getBoolean(PreferenceConstants.GPS_IS_ON);
        if (gpsIsOn) {
            startButton.setSelection(true);
            stopButton.setSelection(false);
        }
    }

    protected void doLoadDefault() {
        gpsIsOn = false;
        startButton.setSelection(false);
        stopButton.setSelection(true);
    }

    protected void doStore() {
        getPreferenceStore().setValue(PreferenceConstants.GPS_IS_ON, gpsIsOn);
    }

    public int getNumberOfControls() {
        return 1;
    }

    public void dispose() {
        GpsActivator.getDefault().removeObserverFromGps(StartStopGpsFieldEditor.this);
        super.dispose();
    }

    public void update( Observable o, Object arg ) {
        if (o instanceof NmeaGpsImpl) {
            NmeaGpsImpl nmeaObs = (NmeaGpsImpl) o;
            final String currentGpsSentence = nmeaObs.getCurrentGpsData();

            if (text.isDisposed()) {
                if (isFirst) {
                    GpsActivator.getDefault().stopGpsLogging();
                    isFirst = false;
                }
                return;
            }

            Display.getDefault().syncExec(new Runnable(){
                public void run() {
                    if (currentGpsSentence != null
                            && currentGpsSentence.startsWith(NmeaGpsPoint.GPGGA)) {
                        text.setText(currentGpsSentence
                                + "\n\n This seems to be the right Gps connection port.");
                    } else {
                        text
                                .setText("The selected port doesn't seem to be properly attached to a GPS device. Please wait another minute while the application is trying to connect.");
                    }
                }
            });

        }
    }

}
