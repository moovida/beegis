/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package eu.hydrologis.jgrass.gpsbabel.importwizard;

import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.beegisutils.jgrassported.FileChooserWidget;

public class GpsBabelImportWizardPage extends WizardPage {

    public static final String ID = "GpsBabelImportWizardPage";
    private FileChooserWidget fcwOpen;
    private Button is3dButton;
    private Text epsgText;
    private Button onlyCsvButton;
    private FileChooserWidget fcwSave;
    private Text fieldsText;

    public GpsBabelImportWizardPage( String pageName, Map<String, String> params ) {
        super(ID);
        setTitle(pageName);
        setDescription("Import a gpsbabel compatible file as shapefile. Since gpsbabel doesn't support the shapefile format, the start file will be converted to csv format first and JGrass will then try to build out of it a shapefile."); // NON-NLS-1
    }

    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite(parent, SWT.NONE);
        fileSelectionArea.setLayout(new GridLayout());

        Group inputGroup = new Group(fileSelectionArea, SWT.None);
        inputGroup.setText("Choose the gpsbabel file to import");
        inputGroup.setLayout(new GridLayout(2, false));
        inputGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        GridData gridData1 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData1.horizontalSpan = 2;
        fcwOpen = new FileChooserWidget(inputGroup, SWT.None, FileChooserWidget.CHOOSEFILE);
        fcwOpen.setLayoutData(gridData1);

        Group outputGroup = new Group(fileSelectionArea, SWT.None);
        outputGroup.setText("Choose the output file to save to");
        outputGroup.setLayout(new GridLayout(2, false));
        outputGroup
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData2.horizontalSpan = 2;
        fcwSave = new FileChooserWidget(outputGroup, SWT.None, FileChooserWidget.SAVEFILE);
        fcwSave.setLayoutData(gridData2);

        Group fieldsGroup = new Group(fileSelectionArea, SWT.None);
        fieldsGroup.setText("Define the types for the fields (empty assumes one integer field): i=integer, d=double, s=string");
        fieldsGroup.setLayout(new GridLayout(2, false));
        fieldsGroup
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData3.horizontalSpan = 2;
        fieldsText = new Text(fieldsGroup, SWT.BORDER);
        fieldsText.setText("geom:MultiPoint,cat:i");
        fieldsText.setLayoutData(gridData3);

        Group parametersGroup = new Group(fileSelectionArea, SWT.None);
        parametersGroup.setText("Import parameters");
        parametersGroup.setLayout(new GridLayout(2, false));
        parametersGroup.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL));

        Label epsgLabel = new Label(parametersGroup, SWT.None);
        epsgLabel.setText("The reference system for the input file");
        epsgText = new Text(parametersGroup, SWT.BORDER);
        epsgText.setText("EPSG:4326");

        is3dButton = new Button(parametersGroup, SWT.BORDER | SWT.CHECK);
        is3dButton.setText("the imported data have the third dimension information");
        is3dButton.setSelection(false);
        new Label(parametersGroup, SWT.None);

        onlyCsvButton = new Button(parametersGroup, SWT.BORDER | SWT.CHECK);
        onlyCsvButton.setText("only convert to csv (usefull for debug if something goes wrong)");
        onlyCsvButton.setSelection(false);

        setControl(fileSelectionArea);
    }

    public void dispose() {}

    public FileChooserWidget getFcwOpen() {
        return fcwOpen;
    }

    public Button getIs3dButton() {
        return is3dButton;
    }

    public Text getEpsgText() {
        return epsgText;
    }

    public Button getOnlyCsvButton() {
        return onlyCsvButton;
    }

    public FileChooserWidget getFcwSave() {
        return fcwSave;
    }

    public Text getFieldsText() {
        return fieldsText;
    }

}
