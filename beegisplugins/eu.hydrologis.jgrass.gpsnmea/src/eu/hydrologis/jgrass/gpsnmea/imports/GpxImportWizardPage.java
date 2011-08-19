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
package eu.hydrologis.jgrass.gpsnmea.imports;

import java.io.File;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class GpxImportWizardPage extends WizardPage {

    public static final String ID = "GpxImportWizardPage"; //$NON-NLS-1$
    private File inFile = null;

    private boolean inIsOk = false;

    public GpxImportWizardPage( String pageName, Map<String, String> params ) {
        super(ID);
        setTitle(pageName);
        setDescription("Imports the selected gpx file to a feature layer."); // NON-NLS-1
    }

    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite(parent, SWT.NONE);
        fileSelectionArea.setLayout(new GridLayout());

        Group inputGroup = new Group(fileSelectionArea, SWT.None);
        inputGroup.setText("Choose the gpx file");
        inputGroup.setLayout(new GridLayout(2, false));
        inputGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        GridData gridData1 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData1.horizontalSpan = 2;

        final Text gpsText = new Text(inputGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        gpsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        gpsText.setText("");
        final Button gpxButton = new Button(inputGroup, SWT.PUSH);
        gpxButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        gpxButton.setText("...");
        gpxButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(gpxButton.getShell(), SWT.OPEN);
                String path = fileDialog.open();
                if (path != null) {
                    File f = new File(path);
                    if (f.exists()) {
                        inIsOk = true;
                        gpsText.setText(path);
                        inFile = f;
                    } else {
                        inIsOk = false;
                    }
                }
                checkFinish();
            }
        });

        setControl(fileSelectionArea);
    }

    public void dispose() {
    }

    public File getGpxFile() {
        return inFile;
    }

    private void checkFinish() {
        if (inIsOk) {
            GpxImportWizard.canFinish = true;
        } else {
            GpxImportWizard.canFinish = false;
        }
        getWizard().getContainer().updateButtons();
    }

}
