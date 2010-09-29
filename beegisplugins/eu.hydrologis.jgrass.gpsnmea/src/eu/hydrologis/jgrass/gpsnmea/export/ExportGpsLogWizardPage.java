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
package eu.hydrologis.jgrass.gpsnmea.export;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;

public class ExportGpsLogWizardPage extends WizardPage implements KeyListener, SelectionListener {

    protected DirectoryFieldEditor editor;
    private Text fileText;

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Text startDateText;
    private Text endDateText;
    private Button lineButton;
    private Button pointButton;

    /*
     * needed vars
     */
    private String startDateStr = "";
    private String endDateStr = "";
    private boolean isLine = true;
    private boolean isShp = true;
    private String filePath = null;
    private Button shpButton;
    private Button gpxButton;

    public ExportGpsLogWizardPage( String pageName, IStructuredSelection selection ) {
        super(pageName);
        setTitle(pageName); // NON-NLS-1
        setDescription("Export the embedded database gps log to shapefile."); // NON-NLS-1
        ImageDescriptor imageDescriptorFromPlugin = AbstractUIPlugin.imageDescriptorFromPlugin(
                GeonotesPlugin.PLUGIN_ID, "icons/antenna16.png");
        setImageDescriptor(imageDescriptorFromPlugin);
    }

    public void createControl( Composite parent ) {
        final Composite fileSelectionArea = new Composite(parent, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL);
        fileSelectionArea.setLayoutData(fileSelectionData);

        GridLayout fileSelectionLayout = new GridLayout();
        fileSelectionLayout.numColumns = 2;
        fileSelectionLayout.makeColumnsEqualWidth = false;
        fileSelectionLayout.marginWidth = 0;
        fileSelectionLayout.marginHeight = 0;
        fileSelectionArea.setLayout(fileSelectionLayout);

        Label startDateLabel = new Label(fileSelectionArea, SWT.NONE);
        startDateLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        startDateLabel.setText("Start date and time of log [yyyy-mm-dd hh:mm]");
        startDateText = new Text(fileSelectionArea, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        startDateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        startDateText.setText("");
        startDateText.addKeyListener(this);
        Label endDateLabel = new Label(fileSelectionArea, SWT.NONE);
        endDateLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        endDateLabel.setText("End date and time of log [yyyy-mm-dd hh:mm]");
        endDateText = new Text(fileSelectionArea, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        endDateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        endDateText.setText("");
        endDateText.addKeyListener(this);

        Group typeGroup = new Group(fileSelectionArea, SWT.NONE);
        GridData tyepGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        tyepGD.horizontalSpan = 2;
        typeGroup.setLayoutData(tyepGD);
        typeGroup.setLayout(new GridLayout(1, false));
        typeGroup.setText("Geometry type (for Shapefile export)");

        lineButton = new Button(typeGroup, SWT.RADIO);
        lineButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        lineButton.setText("export lines");
        lineButton.addSelectionListener(this);
        lineButton.setSelection(true);
        pointButton = new Button(typeGroup, SWT.RADIO);
        pointButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        pointButton.setText("export points");
        pointButton.addSelectionListener(this);

        Group filetypeGroup = new Group(fileSelectionArea, SWT.NONE);
        GridData filetypeGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        filetypeGD.horizontalSpan = 2;
        filetypeGroup.setLayoutData(filetypeGD);
        filetypeGroup.setLayout(new GridLayout(1, false));
        filetypeGroup.setText("File type");
        
        shpButton = new Button(filetypeGroup, SWT.RADIO);
        shpButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        shpButton.setText("export shapefile");
        shpButton.addSelectionListener(this);
        shpButton.setSelection(true);
        gpxButton = new Button(filetypeGroup, SWT.RADIO);
        gpxButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        gpxButton.setText("export gpx");
        gpxButton.addSelectionListener(this);

        // folder chooser
        fileText = new Text(fileSelectionArea, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fileText.setText("Insert here the file to save...");
        Button folderButton = new Button(fileSelectionArea, SWT.PUSH);
        folderButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        folderButton.setText("...");
        folderButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(fileSelectionArea.getShell(), SWT.SAVE);
                fileDialog.setText("Save file");
                filePath = fileDialog.open();
                if (filePath == null || filePath.length() < 1) {
                    fileText.setText("");
                } else {
                    fileText.setText(filePath);
                }
            }
        });

        setControl(fileSelectionArea);
    }

    public void keyReleased( KeyEvent e ) {
        boolean setComplete = false;

        Object source = e.getSource();
        if (source.equals(startDateText)) {
            // check start date
            String text = startDateText.getText();
            if (text.equals("")) {
                setComplete = true;
            } else {
                try {
                    dateFormatter.parse(text);
                    startDateStr = text;
                    setComplete = true;
                } catch (ParseException e1) {
                    setComplete = false;
                }
            }
        }
        if (source.equals(endDateText)) {
            // check end date
            String text = endDateText.getText();
            if (text.equals("")) {
                setComplete = true;
            } else {
                try {
                    dateFormatter.parse(text);
                    endDateStr = text;
                    setComplete = true;
                } catch (ParseException e1) {
                    setComplete = false;
                }
            }
        }

        setPageComplete(setComplete);
    }

    public void widgetSelected( SelectionEvent e ) {
        Object source = e.getSource();
        if (source.equals(lineButton) || source.equals(pointButton)) {
            boolean selection = lineButton.getSelection();
            isLine = selection;
        }
        if (source.equals(shpButton) || source.equals(gpxButton)) {
            boolean selection = shpButton.getSelection();
            isShp = selection;
        }
    }

    public void keyPressed( KeyEvent e ) {
    }
    public void widgetDefaultSelected( SelectionEvent e ) {
    }
    public String getEndDateStr() {
        return endDateStr;
    }

    public String getStartDateStr() {
        return startDateStr;
    }

    public boolean isLine() {
        return isLine;
    }
    
    public boolean isShp() {
        return isShp;
    }

    public String getFilePath() {
        return filePath;
    }

}
