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
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.AbstractGps;

/**
 * {@link FieldEditor} to retrieve available serial ports.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SerialPortsFieldEditor extends FieldEditor {

    private int columns = 2;

    private String gpsPortUsed = ""; //$NON-NLS-1$

    private Label label;

    public SerialPortsFieldEditor( String preferenceName, Composite parent ) {
        init(preferenceName, ""); //$NON-NLS-1$
        createControl(parent);
    }

    protected void adjustForNumColumns( int numColumns ) {
        columns = numColumns;
    }

    protected void doFillIntoGrid( final Composite parent, int numColumns ) {

        Group group = new Group(parent, SWT.None);
        group.setText(PreferenceConstants.PORTUSED);
        group.setLayout(new GridLayout(2, false));
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = columns;
        group.setLayoutData(gridData);

        label = new Label(group, SWT.None);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Button button = new Button(group, SWT.BORDER | SWT.PUSH);
        button.setText("search port");
        button.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                String[] ports = getPorts();
                final Shell shell = new Shell(parent.getShell());
                shell.setSize(new Point(300, 400));
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
                        label.setText(firstElement);
                        gpsPortUsed = firstElement;

                        // set the preference
                        IPreferenceStore prefs = GpsActivator.getDefault().getPreferenceStore();
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

    }

    protected void doLoad() {
        gpsPortUsed = "";// getPreferenceStore().getString(
        // PreferenceConstants.PORTUSED);
        label.setText(gpsPortUsed);
    }

    protected void doLoadDefault() {
        gpsPortUsed = "";
        label.setText(gpsPortUsed);
    }

    protected void doStore() {
        getPreferenceStore().setValue(PreferenceConstants.PORTUSED, gpsPortUsed);
    }

    public int getNumberOfControls() {
        return 1;
    }

    public String[] getPorts() {
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
            String message = "An error occurred while retriving the list of available ports.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID,
                    e1);
            e1.printStackTrace();
        }

        return (String[]) ports.toArray(new String[ports.size()]);
    }

}
