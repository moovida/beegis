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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.AbstractGps;
import eu.hydrologis.jgrass.gpsnmea.gps.NmeaGpsImpl;
import eu.hydrologis.jgrass.gpsnmea.gps.NmeaGpsPoint;
import eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants;

/**
 * The gps settings composite.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsSettingsComposite implements Observer {

    private boolean gpsIsOn = false;
    private boolean isFirst = false;
    private Button startButton;
    private Button stopButton;
    private Text text;
    private IPreferenceStore prefs;
    private Label portLabel;
    private String gpsPortUsed = ""; //$NON-NLS-1$
    private Text intervalText;
    private Text distanceText;
    private Button dummyModeButton;

    public GpsSettingsComposite( final Shell parent ) {

        /*
         * Serial ports panel
         */
        Group group = new Group(parent, SWT.None);
        group.setText(PreferenceConstants.PORTUSED);
        group.setLayout(new GridLayout(2, false));
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        group.setLayoutData(gridData);

        portLabel = new Label(group, SWT.None);
        portLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Button searchPortButton = new Button(group, SWT.BORDER | SWT.PUSH);
        searchPortButton.setText("search port");
        searchPortButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                String[] ports = getPorts();
                final Shell shell = new Shell(parent.getShell());
                shell.setSize(new Point(300, 400));
                Point cursorLocation = parent.getShell().getDisplay().getCursorLocation();
                shell.setLocation(cursorLocation);
                shell.setLayout(new GridLayout(2, true));

                GridData gridDataList = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                        | GridData.GRAB_VERTICAL);
                gridDataList.horizontalSpan = 2;
                final ListViewer v = new ListViewer(shell, SWT.H_SCROLL | SWT.V_SCROLL);
                v.setLabelProvider(new LabelProvider());
                v.setContentProvider(new ArrayContentProvider());
                v.setInput(ports);
                v.getList().setLayoutData(gridDataList);

                Button ok = new Button(shell, SWT.BORDER | SWT.PUSH);
                ok.setText("Ok");
                ok.addSelectionListener(new SelectionAdapter(){
                    public void widgetSelected( SelectionEvent e ) {
                        IStructuredSelection selection = (IStructuredSelection) v.getSelection();
                        String firstElement = (String) selection.getFirstElement();
                        portLabel.setText(firstElement);
                        gpsPortUsed = firstElement;

                        // set the preference
                        prefs.setValue(PreferenceConstants.PORTUSED, gpsPortUsed);

                        shell.dispose();
                    }
                });
                Button cancel = new Button(shell, SWT.BORDER | SWT.PUSH);
                cancel.setText("Cancel");
                cancel.addSelectionListener(new SelectionAdapter(){
                    public void widgetSelected( SelectionEvent e ) {
                        shell.dispose();
                    }
                });

                shell.open();
            }
        });

        /*
         * start stop gps connection panel
         */
        Group group2 = new Group(parent, SWT.None);
        group2.setText("Start / Stop the Gps connection");
        group2.setLayout(new GridLayout(2, true));
        GridData gridData2 = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        group2.setLayoutData(gridData2);

        startButton = new Button(group2, SWT.BORDER | SWT.RADIO);
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
                                                GpsSettingsComposite.this);

                                    }
                                }
                            });

                } catch (Exception e1) {
                    String message = "An error occurred while starting the gps logging.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            GpsActivator.PLUGIN_ID, e1);
                    e1.printStackTrace();
                }
            }
        });

        stopButton = new Button(group2, SWT.BORDER | SWT.RADIO);
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
                    String message = "An error occurred while stopping the gps logging.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            GpsActivator.PLUGIN_ID, e1);
                    e1.printStackTrace();
                }
            }
        });

        text = new Text(group2, SWT.BORDER | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        text.setLayoutData(gd);

        /*
         * distance and position preferences
         */
        Group group3 = new Group(parent, SWT.None);
        group3.setText("Time and position settings");
        group3.setLayout(new GridLayout(2, false));
        GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        group3.setLayoutData(gridData3);

        Label intervalLabel = new Label(group3, SWT.NONE);
        intervalLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        intervalLabel.setText(PreferenceConstants.INTERVAL_SECONDS);

        intervalText = new Text(group3, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        intervalText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        intervalText.setText("");

        Label distanceLabel = new Label(group3, SWT.NONE);
        distanceLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        distanceLabel.setText(PreferenceConstants.DISTANCE_THRESHOLD);

        distanceText = new Text(group3, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        distanceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        distanceText.setText("");

        dummyModeButton = new Button(parent, SWT.CHECK);
        dummyModeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        dummyModeButton.setText(PreferenceConstants.TESTMODE);
        dummyModeButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                prefs.setValue(PreferenceConstants.TESTMODE, dummyModeButton.getSelection());
            }
        });

        /*
         * load saved preferences
         */
        loadPreferences();

        /*
         * ok and cancel buttons
         */
        Composite okCancelComposite = new Composite(parent, SWT.NONE);
        okCancelComposite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));
        okCancelComposite.setLayout(new GridLayout(2, true));

        Button okButton = new Button(okCancelComposite, SWT.PUSH);
        okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        okButton.setText("ok");
        okButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                savePreferences();
                parent.close();
            }
        });
        Button cancelButton = new Button(okCancelComposite, SWT.PUSH);
        cancelButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        cancelButton.setText("cancel");
        cancelButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                parent.close();
            }
        });

    }

    private void loadPreferences() {
        prefs = GpsActivator.getDefault().getPreferenceStore();

        // gps status
        gpsIsOn = GpsActivator.getDefault().isGpsConnected();
        if (gpsIsOn) {
            startButton.setSelection(true);
            stopButton.setSelection(false);
        } else {
            startButton.setSelection(false);
            stopButton.setSelection(true);
        }
        // prior port used
        gpsPortUsed = prefs.getString(PreferenceConstants.PORTUSED);
        portLabel.setText(gpsPortUsed);
        // prior time interval used
        int interval = prefs.getInt(PreferenceConstants.INTERVAL_SECONDS);
        intervalText.setText(interval + "");
        // prior distance threshold used
        double distance = prefs.getDouble(PreferenceConstants.DISTANCE_THRESHOLD);
        distanceText.setText(distance + "");
        // dummy dataset checkbox
        boolean useDummy = prefs.getBoolean(PreferenceConstants.TESTMODE);
        dummyModeButton.setSelection(useDummy);
    }

    private void savePreferences() {
        prefs.setValue(PreferenceConstants.GPS_IS_ON, gpsIsOn);
        prefs.setValue(PreferenceConstants.PORTUSED, gpsPortUsed);
        int secs = 3;
        try {
            secs = Integer.parseInt(intervalText.getText());
        } catch (Exception e) {
        }
        prefs.setValue(PreferenceConstants.INTERVAL_SECONDS, secs);
        double dist = 3;
        try {
            dist = Double.parseDouble(distanceText.getText());
        } catch (Exception e) {
        }
        prefs.setValue(PreferenceConstants.DISTANCE_THRESHOLD, dist);
        prefs.setValue(PreferenceConstants.TESTMODE, dummyModeButton.getSelection());
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
                                .setText("The selected port doesn't seem to be properly attached to a GPS device.");
                    }
                }
            });

        }
    }

    private String[] getPorts() {
        final List<String> ports = new ArrayList<String>();
        try {

            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
                    new IRunnableWithProgress(){
                        public void run( IProgressMonitor pm ) throws InvocationTargetException,
                                InterruptedException {
                            pm.beginTask("Search for available serial ports...",
                                    IProgressMonitor.UNKNOWN);
                            String[][] portsStrings = AbstractGps.checkGpsPorts();

                            for( String[] strings : portsStrings ) {
                                ports.add(strings[1]);
                            }

                            pm.done();
                        }
                    });

        } catch (Exception e1) {
            String message = "An error occurred whil retrieving the available port.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID,
                    e1);
            e1.printStackTrace();
        }

        return (String[]) ports.toArray(new String[ports.size()]);
    }
}
